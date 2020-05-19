package org.oppia.util.parser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import org.oppia.util.R

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.
/**
 * A version of [LeadingMarginSpan] that shows text inside the margin.
 *
 * @param marginWidth Size of the margin.
 * @param indentation The zero-based indentation level of this item.
 * @param string String to show inside the margin.
 */
class TextLeadingMarginSpan(
  context: Context,
  private val indentation: Int,
  private val string: String
) : LeadingMarginSpan {

  var gapWidth: Int = 0
  private var bulletLeadingMargin: Int = 0

  init {
    gapWidth = context.resources.getDimensionPixelSize(R.dimen.bullet_gap_width)
    bulletLeadingMargin = context.resources.getDimensionPixelSize(R.dimen.bullet_leading_margin)
  }

  override fun drawLeadingMargin(
    canvas: Canvas,
    paint: Paint,
    x: Int,
    dir: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    text: CharSequence,
    start: Int,
    end: Int,
    first: Boolean,
    layout: Layout
  ) {
    val startCharOfSpan = (text as Spanned).getSpanStart(this)
    val isFirstCharacter = startCharOfSpan == start
    if (isFirstCharacter) {
      // Depending on the phone, x might always be 0. We need to re-calculate it here.
      val trueX = gapWidth * indentation
      canvas.drawText(string, trueX.toFloat(), baseline.toFloat(), paint)
    }
  }

  override fun getLeadingMargin(first: Boolean): Int = bulletLeadingMargin
}
