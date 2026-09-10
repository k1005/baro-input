package im.umo.baro_input

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.LocaleList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalizationTest {
    private fun context(tags: String): Context {
        val app = RuntimeEnvironment.getApplication()
        return app.createConfigurationContext(Configuration(app.resources.configuration).apply {
            setLocales(LocaleList.forLanguageTags(tags))
        })
    }

    private fun children(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { children(view.getChildAt(it)) } else emptyList()

    @Test fun `regional locales and unsupported languages resolve UI and policy together`() {
        val cases = mapOf(
            "en-US" to ("Insert" to "Privacy Policy"),
            "en-GB" to ("Insert" to "Privacy Policy"),
            "ja-JP" to ("入力" to "プライバシーポリシー"),
            "ko-KR" to ("입력" to "개인정보처리방침"),
            "zh-CN" to ("插入" to "隐私政策"),
            "zh-SG" to ("插入" to "隐私政策"),
            "zh-TW" to ("插入" to "隱私權政策"),
            "zh-HK" to ("插入" to "隱私權政策"),
            "zh-Hant" to ("插入" to "隱私權政策"),
            "fr-FR" to ("Insert" to "Privacy Policy"),
            "de-DE,ja-JP,en-US" to ("入力" to "プライバシーポリシー")
        )
        cases.forEach { (tags, expected) ->
            val c = context(tags)
            assertEquals(tags, expected.first, c.getString(R.string.insert))
            val policy = c.resources.openRawResource(R.raw.privacy_policy).bufferedReader().use { it.readText() }
            assertTrue(tags, policy.contains(expected.second))
        }
    }

    @Test fun `recognition follows preferred supported language until explicitly changed`() {
        val ko = context("ko-KR")
        ko.getSharedPreferences("recognition", Context.MODE_PRIVATE).edit().clear().commit()
        assertEquals(OcrLanguage.KOREAN, OcrLanguage.selected(ko))
        assertEquals(OcrLanguage.JAPANESE, OcrLanguage.selected(context("fr-FR,ja-JP")))
        assertEquals(OcrLanguage.CHINESE, OcrLanguage.selected(context("zh-Hant-TW")))
        assertEquals(OcrLanguage.ENGLISH, OcrLanguage.selected(context("de-DE")))
        OcrLanguage.JAPANESE.save(ko)
        assertEquals(OcrLanguage.JAPANESE, OcrLanguage.selected(ko))
        assertEquals(OcrLanguage.JAPANESE, OcrLanguage.selected(context("en-US")))
        assertEquals("입력", ko.getString(R.string.insert))
    }

    @Test fun `text mode offers all languages and numeric mode hides language choice`() {
        val c = context("ko-KR")
        c.getSharedPreferences("recognition", Context.MODE_PRIVATE).edit().clear().commit()
        val bitmap = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888)
        val panel = ScanPanel(c, false, false, {}, {}, { true }, sampleBitmap = bitmap)
        panel.begin()
        val picker = children(panel).filterIsInstance<Spinner>().single()
        assertEquals(4, picker.count)
        assertEquals("한국어", picker.selectedItem)
        picker.setSelection(OcrLanguage.JAPANESE.ordinal)
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(OcrLanguage.JAPANESE, OcrLanguage.selected(c))
        children(panel).filterIsInstance<Button>().single { it.text == c.getString(R.string.number_mode) }.performClick()
        assertTrue(children(panel).filterIsInstance<Spinner>().isEmpty())
        children(panel).filterIsInstance<Button>().single { it.text == c.getString(R.string.text_mode) }.performClick()
        assertEquals("일본어", children(panel).filterIsInstance<Spinner>().single().selectedItem)
        panel.dispose()
    }

    @Test fun `translated confirmation actions fit small panels with large fonts`() {
        listOf("en-US", "ko-KR", "ja-JP", "zh-CN", "zh-TW").forEach { tag ->
            val base = context(tag)
            val c = base.createConfigurationContext(Configuration(base.resources.configuration).apply { fontScale = 1.3f })
            val panel = ScanPanel(c, true, false, {}, {}, { true })
            panel.acceptRecognition(Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888), "0012", listOf("0012"))
            fun check(id: Int) {
                panel.measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.AT_MOST))
                panel.layout(0, 0, panel.measuredWidth, panel.measuredHeight)
                val button = children(panel).filterIsInstance<Button>().single { it.text == c.getString(id) }
                val rect = android.graphics.Rect()
                button.getDrawingRect(rect)
                panel.offsetDescendantRectToMyCoords(button, rect)
                assertTrue(tag, rect.top >= 0 && rect.bottom <= panel.height)
                assertTrue(tag, rect.left >= 0 && rect.right <= panel.width)
                assertTrue(tag, button.height >= 48)
            }
            check(R.string.insert)
            children(panel).filterIsInstance<Button>().single { it.text == c.getString(R.string.edit) }.performClick()
            check(R.string.edit_done)
            check(R.string.edit_cancel)
            panel.dispose()
        }
    }
}
