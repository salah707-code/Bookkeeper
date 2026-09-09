package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.entity.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let {
            try {
                TransactionType.valueOf(it)
            } catch (e: Exception) {
                TransactionType.EXPENSE
            }
        }
    }
}
