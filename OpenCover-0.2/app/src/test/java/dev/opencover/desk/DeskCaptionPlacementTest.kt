package dev.opencover.desk

import org.junit.Assert.*
import org.junit.Test

class DeskCaptionPlacementTest {
    @Test fun followsTopRightOfMovedOrResizedWindow() {
        assertEquals(DeskLayout.Bounds(808, 120, 1000, 160),
            DeskCaptionPlacement.bounds(DeskLayout.Bounds(200, 120, 1000, 800), emptyList()))
        assertEquals(DeskLayout.Bounds(1968, 0, 2160, 40),
            DeskCaptionPlacement.bounds(DeskLayout.Bounds(0, 0, 2160, 1384), emptyList()))
    }
    @Test fun doesNotFloatOverAWindowInFront() {
        val window = DeskLayout.Bounds(200, 120, 1000, 800)
        assertNull(DeskCaptionPlacement.bounds(window, listOf(DeskLayout.Bounds(900, 100, 1200, 500))))
        assertNotNull(DeskCaptionPlacement.bounds(window, listOf(DeskLayout.Bounds(200, 160, 1000, 800))))
    }
    @Test fun rejectsWindowsWithoutRoomForCaption() {
        assertNull(DeskCaptionPlacement.bounds(DeskLayout.Bounds(0, 0, 100, 500), emptyList()))
        assertNull(DeskCaptionPlacement.bounds(DeskLayout.Bounds(0, -10, 800, 500), emptyList()))
    }
}
