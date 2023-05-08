package com.iot.control.infrastructure.utils

import org.json.JSONObject
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator


fun tryParse(payload: String): JSONObject? {
    return try {
        JSONObject(payload)
    } catch (e: Exception) {
        null
    }
}

fun compare(excepted: JSONObject, actual: JSONObject, ignoreField: String?): Boolean {
    val comparator = CustomComparator(JSONCompareMode.LENIENT, Customization(ignoreField) { _, _ -> true })
    val result = JSONCompare.compareJSON(excepted, actual, comparator)

    return !(result.isMissingOnField && result.isFailureOnField)
}

