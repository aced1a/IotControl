package com.iot.control.infrastructure.utils

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient.Mqtt3SubscribeAndCallbackBuilder.Call.Ex
import com.jayway.jsonpath.JsonPath
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

fun getPayload(json: JSONObject, value: String?): String? {
    if(value == null) return null

    return if(value.startsWith('$')) {
        getWithPath(json, value)
    } else {
        if(json.has(value))
            json.getString(value)
        else
            null
    }
}

fun getWithPath(json: JSONObject, path: String): String? {
    return try {
        val data: Any =  JsonPath.parse(json).read(path)
        return data.toString()
    } catch (e: Throwable) {
        return null
    }
}

fun compare(excepted: String, actual: JSONObject, ignoreField: String?): Boolean {
    val json = tryParse(excepted)

    return if(json == null) {
        compareWithPath(excepted, actual)
    } else {
        compareWithJson(json, actual, ignoreField)
    }
}

fun compareWithPath(excepted: String, actual: JSONObject): Boolean {
    return try {
        val data: List<Any> = JsonPath.parse(actual).read(excepted)

        data.isNotEmpty()
    } catch (e: Throwable) {
        false
    }
}

fun compareWithJson(excepted: JSONObject, actual: JSONObject, ignoreField: String?): Boolean {

    val comparator = CustomComparator(JSONCompareMode.LENIENT, Customization(ignoreField) { _, _ -> true })
    val result = JSONCompare.compareJSON(excepted, actual, comparator)

    return !(result.isMissingOnField && result.isFailureOnField)
}

fun setValue(payload: String, field: String, value: String): String {
    val json = tryParse(payload) ?: return payload

    return if(field.startsWith('$'))
        return setWithPath(json, field, value) ?: payload
    else
        setWithJson(json, field, value)
}

fun setWithPath(json: JSONObject, path: String, value: String): String? {
    return try {
        JsonPath.parse(json).set(path, value).toString()
    } catch (e: Throwable) {
        null
    }
}

fun setWithJson(json: JSONObject, field: String, value: String): String {
    val result = json.put(field, value)

    return result.toString()
}