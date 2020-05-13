package org.oppia.util.parser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import org.oppia.util.R

/**
 * A version of [LeadingMarginSpan] that shows text inside the margin.
 *
 * @param marginWidth Size of the margin.
 * @param indentation The zero-based indentation level of this item.
 * @param string String to show inside the margin.
 */
class TextLeadingMarginSpan(
  context: Context,
  private val marginWidth: Int,
  private val indentation: Int,
  private val string: String
) : LeadingMarginSpan {

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

  private var mBulletPath: Path? = null

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
      val trueX = 28 + marginWidth * indentation
      if (string != "•") {
        canvas.drawText(string, trueX.toFloat(), baseline.toFloat(), paint)
      }else {
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
          if (mBulletPath == null) {
            mBulletPath = Path()
            mBulletPath!!.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
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

  override fun getLeadingMargin(first: Boolean): Int = marginWidth
}
