package com.miktuga.design.feedback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Launches TugaStore's FeedbackActivity from any of the utility apps.
 *
 * Tries an explicit ComponentName first (TugaStore must be installed). Falls back to an
 * action-only intent if the FQN ever changes.
 */
object FeedbackLauncher {
    private const val STORE_PKG = "com.miktuga.store"
    private const val STORE_ACTIVITY = "com.miktuga.store.FeedbackActivity"
    const val ACTION = "com.miktuga.action.FEEDBACK"
    const val EXTRA_SOURCE_APP = "source_app"
    const val EXTRA_SOURCE_VERSION = "source_version"

    fun launch(context: Context, sourceApp: String, sourceVersion: String) {
        val explicit = Intent().apply {
            component = ComponentName(STORE_PKG, STORE_ACTIVITY)
            action = ACTION
            putExtra(EXTRA_SOURCE_APP, sourceApp)
            putExtra(EXTRA_SOURCE_VERSION, sourceVersion)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStart(context, explicit)) return

        val action = Intent(ACTION).apply {
            setPackage(STORE_PKG)
            putExtra(EXTRA_SOURCE_APP, sourceApp)
            putExtra(EXTRA_SOURCE_VERSION, sourceVersion)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStart(context, action)) return

        Toast.makeText(context, "Tuga Store не установлен", Toast.LENGTH_SHORT).show()
    }

    private fun tryStart(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}
