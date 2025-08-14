
package com.example.meterreader.model

data class MeterReading(
    val meterId: String,
    val type: String,
    val reading: String,
    val digitsOk: Boolean,
    val varianceOk: Boolean,
    val previous: String?,
    val disconnected: Boolean,
    val remarks: String,
    val timestamp: Long
)
