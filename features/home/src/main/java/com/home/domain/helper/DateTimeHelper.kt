package com.home.domain.helper

import androidx.annotation.VisibleForTesting
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DateTimeHelper @Inject constructor() {

    @kotlin.jvm.Throws(Exception::class)
    fun toHumanReadableTime(dateString: String): String {
        val formatter = getDateFormatter()
        val date = formatter.parse(dateString)
        return formatter.format(date)
    }

    @VisibleForTesting
    fun getDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
}