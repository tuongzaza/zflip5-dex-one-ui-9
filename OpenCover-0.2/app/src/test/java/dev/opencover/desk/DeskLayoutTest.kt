package dev.opencover.desk

import org.junit.Assert.*
import org.junit.Test

class DeskLayoutTest {
    @Test fun thirdsCoverOddWidthWithoutOverflowOrGap() {
        val layout = DeskLayout(1919, 1024)
        val parts = layout.columns(3)
        assertEquals(DeskLayout.Bounds(0, 0, 639, 1024), parts[0])
        assertEquals(parts[0].right, parts[1].left)
        assertEquals(parts[1].right, parts[2].left)
        assertEquals(1919, parts.last().right)
        assertTrue(parts.all { it.bottom == 1024 })
    }
    @Test fun halvesUseOnlyTheAvailableWorkArea() {
        assertEquals(listOf(DeskLayout.Bounds(0, 0, 1080, 1384), DeskLayout.Bounds(1080, 0, 2160, 1384)), DeskLayout(2160, 1384).columns(2))
    }
    @Test fun maximizationDoesNotOverwritePreviousSize() {
        val layout = DeskLayout(2160, 1384)
        val normal = DeskLayout.Bounds(519, 179, 1599, 999)
        layout.observe(1, normal)
        val full = layout.toggle(1, normal)
        assertEquals(DeskLayout.Bounds(0, 0, 2160, 1384), full)
        layout.observe(1, DeskLayout.Bounds(0, 0, 2160, 1440))
        assertEquals(normal, layout.toggle(1, full))
    }
    @Test fun restoresEachTaskAndKeepsSplitSizeBeforeMaximizing() {
        val layout = DeskLayout(2160, 1384)
        val left = layout.columns(3)[0]
        val right = layout.columns(2)[1]
        layout.toggle(1, left)
        layout.toggle(2, right)
        val full = DeskLayout.Bounds(0, 0, 2160, 1384)
        assertEquals(left, layout.toggle(1, full))
        assertEquals(right, layout.toggle(2, full))
    }
    @Test fun removedTaskHasNoStaleRestoreSizeAndFallbackFits() {
        val layout = DeskLayout(2160, 1384)
        layout.observe(1, DeskLayout.Bounds(-50, -10, 550, 600))
        layout.retain(emptySet())
        val fallback = layout.toggle(1, DeskLayout.Bounds(0, 0, 2160, 1440))
        assertTrue(fallback.left > 0 && fallback.top > 0 && fallback.right < 2160 && fallback.bottom < 1384)
    }
    @Test fun clampsRestoredWindowInsideWorkArea() {
        val layout = DeskLayout(2160, 1384)
        layout.observe(1, DeskLayout.Bounds(1900, 1200, 2500, 1800))
        assertEquals(DeskLayout.Bounds(1560, 784, 2160, 1384), layout.toggle(1, DeskLayout.Bounds(0, 0, 2160, 1384)))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsUnsupportedColumnCount() { DeskLayout(2160, 1384).columns(4) }
}
