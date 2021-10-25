package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import org.oppia.android.util.R

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.

/**
 * Copy of [android.text.style.BulletSpan] from android SDK 28 with removed internal code.
 * This class helps us to customise bullet radius, gap width and offset present in rich-text.
 * Reference: https://github.com/davidbilik/bullet-span-sample
 */
class CustomBulletSpan(context: Context) : LeadingMarginSpan {
  private val resources = context.resources
  private val bulletRadius = resources.getDimensionPixelSize(R.dimen.bullet_radius)
  private val gapWidth = resources.getDimensionPixelSize(R.dimen.bullet_gap_width)
  private val yOffset = resources.getDimensionPixelSize(R.dimen.bullet_y_offset)
  /** The space between the start of the line and the bullet. */
  private val spacingBeforeBullet = resources.getDimensionPixelSize(R.dimen.spacing_before_bullet)
  /** The space between the bullet and the text. */
  private val spacingBeforeText = resources.getDimensionPixelSize(R.dimen.spacing_before_text)
  /** The total spacing between the start of the line and the text. */
  private val totalSpacingToTextStart = spacingBeforeBullet + spacingBeforeText + bulletRadius * 2
  private var bulletPath: Path? = null

  override fun getLeadingMargin(first: Boolean): Int {
    return totalSpacingToTextStart
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

      val xPosition = (x + dir * bulletRadius).toFloat() + spacingBeforeBullet

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

  /**
   * Update and return the [SpannableStringBuilder] by replacing all the [BulletSpan]s in the
   * [SpannableStringBuilder] with [CustomBulletSpan].
   */
  companion object {
    fun replaceBulletSpan(
      spannableStringBuilder: SpannableStringBuilder,
      context: Context
    ): SpannableStringBuilder {
      val bulletSpans = spannableStringBuilder.getSpans(
        /* queryStart= */ 0,
        spannableStringBuilder.length,
        BulletSpan::class.java
      )

      bulletSpans.forEach {
        val start = spannableStringBuilder.getSpanStart(it)
        val end = spannableStringBuilder.getSpanEnd(it)
        spannableStringBuilder.removeSpan(it)
        spannableStringBuilder.setSpan(
          CustomBulletSpan(context),
          start,
          end,
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
      }
      return spannableStringBuilder
    }
  }
}
