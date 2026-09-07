package im.umo.baro_input

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import im.umo.baro_input.Ui.block
import im.umo.baro_input.Ui.cell
import im.umo.baro_input.Ui.dp

internal class ScanPanel(
    context: Context,
    private var numeric: Boolean,
    private val restricted: Boolean,
    private val switchKeyboard: () -> Unit,
    private val openSetup: () -> Unit,
    private val commit: (String) -> Boolean,
    private var sampleBitmap: Bitmap? = null,
    private val pickImage: (() -> Unit)? = null,
    private val embedded: Boolean = false
) : LinearLayout(context) {
    private enum class Page { IDLE, CAMERA, READING, LINES, RESULT, EDIT, ERROR, SENT }
    private var page = Page.IDLE
    private var errorMessage = R.string.camera_error
    private var image: Bitmap? = null
    private var raw = ""
    private var lines: List<String> = emptyList()
    private var removeSpaces = false
    private var dead = false
    private var operation = 0
    private var camera = CameraSession(context)
    private val korean = lazy { TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build()) }
    private val latin = lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private lateinit var body: LinearLayout
    private lateinit var footer: LinearLayout

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.WHITE)
        render()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (embedded) { super.onMeasure(widthMeasureSpec, heightMeasureSpec); return }
        // InputMethodService replaces root LayoutParams with WRAP_CONTENT. Bound the
        // height here so the weighted scroll area does not collapse to only the header.
        val preferred = minOf(context.dp(540), (resources.displayMetrics.heightPixels * .68).toInt())
        val limit = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) preferred else MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(minOf(preferred, limit), MeasureSpec.EXACTLY))
    }

    fun begin() {
        if (dead) return
        if (restricted) fail(R.string.restricted)
        else if (sampleBitmap == null && context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) fail(R.string.camera_missing)
        else show(Page.CAMERA)
    }

    private fun message(id: Int) = context.getString(id)
    private fun show(next: Page) {
        if (dead) return
        camera.stop(); operation++; page = next; render()
    }
    private fun fail(id: Int) { errorMessage = id; show(Page.ERROR) }
    private fun action(id: Int, primary: Boolean = false, click: () -> Unit) = Ui.button(context, message(id), primary, click)

    private fun render() {
        removeAllViews()
        val header = Ui.row(context).apply { setPadding(context.dp(20), context.dp(4), context.dp(8), context.dp(4)) }
        header.addView(Ui.text(context, message(R.string.app_name), 18f, true), LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        header.addView(Ui.quietButton(context, message(if (pickImage != null || sampleBitmap == null) R.string.switch_keyboard else R.string.choose_test_image)) { camera.stop(); switchKeyboard() })
        addView(header)
        addView(View(context).apply { setBackgroundColor(Ui.line) }, LayoutParams(LayoutParams.MATCH_PARENT, context.dp(1)))
        val scroll = ScrollView(context).apply { isFillViewport = false }
        body = Ui.column(context).apply { setPadding(context.dp(20), context.dp(16), context.dp(20), context.dp(12)) }
        if (!restricted && pickImage != null && page in listOf(Page.IDLE, Page.CAMERA, Page.ERROR, Page.SENT)) {
            body.block(action(R.string.pick_image) { camera.stop(); pickImage.invoke() }, 0)
            if (sampleBitmap != null) body.block(Ui.quietButton(context, message(R.string.use_camera)) { sampleBitmap = null; image = null; begin() }, 4)
        }
        scroll.addView(body)
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        footer = Ui.column(context).apply { setPadding(context.dp(20), context.dp(8), context.dp(20), context.dp(12)) }
        when (page) {
            Page.IDLE -> body.block(action(R.string.scan_start, true) { begin() })
            Page.CAMERA -> cameraPage()
            Page.READING -> {
                body.block(Ui.text(context, message(R.string.processing), 20f, true), 20)
                body.block(ProgressBar(context), 16, context.dp(48))
                footer.block(action(R.string.rescan) { begin() }, 0)
            }
            Page.LINES -> linePage()
            Page.RESULT -> resultPage()
            Page.EDIT -> editPage()
            Page.ERROR -> {
                body.block(Ui.text(context, message(errorMessage), 18f, true).apply {
                    background = Ui.shape(context, Ui.canvas)
                    setPadding(context.dp(16), context.dp(20), context.dp(16), context.dp(20))
                }, 16)
                if (errorMessage == R.string.camera_missing) footer.block(action(R.string.setup, true) { openSetup() }, 0)
                else if (!restricted) footer.block(action(R.string.rescan, true) { begin() }, 0)
            }
            Page.SENT -> {
                body.block(Ui.text(context, message(R.string.inserted), 22f, true), 24)
                footer.block(action(R.string.scan_start, true) { begin() }, 0)
            }
        }
        if (footer.childCount > 0) {
            addView(View(context).apply { setBackgroundColor(Ui.line) }, LayoutParams(LayoutParams.MATCH_PARENT, context.dp(1)))
            addView(footer)
        }
    }

    private fun cameraPage() {
        val modes = Ui.row(context)
        modes.cell(Ui.modeButton(context, message(R.string.text_mode), !numeric) { numeric = false; show(Page.CAMERA) })
        modes.cell(Ui.modeButton(context, message(R.string.number_mode), numeric) { numeric = true; show(Page.CAMERA) })
        body.block(modes, if (body.childCount == 0) 0 else 12)
        sampleBitmap?.let { bitmap ->
            body.block(ImageView(context).apply {
                setImageBitmap(bitmap); scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = message(R.string.test_image)
            }, 12, context.dp(170))
            body.block(Ui.note(context, message(R.string.image_test_note)), 8)
            footer.block(action(R.string.read_test_image, true) {
                image = bitmap; show(Page.READING); recognize(bitmap)
            }, 0)
            return
        }
        val preview = PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
            setBackgroundColor(Ui.ink)
            contentDescription = message(R.string.scan_preview)
        }
        body.block(preview, 12, context.dp(170))
        body.block(Ui.note(context, message(R.string.scan_hint)), 8)
        var torch = false
        val torchButton = Ui.quietButton(context, message(R.string.torch_on)) { }.apply { isEnabled = false }
        torchButton.setOnClickListener {
            torch = !torch; camera.torch(torch)
            torchButton.text = message(if (torch) R.string.torch_off else R.string.torch_on)
        }
        body.block(torchButton, 0)
        val shutter = action(R.string.capture, true) { capture() }.apply { isEnabled = false }
        footer.block(shutter, 0)
        val version = operation
        preview.post {
            if (!dead && page == Page.CAMERA && version == operation) {
                camera.start(preview, ready = { flash ->
                    if (!dead && version == operation) { shutter.isEnabled = true; torchButton.isEnabled = flash }
                }, error = { if (!dead && version == operation) fail(R.string.camera_error) })
            }
        }
    }

    private fun capture() {
        if (page != Page.CAMERA || dead) return
        // Keep the bound camera alive until ImageCapture returns, but ignore repeated presses.
        val version = operation
        page = Page.READING
        footer.removeAllViews()
        body.removeAllViews()
        body.block(Ui.text(context, message(R.string.processing), 20f, true), 20)
        camera.photograph(done = { bitmap ->
            if (dead || version != operation) return@photograph
            camera.stop()
            image = bitmap
            show(Page.READING)
            recognize(bitmap)
        }, error = { if (!dead && version == operation) fail(R.string.camera_error) })
    }

    private fun recognize(bitmap: Bitmap) {
        val version = operation
        val task = try {
            (if (numeric) latin.value else korean.value).process(InputImage.fromBitmap(bitmap, 0))
        } catch (_: Exception) { fail(R.string.ocr_error); return }
        task
            .addOnSuccessListener { result ->
                if (dead || version != operation) return@addOnSuccessListener
                acceptRecognition(bitmap, result.text, result.textBlocks.flatMap { it.lines }.map { it.text })
            }
            .addOnFailureListener { if (!dead && version == operation) fail(R.string.ocr_error) }
    }

    internal fun acceptRecognition(bitmap: Bitmap, text: String, recognizedLines: List<String>) {
        if (dead) return
        image = bitmap; raw = text.trim(); lines = recognizedLines.filter { it.isNotBlank() }
        removeSpaces = false
        when {
            raw.isBlank() -> fail(R.string.no_text)
            lines.size > 1 -> show(Page.LINES)
            else -> show(Page.RESULT)
        }
        // A named pane provides a single state announcement without reading every camera frame.
        androidx.core.view.ViewCompat.setAccessibilityPaneTitle(this, message(if (raw.isBlank()) R.string.no_text else R.string.confirm))
    }

    private fun original(height: Int = 136) {
        image?.let { bitmap ->
            body.block(ImageView(context).apply {
                setImageBitmap(bitmap); scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = message(R.string.original); setBackgroundColor(Ui.canvas)
                adjustViewBounds = true
            }, 0, context.dp(height))
        }
    }

    private fun linePage() {
        original(110)
        body.block(Ui.text(context, message(R.string.select_line), 18f, true))
        lines.forEach { value -> body.block(Ui.button(context, value) { raw = value; show(Page.RESULT) }.apply { gravity = Gravity.START or Gravity.CENTER_VERTICAL; setTextColor(Ui.ink) }, 8) }
        body.block(action(R.string.all_lines) { show(Page.RESULT) })
        footer.block(Ui.quietButton(context, message(R.string.rescan)) { begin() }, 0)
    }

    private fun resultPage() {
        original()
        body.block(Ui.text(context, message(R.string.confirm), 16f).apply { gravity = Gravity.CENTER }, 16)
        val result = Ui.text(context, TextRules.output(raw, removeSpaces), if (numeric) 30f else 24f, true).apply {
            gravity = Gravity.CENTER
            background = Ui.shape(context, Ui.softTeal, Ui.softTeal)
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
            if (numeric) typeface = Typeface.create("sans-serif", Typeface.BOLD)
            contentDescription = message(R.string.recognized) + ": " + text
        }
        body.block(result, 12)
        val warning = Ui.text(context, message(R.string.invalid_number), 15f).apply { setTextColor(Color.rgb(147, 50, 20)) }
        body.block(warning, 8)
        val submit = action(R.string.insert, true) {
            val output = TextRules.output(raw, removeSpaces)
            if (output.isNotBlank() && (!numeric || TextRules.isNumberCandidate(output))) {
                if (commit(output)) { raw = ""; image = null; lines = emptyList(); show(Page.SENT) }
                else fail(R.string.insert_failed)
            }
        }
        fun update() {
            val output = TextRules.output(raw, removeSpaces)
            result.text = output; result.contentDescription = message(R.string.recognized) + ": " + output
            val valid = !numeric || TextRules.isNumberCandidate(output)
            warning.visibility = if (valid) View.GONE else View.VISIBLE
            submit.isEnabled = valid && output.isNotBlank()
        }
        body.block(Switch(context).apply {
            text = message(R.string.remove_spaces); textSize = 16f; minHeight = context.dp(56)
            isChecked = removeSpaces
            setOnCheckedChangeListener { _, checked -> removeSpaces = checked; update() }
        }, 12)
        val actions = Ui.row(context)
        actions.cell(action(R.string.edit) { show(Page.EDIT) })
        actions.cell(action(R.string.rescan) { begin() })
        body.block(actions, 4)
        footer.block(submit, 0)
        update()
    }

    private fun editPage() {
        body.block(Ui.text(context, message(R.string.edit), 18f, true), 0)
        val editor = EditText(context).apply {
            setText(raw); textSize = 24f; setTextColor(Ui.ink)
            contentDescription = message(R.string.recognized)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            showSoftInputOnFocus = false; isSaveEnabled = false; minHeight = context.dp(64)
            background = Ui.shape(context); setPadding(context.dp(12), context.dp(8), context.dp(12), context.dp(8))
        }
        body.block(editor, 8)
        fun replaceSelection(value: String) {
            val start = minOf(editor.selectionStart, editor.selectionEnd).coerceAtLeast(0)
            val end = maxOf(editor.selectionStart, editor.selectionEnd).coerceAtLeast(start)
            editor.text.replace(start, end, value); editor.setSelection(start + value.length)
        }
        fun move(direction: Int) {
            val value = editor.text.toString()
            val pos = editor.selectionEnd.coerceIn(0, value.length)
            if ((direction < 0 && pos > 0) || (direction > 0 && pos < value.length)) editor.setSelection(value.offsetByCodePoints(pos, direction))
        }
        val movement = Ui.row(context)
        movement.cell(action(R.string.cursor_left) { move(-1) })
        movement.cell(action(R.string.delete) {
            if (editor.selectionStart != editor.selectionEnd) replaceSelection("")
            else {
                val (text, cursor) = TextRules.deleteBefore(editor.text.toString(), editor.selectionStart)
                editor.setText(text); editor.setSelection(cursor)
            }
        })
        movement.cell(action(R.string.cursor_right) { move(1) })
        body.block(movement, 4)
        if (numeric) {
            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("-", "0", ".")).forEach { keys ->
                val row = Ui.row(context)
                keys.forEach { key -> row.cell(Ui.button(context, key) { replaceSelection(key) }) }
                body.block(row, 8)
            }
        } else body.block(Ui.text(context, message(R.string.edit_help), 14f))
        val separators = Ui.row(context)
        separators.cell(action(R.string.space) { replaceSelection(" ") })
        separators.cell(action(R.string.newline) { replaceSelection("\n") })
        body.block(separators, 8)
        val finishActions = Ui.row(context)
        finishActions.cell(action(R.string.edit_cancel) { show(Page.RESULT) }.apply { minHeight = context.dp(56); minimumHeight = minHeight })
        finishActions.cell(action(R.string.edit_done, true) { raw = editor.text.toString(); show(Page.RESULT) })
        footer.block(finishActions, 0)
        editor.requestFocus(); editor.setSelection(editor.text.length)
    }

    fun dispose() {
        if (dead) return
        dead = true; operation++; camera.close()
        if (korean.isInitialized()) korean.value.close()
        if (latin.isInitialized()) latin.value.close()
        sampleBitmap = null; image = null; raw = ""; lines = emptyList(); removeAllViews()
    }
}
