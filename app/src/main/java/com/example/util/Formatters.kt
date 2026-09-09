package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatAmount(amount: Double, currencySymbol: String, useArabicIndic: Boolean): String {
        val df = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val formatted = df.format(amount)
        val finalNum = if (useArabicIndic) toArabicNumerals(formatted) else formatted
        return "$finalNum $currencySymbol"
    }

    fun formatNumber(number: Double, useArabicIndic: Boolean): String {
        val df = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val formatted = df.format(number)
        return if (useArabicIndic) toArabicNumerals(formatted) else formatted
    }

    fun toArabicNumerals(input: String): String {
        val western = "0123456789,"
        val eastern = "٠١٢٣٤٥٦٧٨٩٬"
        val sb = StringBuilder()
        for (ch in input) {
            val idx = western.indexOf(ch)
            if (idx != -1) {
                sb.append(eastern[idx])
            } else if (ch == '.') {
                sb.append('٫')
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatDate(timestamp: Long, isArabic: Boolean): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            val timeSdf = SimpleDateFormat("hh:mm a", if (isArabic) Locale("ar") else Locale.US)
            return if (isArabic) "اليوم، ${timeSdf.format(Date(timestamp))}" else "Today, ${timeSdf.format(Date(timestamp))}"
        }

        if (isYesterday) {
            val timeSdf = SimpleDateFormat("hh:mm a", if (isArabic) Locale("ar") else Locale.US)
            return if (isArabic) "أمس، ${timeSdf.format(Date(timestamp))}" else "Yesterday, ${timeSdf.format(Date(timestamp))}"
        }

        val pattern = if (isArabic) "d MMMM yyyy" else "MMM d, yyyy"
        val sdf = SimpleDateFormat(pattern, if (isArabic) Locale("ar") else Locale.US)
        return sdf.format(Date(timestamp))
    }
}
