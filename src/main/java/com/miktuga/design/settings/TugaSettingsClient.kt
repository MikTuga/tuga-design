package com.miktuga.design.settings

import android.content.ContentValues
import android.content.Context
import android.net.Uri

/**
 * Typed accessor for the TugaSettings ContentProvider exposed by `com.miktuga.settings`.
 *
 * Caller must declare `com.miktuga.permission.READ_SETTINGS` (and `WRITE_SETTINGS` for writes)
 * in its manifest and be signed with the same Tuga keystore as `com.miktuga.settings`.
 *
 * If the TugaSettings app is not installed (or the query fails for any reason), `get` returns
 * the setting's declared default — callers never need to handle missing-provider as an error.
 */
object TugaSettingsClient {
    const val AUTHORITY = "com.miktuga.settings.provider"
    const val PATH_SETTINGS = "settings"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_SETTINGS")

    const val COL_KEY = "key"
    const val COL_VALUE = "value"

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(context: Context, setting: TugaSetting<T>): T {
        val raw = readRaw(context, setting.key) ?: return setting.default
        return when (setting) {
            is TugaSetting.UnitsSpeed ->
                runCatching { UnitsSpeedValue.valueOf(raw) }.getOrDefault(setting.default) as T
            is TugaSetting.UnitsTemp ->
                runCatching { UnitsTempValue.valueOf(raw) }.getOrDefault(setting.default) as T
            is TugaSetting.UnitsDistance ->
                runCatching { UnitsDistanceValue.valueOf(raw) }.getOrDefault(setting.default) as T
            is TugaSetting.UsbMountPath,
            is TugaSetting.MusicFolder,
            is TugaSetting.ReportsFolder -> raw as T
            is TugaSetting.AutoUpdateCheck -> raw.toBoolean() as T
        }
    }

    fun <T : Any> set(context: Context, setting: TugaSetting<T>, value: T): Boolean {
        val serialized: String = when (value) {
            is Enum<*> -> value.name
            else -> value.toString()
        }
        val values = ContentValues().apply {
            put(COL_KEY, setting.key)
            put(COL_VALUE, serialized)
        }
        return try {
            context.contentResolver.insert(CONTENT_URI, values) != null
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun readRaw(context: Context, key: String): String? {
        return try {
            context.contentResolver.query(
                CONTENT_URI,
                arrayOf(COL_VALUE),
                "$COL_KEY = ?",
                arrayOf(key),
                null
            )?.use { c ->
                if (c.moveToFirst() && !c.isNull(0)) c.getString(0) else null
            }
        } catch (e: SecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
