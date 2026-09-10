package im.umo.baro_input

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Opt-in captures of the real photo-practice flow, including real OCR results. */
@RunWith(AndroidJUnit4::class)
class StoreCaptureTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private fun children(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { children(view.getChildAt(it)) } else emptyList()

    @Test fun capturePractice() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("storeCapture") == "true")
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(Intent(context, ImageTestActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        val directory = File(context.getExternalFilesDir(null), "store-captures").apply { mkdirs() }
        fun waitUntil(condition: () -> Boolean) {
            val end = System.currentTimeMillis() + 20000
            while (System.currentTimeMillis() < end) {
                var ready = false
                instrumentation.runOnMainSync { ready = condition() }
                if (ready) { instrumentation.waitForIdleSync(); return }
                Thread.sleep(100)
            }
            var visibleText = ""
            instrumentation.runOnMainSync {
                visibleText = children(activity.window.decorView).filterIsInstance<TextView>().joinToString(" | ") { it.text }
            }
            error("Capture state timed out: $visibleText")
        }
        fun has(id: Int) = children(activity.window.decorView).filterIsInstance<Button>()
            .any { it.text == activity.getString(id) }
        fun click(id: Int) {
            instrumentation.runOnMainSync {
                children(activity.window.decorView).filterIsInstance<Button>()
                    .single { it.text == activity.getString(id) }.performClick()
            }
            instrumentation.waitForIdleSync()
        }
        fun capture(name: String) {
            instrumentation.waitForIdleSync()
            Thread.sleep(250)
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        try {
            click(R.string.bundled_sample)
            waitUntil { has(R.string.read_test_image) }
            capture("01-photo")
            click(R.string.read_test_image)
            waitUntil { has(R.string.all_lines) }
            capture("02-lines")
            instrumentation.runOnMainSync {
                children(activity.window.decorView).filterIsInstance<Button>()
                    .single { it.text.toString().replace(" ", "") == "001234567890" }.performClick()
            }
            instrumentation.waitForIdleSync()
            capture("03-confirm")
            click(R.string.insert)
            waitUntil { has(R.string.scan_start) }
            capture("04-done")
            click(R.string.scan_start)
            click(R.string.text_mode)
            instrumentation.runOnMainSync {
                children(activity.window.decorView).filterIsInstance<Spinner>().single().performClick()
            }
            capture("05-languages")
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
