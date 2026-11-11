package dev.opencover.desk

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dev.opencover.COVER_DISPLAY

/** A choice on the phone, never on the external monitor. */
class PhoneModeActivity : Activity() {
    private lateinit var root: LinearLayout
    private var pad: PhoneTouchpadView? = null
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        current = this
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        showMode()
    }
    private fun showMode() {
        val density = resources.displayMetrics.density
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL;setPadding((12*density).toInt(), 0, (12*density).toInt(), 0)
            setBackgroundColor(0xff17191e.toInt())
        }
        val buttons = LinearLayout(this)
        fun choice(label: String, action: () -> Unit) = Button(this).apply { text = label;setOnClickListener { action() } }
        buttons.addView(choice("Điện thoại") { Desk.phoneMode(this, false);finish() }, LinearLayout.LayoutParams(0, -2, 1f))
        buttons.addView(choice("Bàn di chuột") { if (Desk.running) { Desk.phoneMode(this, true);showMode() } }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(buttons)
        val enabled = Desk.running && Desk.phoneTouchpad(this)
        root.addView(TextView(this).apply {
            text = when {
                !Desk.running -> "Kết nối màn hình USB-C và bật Desktop trong OpenCover để dùng bàn di chuột."
                enabled -> "Rê một ngón để di chuyển · Chạm để bấm\nHai ngón để cuộn · Giữ để kéo thả"
                else -> "Chọn Điện thoại để dùng bình thường, hoặc Bàn di chuột để điều khiển màn hình ngoài."
            }
            setTextColor(Color.WHITE);textSize = 14f;gravity = Gravity.CENTER;setPadding(0, 8, 0, 8)
        })
        pad?.cancelGestures()
        pad = if (enabled) PhoneTouchpadView(this) else null
        pad?.let { root.addView(it, LinearLayout.LayoutParams(-1, 0, 1f)) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            val safe = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            val side = (12*density).toInt()
            view.setPadding(side+safe.left, safe.top, side+safe.right, safe.bottom)
            insets
        }
        setContentView(root)
        root.requestApplyInsets()
    }
    override fun onPause() { pad?.cancelGestures();super.onPause() }
    override fun onDestroy() { if (current === this) current = null;super.onDestroy() }
    @Deprecated("Phone mode back")
    override fun onBackPressed() { Desk.phoneMode(this, false);super.onBackPressed() }

    companion object {
        var current: PhoneModeActivity? = null;private set
        internal fun phoneDisplay(ctx: Context): Int {
            val dm = ctx.getSystemService(DisplayManager::class.java)
            return if (dm.getDisplay(Display.DEFAULT_DISPLAY)?.state == Display.STATE_ON) Display.DEFAULT_DISPLAY else COVER_DISPLAY
        }
        fun open(ctx: Context) {
            val id = phoneDisplay(ctx)
            val options = ActivityOptions.makeBasic().setLaunchDisplayId(id)
            ctx.startActivity(Intent(ctx, PhoneModeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP), options.toBundle())
        }
        fun moveIfNeeded(ctx: Context) {
            val a = current ?: return
            if (!a.isFinishing && a.display?.displayId != phoneDisplay(ctx)) { a.finish();open(ctx) }
        }
    }
}
