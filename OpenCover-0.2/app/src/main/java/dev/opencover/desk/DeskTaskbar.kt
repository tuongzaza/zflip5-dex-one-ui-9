package dev.opencover.desk

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
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
class DeskTaskbar(private val ctx: Context, private val displayId: Int) {
    private val wctx = ctx.createDisplayContext(ctx.getSystemService(DisplayManager::class.java).getDisplay(displayId))
        .createWindowContext(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, null)
    private var wm = wctx.getSystemService(WindowManager::class.java)
    private val row = LinearLayout(wctx).apply { orientation = LinearLayout.HORIZONTAL }
    private val main = Handler(Looper.getMainLooper())
    private val thread = HandlerThread("desk-taskbar").apply { start() }
    private val bg = Handler(thread.looper)
    private var shown = emptyList<Desk.Window>()
    private var stopped = false
    private var attachFailed = false
    private var added = false
    private var shownLocked = false
    private var popup: PopupMenu? = null
    private val captions = mutableMapOf<Int, DeskCaption>()

    fun start() {
        row.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) { added = false }
        })
        attach()
        show(emptyList())
        bg.post(poll)
    }

    private fun attach() {
        if (stopped || added) return
        val display = ctx.getSystemService(DisplayManager::class.java).getDisplay(displayId) ?: return
        // A newly created display may not have its accessibility token yet. Refresh it on every retry.
        val lp = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, BAR_PX,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply {
                gravity = Gravity.BOTTOM or Gravity.START; x = START_PX
                title = "OpenCover desktop taskbar"
            }
        runCatching {
            val context = ctx.createWindowContext(display, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, null)
            wm = context.getSystemService(WindowManager::class.java)
            wm.addView(row, lp)
            added = true
            Log.i("OpenCoverDesk", "taskbar attached on display $displayId")
            attachFailed = false
        }.onFailure {
            if (!attachFailed) Log.w("OpenCoverDesk", "taskbar unavailable on display $displayId; retrying", it)
            attachFailed = true
        }
    }

    fun stop() {
        stopped = true
        bg.removeCallbacksAndMessages(null)
        thread.quitSafely()
        main.post { popup?.dismiss(); captions.values.forEach { it.stop() }; captions.clear(); if (added) runCatching { wm.removeView(row) }; added = false }
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
            if (now != shown || locked != shownLocked) main.post { if (!stopped) show(now) }
            main.post { if (!stopped) { attach(); showCaptions(all) } }
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

    private fun showCaptions(windows: List<Desk.Window>) {
        val visible = mutableSetOf<Int>()
        val above = mutableListOf<DeskLayout.Bounds>()
        windows.filter { it.visible }.forEach { w ->
            val b = w.bounds.let { DeskLayout.Bounds(it.left, it.top, it.right, it.bottom) }
            val strip = DeskCaptionPlacement.bounds(b, above)
            if (strip != null) {
                visible += w.task
                captions.getOrPut(w.task) { DeskCaption(ctx, displayId, w, ::action, ::windowMenu) }.update(w, strip)
            }
            above += b
        }
        captions.keys.filter { it !in visible }.forEach { captions.remove(it)?.stop() }
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
