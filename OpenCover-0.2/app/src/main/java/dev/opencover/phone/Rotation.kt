package dev.opencover.phone

import dev.opencover.COVER_DISPLAY

/**
 * Shell script that pins the cover at [rotation] (Surface.ROTATION_*). The window manager never turns the cover from
 * an app's or an overlay's orientation request (nothing feeds it a sensor while the phone is folded), but it does obey
 * the user rotation once the cover is fixed to it.
 */
fun holdRotation(rotation: Int): String =
    "cmd window fixed-to-user-rotation -d $COVER_DISPLAY enabled; cmd window user-rotation -d $COVER_DISPLAY lock $rotation"

/** Clockwise quarter turns after [turns]: 0 -> 1 -> 2 -> 3 -> 0. */
fun nextTurns(turns: Int): Int = (turns + 1) % 4

/** Convert the device's orientation to display rotation, with a dead band around each diagonal. */
fun sensedCoverTurns(degrees: Int, previous: Int?): Int? {
    if (degrees !in 0..359) return previous
    if (previous != null) {
        val distance = kotlin.math.abs((degrees + previous * 90 + 180).mod(360) - 180)
        if (distance <= 55) return previous
    }
    return (-(degrees + 45) / 90).mod(4)
}

/** "pkg=turns;pkg=turns" as stored in PhonePrefs. Upright (0) apps are not stored; bad entries are skipped. */
fun parseRotations(s: String?): Map<String, Int> =
    s.orEmpty().split(';').mapNotNull {
        val parts = it.split('=', limit = 2)
        val turns = parts.getOrNull(1)?.toIntOrNull()
        if (parts[0].isEmpty() || turns == null || turns !in 1..3) null else parts[0] to turns
    }.toMap()

fun encodeRotations(m: Map<String, Int>): String = m.filterValues { it in 1..3 }.entries.joinToString(";") { "${it.key}=${it.value}" }
