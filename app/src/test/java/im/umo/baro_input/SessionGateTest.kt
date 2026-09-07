package im.umo.baro_input

import org.junit.Assert.*
import org.junit.Test

class SessionGateTest {
    @Test fun `a late OCR result cannot enter a different field`() {
        val gate = SessionGate()
        val first = gate.begin()
        assertTrue(gate.accepts(first))
        val second = gate.begin()
        assertFalse(gate.accepts(first))
        assertTrue(gate.accepts(second))
    }
    @Test fun `hiding keyboard invalidates even the latest scan`() {
        val gate = SessionGate()
        val token = gate.begin()
        gate.invalidate()
        assertFalse(gate.accepts(token))
    }
}
