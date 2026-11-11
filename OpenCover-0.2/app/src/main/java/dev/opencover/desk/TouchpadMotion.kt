package dev.opencover.desk

/** Relative motion only; touching a different place or changing fingers never warps the cursor. */
data class PadDelta(val dx: Float = 0f, val dy: Float = 0f, val scroll: Float = 0f)

class TouchpadMotion(private val gain: Float, private val scrollPixels: Float) {
    private var previous: Pair<Float, Float>? = null
    private var fingers = 0
    fun reset() { previous = null; fingers = 0 }
    fun update(points: List<Pair<Float, Float>>): PadDelta {
        if (points.isEmpty() || points.any { !it.first.isFinite() || !it.second.isFinite() }) {
            reset();return PadDelta()
        }
        val now = points.map { it.first }.average().toFloat() to points.map { it.second }.average().toFloat()
        val old = previous
        previous = now
        val changed = fingers != points.size
        fingers = points.size
        if (old == null || changed) return PadDelta()
        return if (fingers == 1) PadDelta((now.first-old.first)*gain, (now.second-old.second)*gain)
        else PadDelta(scroll = -(now.second-old.second)/scrollPixels)
    }
}
