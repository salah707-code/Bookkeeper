package com.example.data.repository

import com.example.data.dao.AppSettingsDao
import com.example.data.dao.SavingsGoalDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserProfileDao
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.data.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class MizanRepository(
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val userProfileDao: UserProfileDao,
    private val appSettingsDao: AppSettingsDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val appSettings: Flow<AppSettingsEntity?> = appSettingsDao.getSettings()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        if (transaction.type == TransactionType.SAVINGS && transaction.goalId != null) {
            savingsGoalDao.addAmountToGoal(transaction.goalId, transaction.amount)
        }
        return id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertGoal(goal: SavingsGoalEntity): Long {
        return savingsGoalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteGoal(goal)
    }

    suspend fun addAmountToGoal(goalId: Long, amount: Double) {
        savingsGoalDao.addAmountToGoal(goalId, amount)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        userProfileDao.updateProfile(profile)
    }

    suspend fun updateSettings(settings: AppSettingsEntity) {
        appSettingsDao.updateSettings(settings)
    }

    suspend fun getSettingsOnce(): AppSettingsEntity? {
        return appSettingsDao.getSettingsOnce()
    }

    suspend fun getUserProfileOnce(): UserProfileEntity? {
        return userProfileDao.getUserProfileOnce()
    }

    suspend fun clearAllData() {
        transactionDao.clearAll()
        savingsGoalDao.clearAll()
    }

    suspend fun exportToJson(transactions: List<TransactionEntity>, goals: List<SavingsGoalEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Mizan")
        root.put("exportTime", System.currentTimeMillis())

        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("type", tx.type.name)
            obj.put("amount", tx.amount)
            obj.put("category", tx.category)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("note", tx.note ?: "")
            obj.put("goalId", tx.goalId ?: -1)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val goalsArray = JSONArray()
        for (g in goals) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("title", g.title)
            obj.put("targetAmount", g.targetAmount)
            obj.put("currentAmount", g.currentAmount)
            obj.put("targetDateMillis", g.targetDateMillis ?: -1)
            obj.put("colorHex", g.colorHex)
            obj.put("iconName", g.iconName)
            obj.put("isCompleted", g.isCompleted)
            goalsArray.put(obj)
        }
        root.put("goals", goalsArray)

        return root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                val list = mutableListOf<TransactionEntity>()
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    val typeStr = obj.optString("type", "EXPENSE")
                    val type = try { TransactionType.valueOf(typeStr) } catch (e: Exception) { TransactionType.EXPENSE }
                    val goalId = if (obj.has("goalId") && obj.getLong("goalId") > 0) obj.getLong("goalId") else null
                    val note = if (obj.has("note") && obj.getString("note").isNotEmpty()) obj.getString("note") else null
                    list.add(
                        TransactionEntity(
                            type = type,
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            dateMillis = obj.getLong("dateMillis"),
                            note = note,
                            goalId = goalId
                        )
                    )
                }
                transactionDao.insertAll(list)
            }

            if (root.has("goals")) {
                val goalsArray = root.getJSONArray("goals")
                val list = mutableListOf<SavingsGoalEntity>()
                for (i in 0 until goalsArray.length()) {
                    val obj = goalsArray.getJSONObject(i)
                    val targetDate = if (obj.has("targetDateMillis") && obj.getLong("targetDateMillis") > 0) obj.getLong("targetDateMillis") else null
                    list.add(
                        SavingsGoalEntity(
                            title = obj.getString("title"),
                            targetAmount = obj.getDouble("targetAmount"),
                            currentAmount = obj.getDouble("currentAmount"),
                            targetDateMillis = targetDate,
                            colorHex = obj.optString("colorHex", "#10B981"),
                            iconName = obj.optString("iconName", "savings"),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
                savingsGoalDao.insertAll(list)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Type,Category,Amount,Date,Note\n")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        for (tx in transactions) {
            val dateStr = sdf.format(java.util.Date(tx.dateMillis))
            val escapedNote = (tx.note ?: "").replace("\"", "\"\"")
            sb.append("${tx.id},${tx.type.name},\"${tx.category}\",${tx.amount},\"$dateStr\",\"$escapedNote\"\n")
        }
        return sb.toString()
    }
}
