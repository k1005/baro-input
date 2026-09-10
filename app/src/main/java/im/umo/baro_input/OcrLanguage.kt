package im.umo.baro_input

import android.content.Context
import android.os.LocaleList
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** OCR choice is independent of UI language; only this preference is persisted. */
internal enum class OcrLanguage(val tag: String, val label: Int) {
    ENGLISH("en", R.string.language_english),
    JAPANESE("ja", R.string.language_japanese),
    CHINESE("zh", R.string.language_chinese),
    KOREAN("ko", R.string.language_korean);

    fun createRecognizer(): TextRecognizer = when (this) {
        ENGLISH -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        JAPANESE -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
        CHINESE -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        KOREAN -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    fun save(context: Context) {
        context.getSharedPreferences("recognition", Context.MODE_PRIVATE).edit().putString("language", tag).apply()
    }

    companion object {
        fun fromLocales(locales: LocaleList): OcrLanguage {
            for (i in 0 until locales.size()) {
                entries.firstOrNull { it.tag == locales[i].language }?.let { return it }
            }
            return ENGLISH
        }

        fun selected(context: Context): OcrLanguage {
            val saved = context.getSharedPreferences("recognition", Context.MODE_PRIVATE).getString("language", null)
            return entries.firstOrNull { it.tag == saved } ?: fromLocales(context.resources.configuration.locales)
        }
    }
}
