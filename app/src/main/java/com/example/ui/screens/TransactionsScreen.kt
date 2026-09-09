package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.data.entity.UserProfileEntity
import com.example.ui.components.MizanTopBar
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    userProfile: UserProfileEntity,
    appSettings: AppSettingsEntity,
    searchQuery: String,
    selectedTypeFilter: TransactionType?,
    isArabic: Boolean,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val currencySymbol = userProfile.currencySymbol
    val useArabicIndic = appSettings.numberFormat == "ARABIC_INDIC"

    Scaffold(
        topBar = {
            MizanTopBar(
                title = if (isArabic) "سجل العمليات" else "Transactions"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("transactions_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة عملية",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(if (isArabic) "البحث في العمليات والتصنيفات..." else "Search transactions...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("transactions_search_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { onTypeFilterChange(null) },
                        label = { Text(if (isArabic) "الكل" else "All") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }

                item {
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = {
                            onTypeFilterChange(
                                if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                            )
                        },
                        label = { Text(if (isArabic) "المصاريف" else "Expenses") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseRed,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_expense")
                    )
                }

                item {
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = {
                            onTypeFilterChange(
                                if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME
                            )
                        },
                        label = { Text(if (isArabic) "الدخل" else "Income") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_income")
                    )
                }

                item {
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.SAVINGS,
                        onClick = {
                            onTypeFilterChange(
                                if (selectedTypeFilter == TransactionType.SAVINGS) null else TransactionType.SAVINGS
                            )
                        },
                        label = { Text(if (isArabic) "الادخار" else "Savings") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SavingsBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_savings")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isArabic) "لا توجد نتائج مطابقة" else "No matching transactions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isArabic) "جرب تغيير كلمات البحث أو الفلتر" else "Try adjusting your search or filters",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
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
}
