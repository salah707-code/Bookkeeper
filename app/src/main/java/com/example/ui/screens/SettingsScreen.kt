package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.UserProfileEntity
import com.example.security.SecurityManager
import com.example.ui.components.MizanTopBar
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.OceanPrimaryLight
import com.example.ui.theme.PurplePrimaryLight
import com.example.ui.theme.RosePrimaryLight
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    appSettings: AppSettingsEntity,
    userProfile: UserProfileEntity,
    isArabic: Boolean,
    onNavigateToProfile: () -> Unit,
    onUpdateSettings: (AppSettingsEntity) -> Unit,
    onSetPasscode: (String) -> Unit,
    onRemovePasscode: () -> Unit,
    onExportJson: suspend () -> String,
    onImportJson: suspend (String) -> Boolean,
    onExportCsv: () -> String,
    onClearAllData: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPasscodeDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showBudgetLimitDialog by remember { mutableStateOf(false) }
    var budgetLimitInput by remember { mutableStateOf(appSettings.monthlyBudgetLimit.toString()) }

    // File picker launcher for importing JSON
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val stream = context.contentResolver.openInputStream(it)
                    val content = stream?.bufferedReader()?.use { reader -> reader.readText() }
                    if (!content.isNullOrEmpty()) {
                        val success = onImportJson(content)
                        snackbarHostState.showSnackbar(
                            if (success) {
                                if (isArabic) "تمت استعادة البيانات بنجاح" else "Data restored successfully"
                            } else {
                                if (isArabic) "فشل استيراد الملف، تأكد من الصيغة" else "Failed to import file"
                            }
                        )
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        if (isArabic) "حدث خطأ أثناء قراءة الملف" else "Error reading file"
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MizanTopBar(title = if (isArabic) "الإعدادات" else "Settings")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Profile Card Tile
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigateToProfile() }
                        .testTag("settings_profile_tile"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (isArabic) "بياناتي (${userProfile.name})" else "My Info (${userProfile.name})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isArabic) "الاسم، الصورة، العملة (${userProfile.currencySymbol})"
                                    else "Name, photo, currency (${userProfile.currencySymbol})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1. المظهر (Appearance)
            item {
                SettingsSectionHeader(title = if (isArabic) "المظهر والتخصيص" else "Appearance & Styling")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Theme Mode (System, Light, Dark)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Brightness4,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "نمط العرض" else "Theme Mode",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val modes = listOf(
                                    Triple("LIGHT", if (isArabic) "فاتح" else "Light", "light_mode_btn"),
                                    Triple("DARK", if (isArabic) "داكن" else "Dark", "dark_mode_btn"),
                                    Triple("SYSTEM", if (isArabic) "تلقائي" else "Auto", "system_mode_btn")
                                )
                                for ((mode, label, tag) in modes) {
                                    val isSelected = appSettings.themeMode == mode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { onUpdateSettings(appSettings.copy(themeMode = mode)) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag(tag)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Color Palette
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "اللون الرئيسي" else "Accent Color",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val palettes = listOf(
                                    Pair("EMERALD", EmeraldPrimaryLight),
                                    Pair("OCEAN", OceanPrimaryLight),
                                    Pair("GOLD", GoldPrimaryLight),
                                    Pair("ROSE", RosePrimaryLight),
                                    Pair("PURPLE", PurplePrimaryLight)
                                )
                                for ((pal, color) in palettes) {
                                    val isSelected = appSettings.colorPalette == pal
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .clickable { onUpdateSettings(appSettings.copy(colorPalette = pal)) }
                                            .then(
                                                if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                                else Modifier
                                            )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Number Size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Numbers,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "حجم الأرقام المالية" else "Number Size",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val sizes = listOf(
                                    Triple("STANDARD", if (isArabic) "عادي" else "Normal", "num_standard"),
                                    Triple("LARGE", if (isArabic) "كبير" else "Large", "num_large"),
                                    Triple("EXTRA_LARGE", if (isArabic) "بارز" else "XL", "num_xl")
                                )
                                for ((size, label, tag) in sizes) {
                                    val isSelected = appSettings.numberSize == size
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { onUpdateSettings(appSettings.copy(numberSize = size)) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .testTag(tag)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Number Numerals (Eastern Arabic vs Western)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "شكل الأرقام (١٢٣ / 123)" else "Number Format",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val formats = listOf(
                                    Pair("ARABIC_INDIC", "١٢٣"),
                                    Pair("WESTERN", "123")
                                )
                                for ((fmt, label) in formats) {
                                    val isSelected = appSettings.numberFormat == fmt
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { onUpdateSettings(appSettings.copy(numberFormat = fmt)) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Language Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "اللغة (Language)" else "Language",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val langs = listOf(
                                    Pair("AR", "العربية"),
                                    Pair("EN", "English")
                                )
                                for ((code, label) in langs) {
                                    val isSelected = appSettings.language == code
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { onUpdateSettings(appSettings.copy(language = code)) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. الحساب والأمان (Security)
            item {
                SettingsSectionHeader(title = if (isArabic) "الأمان والقفل المحلي" else "Security & Local Lock")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Passcode Lock Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) "القفل برمز مرور" else "Passcode Lock",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (appSettings.isPasscodeEnabled) {
                                            if (isArabic) "مفعّل" else "Enabled"
                                        } else {
                                            if (isArabic) "معطل" else "Disabled"
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = appSettings.isPasscodeEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        showPasscodeDialog = true
                                    } else {
                                        onRemovePasscode()
                                    }
                                },
                                modifier = Modifier.testTag("toggle_passcode_switch")
                            )
                        }

                        if (appSettings.isPasscodeEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { showPasscodeDialog = true },
                                modifier = Modifier.testTag("change_passcode_button")
                            ) {
                                Text(
                                    text = if (isArabic) "تغيير رمز المرور" else "Change Passcode",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Biometrics Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isArabic) "فتح القفل بالبصمة" else "Biometric Unlock",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }

                                Switch(
                                    checked = appSettings.isBiometricEnabled,
                                    onCheckedChange = { enabled ->
                                        onUpdateSettings(appSettings.copy(isBiometricEnabled = enabled))
                                    },
                                    modifier = Modifier.testTag("toggle_biometric_switch")
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Auto-lock duration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "القفل التلقائي" else "Auto-lock",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val lockOptions = listOf(
                                        Pair(0, if (isArabic) "فوري" else "Instant"),
                                        Pair(1, if (isArabic) "دقيقة" else "1 min"),
                                        Pair(5, if (isArabic) "5 دقائق" else "5 min")
                                    )
                                    for ((mins, label) in lockOptions) {
                                        val isSelected = appSettings.autoLockMinutes == mins
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { onUpdateSettings(appSettings.copy(autoLockMinutes = mins)) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. التنبيهات (Alerts)
            item {
                SettingsSectionHeader(title = if (isArabic) "التنبيهات والميزانية" else "Alerts & Budget")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Budget limit alert toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) "تنبيه تجاوز الميزانية" else "Budget Exceed Alert",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (appSettings.monthlyBudgetLimit > 0) {
                                            "${appSettings.monthlyBudgetLimit} ${userProfile.currencySymbol}"
                                        } else {
                                            if (isArabic) "غير محدد" else "Not set"
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = appSettings.isBudgetAlertEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings(appSettings.copy(isBudgetAlertEnabled = enabled))
                                    if (enabled && appSettings.monthlyBudgetLimit <= 0) {
                                        showBudgetLimitDialog = true
                                    }
                                }
                            )
                        }

                        if (appSettings.isBudgetAlertEnabled) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(onClick = { showBudgetLimitDialog = true }) {
                                Text(
                                    text = if (isArabic) "تحديد سقف الميزانية الشهرية" else "Set Monthly Budget Limit",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Savings Alert Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "تنبيه تحقيق أهداف الادخار" else "Savings Milestone Alerts",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isArabic) "إشعار احتفالي عند إتمام أي هدف ادخار" else "Celebration alert when goal is completed",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = appSettings.isSavingsAlertEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings(appSettings.copy(isSavingsAlertEnabled = enabled))
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. إدارة البيانات (Data Management)
            item {
                SettingsSectionHeader(title = if (isArabic) "إدارة البيانات والنسخ الاحتياطي" else "Data & Backup")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Export Backup (JSON)
                        DataActionRow(
                            title = if (isArabic) "تصدير نسخة احتياطية (JSON)" else "Export Backup (JSON)",
                            icon = Icons.Default.Share,
                            onClick = {
                                scope.launch {
                                    val json = onExportJson()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        type = "application/json"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Mizan Backup")
                                    context.startActivity(shareIntent)
                                }
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Import Backup (JSON)
                        DataActionRow(
                            title = if (isArabic) "استعادة نسخة احتياطية (استيراد JSON)" else "Import Backup (JSON)",
                            icon = Icons.Default.Download,
                            onClick = {
                                importFileLauncher.launch("application/json")
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Export CSV
                        DataActionRow(
                            title = if (isArabic) "تصدير العمليات بصيغة جدول (CSV)" else "Export Transactions to CSV",
                            icon = Icons.Default.Storage,
                            onClick = {
                                val csv = onExportCsv()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/csv"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Mizan CSV")
                                context.startActivity(shareIntent)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Clear Data
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showClearConfirmDialog = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = ExpenseRed
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isArabic) "حذف جميع البيانات والبدء من جديد" else "Clear All Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ExpenseRed
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. About & Privacy Guarantee
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isArabic) "ميزان - الإصدار 1.0" else "Mizan - Version 1.0",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "🔒 بياناتك محفوظة محلياً بنسبة 100% على جهازك دون أي خوادم سحابية."
                            else "🔒 100% offline-first. Your financial data stays only on your device.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialogs
    if (showPasscodeDialog) {
        PasscodeSetupDialog(
            isArabic = isArabic,
            onDismiss = { showPasscodeDialog = false },
            onConfirm = { pin ->
                onSetPasscode(pin)
                showPasscodeDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (isArabic) "تم تعيين رمز المرور بنجاح" else "Passcode set successfully"
                    )
                }
            }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = if (isArabic) "تأكيد حذف البيانات" else "Confirm Reset",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isArabic) "هل أنت متأكد من حذف جميع العمليات وأهداف الادخار؟ هذا الإجراء نهائي ولا يمكن التراجع عنه."
                    else "Are you sure you want to delete all transactions and savings goals? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearConfirmDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isArabic) "تم مسح جميع البيانات" else "All data cleared"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(if (isArabic) "نعم، حذف الكل" else "Yes, Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showBudgetLimitDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetLimitDialog = false },
            title = {
                Text(
                    text = if (isArabic) "سقف الميزانية الشهرية" else "Monthly Budget Limit",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isArabic) "أدخل المبلغ الأقصى لمصاريفك في الشهر الواحد:"
                        else "Enter maximum monthly expense limit:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetLimitInput,
                        onValueChange = { budgetLimitInput = it },
                        label = { Text(if (isArabic) "المبلغ" else "Amount") },
                        suffix = { Text(userProfile.currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = budgetLimitInput.toDoubleOrNull() ?: 0.0
                        onUpdateSettings(appSettings.copy(monthlyBudgetLimit = limit, isBudgetAlertEnabled = limit > 0))
                        showBudgetLimitDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isArabic) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetLimitDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

@Composable
private fun DataActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
