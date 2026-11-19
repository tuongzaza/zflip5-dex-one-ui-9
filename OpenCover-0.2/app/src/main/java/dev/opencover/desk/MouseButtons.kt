package dev.opencover.desk

/** Each reader owns its buttons; reports from an idle second mouse must not release a drag. */
internal class MouseButtons {
    private val pressed = mutableMapOf<Int, Int>()
    fun update(source: Int, buttons: Int): Int {
        if (buttons == 0) pressed.remove(source) else pressed[source] = buttons
        return pressed.values.fold(0) { all, value -> all or value }
    }
}
