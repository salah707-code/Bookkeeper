package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long? = null,
    val colorHex: String = "#10B981",
    val iconName: String = "savings",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
