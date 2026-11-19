package dev.opencover.desk

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.WindowManager

/**
 * The desk's open apps, on DeX's taskbar just after its navigation buttons. DeX lists running apps only for its own
 * workspaces, which Samsung never makes on a virtual display, so OpenCover draws them: click one to bring it to the
 * front (or back from minimized). A bar under each; brighter when it's showing.
 */
class DeskTaskbar(ctx: Context, displayId: Int) {
    private val wctx = ctx.createDisplayContext(ctx.getSystemService(DisplayManager::class.java).getDisplay(displayId))
        .createWindowContext(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, null)
    private val wm = wctx.getSystemService(WindowManager::class.java)
    private val row = LinearLayout(wctx).apply { orientation = LinearLayout.HORIZONTAL }
    private val main = Handler(Looper.getMainLooper())
    private val thread = HandlerThread("desk-taskbar").apply { start() }
    private val bg = Handler(thread.looper)
    private var shown = emptyList<Desk.Window>()
    private var added = false
    private var shownLocked = false
    private var popup: PopupMenu? = null

    fun start() {
        val lp = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, BAR_PX,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.BOTTOM or Gravity.START; x = START_PX }
        added = runCatching { wm.addView(row, lp) }.isSuccess
        show(emptyList())
        bg.post(poll)
    }

    fun stop() {
        bg.removeCallbacksAndMessages(null)
        thread.quitSafely()
        main.post { popup?.dismiss(); if (added) runCatching { wm.removeView(row) }; added = false }
    }

    // ponytail: polls the window list a few times a second; a task-stack listener if this ever shows up in battery use.
    private val poll = object : Runnable {
        override fun run() {
            // DeX hides its taskbar while the phone is locked; so does this.
            val locked = wctx.getSystemService(android.app.KeyguardManager::class.java).isKeyguardLocked
            Desk.lockChanged(locked)
            val all = if (locked) emptyList() else Desk.windows().filter { it.pkg != wctx.packageName }
            Desk.windowsChanged(all)
            if (!locked) DeskGame.windowsChanged(all.map { it.pkg }.toSet())
            val now = all.map { it.copy(starting = false) }.sortedBy { it.task } // launch order, as a taskbar keeps it
            if (now != shown || locked != shownLocked) main.post { show(now) }
            bg.postDelayed(this, 300) // often enough for the pointer to follow windows as they move
        }
    }

    private fun show(windows: List<Desk.Window>) {
        val locked = wctx.getSystemService(android.app.KeyguardManager::class.java).isKeyguardLocked
        if (locked) {
            popup?.dismiss()
            shownLocked = true; shown = emptyList(); row.removeAllViews()
            return
        }
        if (popup != null) return
        shownLocked = false
        shown = windows
        row.removeAllViews()
        val active = windows.firstOrNull { it.focused } ?: windows.firstOrNull { it.visible } ?: windows.firstOrNull()
        fun control(symbol: String, label: String, enabled: Boolean = active != null, click: (View) -> Unit) {
            row.addView(Button(wctx).apply {
                text = symbol; textSize = 18f
                contentDescription = label; tooltipText = label
                isEnabled = enabled
                minWidth = 0; minimumWidth = 0; setPadding(0, 0, 0, 0)
                setOnClickListener { click(it) }
            }, LinearLayout.LayoutParams(42, BAR_PX))
        }
        control("−", "Thu nhỏ cửa sổ", active?.visible == true) { active?.let { w -> action { Desk.minimize(w.task) } } }
        control(if (active != null && Desk.isMaximized(active)) "❐" else "□",
            if (active != null && Desk.isMaximized(active)) "Khôi phục cửa sổ" else "Phóng to cửa sổ") {
            active?.let { w -> action { Desk.toggleMaximize(w.task) } }
        }
        control("▥", "Chia cửa sổ", true) { windowMenu(it, active) }
        control("×", "Đóng cửa sổ") { active?.let { w -> action { Desk.close(w.task); null } } }
        val pm = wctx.packageManager
        windows.forEach { w ->
            row.addView(FrameLayout(wctx).apply {
                addView(ImageView(wctx).apply {
                    runCatching { setImageDrawable(pm.getApplicationIcon(w.pkg)) }
                    contentDescription = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(w.pkg, 0)) }.getOrNull()
                }, FrameLayout.LayoutParams(ICON_PX, ICON_PX, Gravity.CENTER))
                addView(View(wctx).apply {
                    background = GradientDrawable().apply { cornerRadius = 2f; setColor(if (w.focused) Color.WHITE else Color.GRAY) }
                }, FrameLayout.LayoutParams(if (w.focused) 16 else 6, 3, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply { bottomMargin = 4 })
                setOnClickListener { action { Desk.show(w.task); null } }
                setOnLongClickListener { windowMenu(it, w); true }
                setOnContextClickListener { windowMenu(it, w); true }
            }, LinearLayout.LayoutParams(CELL_PX, BAR_PX))
        }
    }

    private fun action(work: () -> String?) {
        bg.post {
            val error = runCatching { work() }.getOrElse { "Không điều khiển được cửa sổ" }
            if (error != null) main.post { Toast.makeText(wctx, error, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun windowMenu(anchor: View, window: Desk.Window?) {
        popup?.dismiss()
        val menu = PopupMenu(wctx, anchor)
        popup = menu
        fun item(label: String, work: () -> String?) {
            menu.menu.add(label).setOnMenuItemClickListener { action(work); true }
        }
        item("Chia đôi: 2 ứng dụng") { Desk.arrange(2) }
        item("Chia ba: 3 ứng dụng") { Desk.arrange(3) }
        if (window != null) {
            item("Thu nhỏ cửa sổ") { Desk.minimize(window.task) }
            item(if (Desk.isMaximized(window)) "Khôi phục cửa sổ" else "Phóng to cửa sổ") { Desk.toggleMaximize(window.task) }
            item("Nửa trái") { Desk.snap(window.task, 2, 0) }
            item("Nửa phải") { Desk.snap(window.task, 2, 1) }
            item("Một phần ba bên trái") { Desk.snap(window.task, 3, 0) }
            item("Một phần ba ở giữa") { Desk.snap(window.task, 3, 1) }
            item("Một phần ba bên phải") { Desk.snap(window.task, 3, 2) }
            item("Đóng cửa sổ") { Desk.close(window.task); null }
        }
        menu.setOnDismissListener { if (popup === menu) popup = null }
        menu.show()
    }

    private companion object {
        const val BAR_PX = Desk.TASKBAR_HEIGHT // DeX's taskbar at the desk's density
        const val CELL_PX = 54
        const val ICON_PX = 36
        const val START_PX = 200 // after DeX's Recents, Home and Back
    }
}
