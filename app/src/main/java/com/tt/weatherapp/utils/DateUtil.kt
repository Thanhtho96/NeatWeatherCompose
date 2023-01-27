package com.tt.weatherapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    enum class DateFormat(val value: String) {
        DAY_OF_WEEK_MONTH_DAY("EEEE, MMMM dd"),
        DAY("dd"),
        HOUR_MINUTE("H:mm"),
        DAY_OF_WEEK("EEE")
    }

    /**
     * 0 - to pad with zeros
     * 2 - to set width to 2
     */
    fun prefixZero(num: Long) = String.format("%02d", num)

    fun format(dateFormat: DateFormat, date: Any): String =
        SimpleDateFormat(
            dateFormat.value,
            Locale.getDefault()
        ).format(date)
}