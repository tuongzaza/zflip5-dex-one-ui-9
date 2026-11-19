package dev.opencover.desk

/** Caption controls stay inside their window and never cover a window in front. */
internal object DeskCaptionPlacement {
    const val WIDTH = 192
    const val HEIGHT = 40
    fun bounds(window: DeskLayout.Bounds, above: List<DeskLayout.Bounds>): DeskLayout.Bounds? {
        if (window.right - window.left < WIDTH || window.bottom - window.top < HEIGHT || window.top < 0) return null
        val strip = DeskLayout.Bounds(window.right - WIDTH, window.top, window.right, window.top + HEIGHT)
        if (above.any { it.left < strip.right && it.right > strip.left && it.top < strip.bottom && it.bottom > strip.top }) return null
        return strip
    }
}
