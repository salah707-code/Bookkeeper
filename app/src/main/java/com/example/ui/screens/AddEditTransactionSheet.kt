package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.util.Categories
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    sheetState: SheetState,
    existingTransaction: TransactionEntity? = null,
    goals: List<SavingsGoalEntity> = emptyList(),
    currencySymbol: String,
    useArabicIndic: Boolean,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Double, String, Long, String?, Long?) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    var selectedType by remember {
        mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE)
    }

    var amountText by remember {
        mutableStateOf(existingTransaction?.amount?.toString() ?: "")
    }

    var selectedCategory by remember {
        val initialCat = existingTransaction?.category
            ?: if (selectedType == TransactionType.EXPENSE) Categories.expenseCategories[0].nameAr
            else if (selectedType == TransactionType.INCOME) Categories.incomeCategories[0].nameAr
            else Categories.savingsCategories[0].nameAr
        mutableStateOf(initialCat)
    }

    var noteText by remember {
        mutableStateOf(existingTransaction?.note ?: "")
    }

    var selectedGoalId by remember {
        mutableStateOf(existingTransaction?.goalId)
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = when (selectedType) {
        TransactionType.EXPENSE -> Categories.expenseCategories
        TransactionType.INCOME -> Categories.incomeCategories
        TransactionType.SAVINGS -> Categories.savingsCategories
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTransaction == null) {
                        if (isArabic) "إضافة عملية جديدة" else "Add Transaction"
                    } else {
                        if (isArabic) "تعديل العملية" else "Edit Transaction"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    Triple(TransactionType.EXPENSE, if (isArabic) "مصروف" else "Expense", ExpenseRed),
                    Triple(TransactionType.INCOME, if (isArabic) "دخل" else "Income", IncomeGreen),
                    Triple(TransactionType.SAVINGS, if (isArabic) "ادخار" else "Savings", SavingsBlue)
                )

                for ((type, label, color) in types) {
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) color else color.copy(alpha = 0.12f)
                            )
                            .clickable {
                                selectedType = type
                                selectedCategory = when (type) {
                                    TransactionType.EXPENSE -> Categories.expenseCategories[0].nameAr
                                    TransactionType.INCOME -> Categories.incomeCategories[0].nameAr
                                    TransactionType.SAVINGS -> Categories.savingsCategories[0].nameAr
                                }
                            }
                            .padding(vertical = 12.dp)
                            .testTag("type_selector_${type.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    errorMessage = null
                },
                label = { Text(if (isArabic) "المبلغ" else "Amount") },
                suffix = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_amount_input"),
                shape = RoundedCornerShape(14.dp),
                isError = errorMessage != null
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = ExpenseRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection
            Text(
                text = if (isArabic) "التصنيف" else "Category",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in categories) {
                    val isSelected = selectedCategory == cat.nameAr || selectedCategory == cat.nameEn
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.nameAr },
                        label = { Text(if (isArabic) cat.nameAr else cat.nameEn) },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else cat.color,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("cat_chip_${cat.nameAr}")
                    )
                }
            }

            // Linked Savings Goal (if Savings type)
            if (selectedType == TransactionType.SAVINGS && goals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = if (isArabic) "ربط بهدف ادخار (اختياري)" else "Link to Savings Goal (Optional)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (goal in goals) {
                        val isGoalSelected = selectedGoalId == goal.id
                        FilterChip(
                            selected = isGoalSelected,
                            onClick = {
                                selectedGoalId = if (isGoalSelected) null else goal.id
                            },
                            label = { Text(goal.title) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SavingsBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Note Input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(if (isArabic) "ملاحظة (اختياري)" else "Note (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_note_input"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = if (isArabic) "الرجاء إدخال مبلغ صحيح أكبر من الصفر" else "Please enter a valid amount"
                        return@Button
                    }
                    onSave(
                        selectedType,
                        amount,
                        selectedCategory,
                        existingTransaction?.dateMillis ?: System.currentTimeMillis(),
                        noteText.ifBlank { null },
                        selectedGoalId
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (existingTransaction == null) {
                        if (isArabic) "حفظ العملية" else "Save Transaction"
                    } else {
                        if (isArabic) "تحديث العملية" else "Update Transaction"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (existingTransaction != null && onDelete != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onDelete(existingTransaction)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_transaction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed.copy(alpha = 0.12f),
                        contentColor = ExpenseRed
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "حذف العملية" else "Delete Transaction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
