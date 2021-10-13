package org.oppia.android.app.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat

/** The custom Textview class for toolbar with Marquee effect. */
class MarqueeToolbarTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyle), View.OnClickListener {

  private var textView = this

  init {
    textView.setOnClickListener(this)
  }

  override fun onClick(v: View?) {
    textView.isSelected = true
    setTextDirection(textView)
  }

  private fun setTextDirection(view: TextView) {
    if (ViewCompat.isLayoutDirectionResolved(view)) {
      when (getLayoutDirection(textView)) {
        ViewCompat.LAYOUT_DIRECTION_RTL -> {
          textView.textDirection = View.TEXT_DIRECTION_RTL
        }
        ViewCompat.LAYOUT_DIRECTION_LTR -> {
          textView.textDirection = View.TEXT_DIRECTION_LTR
        }
      }
    } else {
      textView.textDirection = View.TEXT_DIRECTION_LOCALE
    }
  }

  private fun getLayoutDirection(view: View): Int {
    return ViewCompat.getLayoutDirection(view)
  }
}
