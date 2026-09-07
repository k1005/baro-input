package im.umo.baro_input

import android.text.InputType
import org.junit.Assert.*
import org.junit.Test

class EditorPolicyTest {
    @Test fun `all password variations are restricted`() {
        listOf(InputType.TYPE_TEXT_VARIATION_PASSWORD, InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD).forEach {
            assertTrue(EditorPolicy.isRestricted(InputType.TYPE_CLASS_TEXT or it))
        }
        assertTrue(EditorPolicy.isRestricted(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD))
    }
    @Test fun `regular text numbers and phone fields remain available`() {
        assertFalse(EditorPolicy.isRestricted(InputType.TYPE_CLASS_TEXT))
        assertFalse(EditorPolicy.isRestricted(InputType.TYPE_CLASS_NUMBER))
        assertFalse(EditorPolicy.isRestricted(InputType.TYPE_CLASS_PHONE))
        assertTrue(EditorPolicy.isRestricted(InputType.TYPE_NULL))
    }
}
