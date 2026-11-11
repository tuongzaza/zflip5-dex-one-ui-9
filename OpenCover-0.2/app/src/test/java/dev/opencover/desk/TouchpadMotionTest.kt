package dev.opencover.desk

import org.junit.Assert.*
import org.junit.Test

class TouchpadMotionTest {
    @Test fun movementIsRelativeAndFirstContactDoesNotJump() {
        val pad = TouchpadMotion(2f, 40f)
        assertEquals(PadDelta(), pad.update(listOf(100f to 200f)))
        assertEquals(PadDelta(20f, -10f), pad.update(listOf(110f to 195f)))
    }
    @Test fun twoFingersScrollWithoutMovingPointer() {
        val pad = TouchpadMotion(2f, 40f)
        pad.update(listOf(10f to 10f, 30f to 10f))
        assertEquals(PadDelta(scroll = -1f), pad.update(listOf(10f to 50f, 30f to 50f)))
    }
    @Test fun addingOrLiftingFingerNeverJumps() {
        val pad = TouchpadMotion(2f, 40f)
        pad.update(listOf(100f to 100f))
        assertEquals(PadDelta(), pad.update(listOf(100f to 100f, 500f to 500f)))
        assertEquals(PadDelta(), pad.update(listOf(500f to 500f)))
    }
    @Test fun resetAndInvalidCoordinatesCannotMovePointer() {
        val pad = TouchpadMotion(2f, 40f)
        pad.update(listOf(20f to 20f));pad.reset()
        assertEquals(PadDelta(), pad.update(listOf(500f to 500f)))
        assertEquals(PadDelta(), pad.update(listOf(Float.NaN to 0f)))
        assertEquals(PadDelta(), pad.update(listOf(100f to 100f)))
    }
}
