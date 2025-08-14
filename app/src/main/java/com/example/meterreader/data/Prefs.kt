
package com.example.meterreader.data

import android.content.Context
import org.json.JSONObject

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("mr_prefs", Context.MODE_PRIVATE)

    fun saveLastReading(meterId: String, reading: String) {
        val map = getAllLastReadings().toMutableMap()
        map[meterId] = reading
        sp.edit().putString("last_map", JSONObject(map as Map<*, *>).toString()).apply()
    }

    fun getLastReading(meterId: String): String? = getAllLastReadings()[meterId]

    private fun getAllLastReadings(): Map<String, String> {
        val json = sp.getString("last_map", "{}") ?: "{}"
        val o = JSONObject(json)
        val out = mutableMapOf<String, String>()
        o.keys().forEach { k -> out[k] = o.getString(k) }
        return out
    }
}
