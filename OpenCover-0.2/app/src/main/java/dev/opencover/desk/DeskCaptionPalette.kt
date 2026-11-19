package dev.opencover.desk

internal object DeskCaptionPalette {
    fun dominant(pixels: IntArray): Int? = pixels.filter { it ushr 24 == 255 }
        .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

    fun foreground(background: Int): Int {
        val brightness = ((background shr 16 and 255) * 299 + (background shr 8 and 255) * 587 + (background and 255) * 114) / 1000
        return if (brightness >= 160) 0xff17171a.toInt() else -1
    }
}
