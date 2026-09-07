package im.umo.baro_input

import android.graphics.Bitmap
import android.os.SystemClock
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import java.util.UUID

/** Main-thread-only, one-use handoff. Never retains an InputConnection or commits text. */
internal object PendingImage {
    private data class Target(val packageName: String?, val fieldId: Int, val type: Int)
    private fun target(info: EditorInfo) = Target(info.packageName, info.fieldId, info.inputType)
    private var destination: Target? = null
    private var token: String? = null
    private var expires = 0L
    private var bitmap: Bitmap? = null

    fun begin(info: EditorInfo): String {
        clear()
        destination = target(info)
        expires = SystemClock.elapsedRealtime() + 300_000
        return UUID.randomUUID().toString().also { id ->
            token = id
            Handler(Looper.getMainLooper()).postDelayed({ cancel(id) }, 300_000)
        }
    }

    fun active(id: String?): Boolean = id != null && id == token && SystemClock.elapsedRealtime() < expires

    fun deliver(id: String?, image: Bitmap): Boolean {
        if (!active(id)) { image.recycle(); return false }
        bitmap?.recycle()
        bitmap = image
        return true
    }

    fun take(info: EditorInfo?): Bitmap? {
        if (!active(token) || info == null || destination != target(info) || EditorPolicy.isRestricted(info.inputType)) {
            clear(); return null
        }
        val result = bitmap ?: return null
        bitmap = null
        clear()
        return result
    }

    fun cancel(id: String?) { if (id == token) clear() }
    private fun clear() {
        bitmap?.recycle(); bitmap = null; destination = null; token = null
    }
}
