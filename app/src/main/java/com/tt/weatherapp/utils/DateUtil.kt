package com.tt.weatherapp.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateUtil {

    enum class DateFormat(val value: String) {
        DAY_OF_WEEK_MONTH_DAY("EEEE, MMMM dd"),
        DAY("dd"),
        HOUR_MINUTE("HH:mm"),
        DAY_OF_WEEK("EEE")
    }

    /**
     * 0 - to pad with zeros
     * 2 - to set width to 2
     */
    fun prefixZero(num: Long) = String.format("%02d", num)

    fun format(dt: Long, timezone: Int, dateFormat: DateFormat) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            convertToHourMinute(dt, timezone, dateFormat)
        } else {
            convertToHourMinuteLegacy(dt, timezone, dateFormat)
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToHourMinute(dt: Long, timezone: Int, dateFormat: DateFormat): String {
        val instant = Instant.ofEpochSecond(dt)
        val zoneId = ZoneId.ofOffset("UTC", ZoneOffset.ofTotalSeconds(timezone))
        val formatter = DateTimeFormatter.ofPattern(dateFormat.value)

        return instant.atZone(zoneId).format(formatter)
    }

    private fun convertToHourMinuteLegacy(dt: Long, timezone: Int, dateFormat: DateFormat): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = dt * 1000 // Convert seconds to milliseconds
        calendar.add(Calendar.SECOND, timezone) // Apply timezone offset

        val formatter = SimpleDateFormat(dateFormat.value, Locale.getDefault())
        return formatter.format(calendar.time)
    }
}