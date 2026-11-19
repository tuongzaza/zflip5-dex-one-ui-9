package dev.opencover.desk

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow

/** Visual snap targets; hovering the button or panel keeps it open without taking keyboard focus. */
internal class DeskSnapPopup(
    context: Context,
    window: Desk.Window,
    action: (() -> String?) -> Unit,
    dismissed: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val close = Runnable { dismiss() }
    private val dark = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    private val iconColor = if (dark) Color.WHITE else 0xff17171a.toInt()
    private val content = object : LinearLayout(context) {
        override fun dispatchHoverEvent(event: MotionEvent): Boolean {
            if (event.actionMasked == MotionEvent.ACTION_HOVER_EXIT) leave() else keepOpen()
            return super.dispatchHoverEvent(event)
        }
    }.apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(12, 12, 12, 12)
        background = shape(if (dark) 0xff252528.toInt() else 0xfff3f3f3.toInt(), 12f)
        contentDescription = "Bố cục cửa sổ"
    }
    private val popup = PopupWindow(content, 308, ViewGroup.LayoutParams.WRAP_CONTENT, false).apply {
        isOutsideTouchable = true
        setTouchModal(false)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        elevation = 12f
        setOnDismissListener { handler.removeCallbacks(close); dismissed() }
    }
    val isShowing get() = popup.isShowing

    init {
        listOf(2, 3).forEach { count ->
            val group = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            val diagram = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(6, 6, 6, 6)
                background = shape(if (dark) 0xff363639.toInt() else 0xffdddddf.toInt(), 7f)
            }
            repeat(count) { index ->
                val label = if (count == 2) listOf("Nửa trái", "Nửa phải")[index]
                    else listOf("Một phần ba bên trái", "Một phần ba ở giữa", "Một phần ba bên phải")[index]
                diagram.addView(View(context).apply {
                    contentDescription = label; tooltipText = label
                    isFocusable = true
                    background = StateListDrawable().apply {
                        addState(intArrayOf(android.R.attr.state_pressed), shape(0xff2693ed.toInt(), 3f))
                        addState(intArrayOf(android.R.attr.state_hovered), shape(0xff54adfa.toInt(), 3f))
                        addState(intArrayOf(android.R.attr.state_focused), shape(0xff54adfa.toInt(), 3f))
                        addState(intArrayOf(), shape(if (dark) 0xff79797d.toInt() else 0xffb2b2b7.toInt(), 3f))
                    }
                    setOnClickListener { dismiss(); action { Desk.snap(window.task, count, index) } }
                }, LinearLayout.LayoutParams(0, 66, 1f).apply { if (index < count - 1) rightMargin = 4 })
            }
            group.addView(diagram, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 78))
            group.addView(Button(context).apply {
                text = "↻"; textSize = 23f; setTextColor(iconColor)
                minWidth = 0; minimumWidth = 0; setPadding(0, 0, 0, 0)
                background = ColorDrawable(Color.TRANSPARENT)
                contentDescription = "Tự sắp xếp $count ứng dụng"
                tooltipText = contentDescription
                setOnClickListener { dismiss(); action { Desk.arrange(count) } }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 36))
            content.addView(group, LinearLayout.LayoutParams(136, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                if (count == 2) rightMargin = 12
            })
        }
    }

    fun show(anchor: View) { popup.showAsDropDown(anchor, anchor.width - 308, 4) }
    fun keepOpen() { handler.removeCallbacks(close) }
    fun leave() { handler.removeCallbacks(close); handler.postDelayed(close, 250) }
    fun dismiss() { handler.removeCallbacks(close); popup.dismiss() }

    private fun shape(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color); cornerRadius = radius
    }
}
