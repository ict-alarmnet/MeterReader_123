
package com.example.meterreader

import android.content.Context
import android.content.Intent
import android.os.Bundle

object DW {
    private const val DW_ACTION = "com.symbol.datawedge.api.ACTION"
    private const val CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE"
    private const val SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"
    private const val SWITCH_DATACAPTURE = "com.symbol.datawedge.api.SWITCH_DATACAPTURE"

    const val PROFILE = "MR_APP"
    const val INTENT_ACTION = "com.example.meterreader.RESULT"

    fun createOrUpdateProfile(ctx: Context) {
        ctx.sendBroadcast(Intent(DW_ACTION).apply { putExtra(CREATE_PROFILE, PROFILE) })

        val intentPlugin = Bundle().apply {
            putString("PLUGIN_NAME", "INTENT")
            putString("RESET_CONFIG", "true")
            putBundle("PARAM_LIST", Bundle().apply {
                putString("intent_output_enabled", "true")
                putString("intent_action", INTENT_ACTION)
                putString("intent_delivery", "BROADCAST")
            })
        }
        val barcodePlugin = Bundle().apply {
            putString("PLUGIN_NAME", "BARCODE")
            putString("RESET_CONFIG", "true")
            putBundle("PARAM_LIST", Bundle())
        }
        val appList = Bundle().apply {
            putString("PACKAGE_NAME", ctx.packageName)
            putStringArray("ACTIVITY_LIST", arrayOf(".MainActivity", ".BarcodeActivity", ".OcrActivity"))
        }
        val config = Bundle().apply {
            putString("PROFILE_NAME", PROFILE)
            putString("PROFILE_ENABLED", "true")
            putString("CONFIG_MODE", "UPDATE")
            putParcelableArrayList("PLUGIN_CONFIG", arrayListOf(barcodePlugin, intentPlugin))
            putParcelableArrayList("APP_LIST", arrayListOf(appList))
        }
        ctx.sendBroadcast(Intent(DW_ACTION).apply { putExtra(SET_CONFIG, config) })
    }

    fun switchToBarcode(ctx: Context) {
        val i = Intent(DW_ACTION)
        i.putExtra(SWITCH_DATACAPTURE, "BARCODE")
        i.putExtra("PARAM_LIST", Bundle().apply {
            putString("scanner_selection_by_identifier", "INTERNAL_IMAGER")
        })
        ctx.sendBroadcast(i)
    }

    fun switchToWorkflowMeter(ctx: Context, sessionTimeoutMs: Int = 15000) {
        val i = Intent(DW_ACTION)
        i.putExtra(SWITCH_DATACAPTURE, "WORKFLOW")
        val paramList = Bundle().apply {
            putString("workflow_name", "meter_reading")
            putString("workflow_input_source", "2") // CAMERA
            val module = Bundle().apply {
                putString("module", "MeterReaderModule")
                putBundle("module_params", Bundle().apply {
                    putString("session_timeout", sessionTimeoutMs.toString())
                })
            }
            putParcelableArrayList("workflow_params", arrayListOf(module))
        }
        i.putExtra("PARAM_LIST", paramList)
        ctx.sendBroadcast(i)
    }
}
