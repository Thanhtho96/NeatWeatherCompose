package com.tt.weatherapp.utils

import android.widget.EditText
import java.util.*

object StringUtils {
    fun String.capitalize() =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    fun EditText.clearSpace(text: String) {
        if (text.contains(" ")) {
            val newText = text.replace(Regex(" "), "")
            this.setText(newText)
            this.setSelection(newText.length)
        }
    }
}