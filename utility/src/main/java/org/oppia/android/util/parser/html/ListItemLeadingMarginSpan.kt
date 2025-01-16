package org.oppia.android.util.parser.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import org.oppia.android.util.R
import org.oppia.android.util.R.dimen.spacing_before_bullet

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.

/**
 * A version of [LeadingMarginSpan] that shows text inside the margin.
 * Reference: https://medium.com/swlh/making-nested-lists-with-android-spannables-in-kotlin-4ad00052912c
 */
sealed class ListItemLeadingMarginSpan : LeadingMarginSpan {
  /** The parent list of this span, or null if it doesn't have one (that is, it's a root list). */
  protected abstract val parent: ListItemLeadingMarginSpan?

  private val absoluteLeadingMargin: Int
    get() = parentAbsoluteLeadingMargin + getLeadingMargin(/* first= */ true)

  /** The leading margin of all parents of this span, up to the start of the text area. */
  protected val parentAbsoluteLeadingMargin: Int
    get() = parent?.absoluteLeadingMargin ?: 0

  /** A subclass of [LeadingMarginSpan] that shows nested list span for <ul> tags. */
  class UlSpan(
    override val parent: ListItemLeadingMarginSpan?,
    context: Context,
    private val indentationLevel: Int,
  ) : ListItemLeadingMarginSpan() {
    private val resources = context.resources
    private val bulletRadius = resources.getDimensionPixelSize(R.dimen.bullet_radius)

    private val bulletDiameter by lazy { bulletRadius * 2 }
    private val baseMargin = context.resources.getDimensionPixelSize((spacing_before_bullet))

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
        val previousStyle = paint.style
        val bulletDrawRadius = bulletRadius.toFloat()

        // Force left alignment
        paint.textAlign = Paint.Align.LEFT

        // Positioning calculation

        val bulletCenterLtrX = x.toFloat() + baseMargin * (indentationLevel + 1)
        val bulletCenterX = bulletCenterLtrX
        val bulletCenterY = (top + bottom) / 2f

        when (indentationLevel) {
          0 -> {
            // A solid circle is used for the outermost bullet.
            paint.style = Paint.Style.FILL
            canvas.drawCircle(bulletCenterX, bulletCenterY, bulletDrawRadius, paint)
          }
          1 -> {
            // An inner open circle is used for second-level bullets.
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawCircle(bulletCenterX, bulletCenterY, bulletDrawRadius, paint)
          }
          else -> {
            // A filled square is used for all subsequent bullets.
            paint.style = Paint.Style.FILL
            val rectSize = bulletDiameter.toFloat()
            canvas.drawRect(
              RectF().apply {
                left = bulletCenterX
                right = left + rectSize
                this.top = bulletCenterY - bulletDrawRadius
                this.bottom = this.top + rectSize
              },
              paint
            )
          }
        }
        paint.style = previousStyle // Restore the previously used paint style.
      }
    }

    override fun getLeadingMargin(first: Boolean) =
      baseMargin * (indentationLevel + 2)
  }

  /** A subclass of [LeadingMarginSpan] that shows nested list span for <ol> tags. */
  class OlSpan(
    override val parent: ListItemLeadingMarginSpan?,
    context: Context,
    private val numberedItemPrefix: String
  ) : ListItemLeadingMarginSpan() {
    private val baseMargin =
      context.resources.getDimensionPixelSize((R.dimen.spacing_before_number_prefix))

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
        // Positioning calculation
        val prefixStartX = x.toFloat() + baseMargin

        // Draw the numbered prefix
        canvas.drawText(numberedItemPrefix, prefixStartX, baseline.toFloat(), paint)
      }
    }

    override fun getLeadingMargin(first: Boolean) =
      baseMargin * 2
  }
}
