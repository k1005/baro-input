package im.umo.baro_input

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import im.umo.baro_input.Ui.block
import im.umo.baro_input.Ui.insets

/** Bundled policy remains accessible without an internet connection. */
class PrivacyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { setBackgroundColor(Ui.canvas) }
        val content = Ui.column(this)
        content.block(Ui.back(this) { finish() }, 0)
        content.block(Ui.text(this, "개인정보처리방침", 26f, true), 16)
        val policy = assets.open("privacy-policy.txt").bufferedReader().use { it.readText() }
        content.block(Ui.text(this, policy, 16f).apply { setTextIsSelectable(true) }, 16)
        scroll.addView(content)
        setContentView(scroll)
        insets(scroll)
    }
}
