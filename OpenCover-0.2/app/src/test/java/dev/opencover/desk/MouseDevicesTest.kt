package dev.opencover.desk

import org.junit.Assert.*
import org.junit.Test

class MouseDevicesTest {
    @Test fun keepsRealMouseWhenKeyboardAlsoExposesAMouse() {
        val listing = """
            add device 1: /dev/input/event14
              name: "AULA-F99 Mouse"
              events:
                KEY (0001): BTN_MOUSE BTN_RIGHT
                REL (0002): REL_X REL_Y
            add device 2: /dev/input/event16
              name: "AULA-SC650 Mouse"
              events:
                KEY (0001): BTN_MOUSE BTN_RIGHT BTN_MIDDLE
                REL (0002): REL_X REL_Y REL_WHEEL
        """.trimIndent()
        assertEquals(listOf("/dev/input/event14", "/dev/input/event16"), MouseDevices.all(listing))
    }

    @Test fun findsBluetoothMouseWithoutSysfsAccess() {
        val listing = """
            add device 1: /dev/input/event14
              name: "AULA-SC650"
              events:
                ABS (0003): ABS_MISC
            add device 2: /dev/input/event13
              name: "AULA-SC650 Mouse"
              events:
                KEY (0001): BTN_MOUSE BTN_RIGHT BTN_MIDDLE
                REL (0002): REL_X REL_Y REL_HWHEEL REL_WHEEL
            add device 3: /dev/input/event12
              name: "AULA-SC650 Keyboard"
              events:
                KEY (0001): KEY_ESC KEY_1
                REL (0002): REL_HWHEEL
        """.trimIndent()
        assertEquals("/dev/input/event13", MouseDevices.find(listing))
    }

    @Test fun rejectsTouchscreensSensorsAndKeyboard() {
        assertNull(MouseDevices.find("""
            add device 1: /dev/input/event10
              events:
                KEY (0001): BTN_TOUCH
                ABS (0003): ABS_X ABS_Y
            add device 2: /dev/input/event9
              events:
                REL (0002): REL_X REL_Y
            add device 3: /dev/input/event12
              events:
                KEY (0001): KEY_ESC KEY_1
                REL (0002): REL_HWHEEL
        """.trimIndent()))
    }

    @Test fun capabilitiesMustBelongToTheSameDevice() {
        assertNull(MouseDevices.find("""
            add device 1: /dev/input/event2
                KEY (0001): BTN_MOUSE
                REL (0002): REL_X
            add device 2: /dev/input/event3
                REL (0002): REL_Y
        """.trimIndent()))
    }

    @Test fun handlesNoDeviceAndRejectsUnexpectedPaths() {
        assertNull(MouseDevices.find("could not open /dev/input/event13, Permission denied"))
        assertNull(MouseDevices.find("""
            add device 1: /tmp/event13
                KEY (0001): BTN_MOUSE
                REL (0002): REL_X REL_Y
        """.trimIndent()))
        assertNull(MouseDevices.find(""))
    }
}
