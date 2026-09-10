package im.umo.baro_input

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ScanPanelTest {
    private fun children(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { children(view.getChildAt(it)) } else emptyList()
    private fun button(panel: View, title: String) = children(panel).filterIsInstance<Button>().single { it.text == title }
    private val bitmap get() = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888)

    @Test fun `bottom actions avoid navigation controls across pages and navigation modes`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, false, {}, {}, { true })
        fun applyNavigation(left: Int, right: Int, bottom: Int) {
            ViewCompat.dispatchApplyWindowInsets(panel, WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(left, 0, right, bottom))
                .build())
        }
        fun assertSafe(title: String, left: Int, right: Int, bottom: Int) {
            panel.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.AT_MOST))
            panel.layout(0, 0, panel.measuredWidth, panel.measuredHeight)
            val target = button(panel, title)
            val rect = android.graphics.Rect()
            target.getDrawingRect(rect)
            panel.offsetDescendantRectToMyCoords(target, rect)
            assertTrue(rect.top >= 0)
            assertTrue(rect.bottom <= panel.height - bottom)
            assertTrue(rect.left >= left && rect.right <= panel.width - right)
            assertTrue(target.height >= 48)
        }
        applyNavigation(0, 0, 48)
        panel.acceptRecognition(bitmap, "0012", listOf("0012"))
        assertSafe(RuntimeEnvironment.getApplication().getString(R.string.insert), 0, 0, 48)
        button(panel, RuntimeEnvironment.getApplication().getString(R.string.edit)).performClick()
        assertSafe(RuntimeEnvironment.getApplication().getString(R.string.edit_done), 0, 0, 48)
        assertSafe(RuntimeEnvironment.getApplication().getString(R.string.edit_cancel), 0, 0, 48)
        applyNavigation(0, 0, 24)
        assertSafe(RuntimeEnvironment.getApplication().getString(R.string.edit_done), 0, 0, 24)
        applyNavigation(0, 48, 0)
        assertSafe(RuntimeEnvironment.getApplication().getString(R.string.edit_done), 0, 48, 0)
        applyNavigation(0, 0, 48)
        panel.acceptRecognition(bitmap, "first\nsecond", listOf("first", "second"))
        assertSafe(RuntimeEnvironment.getApplication().getString(R.string.rescan), 0, 0, 48)
        panel.dispose()
    }

    @Test fun `embedded preview leaves system insets to its activity`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), false, false, {}, {}, { true }, embedded = true)
        ViewCompat.dispatchApplyWindowInsets(panel, WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 48, 48))
            .build())
        assertEquals(0, panel.paddingBottom)
        assertEquals(0, panel.paddingRight)
        panel.dispose()
    }

    @Test fun `confirmation and edit completion stay visible in a short panel with large text`() {
        val app = RuntimeEnvironment.getApplication()
        val config = android.content.res.Configuration(app.resources.configuration).apply { fontScale = 1.3f }
        val context = app.createConfigurationContext(config)
        val panel = ScanPanel(context, true, false, {}, {}, { true })
        panel.acceptRecognition(bitmap, "0012 3456 7890", listOf("0012 3456 7890"))
        fun assertVisible(title: String) {
            panel.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.AT_MOST))
            panel.layout(0, 0, panel.measuredWidth, panel.measuredHeight)
            val target = button(panel, title)
            val rect = android.graphics.Rect()
            target.getDrawingRect(rect)
            panel.offsetDescendantRectToMyCoords(target, rect)
            assertTrue(rect.top >= 0 && rect.bottom <= panel.height)
            assertTrue(rect.left >= 0 && rect.right <= panel.width)
            assertTrue(target.height >= 48)
        }
        assertVisible(RuntimeEnvironment.getApplication().getString(R.string.insert))
        button(panel, RuntimeEnvironment.getApplication().getString(R.string.edit)).performClick()
        assertVisible(RuntimeEnvironment.getApplication().getString(R.string.edit_done))
        panel.dispose()
    }

    @Test fun `image picker is available without camera permission`() {
        var selected = false
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), false, false, {}, {}, { true },
            pickImage = { selected = true })
        panel.begin()
        button(panel, RuntimeEnvironment.getApplication().getString(R.string.pick_image)).performClick()
        assertTrue(selected)
        panel.dispose()
    }

    @Test fun `restricted field does not offer image import`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), false, true, {}, {}, { true },
            pickImage = { fail("restricted picker") })
        panel.begin()
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == RuntimeEnvironment.getApplication().getString(R.string.pick_image) })
        panel.dispose()
    }

    @Test fun `sample starts without camera permission and still requires explicit confirmation`() {
        var committed: String? = null
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, false, {}, {},
            { committed = it; true }, sampleBitmap = bitmap)
        panel.begin()
        assertTrue(button(panel, RuntimeEnvironment.getApplication().getString(R.string.read_test_image)).isEnabled)
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == RuntimeEnvironment.getApplication().getString(R.string.capture) })
        panel.acceptRecognition(bitmap, "SERIAL NUMBER\n0012 3456 7890", listOf("SERIAL NUMBER", "0012 3456 7890"))
        button(panel, "0012 3456 7890").performClick()
        assertNull(committed)
        children(panel).filterIsInstance<Switch>().single().isChecked = true
        button(panel, RuntimeEnvironment.getApplication().getString(R.string.insert)).performClick()
        assertEquals("001234567890", committed)
        panel.dispose()
    }

    @Test fun `sample does not bypass restricted editor policy`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, true, {}, {}, { true }, sampleBitmap = bitmap)
        panel.begin()
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == RuntimeEnvironment.getApplication().getString(R.string.read_test_image) })
        panel.dispose()
    }

    @Test fun `IME wrap content host retains a visible scrollable input panel`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), false, false, {}, {}, { true })
        panel.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        panel.measure(View.MeasureSpec.makeMeasureSpec(390, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(844, View.MeasureSpec.AT_MOST))
        assertTrue(panel.measuredHeight > 200)
        assertTrue(panel.measuredHeight <= 844)
        panel.dispose()
    }

    @Test fun `confirm requires a tap and preserves leading zeros after optional space removal`() {
        var committed: String? = null
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, false, {}, {}, { committed = it; true })
        panel.acceptRecognition(bitmap, "0012 3456", listOf("0012 3456"))
        assertNull(committed)
        children(panel).filterIsInstance<Switch>().single().isChecked = true
        button(panel, RuntimeEnvironment.getApplication().getString(R.string.insert)).performClick()
        assertEquals("00123456", committed)
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == RuntimeEnvironment.getApplication().getString(R.string.insert) })
        panel.dispose()
    }
    @Test fun `ambiguous OCR must be corrected before number insertion`() {
        var inserted = false
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, false, {}, {}, { inserted = true; true })
        panel.acceptRecognition(bitmap, "O012", listOf("O012"))
        assertFalse(button(panel, RuntimeEnvironment.getApplication().getString(R.string.insert)).isEnabled)
        button(panel, RuntimeEnvironment.getApplication().getString(R.string.insert)).performClick()
        assertFalse(inserted)
        panel.dispose()
    }
    @Test fun `disposing a panel discards a late recognition result`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), false, false, {}, {}, { fail("late commit"); false })
        panel.dispose()
        panel.acceptRecognition(bitmap, "0012", listOf("0012"))
        assertEquals(0, panel.childCount)
    }
}
