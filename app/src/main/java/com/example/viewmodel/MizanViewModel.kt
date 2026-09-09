package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.MizanDatabase
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.data.entity.UserProfileEntity
import com.example.data.repository.MizanRepository
import com.example.security.SecurityManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val currentBalance: Double = 0.0,
    val thisMonthExpense: Double = 0.0,
    val isBudgetExceeded: Boolean = false
)

class MizanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MizanDatabase.getDatabase(application, viewModelScope)
    private val repository = MizanRepository(
        database.transactionDao(),
        database.savingsGoalDao(),
        database.userProfileDao(),
        database.appSettingsDao()
    )

    // Security & App Lock State
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _appUnlockedAt = MutableStateFlow(System.currentTimeMillis())

    // All transactions from Room
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All savings goals
    val allGoals: StateFlow<List<SavingsGoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile & Settings
    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .combine(MutableStateFlow(UserProfileEntity())) { profile, default ->
            profile ?: default
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .combine(MutableStateFlow(AppSettingsEntity())) { settings, default ->
            settings ?: default
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    // Filter & Search states for Transactions screen
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter
    ) { txs, query, typeFilter, catFilter ->
        txs.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.category.contains(query, ignoreCase = true) ||
                    (tx.note?.contains(query, ignoreCase = true) == true)
            val matchesType = typeFilter == null || tx.type == typeFilter
            val matchesCat = catFilter == null || tx.category == catFilter
            matchesQuery && matchesType && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Summary
    val summary: StateFlow<FinancialSummary> = combine(
        allTransactions,
        appSettings
    ) { txs, settings ->
        var income = 0.0
        var expense = 0.0
        var savings = 0.0
        var thisMonthExp = 0.0

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        for (tx in txs) {
            when (tx.type) {
                TransactionType.INCOME -> income += tx.amount
                TransactionType.EXPENSE -> {
                    expense += tx.amount
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                    if (txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear) {
                        thisMonthExp += tx.amount
                    }
                }
                TransactionType.SAVINGS -> savings += tx.amount
            }
        }

        val balance = income - expense - savings
        val budgetExceeded = settings.isBudgetAlertEnabled &&
                settings.monthlyBudgetLimit > 0 &&
                thisMonthExp > settings.monthlyBudgetLimit

        FinancialSummary(
            totalIncome = income,
            totalExpense = expense,
            totalSavings = savings,
            currentBalance = balance,
            thisMonthExpense = thisMonthExp,
            isBudgetExceeded = budgetExceeded
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    init {
        // Check lock on initial start
        viewModelScope.launch(Dispatchers.IO) {
            val settings = repository.getSettingsOnce()
            if (settings != null && settings.isPasscodeEnabled && !settings.passcodeHash.isNullOrEmpty()) {
                _isLocked.value = true
            }
        }
    }

    // Lock Screen Actions
    fun unlockWithPasscode(input: String): Boolean {
        val current = appSettings.value
        if (!current.isPasscodeEnabled || current.passcodeHash.isNullOrEmpty()) {
            _isLocked.value = false
            _appUnlockedAt.value = System.currentTimeMillis()
            return true
        }
        val isMatch = SecurityManager.verifyPasscode(input, current.passcodeHash)
        if (isMatch) {
            _isLocked.value = false
            _appUnlockedAt.value = System.currentTimeMillis()
        }
        return isMatch
    }

    fun unlockWithBiometricSuccess() {
        _isLocked.value = false
        _appUnlockedAt.value = System.currentTimeMillis()
    }

    fun setPasscode(passcode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val hash = SecurityManager.hashPasscode(passcode)
            val updated = appSettings.value.copy(
                passcodeHash = hash,
                isPasscodeEnabled = true
            )
            repository.updateSettings(updated)
        }
    }

    fun removePasscode() {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = appSettings.value.copy(
                passcodeHash = null,
                isPasscodeEnabled = false,
                isBiometricEnabled = false
            )
            repository.updateSettings(updated)
        }
    }

    fun onAppBackgrounded() {
        val settings = appSettings.value
        if (settings.isPasscodeEnabled && settings.autoLockMinutes == 0) {
            _isLocked.value = true
        }
    }

    fun onAppResumed() {
        val settings = appSettings.value
        if (settings.isPasscodeEnabled && settings.autoLockMinutes > 0) {
            val elapsedMinutes = (System.currentTimeMillis() - _appUnlockedAt.value) / 60000
            if (elapsedMinutes >= settings.autoLockMinutes) {
                _isLocked.value = true
            }
        }
    }

    // Search & Filter
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    // Transactions CRUD
    fun addTransaction(
        type: TransactionType,
        amount: Double,
        category: String,
        dateMillis: Long,
        note: String?,
        goalId: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val tx = TransactionEntity(
                type = type,
                amount = amount,
                category = category,
                dateMillis = dateMillis,
                note = note,
                goalId = goalId
            )
            repository.insertTransaction(tx)

            // Budget alert check
            if (type == TransactionType.EXPENSE) {
                val settings = appSettings.value
                val currentSum = summary.value
                val newMonthExp = currentSum.thisMonthExpense + amount
                if (settings.isBudgetAlertEnabled && settings.monthlyBudgetLimit > 0 && newMonthExp > settings.monthlyBudgetLimit) {
                    NotificationHelper.showNotification(
                        getApplication(),
                        101,
                        "تنبيه ميزان: تجاوز الميزانية",
                        "لقد تجاوزت ميزانيتك الشهرية المحددة بمبلغ $amount"
                    )
                }
            }
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(tx)
        }
    }

    // Goals CRUD
    fun addGoal(title: String, targetAmount: Double, colorHex: String, iconName: String, targetDateMillis: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = SavingsGoalEntity(
                title = title,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                colorHex = colorHex,
                iconName = iconName,
                targetDateMillis = targetDateMillis
            )
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGoal(goal)
        }
    }

    fun depositToGoal(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAmount = goal.currentAmount + amount
            val isCompleted = newAmount >= goal.targetAmount
            repository.updateGoal(goal.copy(currentAmount = newAmount, isCompleted = isCompleted))

            // Also record a savings transaction
            repository.insertTransaction(
                TransactionEntity(
                    type = TransactionType.SAVINGS,
                    amount = amount,
                    category = goal.title,
                    dateMillis = System.currentTimeMillis(),
                    note = "إيداع في هدف: ${goal.title}",
                    goalId = goal.id
                )
            )

            if (isCompleted && appSettings.value.isSavingsAlertEnabled) {
                NotificationHelper.showNotification(
                    getApplication(),
                    202,
                    "مبارك! تم إنجاز هدف الادخار 🎉",
                    "لقد وصلت بنجاح إلى هدفك: ${goal.title}"
                )
            }
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(goal)
        }
    }

    // User Profile
    fun updateProfile(
        name: String,
        phone: String,
        email: String,
        currencyCode: String,
        currencySymbol: String,
        photoUri: String?,
        bio: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userProfile.value
            val updated = current.copy(
                name = name,
                phone = phone,
                email = email,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                photoUri = photoUri ?: current.photoUri,
                bio = bio
            )
            repository.updateProfile(updated)
        }
    }

    // App Settings
    fun updateSettings(settings: AppSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSettings(settings)
        }
    }

    // Data Export/Import
    suspend fun getExportJson(): String {
        return repository.exportToJson(allTransactions.value, allGoals.value)
    }

    suspend fun importData(json: String): Boolean {
        return repository.importFromJson(json)
    }

    fun getExportCsv(): String {
        return repository.exportToCsv(allTransactions.value)
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
        }
    }
}
