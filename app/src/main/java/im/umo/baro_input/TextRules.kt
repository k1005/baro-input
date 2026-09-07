package im.umo.baro_input

/** Lossless by default: identifiers are strings, never numbers. */
object TextRules {
    fun output(raw: String, removeSpaces: Boolean): String =
        if (removeSpaces) raw.filterNot { it.isWhitespace() || it == '\u00a0' } else raw

    fun isNumberCandidate(value: String): Boolean =
        value.any { it in '0'..'9' } && value.all {
            it in '0'..'9' || it.isWhitespace() || it == '\u00a0' || it in "-+./(),"
        }

    fun deleteBefore(text: String, cursor: Int): Pair<String, Int> {
        val end = cursor.coerceIn(0, text.length)
        if (end == 0) return text to 0
        val start = text.offsetByCodePoints(end, -1)
        return text.removeRange(start, end) to start
    }
}
