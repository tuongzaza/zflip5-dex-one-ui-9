package dev.opencover.desk

import android.content.Context
import android.graphics.Color
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.os.Handler
import android.os.Looper
import android.content.res.Configuration
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

/** A non-focusable caption on one app window; input continues to belong to the app. Main thread. */
internal class DeskCaption(
    private val ctx: Context,
    private val displayId: Int,
    private var window: Desk.Window,
    private val action: (() -> String?) -> Unit,
) {
    private val context = ctx.createDisplayContext(ctx.getSystemService(DisplayManager::class.java).getDisplay(displayId))
    private val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
    private var wm: WindowManager? = null
    private var attached = false
    private var failed = false
    private var position: DeskLayout.Bounds? = null
    private val maximize: Button
    private val handler = Handler(Looper.getMainLooper())
    private var layouts: DeskSnapPopup? = null
    private val openLayouts = Runnable { if (attached) showLayouts() }
    private var appearance: Triple<DeskLayout.Bounds, Boolean, Int>? = null
    private var nextColorAt = 0L
    private var colorRequest: Runnable? = null
    private var generation = 0

    init {
        fun button(symbol: String, label: String, click: (View) -> Unit): Button = Button(context).apply {
            text = symbol; textSize = 18f; contentDescription = label; tooltipText = label
            minWidth = 0; minimumWidth = 0; setPadding(0, 0, 0, 0); setTextColor(Color.WHITE)
            background = RippleDrawable(ColorStateList.valueOf(0x55ffffff), ColorDrawable(Color.TRANSPARENT), null)
            setOnClickListener(click)
            row.addView(this, LinearLayout.LayoutParams(DeskCaptionPlacement.WIDTH / 4, DeskCaptionPlacement.HEIGHT))
        }
        button("−", "Thu nhỏ cửa sổ") { action { Desk.minimize(window.task) } }
        maximize = button("□", "Phóng to cửa sổ") { action { Desk.toggleMaximize(window.task) } }
        button("▥", "Chia cửa sổ") { showLayouts() }.apply {
            tooltipText = null
            setOnHoverListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_HOVER_ENTER -> {
                        layouts?.keepOpen()
                        handler.removeCallbacks(openLayouts)
                        handler.postDelayed(openLayouts, 250)
                    }
                    MotionEvent.ACTION_HOVER_EXIT -> {
                        handler.removeCallbacks(openLayouts)
                        layouts?.leave()
                    }
                }
                false
            }
        }
        button("×", "Đóng cửa sổ") { action { Desk.close(window.task); null } }
        row.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) { attached = false }
        })
    }

    fun update(window: Desk.Window, bounds: DeskLayout.Bounds) {
        this.window = window
        val full = Desk.isMaximized(window)
        maximize.text = if (full) "❐" else "□"
        maximize.contentDescription = if (full) "Khôi phục cửa sổ" else "Phóng to cửa sổ"
        maximize.tooltipText = maximize.contentDescription
        val state = Triple(bounds, window.focused, context.resources.configuration.uiMode)
        val now = android.os.SystemClock.uptimeMillis()
        if (appearance != state || now >= nextColorAt) {
            nextColorAt = now + 2_000
            appearance = state
            colorRequest?.let { handler.removeCallbacks(it) }
            val request = ++generation
            if (position == null) tint(if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
                0xff2d2d30.toInt() else 0xfff3f3f3.toInt())
            colorRequest = Runnable {
                if (attached && generation == request) Desk.captionColor(bounds) {
                    if (attached && generation == request) tint(it)
                }
            }.also { handler.postDelayed(it, 250) }
        }
        if (attached && position == bounds) return
        if (position != null && position != bounds) layouts?.dismiss()
        val params = WindowManager.LayoutParams(DeskCaptionPlacement.WIDTH, DeskCaptionPlacement.HEIGHT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply {
                gravity = Gravity.TOP or Gravity.START; x = bounds.left; y = bounds.top
                title = "OpenCover window controls ${window.task}"
                // Native captions occupy the top edge, including when the app is maximized.
                setFitInsetsTypes(0)
            }
        runCatching {
            if (attached) wm?.updateViewLayout(row, params) else {
                val display = ctx.getSystemService(DisplayManager::class.java).getDisplay(displayId) ?: return
                val fresh = ctx.createWindowContext(display, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, null)
                wm = fresh.getSystemService(WindowManager::class.java)
                wm?.addView(row, params)
                attached = true
            }
            position = bounds
            failed = false
        }.onFailure {
            if (!failed) Log.w("OpenCoverDesk", "couldn't attach window controls for ${window.task}; retrying", it)
            failed = true
        }
    }

    private fun tint(color: Int) {
        row.setBackgroundColor(color)
        val foreground = DeskCaptionPalette.foreground(color)
        for (i in 0 until row.childCount) (row.getChildAt(i) as Button).setTextColor(foreground)
    }

    private fun showLayouts() {
        if (layouts?.isShowing == true) { layouts?.keepOpen(); return }
        val menu = DeskSnapPopup(context, window, action) { layouts = null }
        layouts = menu
        runCatching { menu.show(row.getChildAt(2)) }.onFailure {
            layouts = null
            Log.w("OpenCoverDesk", "couldn't show snap layouts", it)
        }
    }

    fun stop() {
        generation++
        handler.removeCallbacksAndMessages(null)
        layouts?.dismiss()
        if (attached) runCatching { wm?.removeView(row) }
        attached = false
    }
}
