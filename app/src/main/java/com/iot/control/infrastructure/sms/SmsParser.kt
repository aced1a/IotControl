package com.iot.control.infrastructure.sms

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.substring

class SmsParser {
    private val dict = """\G(?:dict\(|\()?(?<name>[^=\(:]+)[:=]\s*(?<value>[^,;\)]+)(?:;|,|\z|\))""".toRegex(RegexOption.MULTILINE)

    fun parse(parser: String, message: String): Map<String, String> {

        return if(parser.startsWith("dict", ignoreCase = true)) {
            val fields = getDict(parser)
            val data = getDict(message)

            parseDict(fields, data)
        } else {
            parseFormatted(parser, message)
        }
    }

    private fun getDict(parser: String): Map<String, String> {
        val data = mutableMapOf<String, String>()
        val result = dict.findAll(parser)

        for(item in result) {
            val (name, value) = item.destructured

            //kotlin.String does not have .strip() function
            data[name.replace(" ", "").lowercase()] = value.replace(" ", "").lowercase()
        }

        return data
    }

    private fun parseDict(fields: Map<String, String>, data: Map<String, String>): Map<String, String> {
        val result = mutableMapOf<String, String>()

        for(item in fields) {
            val value = data[item.key] ?: continue
            val prev = result[item.value]
            result[item.value] = if(prev == null) value else "$prev:$value"
        }

        return result
    }

    private fun parseFormatted(parser: String, message: String): Map<String, String> {
        val result = mutableMapOf<String, String>()

        val order = getOrder(parser)
        val regex = getFormattedParser(parser)

        val groups = regex.matchEntire(message)?.groupValues ?: return emptyMap()
        order.forEachIndexed { index, field ->
            val groupValue = groups[index+1]
            if(groupValue.isEmpty().not()) {
                val prev = result[field]
                result[field] = if(prev == null) groupValue else "$prev:$groupValue"
            }
        }

        return result
    }

    private fun getOrder(parser: String): List<String> {
        val result = mutableListOf<String>()
        var start = 0

        parser.forEachIndexed { index, symbol ->
            if(symbol == '{') start = index
            else if(symbol == '}') {
                result.add(parser.substring(start+1, index))
            }
        }

        return result
    }

    private fun getFormattedParser(parser: String): Regex {
        return parser
            .replace("{address}", "([^;:,=]+)")
            .replace("{payload}", "([^;:,=]+)")
            .replace("{value}", "([^;:,=]+)").toRegex()

    }
}