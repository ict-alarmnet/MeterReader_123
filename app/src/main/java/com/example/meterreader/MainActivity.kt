
package com.example.meterreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.meterreader.data.Prefs
import com.example.meterreader.model.MeterReading
import com.example.meterreader.util.CsvExporter

class MainActivity : AppCompatActivity() {

    private lateinit var tvMeterId: TextView
    private lateinit var etType: AutoCompleteTextView
    private lateinit var tvReading: TextView
    private lateinit var cbDisconnected: CheckBox
    private lateinit var etRemarks: EditText

    private val rows = mutableListOf<List<String>>()
    private lateinit var prefs: Prefs

    private val pickCsvLocation = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri -> CsvExporter.writeCsv(this, uri, rows) }
            Toast.makeText(this, "Exported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // minimal crash logger (best effort)
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try { getExternalFilesDir(null)?.resolve("crash.txt")?.writeText(e.stackTraceToString()) } catch (_: Exception) {}
        }
        setContentView(R.layout.activity_main)

        prefs = Prefs(this)
        DW.createOrUpdateProfile(this)
        DW.switchToBarcode(this)

        tvMeterId = findViewById(R.id.tvMeterId)
        etType = findViewById(R.id.etType)
        tvReading = findViewById(R.id.tvReading)
        cbDisconnected = findViewById(R.id.cbDisconnected)
        etRemarks = findViewById(R.id.etRemarks)

        etType.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf("Water", "Power")))

        findViewById<Button>(R.id.btnScanId).setOnClickListener {
            startActivityForResult(Intent(this, BarcodeActivity::class.java), 100)
        }
        findViewById<Button>(R.id.btnScanReading).setOnClickListener {
            if (tvMeterId.text.isNullOrBlank()) {
                Toast.makeText(this, "Scan Meter ID first", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            startActivityForResult(Intent(this, OcrActivity::class.java), 200)
        }
        findViewById<Button>(R.id.btnManual).setOnClickListener { showManualDialog() }
        findViewById<Button>(R.id.btnAccept).setOnClickListener { acceptReading() }
        findViewById<Button>(R.id.btnExport).setOnClickListener {
            pickCsvLocation.launch(CsvExporter.createExportIntent())
        }
    }

    private fun showManualDialog() {
        val input = EditText(this)
        input.hint = "Enter reading"
        AlertDialog.Builder(this)
            .setTitle("Manual Reading")
            .setView(input)
            .setPositiveButton("OK") { _, _ -> tvReading.text = input.text.toString() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun acceptReading() {
        val id = tvMeterId.text.toString()
        val type = etType.text.toString().ifBlank { "Water" }
        val reading = tvReading.text.toString()
        if (id.isBlank() || reading.isBlank()) {
            Toast.makeText(this, "Meter ID and Reading are required", Toast.LENGTH_SHORT).show(); return
        }

        val digitsOk = reading.matches(Regex("^\\d{3,8}(?:\\.\\d+)?$"))
        val prev = prefs.getLastReading(id)
        val varianceOk = prev?.let { p ->
            try {
                val prevVal = p.toBigDecimal()
                val curVal = reading.toBigDecimal()
                val delta = curVal - prevVal
                delta >= 0.toBigDecimal() && delta < (prevVal * (0.5).toBigDecimal())
            } catch (_: Exception) { true }
        } ?: true

        val disconnected = cbDisconnected.isChecked
        val remarks = etRemarks.text.toString()
        val ts = System.currentTimeMillis()

        rows.add(listOf(id, type, reading, digitsOk.toString(), varianceOk.toString(), prev ?: "", disconnected.toString(), remarks, ts.toString()))
        prefs.saveLastReading(id, reading)
        Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show()
        tvReading.text = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            100 -> {
                val id = data?.getStringExtra("METER_ID")
                if (!id.isNullOrBlank()) tvMeterId.text = id
                DW.switchToBarcode(this)
            }
            200 -> {
                val value = data?.getStringExtra("METER_READING")
                if (!value.isNullOrBlank()) tvReading.text = value
                DW.switchToBarcode(this)
            }
        }
    }
}
