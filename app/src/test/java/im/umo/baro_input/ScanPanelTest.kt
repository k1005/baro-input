package im.umo.baro_input

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
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
        assertVisible("입력")
        button(panel, "수정").performClick()
        assertVisible("수정 완료")
        panel.dispose()
    }

    @Test fun `image picker is available without camera permission`() {
        var selected = false
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), false, false, {}, {}, { true },
            pickImage = { selected = true })
        panel.begin()
        button(panel, "사진첩 · 이미지 파일 선택").performClick()
        assertTrue(selected)
        panel.dispose()
    }

    @Test fun `restricted field does not offer image import`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), false, true, {}, {}, { true },
            pickImage = { fail("restricted picker") })
        panel.begin()
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == "사진첩 · 이미지 파일 선택" })
        panel.dispose()
    }

    @Test fun `sample starts without camera permission and still requires explicit confirmation`() {
        var committed: String? = null
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, false, {}, {},
            { committed = it; true }, sampleBitmap = bitmap)
        panel.begin()
        assertTrue(button(panel, "이 사진 읽기").isEnabled)
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == "촬영하고 읽기" })
        panel.acceptRecognition(bitmap, "SERIAL NUMBER\n0012 3456 7890", listOf("SERIAL NUMBER", "0012 3456 7890"))
        button(panel, "0012 3456 7890").performClick()
        assertNull(committed)
        children(panel).filterIsInstance<Switch>().single().isChecked = true
        button(panel, "입력").performClick()
        assertEquals("001234567890", committed)
        panel.dispose()
    }

    @Test fun `sample does not bypass restricted editor policy`() {
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, true, {}, {}, { true }, sampleBitmap = bitmap)
        panel.begin()
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == "이 사진 읽기" })
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
        button(panel, "입력").performClick()
        assertEquals("00123456", committed)
        assertTrue(children(panel).filterIsInstance<Button>().none { it.text == "입력" })
        panel.dispose()
    }
    @Test fun `ambiguous OCR must be corrected before number insertion`() {
        var inserted = false
        val panel = ScanPanel(RuntimeEnvironment.getApplication(), true, false, {}, {}, { inserted = true; true })
        panel.acceptRecognition(bitmap, "O012", listOf("O012"))
        assertFalse(button(panel, "입력").isEnabled)
        button(panel, "입력").performClick()
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
