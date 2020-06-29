package org.oppia.util.parser

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

/**
 * Custom Bullet Span implementation (based on [BulletSpan])
 * Default implementation doesn't allow for radius modification
 */
class BulletSpanWithRadius(bulletRadius: Int, gapWidth: Int) : LeadingMarginSpan {
  private val gapWidth: Int = gapWidth
  private val bulletRadius: Int = bulletRadius

  override fun getLeadingMargin(first: Boolean): Int {
    return 2 * bulletRadius + gapWidth
  }

  override fun drawLeadingMargin(
    c: Canvas, p: Paint, x: Int, dir: Int,
    top: Int, baseline: Int, bottom: Int,
    text: CharSequence, start: Int, end: Int,
    first: Boolean, l: Layout?
  ) {
    if ((text as Spanned).getSpanStart(this) == start) {
      val style = p.style

      p.style = Paint.Style.FILL
      // a circle with the correct size is drawn at the correct location
      val xCoordinate = (x + dir * bulletRadius).toFloat()
      val yCoordinate = (top + bottom) / 2f
      c.drawCircle(
        xCoordinate, yCoordinate,
        bulletRadius.toFloat(), p
      )
      p.style = style
    }
  }
}
