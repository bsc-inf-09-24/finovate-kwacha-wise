package com.example.kwachawise.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
    private val fullDateTimeFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return fullDateTimeFormatter.format(Date(timestamp))
    }

    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getDayHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(timestamp, now.timeInMillis) -> "Today"
            isSameDay(timestamp, now.timeInMillis - 86400000) -> "Yesterday"
            else -> formatDate(timestamp)
        }
    }
}
