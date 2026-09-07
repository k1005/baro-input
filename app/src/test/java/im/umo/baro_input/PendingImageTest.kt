package im.umo.baro_input

import android.graphics.Bitmap
import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PendingImageTest {
    private fun editor(field: Int = 1) = EditorInfo().apply {
        packageName = "example.target"; fieldId = field; inputType = InputType.TYPE_CLASS_TEXT
    }
    private fun image() = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)

    @Test fun `image survives picker round trip and is consumed only once`() {
        val id = PendingImage.begin(editor())
        val bitmap = image()
        assertTrue(PendingImage.deliver(id, bitmap))
        assertSame(bitmap, PendingImage.take(editor()))
        assertNull(PendingImage.take(editor()))
    }

    @Test fun `another field discards the selected image`() {
        val id = PendingImage.begin(editor())
        val bitmap = image()
        PendingImage.deliver(id, bitmap)
        assertNull(PendingImage.take(editor(2)))
        assertTrue(bitmap.isRecycled)
        assertNull(PendingImage.take(editor()))
    }

    @Test fun `cancelled or superseded picker cannot deliver late image`() {
        val first = PendingImage.begin(editor())
        val second = PendingImage.begin(editor())
        assertFalse(PendingImage.deliver(first, image()))
        PendingImage.cancel(second)
        assertFalse(PendingImage.deliver(second, image()))
    }
}
