package org.oppia.util.parser

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

/**
 * A version of [LeadingMarginSpan] that shows text inside the margin.
 *
 * @param marginWidth Size of the margin.
 * @param indentation The zero-based indentation level of this item.
 * @param string String to show inside the margin.
 */
class TextLeadingMarginSpan(
  private val marginWidth: Int,
  private val indentation: Int,
  private val string: String
) : LeadingMarginSpan {

  override fun drawLeadingMargin(
    c: Canvas,
    p: Paint,
    x: Int,
    dir: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    text: CharSequence,
    start: Int,
    end: Int,
    first: Boolean,
    l: Layout
  ) {
    val startCharOfSpan = (text as Spanned).getSpanStart(this)
    val isFirstCharacter = startCharOfSpan == start

    if (isFirstCharacter) {
      // Depending on the phone, x might always be 0. We need to re-calculate it here.
      val trueX = 28 + marginWidth * indentation
      c.drawText(string, trueX.toFloat(), baseline.toFloat(), p)
    }
  }

  override fun getLeadingMargin(first: Boolean): Int = marginWidth
}
