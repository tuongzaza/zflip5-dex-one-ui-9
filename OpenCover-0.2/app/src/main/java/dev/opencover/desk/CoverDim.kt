package dev.opencover.desk

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

/** Manually black out the active phone display without locking the desktop; a touch restores it. */
class CoverDim(private val service: AccessibilityService, displayId: Int) {
    private val ctx = service.createDisplayContext(service.getSystemService(DisplayManager::class.java).getDisplay(displayId))
        .createWindowContext(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, null)
    private val wm = ctx.getSystemService(WindowManager::class.java)
    private val black = View(ctx).apply {
        setBackgroundColor(Color.BLACK)
        setOnTouchListener { _, e -> if (e.action == MotionEvent.ACTION_UP) stop(); true }
    }
    var dark = false; private set

    fun start() = rest()

    fun stop() {
        if (dark) runCatching { wm.removeView(black) }
        dark = false
        (service as? dev.opencover.watcher.CoverAccessibility)?.updateNavBar()
    }

    fun rest() {
        if (dark) return
        dark = runCatching {
            wm.addView(black, WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                // The phone's black overlay must never take keyboard focus away from the desktop.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.OPAQUE).apply {
                title = "OpenCover phone screen off"
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                fitInsetsTypes = 0
            })
        }.isSuccess
        (service as? dev.opencover.watcher.CoverAccessibility)?.updateNavBar()
    }
}
