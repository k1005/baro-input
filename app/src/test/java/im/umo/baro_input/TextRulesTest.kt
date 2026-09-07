package im.umo.baro_input

import org.junit.Assert.*
import org.junit.Test

class TextRulesTest {
    @Test fun `leading zero and punctuation survive default output`() {
        assertEquals("0012 3456-7890", TextRules.output("0012 3456-7890", false))
        assertEquals("00123456-7890", TextRules.output("0012 3456-7890", true))
    }
    @Test fun `removing unicode whitespace is explicit`() {
        val value = "00\u00a012\n34\t56"
        assertEquals(value, TextRules.output(value, false))
        assertEquals("00123456", TextRules.output(value, true))
    }
    @Test fun `ambiguous letters never become numbers silently`() {
        assertFalse(TextRules.isNumberCandidate("O012"))
        assertFalse(TextRules.isNumberCandidate("12I4"))
        assertFalse(TextRules.isNumberCandidate("---"))
        assertTrue(TextRules.isNumberCandidate("+82 (0)10-0012-3456"))
    }
    @Test fun `backspace does not split a supplementary character`() {
        assertEquals("가" to 1, TextRules.deleteBefore("가😀", 3))
        assertEquals("001" to 3, TextRules.deleteBefore("0012", 4))
        assertEquals("0012" to 0, TextRules.deleteBefore("0012", 0))
    }
}
