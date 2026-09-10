package im.umo.baro_input

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/** Model integration smoke tests, not a guarantee of character-perfect recognition. */
@RunWith(AndroidJUnit4::class)
class OfflineOcrTest {
    private fun read(language: OcrLanguage, sample: String, localeTag: String = language.tag) {
        val bitmap = Bitmap.createBitmap(1600, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 80f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textLocale = java.util.Locale.forLanguageTag(localeTag)
        }
        canvas.drawText(sample, 60f, 145f, paint)
        val recognizer = language.createRecognizer()
        try {
            val result = Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)), 60, TimeUnit.SECONDS)
            val normalized = result.text.filterNot { it.isWhitespace() }
            val expected = sample.filterNot { it.isWhitespace() }
            android.util.Log.i("OfflineOcrTest", "$localeTag: expected=$expected actual=$normalized")
            // Chinese models can confuse similar glyphs and simplified/traditional forms.
            // Require at least 75% character accuracy and exact digits to verify model wiring;
            // retain actual output in device logs for quality review.
            val maxErrors = if (language == OcrLanguage.CHINESE) expected.length / 4 else 0
            assertTrue("$localeTag: expected $sample, got ${result.text}",
                distance(expected, normalized) <= maxErrors && normalized.endsWith("12345"))
        } finally {
            recognizer.close()
            bitmap.recycle()
        }
    }

    private fun distance(expected: String, actual: String): Int {
        var previous = IntArray(actual.length + 1) { it }
        expected.forEachIndexed { i, a ->
            val current = IntArray(actual.length + 1)
            current[0] = i + 1
            actual.forEachIndexed { j, b ->
                current[j + 1] = minOf(current[j] + 1, previous[j + 1] + 1,
                    previous[j] + if (a == b) 0 else 1)
            }
            previous = current
        }
        return previous.last()
    }

    @Test fun english() = read(OcrLanguage.ENGLISH, "PRODUCT NUMBER 12345")
    @Test fun japanese() = read(OcrLanguage.JAPANESE, "こんにちは 日本語 12345")
    @Test fun simplifiedChinese() = read(OcrLanguage.CHINESE, "中文识别 产品编号 12345", "zh-CN")
    @Test fun traditionalChinese() = read(OcrLanguage.CHINESE, "中文辨識 產品編號 12345", "zh-TW")
    @Test fun korean() = read(OcrLanguage.KOREAN, "안녕하세요 제품번호 12345")
}
