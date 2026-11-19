package dev.opencover.desk

import org.junit.Assert.*
import org.junit.Test

class MouseButtonsTest {
    @Test fun idleSecondDeviceDoesNotReleaseDrag() {
        val buttons = MouseButtons()
        assertEquals(1, buttons.update(1, 1))
        assertEquals(1, buttons.update(2, 0))
        assertEquals(3, buttons.update(2, 2))
        assertEquals(2, buttons.update(1, 0))
        assertEquals(0, buttons.update(2, 0))
    }
    @Test fun reconnectCannotInheritOldReadersButtons() {
        val buttons = MouseButtons()
        buttons.update(1, 1)
        buttons.update(2, 1)
        assertEquals(1, buttons.update(1, 0))
        assertEquals(0, buttons.update(2, 0))
        assertEquals(0, buttons.update(3, 0))
    }
}
