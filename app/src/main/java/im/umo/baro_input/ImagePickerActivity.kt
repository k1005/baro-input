package im.umo.baro_input

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ScrollView
import im.umo.baro_input.Ui.block
import im.umo.baro_input.Ui.insets
import java.util.concurrent.Executors

/** Owns the system picker and URI grant while decoding; only a bitmap crosses back to the IME. */
class ImagePickerActivity : Activity() {
    private val loader = Executors.newSingleThreadExecutor()
    private val requestToken get() = intent.getStringExtra("request")
    private var delivered = false
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PendingImage.active(requestToken)) { finish(); return }
        val root = Ui.column(this)
        root.block(Ui.back(this) { finish() }, 0)
        root.block(Ui.text(this, getString(R.string.pick_image), 26f, true))
        status = Ui.note(this, getString(R.string.image_return_hint))
        root.block(status, 8)
        root.block(Ui.button(this, getString(R.string.pick_image), true) { choose() })
        val scroll = ScrollView(this).apply { setBackgroundColor(Ui.canvas); addView(root) }
        setContentView(scroll)
        insets(scroll)
        if (savedInstanceState == null) choose()
    }

    @Suppress("DEPRECATION")
    private fun choose() {
        try {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, 1)
        } catch (_: android.content.ActivityNotFoundException) {
            status.setText(R.string.image_load_error)
        }
    }

    @Deprecated("Framework activity result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1) return
        val uri = data?.data
        if (resultCode != RESULT_OK || uri == null) { finish(); return }
        status.setText(R.string.image_loading)
        loader.execute {
            val image = try {
                ImageTestActivity.decodeImage { contentResolver.openInputStream(uri) ?: error("No image stream") }
            } catch (_: Exception) { null }
            runOnUiThread {
                if (isDestroyed || isFinishing) { image?.recycle(); return@runOnUiThread }
                if (image == null) status.setText(R.string.image_load_error)
                else {
                    delivered = PendingImage.deliver(requestToken, image)
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing && !delivered) PendingImage.cancel(requestToken)
        loader.shutdownNow()
        super.onDestroy()
    }
}
