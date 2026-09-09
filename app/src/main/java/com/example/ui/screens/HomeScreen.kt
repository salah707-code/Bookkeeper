package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserProfileEntity
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.util.Formatters
import com.example.viewmodel.FinancialSummary

@Composable
fun HomeScreen(
    summary: FinancialSummary,
    recentTransactions: List<TransactionEntity>,
    userProfile: UserProfileEntity,
    appSettings: AppSettingsEntity,
    isArabic: Boolean,
    onNavigateToTransactions: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val currencySymbol = userProfile.currencySymbol
    val useArabicIndic = appSettings.numberFormat == "ARABIC_INDIC"

    val balanceFontSize = when (appSettings.numberSize) {
        "EXTRA_LARGE" -> 36.sp
        "LARGE" -> 30.sp
        else -> 24.sp
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .testTag("home_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة عملية",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Profile Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "أهلاً بك،" else "Welcome back,",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = userProfile.name.ifBlank { if (isArabic) "مستخدم ميزان" else "Mizan User" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onNavigateToProfile() }
                            .testTag("home_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile.photoUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = userProfile.photoUri,
                                contentDescription = "الصورة الشخصية",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "الملف الشخصي",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Budget Exceeded Warning Banner
            item {
                AnimatedVisibility(visible = summary.isBudgetExceeded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ExpenseRed.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "تحذير",
                                tint = ExpenseRed,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "تنبيه: تجاوزت الميزانية الشهرية!" else "Alert: Monthly budget exceeded!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ExpenseRed
                                )
                                Text(
                                    text = if (isArabic) "مصاريفك هذا الشهر تجاوزت الحد المحدد (${Formatters.formatAmount(appSettings.monthlyBudgetLimit, currencySymbol, useArabicIndic)})"
                                    else "Your expenses exceeded your budget limit",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Current Balance Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("home_balance_card"),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "الرصيد الصافي الحالي" else "Current Net Balance",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = Formatters.formatAmount(summary.currentBalance, currencySymbol, useArabicIndic),
                                fontSize = balanceFontSize,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (summary.currentBalance >= 0) MaterialTheme.colorScheme.primary else ExpenseRed
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Three Summary Pillars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Income
                                SummaryPillar(
                                    title = if (isArabic) "الدخل" else "Income",
                                    amount = summary.totalIncome,
                                    currencySymbol = currencySymbol,
                                    useArabicIndic = useArabicIndic,
                                    color = IncomeGreen,
                                    icon = Icons.Default.ArrowUpward
                                )

                                // Expenses
                                SummaryPillar(
                                    title = if (isArabic) "المصاريف" else "Expenses",
                                    amount = summary.totalExpense,
                                    currencySymbol = currencySymbol,
                                    useArabicIndic = useArabicIndic,
                                    color = ExpenseRed,
                                    icon = Icons.Default.ArrowDownward
                                )

                                // Savings
                                SummaryPillar(
                                    title = if (isArabic) "الادخار" else "Savings",
                                    amount = summary.totalSavings,
                                    currencySymbol = currencySymbol,
                                    useArabicIndic = useArabicIndic,
                                    color = SavingsBlue,
                                    icon = Icons.Default.Savings
                                )
                            }
                        }
                    }
                }
            }

            // Monthly Budget Progress (if enabled)
            if (appSettings.isBudgetAlertEnabled && appSettings.monthlyBudgetLimit > 0) {
                item {
                    val progress = (summary.thisMonthExpense / appSettings.monthlyBudgetLimit).toFloat().coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "الميزانية الشهرية" else "Monthly Budget",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${Formatters.formatAmount(summary.thisMonthExpense, currencySymbol, useArabicIndic)} / ${Formatters.formatAmount(appSettings.monthlyBudgetLimit, currencySymbol, useArabicIndic)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (progress >= 1f) ExpenseRed else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isArabic) "تم استهلاك $percent% من الميزانية المحددة" else "$percent% of monthly budget consumed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "آخر العمليات" else "Recent Transactions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TextButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.testTag("home_view_all_transactions")
                    ) {
                        Text(
                            text = if (isArabic) "عرض الكل" else "View All",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Transactions List
            if (recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isArabic) "لا توجد عمليات مسجلة بعد" else "No transactions yet",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isArabic) "اضغط على زر (+) لإضافة أول عملية" else "Tap (+) to add your first transaction",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(recentTransactions.take(5), key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currencySymbol = currencySymbol,
                        useArabicIndic = useArabicIndic,
                        numberSize = appSettings.numberSize,
                        isArabic = isArabic,
                        onClick = { onTransactionClick(tx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryPillar(
    title: String,
    amount: Double,
    currencySymbol: String,
    useArabicIndic: Boolean,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = Formatters.formatAmount(amount, currencySymbol, useArabicIndic),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
