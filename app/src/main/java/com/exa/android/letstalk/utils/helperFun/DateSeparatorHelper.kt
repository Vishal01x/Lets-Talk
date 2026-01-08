package com.exa.android.letstalk.utils.helperFun

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun getDateSeparatorText(timestampSeconds: Long): String? {
    val timestampMillis = timestampSeconds * 1000L
    val messageDate = Calendar.getInstance().apply {
        timeInMillis = timestampMillis
    }
    
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    
    return when {
        isSameDay(messageDate, today) -> "Today"
        isSameDay(messageDate, yesterday) -> "Yesterday"
        else -> {
            // Format: "December 25, 2024"
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestampMillis))
        }
    }
}

fun shouldShowDateSeparator(currentMessageTime: Long, previousMessageTime: Long?): Boolean {
    if (previousMessageTime == null) return true
    
    val currentDate = Calendar.getInstance().apply {
        timeInMillis = currentMessageTime * 1000L
    }
    val previousDate = Calendar.getInstance().apply {
        timeInMillis = previousMessageTime * 1000L
    }
    
    return !isSameDay(currentDate, previousDate)
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
