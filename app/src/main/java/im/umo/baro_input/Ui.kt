package im.umo.baro_input

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.doOnNextLayout

internal object Ui {
    val teal = Color.rgb(8, 127, 114)
    val ink = Color.rgb(22, 31, 30)
    val muted = Color.rgb(82, 95, 91)
    val line = Color.rgb(220, 226, 223)
    val canvas = Color.rgb(250, 251, 250)
    val softTeal = Color.rgb(232, 245, 241)
    fun Context.dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    fun column(c: Context) = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL }
    fun row(c: Context) = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    fun text(c: Context, value: CharSequence, size: Float = 16f, bold: Boolean = false) = TextView(c).apply {
        text = value; textSize = size; setTextColor(ink)
        includeFontPadding = false
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setLineSpacing(c.dp(3).toFloat(), 1f)
    }
    fun button(c: Context, value: CharSequence, primary: Boolean = false, action: () -> Unit) = Button(c).apply {
        text = value; textSize = 16f; isAllCaps = false
        minHeight = c.dp(if (primary) 56 else 52); minimumHeight = minHeight
        minWidth = 0; minimumWidth = 0; includeFontPadding = false
        setPadding(c.dp(16), c.dp(12), c.dp(16), c.dp(12))
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
            intArrayOf(muted, if (primary) Color.WHITE else teal)
        ))
        backgroundTintList = null
        val states = StateListDrawable().apply {
            addState(intArrayOf(-android.R.attr.state_enabled), shape(c, line))
            addState(intArrayOf(android.R.attr.state_focused), shape(c, if (primary) teal else softTeal, teal))
            addState(intArrayOf(), shape(c, if (primary) teal else Color.WHITE, if (primary) teal else line))
        }
        background = RippleDrawable(ColorStateList.valueOf(Color.argb(28, 8, 127, 114)), states, null)
        stateListAnimator = null; elevation = 0f
        setOnClickListener { action() }
    }
    fun quietButton(c: Context, value: CharSequence, action: () -> Unit) = button(c, value, action = action).apply {
        textSize = 14f; minHeight = c.dp(48); minimumHeight = minHeight
        setPadding(c.dp(12), c.dp(8), c.dp(12), c.dp(8))
        background = RippleDrawable(ColorStateList.valueOf(softTeal), shape(c, Color.TRANSPARENT, Color.TRANSPARENT), null)
    }
    fun modeButton(c: Context, value: CharSequence, selected: Boolean, action: () -> Unit) = button(c, value, action = action).apply {
        minHeight = c.dp(48); minimumHeight = minHeight
        setPadding(c.dp(12), c.dp(8), c.dp(12), c.dp(8))
        isSelected = selected
        setTextColor(if (selected) teal else muted)
        background = RippleDrawable(ColorStateList.valueOf(softTeal), shape(c, if (selected) softTeal else Color.WHITE, if (selected) teal else line), null)
    }
    fun back(c: Context, action: () -> Unit) = row(c).apply {
        addView(quietButton(c, c.getString(R.string.back), action))
    }
    fun note(c: Context, value: CharSequence) = text(c, value, 14f).apply { setTextColor(muted) }
    fun card(c: Context) = column(c).apply {
        background = shape(c)
        setPadding(c.dp(16), c.dp(16), c.dp(16), c.dp(16))
    }
    fun shape(c: Context, fill: Int = Color.WHITE, stroke: Int = line) = GradientDrawable().apply {
        setColor(fill); cornerRadius = c.dp(12).toFloat(); setStroke(c.dp(1), stroke)
    }
    fun LinearLayout.block(view: View, top: Int = 12, height: Int = LinearLayout.LayoutParams.WRAP_CONTENT) {
        addView(view, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height).apply { topMargin = context.dp(top) })
    }
    fun LinearLayout.cell(view: View) {
        addView(view, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            if (childCount > 0) marginStart = context.dp(8)
        })
    }
    fun Activity.insets(root: View, horizontal: Int = 20, vertical: Int = 16) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, root).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        // Apply the design gutter immediately, even before the first insets dispatch.
        root.setPadding(dp(horizontal), dp(vertical), dp(horizontal), dp(vertical))
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            // Resize the scroll viewport above the IME. Padding alone clips the focused
            // field without changing ScrollView's visible-height calculations.
            val layout = v.layoutParams as? ViewGroup.MarginLayoutParams
            if (layout != null && layout.bottomMargin != keyboard.bottom) {
                v.doOnNextLayout {
                    currentFocus?.takeIf { it is android.widget.EditText }?.let { focused ->
                        if (v is android.widget.ScrollView && keyboard.bottom > 0) {
                            v.post {
                                val rect = Rect(0, 0, focused.width, focused.height)
                                v.offsetDescendantRectToMyCoords(focused, rect)
                                v.scrollTo(0, maxOf(0, rect.bottom - v.height + v.paddingBottom))
                            }
                        } else focused.requestRectangleOnScreen(Rect(0, 0, focused.width, focused.height), true)
                    }
                }
                layout.bottomMargin = keyboard.bottom
                v.layoutParams = layout
            }
            val bottom = (if (keyboard.bottom > 0) 0 else bars.bottom) + dp(vertical)
            v.setPadding(dp(horizontal) + bars.left, bars.top + dp(vertical), dp(horizontal) + bars.right, bottom)
            insets
        }
        root.post { ViewCompat.requestApplyInsets(root) }
    }
}
