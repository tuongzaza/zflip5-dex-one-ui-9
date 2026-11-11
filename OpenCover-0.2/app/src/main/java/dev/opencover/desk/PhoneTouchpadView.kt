package dev.opencover.desk

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/** Phone gestures become relative mouse input on the desktop, not touches on a mirrored screen. */
class PhoneTouchpadView(ctx: Context) : View(ctx) {
    private val motion = TouchpadMotion(2.5f, 40f * resources.displayMetrics.density)
    private var dragging = false
    private var multi = false
    private var scrolled = false
    private val gestures = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean { performClick();return true }
        override fun onDoubleTap(e: MotionEvent): Boolean { performClick();performClick();return true }
        override fun onLongPress(e: MotionEvent) { dragging = true;Desk.touchpad(buttons = 1) }
    })
    init {
        contentDescription = "Vùng bàn di chuột"
        isClickable = true
        background = GradientDrawable().apply {
            setColor(0xff282d38.toInt());cornerRadius = 16f * resources.displayMetrics.density
            setStroke(2, Color.GRAY)
        }
    }
    override fun performClick(): Boolean {
        Desk.touchpad(buttons = 1);Desk.touchpad(buttons = 0)
        super.performClick();return true
    }
    fun releaseButtons() { dragging = false;motion.reset();Desk.touchpad(buttons = 0) }
    fun cancelGestures() {
        val now = android.os.SystemClock.uptimeMillis()
        val cancel = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
        gestures.onTouchEvent(cancel);cancel.recycle();releaseButtons()
    }
    override fun onDetachedFromWindow() { cancelGestures();super.onDetachedFromWindow() }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                releaseButtons();multi = false;scrolled = false
                motion.update(listOf(event.x to event.y))
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                multi = true;releaseButtons()
                val cancel = MotionEvent.obtain(event).apply { action = MotionEvent.ACTION_CANCEL }
                gestures.onTouchEvent(cancel);cancel.recycle()
            }
            MotionEvent.ACTION_MOVE -> {
                val delta = motion.update((0 until event.pointerCount).map { event.getX(it) to event.getY(it) })
                if (event.pointerCount > 1 && kotlin.math.abs(delta.scroll) > 0.03f) scrolled = true
                Desk.touchpad(delta.dx, delta.dy, if (dragging) 1 else 0, delta.scroll)
            }
            MotionEvent.ACTION_POINTER_UP -> motion.reset()
            MotionEvent.ACTION_UP -> {
                if (multi && !scrolled) { Desk.touchpad(buttons = 2);Desk.touchpad(buttons = 0) }
                releaseButtons()
            }
            MotionEvent.ACTION_CANCEL -> releaseButtons()
        }
        if (!multi) gestures.onTouchEvent(event)
        return true
    }
}
