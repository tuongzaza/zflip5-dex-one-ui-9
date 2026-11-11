package dev.opencover.desk

import android.app.KeyguardManager
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button

/** A phone-only entry point, available while ordinary phone apps are in use. */
class PhoneModeButton(private val ctx: Context) {
    private val main = Handler(Looper.getMainLooper())
    private var button: Button? = null
    private var wm: WindowManager? = null
    private var displayId = -1

    fun start() { main.post(refresh) }
    fun stop() { main.removeCallbacks(refresh); remove() }

    private fun remove() {
        button?.let { view -> runCatching { wm?.removeView(view) } }
        button = null; wm = null; displayId = -1
    }

    private val refresh = object : Runnable {
        override fun run() {
            val id = PhoneModeActivity.phoneDisplay(ctx)
            if (PhoneModeActivity.visible || ctx.getSystemService(KeyguardManager::class.java).isKeyguardLocked) {
                remove()
            } else if (button == null || displayId != id) {
                remove()
                val display = ctx.getSystemService(DisplayManager::class.java).getDisplay(id)
                if (display != null) {
                    val wc = ctx.createDisplayContext(display)
                        .createWindowContext(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, null)
                    val manager = wc.getSystemService(WindowManager::class.java)
                    val density = wc.resources.displayMetrics.density
                    fun dp(value: Int) = (value * density).toInt()
                    val safe = manager.currentWindowMetrics.windowInsets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                    val view = Button(wc).apply {
                        text = "▧"; textSize = 22f
                        contentDescription = "Đổi chế độ điện thoại / Bàn di chuột"
                        setOnClickListener { PhoneModeActivity.open(ctx) }
                    }
                    val params = WindowManager.LayoutParams(dp(56), dp(48),
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT).apply {
                        gravity = Gravity.TOP or Gravity.END
                        x = safe.right + dp(16); y = safe.top + dp(12)
                        title = "OpenCover Phone Mode"
                    }
                    if (runCatching { manager.addView(view, params) }.isSuccess) {
                        button = view; wm = manager; displayId = id
                    }
                }
            }
            main.postDelayed(this, 500)
        }
    }
}
