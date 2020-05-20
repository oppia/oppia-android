package org.oppia.util.parser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import org.oppia.util.R

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.

/**
 * Copy of [android.text.style.BulletSpan] from android SDK 28 with removed internal code.
 * This class helps us to customise bullet radius, gap width and offset present in rich-text.
 * Reference: https://github.com/davidbilik/bullet-span-sample
 */
class CustomBulletSpan(context: Context) : LeadingMarginSpan {
  private var bulletRadius: Int = 0
  private var gapWidth: Int = 0
  private var yOffset: Int = 0
  private var bulletLeadingMargin: Int = 0

  init {
    bulletRadius = context.resources.getDimensionPixelSize(R.dimen.bullet_radius)
    gapWidth = context.resources.getDimensionPixelSize(R.dimen.bullet_gap_width)
    yOffset = context.resources.getDimensionPixelSize(R.dimen.bullet_y_offset)
    bulletLeadingMargin = context.resources.getDimensionPixelSize(R.dimen.bullet_leading_margin)
  }

  private var bulletPath: Path? = null

  override fun getLeadingMargin(first: Boolean): Int {
    return bulletLeadingMargin
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
    layout: Layout?
  ) {
    if ((text as Spanned).getSpanStart(this) == start) {
      val style = paint.style
      paint.style = Paint.Style.FILL

      var yPosition = if (layout != null) {
        val line = layout.getLineForOffset(start)
        layout.getLineBaseline(line).toFloat() - bulletRadius * 2f
      } else {
        (top + bottom) / 2f
      }
      yPosition += yOffset

      val xPosition = (x + dir * bulletRadius).toFloat()

      if (canvas.isHardwareAccelerated) {
        if (bulletPath == null) {
          bulletPath = Path()
          bulletPath!!.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Direction.CW)
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
