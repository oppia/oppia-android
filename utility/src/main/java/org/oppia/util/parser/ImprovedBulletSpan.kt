package org.oppia.util.parser

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

/**
 * Copy of [android.text.style.BulletSpan] from android SDK 28 with removed internal code.
 *
 * Reference: https://github.com/davidbilik/bullet-span-sample
 */
class ImprovedBulletSpan(
  private val bulletRadius: Int = STANDARD_BULLET_RADIUS,
  private val gapWidth: Int = STANDARD_GAP_WIDTH,
  val color: Int = STANDARD_COLOR
) : LeadingMarginSpan {

  companion object {
    private const val STANDARD_BULLET_RADIUS = 8
    private const val STANDARD_GAP_WIDTH = 32
    private const val STANDARD_COLOR = 0
  }

  private var mBulletPath: Path? = null

  override fun getLeadingMargin(first: Boolean): Int {
    return 2 * bulletRadius + gapWidth
  }

  override fun drawLeadingMargin(
    canvas: Canvas, paint: Paint, x: Int, dir: Int,
    top: Int, baseline: Int, bottom: Int,
    text: CharSequence, start: Int, end: Int,
    first: Boolean,
    layout: Layout?
  ) {
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
        if (mBulletPath == null) {
          mBulletPath = Path()
          mBulletPath!!.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Direction.CW)
        }

        canvas.save()
        canvas.translate(xPosition, yPosition)
        canvas.drawPath(mBulletPath!!, paint)
        canvas.restore()
      } else {
        canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)
      }

      paint.style = style
    }
  }
}
