package im.umo.baro_input

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import im.umo.baro_input.Ui.block
import im.umo.baro_input.Ui.dp
import im.umo.baro_input.Ui.insets

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }
    override fun onResume() { super.onResume(); render() }
    private fun render() {
        val allowed = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val enabled = getSystemService(InputMethodManager::class.java).enabledInputMethodList.any { it.packageName == packageName }
        val scroll = ScrollView(this).apply { setBackgroundColor(Ui.canvas) }
        val content = Ui.column(this)
        content.block(Ui.text(this, getString(R.string.app_name), 15f, true).apply { setTextColor(Ui.teal) }, 0)
        content.block(Ui.text(this, getString(R.string.tagline), 28f, true), 16)
        content.block(Ui.note(this, getString(R.string.intro)), 12)
        val cameraCard = Ui.card(this)
        cameraCard.block(Ui.text(this, getString(R.string.camera_title), 18f, true), 0)
        cameraCard.block(Ui.note(this, getString(R.string.camera_detail)), 8)
        if (allowed) cameraCard.block(Ui.text(this, getString(R.string.camera_ready), 14f, true).apply { setTextColor(Ui.teal) })
        else cameraCard.block(Ui.button(this, getString(R.string.camera_allow)) {
            if (!allowed) requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
        })
        if (!allowed) cameraCard.block(Ui.quietButton(this, getString(R.string.open_settings)) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }, 4)
        content.block(cameraCard, 24)
        val imeCard = Ui.card(this)
        imeCard.block(Ui.text(this, getString(R.string.ime_title), 18f, true), 0)
        imeCard.block(Ui.note(this, getString(R.string.ime_detail)), 8)
        if (enabled) imeCard.block(Ui.text(this, getString(R.string.ime_ready), 14f, true).apply { setTextColor(Ui.teal) })
        imeCard.block(Ui.button(this, getString(R.string.ime_enable), !enabled) { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) })
        content.block(imeCard, 12)
        content.block(Ui.button(this, getString(R.string.practice), true) { startActivity(Intent(this, PracticeActivity::class.java)) }, 24)
        content.block(Ui.text(this, getString(R.string.privacy), 14f).apply { setTextColor(Ui.muted) }, 24)
        scroll.addView(content); setContentView(scroll); insets(scroll)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); render()
    }
}
