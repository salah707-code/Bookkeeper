package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.ExpenseRed

@Composable
fun AddEditGoalDialog(
    existingGoal: SavingsGoalEntity? = null,
    currencySymbol: String,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Double, colorHex: String, iconName: String) -> Unit
) {
    var title by remember { mutableStateOf(existingGoal?.title ?: "") }
    var targetAmountText by remember { mutableStateOf(existingGoal?.targetAmount?.toString() ?: "") }
    var selectedColor by remember { mutableStateOf(existingGoal?.colorHex ?: "#10B981") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf(
        "#10B981", // Emerald
        "#3B82F6", // Blue
        "#F59E0B", // Amber
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#06B6D4"  // Cyan
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingGoal == null) {
                    if (isArabic) "إضافة هدف ادخار جديد" else "New Savings Goal"
                } else {
                    if (isArabic) "تعديل هدف الادخار" else "Edit Savings Goal"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text(if (isArabic) "اسم الهدف (مثل: سيارة، صندوق الطوارئ)" else "Goal Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = {
                        targetAmountText = it
                        errorMessage = null
                    },
                    label = { Text(if (isArabic) "المبلغ المستهدف" else "Target Amount") },
                    suffix = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isArabic) "لون البطاقة" else "Card Color",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (hex in colorOptions) {
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Green }
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = targetAmountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        errorMessage = if (isArabic) "الرجاء إدخال اسم الهدف" else "Please enter a title"
                        return@Button
                    }
                    if (amount == null || amount <= 0) {
                        errorMessage = if (isArabic) "الرجاء إدخال مبلغ مستهدف صحيح" else "Please enter a valid target amount"
                        return@Button
                    }
                    onSave(title.trim(), amount, selectedColor, "savings")
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_goal_button")
            ) {
                Text(if (isArabic) "حفظ" else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_goal_button")
            ) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DepositDialog(
    goal: SavingsGoalEntity,
    currencySymbol: String,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "إيداع في: ${goal.title}" else "Deposit to ${goal.title}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isArabic) "أدخل المبلغ المراد إضافته إلى هذا الهدف:" else "Enter the amount to add:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = if (isArabic) "الرجاء إدخال مبلغ صحيح" else "Please enter a valid amount"
                        return@Button
                    }
                    onConfirm(amount)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isArabic) "إيداع" else "Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
