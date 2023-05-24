package com.iot.control.infrastructure.sms

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.substring

class SmsParser {
    private val dict = """dict\s+[\s+(()+)\s+]"""

    fun parse(parser: String, value: String) {
        if(parser.startsWith("dict", ignoreCase = true)) {
            val fields =  parser.substringAfter('(').substringBefore(')')

            val dict = fields.split(',').map {
                val split = it.split("=")
                //TODO check if split 0 or 1 is unbound

                Pair(split[0], split[1])
            }

        }

    }

    fun parseDict(dict: List<Pair<String, String>>, value: String, delimiter: String) {
        val fields = value.split(delimiter)


    }
}