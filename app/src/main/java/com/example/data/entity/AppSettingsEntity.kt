package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val colorPalette: String = "EMERALD", // EMERALD, OCEAN, GOLD, ROSE, PURPLE
    val textSize: String = "NORMAL", // NORMAL, MEDIUM, LARGE
    val numberSize: String = "LARGE", // STANDARD, LARGE, EXTRA_LARGE
    val numberFormat: String = "ARABIC_INDIC", // ARABIC_INDIC (١٢٣), WESTERN (123)
    val language: String = "AR", // AR, EN
    val passcodeHash: String? = null,
    val isPasscodeEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val autoLockMinutes: Int = 0, // 0 = Immediately, 1 = 1min, 5 = 5min, -1 = Never
    val monthlyBudgetLimit: Double = 0.0,
    val isBudgetAlertEnabled: Boolean = false,
    val isSavingsAlertEnabled: Boolean = true
)
