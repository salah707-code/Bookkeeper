package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppSettingsDao
import com.example.data.dao.SavingsGoalDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserProfileDao
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        SavingsGoalEntity::class,
        UserProfileEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MizanDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: MizanDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MizanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MizanDatabase::class.java,
                    "mizan_database"
                )
                .addCallback(MizanDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MizanDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(database: MizanDatabase) {
            val userProfileDao = database.userProfileDao()
            val settingsDao = database.appSettingsDao()
            val transactionDao = database.transactionDao()
            val savingsGoalDao = database.savingsGoalDao()

            userProfileDao.insertProfile(
                UserProfileEntity(
                    id = 1,
                    name = "سالم الأحمد",
                    phone = "+966 50 123 4567",
                    email = "salem@example.com",
                    currencyCode = "SAR",
                    currencySymbol = "ر.س",
                    bio = "الادخار خطوة نحو حرية مالية حقيقية"
                )
            )

            settingsDao.insertSettings(
                AppSettingsEntity(
                    id = 1,
                    themeMode = "SYSTEM",
                    colorPalette = "EMERALD",
                    textSize = "NORMAL",
                    numberSize = "LARGE",
                    numberFormat = "ARABIC_INDIC",
                    language = "AR",
                    isPasscodeEnabled = false,
                    isBiometricEnabled = false,
                    autoLockMinutes = 0,
                    monthlyBudgetLimit = 5000.0,
                    isBudgetAlertEnabled = true,
                    isSavingsAlertEnabled = true
                )
            )

            // Seed an initial savings goal
            val goalId = savingsGoalDao.insertGoal(
                SavingsGoalEntity(
                    title = "صندوق الطوارئ",
                    targetAmount = 10000.0,
                    currentAmount = 2500.0,
                    colorHex = "#10B981",
                    iconName = "shield"
                )
            )

            savingsGoalDao.insertGoal(
                SavingsGoalEntity(
                    title = "عمرة رمضان",
                    targetAmount = 4000.0,
                    currentAmount = 1800.0,
                    colorHex = "#F59E0B",
                    iconName = "mosque"
                )
            )

            // Seed a few initial transactions
            val now = System.currentTimeMillis()
            val day = 86400000L

            transactionDao.insertTransaction(
                TransactionEntity(
                    type = com.example.data.entity.TransactionType.INCOME,
                    amount = 8500.0,
                    category = "الراتب الشهري",
                    dateMillis = now - (day * 3),
                    note = "إيداع راتب الشهر"
                )
            )

            transactionDao.insertTransaction(
                TransactionEntity(
                    type = com.example.data.entity.TransactionType.SAVINGS,
                    amount = 2500.0,
                    category = "صندوق الطوارئ",
                    dateMillis = now - (day * 2),
                    note = "تحويل للادخار الشهري",
                    goalId = goalId
                )
            )

            transactionDao.insertTransaction(
                TransactionEntity(
                    type = com.example.data.entity.TransactionType.EXPENSE,
                    amount = 450.0,
                    category = "بقالة ومواد غذائية",
                    dateMillis = now - (day * 1),
                    note = "مشتريات السوبرماركت"
                )
            )

            transactionDao.insertTransaction(
                TransactionEntity(
                    type = com.example.data.entity.TransactionType.EXPENSE,
                    amount = 120.0,
                    category = "وقود ومواصلات",
                    dateMillis = now,
                    note = "بنزين السيارة"
                )
            )
        }
    }
}
