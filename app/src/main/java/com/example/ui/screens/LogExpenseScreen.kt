package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.util.Categories
import com.example.util.Formatters
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogExpenseScreen(
    currencySymbol: String,
    useArabicIndic: Boolean,
    isArabic: Boolean,
    onBack: () -> Unit,
    onSaveExpense: (amount: Double, category: String, dateMillis: Long, description: String?) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Form fields
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(
            if (isArabic) Categories.expenseCategories[0].nameAr
            else Categories.expenseCategories[0].nameEn
        )
    }
    var customCategoryText by remember { mutableStateOf("") }
    var isCustomCategorySelected by remember { mutableStateOf(false) }

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var descriptionText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }

    // Quick increment helpers
    val quickAmounts = listOf(10, 20, 50, 100, 500)

    // Formatted date string for display
    val formattedDisplayDate = remember(selectedDateMillis, isArabic) {
        val sdf = SimpleDateFormat(
            if (isArabic) "EEEE، d MMMM yyyy" else "EEEE, MMMM d, yyyy",
            if (isArabic) Locale("ar") else Locale.US
        )
        sdf.format(Date(selectedDateMillis))
    }

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("date_picker_confirm_button")
                ) {
                    Text(
                        text = if (isArabic) "تأكيد" else "Confirm",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    modifier = Modifier.testTag("date_picker_dismiss_button")
                ) {
                    Text(text = if (isArabic) "إلغاء" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isArabic) "تسجيل مصروف يومي" else "Log Daily Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("log_expense_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isArabic) "رجوع" else "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            amountText = ""
                            descriptionText = ""
                            customCategoryText = ""
                            isCustomCategorySelected = false
                            selectedCategory = if (isArabic) Categories.expenseCategories[0].nameAr
                            else Categories.expenseCategories[0].nameEn
                            selectedDateMillis = System.currentTimeMillis()
                            amountError = null
                        },
                        modifier = Modifier.testTag("log_expense_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isArabic) "إعادة تعيين" else "Reset"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val cleanAmount = amountText.replace(',', '.').trim()
                            val parsedAmount = cleanAmount.toDoubleOrNull()

                            if (parsedAmount == null || parsedAmount <= 0) {
                                amountError = if (isArabic) {
                                    "الرجاء إدخال مبلغ صحيح أكبر من الصفر"
                                } else {
                                    "Please enter a valid amount greater than 0"
                                }
                                return@Button
                            }

                            val finalCategory = if (isCustomCategorySelected && customCategoryText.isNotBlank()) {
                                customCategoryText.trim()
                            } else {
                                selectedCategory
                            }

                            onSaveExpense(
                                parsedAmount,
                                finalCategory,
                                selectedDateMillis,
                                descriptionText.ifBlank { null }
                            )

                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isArabic) "تم تسجيل المصروف بنجاح" else "Expense logged successfully"
                                )
                            }
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_expense_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExpenseRed
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "حفظ المصروف" else "Save Expense",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {

            // 1. AMOUNT SECTION
            Text(
                text = if (isArabic) "المبلغ" else "Amount",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ExpenseRed.copy(alpha = 0.06f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (amountError != null) ExpenseRed else ExpenseRed.copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                    amountText = input
                                    amountError = null
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "0.00",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            suffix = {
                                Text(
                                    text = currencySymbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ExpenseRed
                                )
                            },
                            trailingIcon = {
                                if (amountText.isNotEmpty()) {
                                    IconButton(
                                        onClick = { amountText = "" },
                                        modifier = Modifier.testTag("clear_amount_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح"
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            isError = amountError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("amount_input_field"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ExpenseRed,
                                unfocusedBorderColor = Color.Transparent,
                                errorBorderColor = ExpenseRed
                            )
                        )
                    }

                    if (amountError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = amountError!!,
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick add pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        quickAmounts.forEach { increment ->
                            val label = "+$increment"
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = ExpenseRed.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .clickable {
                                        val current = amountText.toDoubleOrNull() ?: 0.0
                                        val nextVal = current + increment
                                        amountText = if (nextVal % 1.0 == 0.0) {
                                            nextVal.toInt().toString()
                                        } else {
                                            String.format(Locale.US, "%.2f", nextVal)
                                        }
                                        amountError = null
                                    }
                                    .testTag("quick_amount_$increment")
                            ) {
                                Text(
                                    text = if (useArabicIndic) Formatters.toArabicNumerals(label) else label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. CATEGORY SECTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "التصنيف" else "Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Categories.expenseCategories.forEach { category ->
                    val catName = if (isArabic) category.nameAr else category.nameEn
                    val isSelected = !isCustomCategorySelected && (selectedCategory == category.nameAr || selectedCategory == category.nameEn)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            isCustomCategorySelected = false
                            selectedCategory = catName
                        },
                        label = {
                            Text(
                                text = catName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else category.color,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.color,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("category_chip_${category.nameEn}")
                    )
                }

                // Custom category chip option
                val customChipSelected = isCustomCategorySelected
                FilterChip(
                    selected = customChipSelected,
                    onClick = {
                        isCustomCategorySelected = true
                    },
                    label = {
                        Text(
                            text = if (isArabic) "تصنيف مخصص" else "Custom",
                            fontSize = 12.sp,
                            fontWeight = if (customChipSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = if (customChipSelected) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("category_chip_custom")
                )
            }

            AnimatedVisibility(visible = isCustomCategorySelected) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        label = { Text(if (isArabic) "اسم التصنيف المخصص" else "Custom Category Name") },
                        placeholder = { Text(if (isArabic) "مثال: صيانة سيارة، اشتراك نادي..." else "e.g. Car maintenance, gym...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_category_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. DATE SECTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "تاريخ المصروف" else "Expense Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePickerDialog = true }
                    .testTag("date_picker_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = formattedDisplayDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "انقر لتغيير التاريخ عبر التقويم" else "Tap to change date via calendar",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showDatePickerDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_calendar_button")
                    ) {
                        Text(
                            text = if (isArabic) "تغيير" else "Change",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick date shortcut chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val now = Calendar.getInstance()

                // Today
                val isToday = remember(selectedDateMillis) {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                    cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                }
                FilterChip(
                    selected = isToday,
                    onClick = {
                        selectedDateMillis = System.currentTimeMillis()
                    },
                    label = { Text(if (isArabic) "اليوم" else "Today", fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("quick_date_today")
                )

                // Yesterday
                val isYesterday = remember(selectedDateMillis) {
                    val yest = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                    cal.get(Calendar.YEAR) == yest.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == yest.get(Calendar.DAY_OF_YEAR)
                }
                FilterChip(
                    selected = isYesterday,
                    onClick = {
                        val yest = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                        selectedDateMillis = yest.timeInMillis
                    },
                    label = { Text(if (isArabic) "أمس" else "Yesterday", fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("quick_date_yesterday")
                )

                // 2 Days ago
                val isTwoDaysAgo = remember(selectedDateMillis) {
                    val twoDays = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                    cal.get(Calendar.YEAR) == twoDays.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == twoDays.get(Calendar.DAY_OF_YEAR)
                }
                FilterChip(
                    selected = isTwoDaysAgo,
                    onClick = {
                        val twoDays = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
                        selectedDateMillis = twoDays.timeInMillis
                    },
                    label = { Text(if (isArabic) "أول أمس" else "2 days ago", fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("quick_date_two_days_ago")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. DESCRIPTION & NOTES SECTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "الوصف والملاحظات" else "Description & Notes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                placeholder = {
                    Text(
                        text = if (isArabic) {
                            "أضف وصفًا للمصروف (مثل: قهوة الصباح، غداء عمل، بنزين...)"
                        } else {
                            "Add expense description (e.g. morning coffee, team lunch, fuel...)"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                minLines = 3,
                maxLines = 5,
                trailingIcon = {
                    if (descriptionText.isNotEmpty()) {
                        IconButton(
                            onClick = { descriptionText = "" },
                            modifier = Modifier.testTag("clear_description_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح الوصف"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input_field"),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. LIVE PREVIEW CARD
            if (amountText.isNotBlank()) {
                val parsed = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                val activeCategory = if (isCustomCategorySelected && customCategoryText.isNotBlank()) {
                    customCategoryText.trim()
                } else {
                    selectedCategory
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_preview_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isArabic) "معاينة المصروف:" else "Expense Preview:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeCategory,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (descriptionText.isNotBlank()) {
                                    Text(
                                        text = descriptionText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "- ${Formatters.formatAmount(parsed, currencySymbol, useArabicIndic)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
