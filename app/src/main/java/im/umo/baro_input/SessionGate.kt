package im.umo.baro_input

/** A scan belongs to one visible editing session, never a later app or field. */
class SessionGate {
    private var generation = 0L
    private var active = false
    fun begin(): Long { generation++; active = true; return generation }
    fun invalidate() { generation++; active = false }
    fun accepts(token: Long): Boolean = active && token == generation
    fun token(): Long = generation
}
