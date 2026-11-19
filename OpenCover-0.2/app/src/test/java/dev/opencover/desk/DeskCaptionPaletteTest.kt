package dev.opencover.desk

import org.junit.Assert.*
import org.junit.Test

class DeskCaptionPaletteTest {
    @Test fun picksBackgroundRatherThanCaptionText() {
        assertEquals(0xff2d2d30.toInt(), DeskCaptionPalette.dominant(intArrayOf(
            0xff2d2d30.toInt(), -1, 0xff2d2d30.toInt(), 0xff252528.toInt(), 0xff2d2d30.toInt())))
    }
    @Test fun ignoresTransparentSamplesAndAllowsBlack() {
        assertNull(DeskCaptionPalette.dominant(intArrayOf(0, 0)))
        assertEquals(0xff000000.toInt(), DeskCaptionPalette.dominant(intArrayOf(0, 0xff000000.toInt())))
    }
    @Test fun foregroundContrastsWithBothThemes() {
        assertEquals(-1, DeskCaptionPalette.foreground(0xff2d2d30.toInt()))
        assertEquals(0xff17171a.toInt(), DeskCaptionPalette.foreground(0xfff3f3f3.toInt()))
    }
}
