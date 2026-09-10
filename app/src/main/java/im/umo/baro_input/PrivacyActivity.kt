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
        content.block(Ui.text(this, getString(R.string.privacy_policy), 26f, true), 16)
        val policy = resources.openRawResource(R.raw.privacy_policy).bufferedReader().use { it.readText() }
        content.block(Ui.text(this, policy, 16f).apply { setTextIsSelectable(true) }, 16)
        scroll.addView(content)
        setContentView(scroll)
        insets(scroll)
    }
}
