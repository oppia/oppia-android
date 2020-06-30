package org.oppia.util.parser

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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

  private var bulletPath: Path? = null
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
      val xPosition = x + dir * bulletRadius.toFloat()
      val yPosition = (top + bottom) / 2f
      
      if (c.isHardwareAccelerated) {
        if (bulletPath == null) {
          bulletPath = Path()
          bulletPath!!.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
        }
        c.save()
        c.translate(xPosition, yPosition)
        c.drawPath(bulletPath, p)
        c.restore()
      } else {
        c.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), p)
      }
      p.style = style
    }
  }
}
