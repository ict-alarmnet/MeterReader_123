
package com.example.meterreader.util

import org.json.JSONArray

object WorkflowParser {
    fun bestNumericFromWorkflow(json: String): String? {
        val arr = JSONArray(json)
        val candidates = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.has("string_data")) {
                val s = o.getString("string_data")
                Regex("""\d+(?:\.\d+)?""").findAll(s).forEach { m ->
                    candidates.add(m.value)
                }
            }
        }
        return candidates.maxByOrNull { it.length }
    }
}