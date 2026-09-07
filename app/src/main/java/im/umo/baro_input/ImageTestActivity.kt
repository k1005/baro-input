package im.umo.baro_input

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import im.umo.baro_input.Ui.block
import im.umo.baro_input.Ui.dp
import im.umo.baro_input.Ui.insets
import java.io.InputStream
import java.util.concurrent.Executors

/** A standalone practice target: importing a photo never resumes a different app's IME session. */
class ImageTestActivity : Activity() {
    private var panel: ScanPanel? = null
    private var generation = 0
    private val loader = Executors.newSingleThreadExecutor()
    private lateinit var content: LinearLayout
    private lateinit var status: TextView
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showChooser()
    }

    private fun showChooser() {
        generation++
        panel?.dispose(); panel = null
        val scroll = ScrollView(this).apply { setBackgroundColor(Ui.canvas) }
        content = Ui.column(this)
        content.block(Ui.back(this) { finish() }, 0)
        content.block(Ui.text(this, getString(R.string.image_test), 26f, true))
        content.block(Ui.note(this, getString(R.string.image_test_intro)), 8)
        content.block(Ui.button(this, getString(R.string.bundled_sample), true) {
            loadImage { assets.open("sample-box.png") }
        }, 24)
        content.block(Ui.button(this, getString(R.string.stored_image)) {
            @Suppress("DEPRECATION")
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, PICK_IMAGE)
        })
        status = Ui.text(this, "", 15f)
        content.block(status)
        scroll.addView(content); setContentView(scroll); insets(scroll)
    }

    @Deprecated("Framework activity result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_IMAGE || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        loadImage { contentResolver.openInputStream(uri) ?: error("No image stream") }
    }

    private fun loadImage(open: () -> InputStream) {
        val version = ++generation
        status.setText(R.string.image_loading)
        loader.execute {
            val bitmap = try { decodeImage(open) } catch (_: Exception) { null }
            runOnUiThread {
                if (isDestroyed || isFinishing || version != generation) {
                    bitmap?.recycle()
                    return@runOnUiThread
                }
                if (bitmap == null) status.setText(R.string.image_load_error)
                else showImage(bitmap)
            }
        }
    }

    private fun showImage(bitmap: Bitmap) {
        val root = Ui.column(this)
        root.setBackgroundColor(Ui.canvas)
        val output = Ui.card(this)
        output.block(Ui.text(this, getString(R.string.test_output), 14f, true).apply { setTextColor(Ui.teal) }, 0)
        result = Ui.text(this, getString(R.string.test_output_hint), 16f).apply {
            isSaveEnabled = false
            setPadding(0, dp(8), 0, 0)
        }
        val resultScroll = ScrollView(this).apply { addView(result) }
        output.block(resultScroll, 0, dp((60 * resources.configuration.fontScale).toInt().coerceAtLeast(60)))
        root.addView(output, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dp(20), dp(8), dp(20), dp(16))
        })
        panel = ScanPanel(this, true, false, { showChooser() }, {}, { value ->
            result.text = value
            true
        }, sampleBitmap = bitmap, embedded = true)
        root.addView(panel, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root); insets(root, horizontal = 0, vertical = 0)
        panel?.begin()
    }

    override fun onDestroy() {
        generation++; panel?.dispose(); loader.shutdownNow()
        super.onDestroy()
    }

    companion object {
        private const val PICK_IMAGE = 1

        /** Bound decode memory and honor photo rotation, including mirrored EXIF orientations. */
        internal fun decodeImage(open: () -> InputStream): Bitmap {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open().use { BitmapFactory.decodeStream(it, null, options) }
            require(options.outWidth > 0 && options.outHeight > 0)
            options.inSampleSize = 1
            while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > 2048) {
                options.inSampleSize *= 2
            }
            options.inJustDecodeBounds = false
            val bitmap = open().use { BitmapFactory.decodeStream(it, null, options) }
                ?: error("Cannot decode image")
            val orientation = try {
                open().use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }
            } catch (_: Exception) { 1 }
            val transform = Matrix()
            when (orientation) {
                2 -> transform.setScale(-1f, 1f)
                3 -> transform.setRotate(180f)
                4 -> transform.setScale(1f, -1f)
                5 -> { transform.setRotate(90f); transform.postScale(-1f, 1f) }
                6 -> transform.setRotate(90f)
                7 -> { transform.setRotate(270f); transform.postScale(-1f, 1f) }
                8 -> transform.setRotate(270f)
            }
            if (transform.isIdentity) return bitmap
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, transform, true).also {
                if (it !== bitmap) bitmap.recycle()
            }
        }
    }
}
