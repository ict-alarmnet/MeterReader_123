
package com.example.meterreader

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.meterreader.util.WorkflowParser

class OcrActivity : AppCompatActivity() {
    private lateinit var tv: TextView
    private var isReceiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            if (i?.action == DW.INTENT_ACTION) {
                val json = i.getStringExtra("com.symbol.datawedge.data")
                val value = try { json?.let { WorkflowParser.bestNumericFromWorkflow(it) } } catch (_: Exception) { null }
                if (!value.isNullOrBlank()) {
                    setResult(Activity.RESULT_OK, Intent().putExtra("METER_READING", value))
                    finish()
                }
            }
        }
    }

    private fun registerDwReceiverCompat() {
        if (isReceiverRegistered) return
        val filter = IntentFilter(DW.INTENT_ACTION)
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                val flags = if (android.os.Build.VERSION.SDK_INT >= 33) Context.RECEIVER_EXPORTED else 0
                registerReceiver(receiver, filter, flags)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(receiver, filter)
            }
            isReceiverRegistered = true
        } catch (_: Exception) { isReceiverRegistered = false }
    }

    private fun unregisterDwReceiverCompat() {
        if (!isReceiverRegistered) return
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        isReceiverRegistered = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)
        tv = findViewById(R.id.tvInfo)
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            DW.switchToWorkflowMeter(this)
            tv.text = "Press the hardware trigger to capture the meter reading via OCR…"
        }
        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        registerDwReceiverCompat()
    }

    override fun onPause() {
        unregisterDwReceiverCompat()
        super.onPause()
    }
}
