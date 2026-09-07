package im.umo.baro_input

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import im.umo.baro_input.Ui.block
import im.umo.baro_input.Ui.dp
import im.umo.baro_input.Ui.insets

class PracticeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { setBackgroundColor(Ui.canvas) }
        val body = Ui.column(this)
        body.block(Ui.back(this) { finish() }, 0)
        body.block(Ui.text(this, getString(R.string.practice_title), 26f, true), 12)
        body.block(Ui.note(this, getString(R.string.practice_intro)), 8)
        body.block(Ui.button(this, getString(R.string.image_test)) {
            startActivity(Intent(this, ImageTestActivity::class.java))
        })
        fun field(label: Int, hint: Int, type: Int) {
            val input = EditText(this).apply {
                id = View.generateViewId(); inputType = type; setHint(hint); textSize = 20f
                minHeight = dp(60); setPadding(dp(16), dp(14), dp(16), dp(14)); background = Ui.shape(this@PracticeActivity)
                setTextColor(Ui.ink); setHintTextColor(Ui.muted)
                isSaveEnabled = false
            }
            body.block(Ui.text(this, getString(label), 17f, true).apply { labelFor = input.id }, 24)
            body.block(input, 8)
        }
        field(R.string.product_number, R.string.product_hint, InputType.TYPE_CLASS_NUMBER)
        field(R.string.memo, R.string.memo_hint, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        body.block(Ui.quietButton(this, getString(R.string.switch_keyboard)) {
            getSystemService(InputMethodManager::class.java).showInputMethodPicker()
        })
        body.block(Ui.text(this, getString(R.string.practice_note), 14f).apply { setTextColor(Ui.muted) }, 24)
        scroll.addView(body); setContentView(scroll); insets(scroll)
    }
}
