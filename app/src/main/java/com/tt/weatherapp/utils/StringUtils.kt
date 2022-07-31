package com.tt.weatherapp.utils

import android.widget.EditText
import java.text.Normalizer

object StringUtils {
    fun EditText.clearSpace(text: String) {
        if (text.contains(" ")) {
            val newText = text.replace(Regex(" "), "")
            this.setText(newText)
            this.setSelection(newText.length)
        }
    }

    fun stripLocationDescriptionPrefix(title: String, description: String): String {
        val stripAccents = description.stripAccents()
        val descriptionWithoutPrefix = stripAccents.removePrefix("$title, ")
        val removedLength = description.length - descriptionWithoutPrefix.length
        return description.removeRange(0, removedLength)
    }

    fun String.stripAccents(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
    }
}