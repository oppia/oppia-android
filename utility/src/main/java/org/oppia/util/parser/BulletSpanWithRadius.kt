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

  override fun getLeadingMargin(first: Boolean): Int {
    return 2 * bulletRadius + gapWidth
  }

  companion object {
    private var bulletPath: Path? = null
  }

  override fun drawLeadingMargin(
    canvas: Canvas, paint: Paint, x: Int, dir: Int,
    top: Int, baseline: Int, bottom: Int,
    text: CharSequence, start: Int, end: Int,
    first: Boolean,
    layout: Layout?
  ) {
    val bottom = bottom
    if ((text as Spanned).getSpanStart(this) == start) {
      val style = paint.style
      paint.style = Paint.Style.FILL

      val yPosition = if (layout != null) {
        val line = layout.getLineForOffset(start)
        layout.getLineBaseline(line).toFloat() - bulletRadius * 2f
      } else {
        (top + bottom) / 2f
      }

      val xPosition = (x + dir * bulletRadius).toFloat()

      if (canvas.isHardwareAccelerated) {
        if (bulletPath == null) {
          bulletPath = Path()
          bulletPath!!.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
        }
        canvas.save()
        canvas.translate(xPosition, yPosition)
        canvas.drawPath(bulletPath!!, paint)
        canvas.restore()
      } else {
        canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)
      }

      paint.style = style
    }
  }
}
