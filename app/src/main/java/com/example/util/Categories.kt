package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.entity.TransactionType

data class CategoryInfo(
    val nameAr: String,
    val nameEn: String,
    val icon: ImageVector,
    val color: Color
)

object Categories {
    val expenseCategories = listOf(
        CategoryInfo("بقالة ومواد غذائية", "Groceries", Icons.Default.Fastfood, Color(0xFFF59E0B)),
        CategoryInfo("سكن وفواتير", "Housing & Bills", Icons.Default.Home, Color(0xFF3B82F6)),
        CategoryInfo("وقود ومواصلات", "Transportation", Icons.Default.DirectionsCar, Color(0xFFEF4444)),
        CategoryInfo("تسوق وملابس", "Shopping", Icons.Default.ShoppingBag, Color(0xFFEC4899)),
        CategoryInfo("صحة وعلاج", "Health", Icons.Default.MedicalServices, Color(0xFF10B981)),
        CategoryInfo("تعليم وتطوير", "Education", Icons.Default.School, Color(0xFF8B5CF6)),
        CategoryInfo("هدايا وترفيه", "Entertainment", Icons.Default.CardGiftcard, Color(0xFFF97316)),
        CategoryInfo("أخرى", "Other", Icons.Default.MoreHoriz, Color(0xFF6B7280))
    )

    val incomeCategories = listOf(
        CategoryInfo("الراتب الشهري", "Salary", Icons.Default.Work, Color(0xFF10B981)),
        CategoryInfo("أرباح واستثمارات", "Investments", Icons.Default.TrendingUp, Color(0xFF06B6D4)),
        CategoryInfo("عمل حر ومكافآت", "Freelance & Bonus", Icons.Default.Paid, Color(0xFF84CC16)),
        CategoryInfo("تحويلات وهدايا", "Gifts & Transfers", Icons.Default.CardGiftcard, Color(0xFFA855F7)),
        CategoryInfo("دخل آخر", "Other Income", Icons.Default.AccountBalance, Color(0xFF64748B))
    )

    val savingsCategories = listOf(
        CategoryInfo("صندوق الطوارئ", "Emergency Fund", Icons.Default.Savings, Color(0xFF10B981)),
        CategoryInfo("شراء أصل أو سيارة", "Vehicle / Asset", Icons.Default.DirectionsCar, Color(0xFF3B82F6)),
        CategoryInfo("سفر وسياحة", "Travel", Icons.Default.CardGiftcard, Color(0xFFF59E0B)),
        CategoryInfo("عمرة وحج", "Hajj & Umrah", Icons.Default.Home, Color(0xFF8B5CF6)),
        CategoryInfo("ادخار عام", "General Savings", Icons.Default.Savings, Color(0xFF14B8A6))
    )

    fun getCategoryInfo(name: String, type: TransactionType): CategoryInfo {
        val list = when (type) {
            TransactionType.EXPENSE -> expenseCategories
            TransactionType.INCOME -> incomeCategories
            TransactionType.SAVINGS -> savingsCategories
        }
        return list.firstOrNull { it.nameAr == name || it.nameEn.equals(name, ignoreCase = true) }
            ?: CategoryInfo(name, name, Icons.Default.MoreHoriz, Color(0xFF64748B))
    }
}
