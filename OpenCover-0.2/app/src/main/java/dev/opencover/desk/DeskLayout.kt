package dev.opencover.desk

/** Pixel geometry and per-task restore history for the desktop work area (taskbar excluded). */
internal class DeskLayout(private val width: Int, private val height: Int) {
    data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int)
    private val normal = mutableMapOf<Int, Bounds>()

    init { require(width > 0 && height > 0) }

    fun columns(count: Int): List<Bounds> {
        require(count in 2..3)
        return List(count) { i -> Bounds(width * i / count, 0, width * (i + 1) / count, height) }
    }
    fun isMaximized(b: Bounds) = b.left == 0 && b.top == 0 && b.right == width && b.bottom >= height

    @Synchronized fun observe(task: Int, b: Bounds) {
        if (b.right > b.left && b.bottom > b.top && !isMaximized(b)) normal[task] = b
    }
    @Synchronized fun toggle(task: Int, current: Bounds): Bounds {
        if (!isMaximized(current)) {
            observe(task, current)
            return Bounds(0, 0, width, height)
        }
        val b = normal[task] ?: Bounds(width / 6, height / 8, width - width / 6, height - height / 8)
        val w = (b.right - b.left).coerceIn(1, width)
        val h = (b.bottom - b.top).coerceIn(1, height)
        val x = b.left.coerceIn(0, width - w)
        val y = b.top.coerceIn(0, height - h)
        return Bounds(x, y, x + w, y + h)
    }
    @Synchronized fun retain(tasks: Set<Int>) { normal.keys.retainAll(tasks) }
}
