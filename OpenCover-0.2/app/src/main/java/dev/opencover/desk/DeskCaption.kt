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
    menu: (View, Desk.Window) -> Unit,
) {
    private val context = ctx.createDisplayContext(ctx.getSystemService(DisplayManager::class.java).getDisplay(displayId))
    private val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
    private var wm: WindowManager? = null
    private var attached = false
    private var failed = false
    private var position: DeskLayout.Bounds? = null
    private val maximize: Button

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
        button("▥", "Chia cửa sổ") { menu(it, window) }
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
        row.setBackgroundColor(if (window.focused) 0xff303030.toInt() else 0xff555555.toInt())
        if (attached && position == bounds) return
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

    fun stop() {
        if (attached) runCatching { wm?.removeView(row) }
        attached = false
    }
}
