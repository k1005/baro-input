package im.umo.baro_input

import android.content.Intent
import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ImagePickerActivityTest {
    @Test fun `picker requests only openable images and cancelling clears handoff`() {
        val id = PendingImage.begin(EditorInfo().apply {
            packageName = "example.target"; fieldId = 1; inputType = InputType.TYPE_CLASS_TEXT
        })
        val controller = Robolectric.buildActivity(ImagePickerActivity::class.java,
            Intent().putExtra("request", id)).setup()
        val activity = controller.get()
        val picker = shadowOf(activity).nextStartedActivityForResult.intent
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, picker.action)
        assertEquals("image/*", picker.type)
        assertTrue(picker.hasCategory(Intent.CATEGORY_OPENABLE))
        activity.finish()
        controller.pause().stop().destroy()
        assertFalse(PendingImage.active(id))
    }
}
