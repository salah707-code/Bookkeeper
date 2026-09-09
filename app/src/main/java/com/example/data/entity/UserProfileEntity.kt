package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "المستخدم",
    val phone: String = "",
    val email: String = "",
    val currencyCode: String = "SAR",
    val currencySymbol: String = "ر.س",
    val photoUri: String? = null,
    val bio: String = "المال خادم أمين وسيد سيء"
)
