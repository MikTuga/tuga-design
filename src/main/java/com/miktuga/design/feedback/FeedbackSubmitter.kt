package com.miktuga.design.feedback

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Builds, posts and queues Tuga feedback JSON.
 *
 * Online: try HTTPS POST to https://miktuga.ru/api/feedback.
 * Offline / failure: write to TugaStore's app-private files dir
 * (`/data/data/com.miktuga.store/files/feedback/<ts>.json`) for later retry.
 *
 * The queue is intentionally in private storage — other apps with
 * WRITE_EXTERNAL_STORAGE could otherwise plant arbitrary JSON that we'd
 * upload as first-party feedback. Only TugaStore calls submit()/retryPending()
 * directly; utility apps invoke TugaStore's FeedbackActivity via intent.
 */
object FeedbackSubmitter {

    const val ENDPOINT = "https://miktuga.ru/api/feedback"
    const val FEEDBACK_DIR = "feedback"
    private const val TAG = "FeedbackSubmitter"
    private const val CONNECT_TIMEOUT_MS = 5_000
    private const val READ_TIMEOUT_MS = 5_000

    /** Outcome reported to UI callbacks. */
    enum class Result { SENT, QUEUED, FAILED }

    // Guards against overlapping retryPending() runs (e.g. rapid onResume cycles in TugaStore),
    // which would otherwise race on f.delete() and potentially double-POST a queued payload.
    private val retryInFlight = AtomicBoolean(false)

    // Monotonic suffix for queue filenames. Two submit() calls in the same second
    // would otherwise collide on the SimpleDateFormat-derived prefix; without a
    // deterministic suffix the second writeText() silently overwrites the first.
    private val queueSeq = AtomicLong(0L)

    /**
     * Build payload, attempt send; queue on failure. Runs network on a background thread,
     * delivers `onResult` on the main thread.
     */
    fun submit(context: Context, payload: FeedbackPayload, onResult: (Result) -> Unit) {
        val main = Handler(Looper.getMainLooper())
        Thread {
            val result = submitSync(context.applicationContext, payload)
            main.post { onResult(result) }
        }.start()
    }

    /** Synchronous variant for use from background threads / retry loops. */
    fun submitSync(context: Context, payload: FeedbackPayload): Result {
        val json = payload.toJson().toString()
        return if (hasNetwork(context) && tryPost(json)) {
            Result.SENT
        } else if (writeToQueue(context, json, payload.timestampMs)) {
            Result.QUEUED
        } else {
            Result.FAILED
        }
    }

    /**
     * Scan the app-private feedback queue for JSON files and attempt to POST each.
     * Successfully sent files are deleted. Runs in background, fires callback on main when done.
     */
    fun retryPending(context: Context, onDone: ((sent: Int, remaining: Int) -> Unit)? = null) {
        val app = context.applicationContext
        val main = Handler(Looper.getMainLooper())
        if (!retryInFlight.compareAndSet(false, true)) {
            onDone?.let { cb -> main.post { cb(0, queueDir(app).listJsonFiles().size) } }
            return
        }
        Thread {
            try {
                if (!hasNetwork(app)) {
                    onDone?.let { cb -> main.post { cb(0, queueDir(app).listJsonFiles().size) } }
                    return@Thread
                }
                val dir = queueDir(app)
                val files = dir.listJsonFiles()
                var sent = 0
                for (f in files) {
                    val body = runCatching { f.readText() }.getOrNull() ?: continue
                    if (tryPost(body)) {
                        runCatching { f.delete() }
                        sent++
                    }
                }
                val remaining = dir.listJsonFiles().size
                onDone?.let { cb -> main.post { cb(sent, remaining) } }
            } finally {
                retryInFlight.set(false)
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun hasNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val info: NetworkInfo? = try { cm.activeNetworkInfo } catch (_: SecurityException) { return false }
        return info != null && info.isConnected
    }

    private fun tryPost(jsonBody: String): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(ENDPOINT)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            conn.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            code in 200..299
        } catch (e: IOException) {
            Log.w(TAG, "POST failed: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w(TAG, "POST error: ${e.message}")
            false
        } finally {
            conn?.disconnect()
        }
    }

    private fun writeToQueue(context: Context, json: String, timestampMs: Long): Boolean {
        return try {
            val dir = queueDir(context)
            if (!dir.exists() && !dir.mkdirs()) return false
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestampMs))
            // Process-monotonic suffix avoids the same-second collision that `timestampMs % N`
            // can produce when two submissions land in the same millisecond bucket modulo N.
            val seq = queueSeq.incrementAndGet()
            val file = File(dir, "${ts}_${timestampMs}_$seq.json")
            file.writeText(json)
            true
        } catch (e: IOException) {
            Log.w(TAG, "Queue write failed: ${e.message}")
            false
        } catch (e: SecurityException) {
            Log.w(TAG, "Queue write denied: ${e.message}")
            false
        }
    }

    private fun queueDir(context: Context): File = File(context.filesDir, FEEDBACK_DIR)

    private fun File.listJsonFiles(): List<File> =
        if (isDirectory) listFiles { f -> f.isFile && f.name.endsWith(".json") }?.toList().orEmpty()
        else emptyList()
}

/** Plain payload that knows how to serialize itself. */
data class FeedbackPayload(
    val app: String,
    val version: String,
    val type: String,
    val message: String,
    val email: String? = null,
    val diagnostic: String? = null,
    val timestampMs: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("app", app)
        put("version", version)
        put("type", type)
        put("message", message)
        put("timestamp", timestampMs)
        if (!email.isNullOrBlank()) put("email", email)
        if (!diagnostic.isNullOrBlank()) put("diagnostic", diagnostic)
    }
}
