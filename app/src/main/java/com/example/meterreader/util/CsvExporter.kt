
package com.example.meterreader.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.OutputStreamWriter

object CsvExporter {
    fun createExportIntent(): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "meter_readings.csv")
        }
    }

    fun writeCsv(ctx: Context, uri: Uri, rows: List<List<String>>) {
        ctx.contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { w ->
                rows.forEach { r ->
                    val line = r.joinToString(",") { v0 ->
                        val v = v0 ?: ""  // if you pass nullable strings, guard here
                        val needQuote = v.contains(',') || v.contains('"') || v.contains('\n') || v.contains('\r')
                        val esc = v.replace("\"", "\"\"")
                        if (needQuote) "\"$esc\"" else esc
                    }
                    // Excel-friendly line endings
                    w.append(line).append("\r\n")
                }
                w.flush()
            }
        }
    }
}