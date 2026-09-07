package im.umo.baro_input

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

class BaroInputService : InputMethodService() {
    private val gate = SessionGate()
    private var panel: ScanPanel? = null
    private var visible = false

    override fun onCreate() { setTheme(R.style.Theme_Baro); super.onCreate() }
    override fun onEvaluateFullscreenMode() = false
    override fun onCreateInputView(): View = makePanel()

    private fun makePanel(importImage: Boolean = false): View {
        panel?.dispose()
        val token = gate.token()
        val info = currentInputEditorInfo
        val restricted = info == null || EditorPolicy.isRestricted(info.inputType)
        val number = info != null && (info.inputType and InputType.TYPE_MASK_CLASS) in listOf(InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE)
        return ScanPanel(this, number, restricted,
            switchKeyboard = { getSystemService(InputMethodManager::class.java).showInputMethodPicker() },
            openSetup = {
                startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            },
            commit = { text ->
                if (!visible || !gate.accepts(token) || text.isEmpty() || currentInputEditorInfo == null || EditorPolicy.isRestricted(currentInputEditorInfo.inputType)) false
                else currentInputConnection?.commitText(text, 1) == true
            },
            sampleBitmap = if (importImage) PendingImage.take(info) else null,
            pickImage = {
                if (visible && gate.accepts(token) && info != null && !restricted) {
                    val request = PendingImage.begin(info)
                    try {
                        startActivity(Intent(this, ImagePickerActivity::class.java)
                            .putExtra("request", request)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (_: android.content.ActivityNotFoundException) {
                        PendingImage.cancel(request)
                    }
                }
            }
        ).also { panel = it }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        gate.invalidate(); panel?.dispose(); panel = null
    }
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        gate.begin(); visible = true
        setInputView(makePanel(importImage = true))
        panel?.begin()
    }
    override fun onFinishInputView(finishingInput: Boolean) {
        clearSession(); super.onFinishInputView(finishingInput)
    }
    override fun onFinishInput() { clearSession(); super.onFinishInput() }
    override fun onWindowHidden() { clearSession(); super.onWindowHidden() }
    override fun onDestroy() { clearSession(); super.onDestroy() }
    private fun clearSession() {
        visible = false; gate.invalidate(); panel?.dispose(); panel = null
    }
}

internal object EditorPolicy {
    fun isRestricted(type: Int): Boolean {
        val cls = type and InputType.TYPE_MASK_CLASS
        val variation = type and InputType.TYPE_MASK_VARIATION
        return type == InputType.TYPE_NULL ||
            (cls == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD) ||
            (cls == InputType.TYPE_CLASS_TEXT && variation in listOf(
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            ))
    }
}
