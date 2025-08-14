
package com.example.meterreader

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BarcodeActivity : AppCompatActivity() {
    private lateinit var tv: TextView
    private var isReceiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            if (i?.action == DW.INTENT_ACTION) {
                val data = i.getStringExtra("com.symbol.datawedge.data_string")
                if (!data.isNullOrBlank()) {
                    setResult(Activity.RESULT_OK, Intent().putExtra("METER_ID", data))
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
        setContentView(R.layout.activity_barcode)
        tv = findViewById(R.id.tvInfo)
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            tv.text = "Aim at Meter ID barcode and press the hardware trigger…"
        }
        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        DW.switchToBarcode(this)
        registerDwReceiverCompat()
    }

    override fun onPause() {
        unregisterDwReceiverCompat()
        super.onPause()
    }
}
