package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.screens.AddEditGoalDialog
import com.example.ui.screens.AddEditTransactionSheet
import com.example.ui.screens.DepositDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavingsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MizanTheme
import com.example.viewmodel.MizanViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: MizanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appSettings by viewModel.appSettings.collectAsState()
            val userProfile by viewModel.userProfile.collectAsState()
            val isLocked by viewModel.isLocked.collectAsState()
            val summary by viewModel.summary.collectAsState()
            val recentTransactions by viewModel.allTransactions.collectAsState()
            val filteredTransactions by viewModel.filteredTransactions.collectAsState()
            val goals by viewModel.allGoals.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()

            val isArabic = appSettings.language == "AR"
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            MizanTheme(
                themeMode = appSettings.themeMode,
                colorPalette = appSettings.colorPalette
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    MizanAppContent(
                        viewModel = viewModel,
                        isArabic = isArabic,
                        isLocked = isLocked,
                        appSettings = appSettings,
                        userProfile = userProfile,
                        summary = summary,
                        recentTransactions = recentTransactions,
                        filteredTransactions = filteredTransactions,
                        goals = goals,
                        searchQuery = searchQuery,
                        selectedTypeFilter = selectedTypeFilter
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppBackgrounded()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MizanAppContent(
    viewModel: MizanViewModel,
    isArabic: Boolean,
    isLocked: Boolean,
    appSettings: com.example.data.entity.AppSettingsEntity,
    userProfile: com.example.data.entity.UserProfileEntity,
    summary: com.example.viewmodel.FinancialSummary,
    recentTransactions: List<TransactionEntity>,
    filteredTransactions: List<TransactionEntity>,
    goals: List<SavingsGoalEntity>,
    searchQuery: String,
    selectedTypeFilter: com.example.data.entity.TransactionType?
) {
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf("HOME") }

    // Transaction Sheet state
    var showTransactionSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Goal Dialog state
    var showGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var depositingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    when {
        showSplash -> {
            SplashScreen(
                isArabic = isArabic,
                onStartApp = { showSplash = false }
            )
        }

        isLocked -> {
            LockScreen(
                isArabic = isArabic,
                isBiometricEnabled = appSettings.isBiometricEnabled,
                onUnlockWithPasscode = { pin -> viewModel.unlockWithPasscode(pin) },
                onUnlockWithBiometrics = { viewModel.unlockWithBiometricSuccess() }
            )
        }

        currentTab == "PROFILE" -> {
            ProfileScreen(
                userProfile = userProfile,
                isArabic = isArabic,
                onBack = { currentTab = "SETTINGS" },
                onSaveProfile = { name, phone, email, code, symbol, photo, bio ->
                    viewModel.updateProfile(name, phone, email, code, symbol, photo, bio)
                }
            )
        }

        else -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        val navItems = listOf(
                            Triple("HOME", if (isArabic) "الرئيسية" else "Home", Icons.Default.Home),
                            Triple("TRANSACTIONS", if (isArabic) "العمليات" else "Transactions", Icons.AutoMirrored.Filled.ReceiptLong),
                            Triple("SAVINGS", if (isArabic) "الادخار" else "Savings", Icons.Default.Savings),
                            Triple("SETTINGS", if (isArabic) "الإعدادات" else "Settings", Icons.Default.Settings)
                        )

                        for ((route, label, icon) in navItems) {
                            val selected = currentTab == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = route },
                                icon = { Icon(imageVector = icon, contentDescription = label) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("nav_item_$route")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        "HOME" -> {
                            HomeScreen(
                                summary = summary,
                                recentTransactions = recentTransactions,
                                userProfile = userProfile,
                                appSettings = appSettings,
                                isArabic = isArabic,
                                onNavigateToTransactions = { currentTab = "TRANSACTIONS" },
                                onNavigateToProfile = { currentTab = "PROFILE" },
                                onTransactionClick = { tx ->
                                    editingTransaction = tx
                                    showTransactionSheet = true
                                },
                                onAddTransactionClick = {
                                    editingTransaction = null
                                    showTransactionSheet = true
                                }
                            )
                        }

                        "TRANSACTIONS" -> {
                            TransactionsScreen(
                                transactions = filteredTransactions,
                                userProfile = userProfile,
                                appSettings = appSettings,
                                searchQuery = searchQuery,
                                selectedTypeFilter = selectedTypeFilter,
                                isArabic = isArabic,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onTypeFilterChange = { viewModel.setTypeFilter(it) },
                                onTransactionClick = { tx ->
                                    editingTransaction = tx
                                    showTransactionSheet = true
                                },
                                onAddTransactionClick = {
                                    editingTransaction = null
                                    showTransactionSheet = true
                                }
                            )
                        }

                        "SAVINGS" -> {
                            SavingsScreen(
                                goals = goals,
                                userProfile = userProfile,
                                appSettings = appSettings,
                                isArabic = isArabic,
                                onAddGoalClick = {
                                    editingGoal = null
                                    showGoalDialog = true
                                },
                                onEditGoalClick = { goal ->
                                    editingGoal = goal
                                    showGoalDialog = true
                                },
                                onDepositClick = { goal ->
                                    depositingGoal = goal
                                },
                                onDeleteGoalClick = { goal ->
                                    viewModel.deleteGoal(goal)
                                }
                            )
                        }

                        "SETTINGS" -> {
                            SettingsScreen(
                                appSettings = appSettings,
                                userProfile = userProfile,
                                isArabic = isArabic,
                                onNavigateToProfile = { currentTab = "PROFILE" },
                                onUpdateSettings = { viewModel.updateSettings(it) },
                                onSetPasscode = { viewModel.setPasscode(it) },
                                onRemovePasscode = { viewModel.removePasscode() },
                                onExportJson = { viewModel.getExportJson() },
                                onImportJson = { viewModel.importData(it) },
                                onExportCsv = { viewModel.getExportCsv() },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }

            // Transaction Add/Edit Bottom Sheet
            if (showTransactionSheet) {
                AddEditTransactionSheet(
                    sheetState = sheetState,
                    existingTransaction = editingTransaction,
                    goals = goals,
                    currencySymbol = userProfile.currencySymbol,
                    useArabicIndic = appSettings.numberFormat == "ARABIC_INDIC",
                    isArabic = isArabic,
                    onDismiss = {
                        showTransactionSheet = false
                        editingTransaction = null
                    },
                    onSave = { type, amount, category, dateMillis, note, goalId ->
                        if (editingTransaction == null) {
                            viewModel.addTransaction(type, amount, category, dateMillis, note, goalId)
                        } else {
                            viewModel.updateTransaction(
                                editingTransaction!!.copy(
                                    type = type,
                                    amount = amount,
                                    category = category,
                                    dateMillis = dateMillis,
                                    note = note,
                                    goalId = goalId
                                )
                            )
                        }
                    },
                    onDelete = { tx ->
                        viewModel.deleteTransaction(tx)
                    }
                )
            }

            // Savings Goal Dialog
            if (showGoalDialog) {
                AddEditGoalDialog(
                    existingGoal = editingGoal,
                    currencySymbol = userProfile.currencySymbol,
                    isArabic = isArabic,
                    onDismiss = {
                        showGoalDialog = false
                        editingGoal = null
                    },
                    onSave = { title, targetAmount, colorHex, iconName ->
                        if (editingGoal == null) {
                            viewModel.addGoal(title, targetAmount, colorHex, iconName, null)
                        } else {
                            viewModel.updateGoal(
                                editingGoal!!.copy(
                                    title = title,
                                    targetAmount = targetAmount,
                                    colorHex = colorHex,
                                    iconName = iconName
                                )
                            )
                        }
                    }
                )
            }

            // Deposit Dialog
            if (depositingGoal != null) {
                DepositDialog(
                    goal = depositingGoal!!,
                    currencySymbol = userProfile.currencySymbol,
                    isArabic = isArabic,
                    onDismiss = { depositingGoal = null },
                    onConfirm = { amount ->
                        viewModel.depositToGoal(depositingGoal!!, amount)
                    }
                )
            }
        }
    }
}
