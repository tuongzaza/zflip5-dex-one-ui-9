package dev.opencover.mirror

import org.junit.Assert.*
import org.junit.Test

class MirrorSourceTest {
    @Test fun excludesCoverAndCaptureDestinations() {
        val all = listOf(
            MirrorSource(0, "Inner", true), MirrorSource(1, "Cover", true),
            MirrorSource(339, "scrcpy", false), MirrorSource(340, "OpenCover-mirror", false),
            MirrorSource(350, "HDMI", false), MirrorSource(351, "Another internal", true))
        assertEquals(listOf(0, 339, 350), mirrorSources(all).map { it.id })
    }
    @Test fun neverFallsBackToInnerWhenDesktopDisappears() {
        assertNull(selectedMirrorSource(listOf(MirrorSource(0, "Inner", true)), 339))
    }
    @Test fun selectionIsByCurrentIdNotListPosition() {
        assertEquals(350, selectedMirrorSource(listOf(MirrorSource(350, "HDMI", false), MirrorSource(0, "Inner", true)), 350)?.id)
    }
}
