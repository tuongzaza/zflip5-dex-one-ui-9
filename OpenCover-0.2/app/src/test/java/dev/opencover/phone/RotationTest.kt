package dev.opencover.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RotationTest {
    @Test fun turnsCycleThroughFourQuarters() {
        assertEquals(1, nextTurns(0))
        assertEquals(3, nextTurns(2))
        assertEquals(0, nextTurns(3))
    }

    @Test fun holdingFixesTheCoverToUserRotationThenLocksIt() {
        val script = holdRotation(2)
        assertTrue(script.indexOf("fixed-to-user-rotation -d 1 enabled") < script.indexOf("user-rotation -d 1 lock 2"))
    }

    @Test fun rotationsRoundTrip() {
        val m = mapOf("a.b" to 1, "c.d" to 3)
        assertEquals(m, parseRotations(encodeRotations(m)))
    }

    @Test fun uprightAndBadEntriesAreDropped() {
        assertEquals(mapOf("a" to 2), parseRotations("a=2;b=0;c=9;d=x;=1;junk"))
        assertEquals(emptyMap<String, Int>(), parseRotations(null))
        assertEquals("a=2", encodeRotations(mapOf("a" to 2, "b" to 0)))
    }

    @Test fun sensorDirectionMapsToAllFourCoverRotations() {
        assertEquals(0, sensedCoverTurns(0, null))
        assertEquals(3, sensedCoverTurns(90, null))
        assertEquals(2, sensedCoverTurns(180, null))
        assertEquals(1, sensedCoverTurns(270, null))
        assertEquals(0, sensedCoverTurns(359, null))
    }

    @Test fun flatAndDiagonalPositionsKeepTheCurrentRotation() {
        assertNull(sensedCoverTurns(-1, null))
        assertEquals(2, sensedCoverTurns(-1, 2))
        assertEquals(0, sensedCoverTurns(53, 0))
        assertEquals(3, sensedCoverTurns(56, 0))
        assertEquals(3, sensedCoverTurns(37, 3))
        assertEquals(0, sensedCoverTurns(34, 3))
    }
}
