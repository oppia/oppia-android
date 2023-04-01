package org.oppia.android.util.parser.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import androidx.core.view.ViewCompat
import org.oppia.android.util.R
import org.oppia.android.util.locale.OppiaLocale
import kotlin.math.max

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
    private val displayLocale: OppiaLocale.DisplayLocale,
  ) : ListItemLeadingMarginSpan() {
    private val resources = context.resources
    private val bulletRadius = resources.getDimensionPixelSize(R.dimen.bullet_radius)
    private val spacingBeforeText = resources.getDimensionPixelSize(R.dimen.spacing_before_text)
    private val spacingBeforeBullet = resources.getDimensionPixelSize(R.dimen.spacing_before_bullet)

    private val bulletDiameter by lazy { bulletRadius * 2 }
    private val isRtl by lazy {
      displayLocale.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
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
        val previousStyle = paint.style
        val bulletDrawRadius = bulletRadius.toFloat()

        val indentedX = parentAbsoluteLeadingMargin + spacingBeforeBullet
        val bulletStartX = (if (isRtl) canvas.width - indentedX - 1 else indentedX).toFloat()
        val bulletCenterX = bulletStartX + bulletDrawRadius
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
                left = bulletStartX
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
      bulletDiameter + spacingBeforeBullet + spacingBeforeText
  }

  /** A subclass of [LeadingMarginSpan] that shows nested list span for <ol> tags. */
  class OlSpan(
    override val parent: ListItemLeadingMarginSpan?,
    context: Context,
    private val numberedItemPrefix: String,
    private val longestNumberedItemPrefix: String,
    private val displayLocale: OppiaLocale.DisplayLocale
  ) : ListItemLeadingMarginSpan() {
    private val resources = context.resources
    private val spacingBeforeText = resources.getDimensionPixelSize(R.dimen.spacing_before_text)
    private val spacingBeforeNumberPrefix =
      resources.getDimensionPixelSize(R.dimen.spacing_before_number_prefix)

    // Try to use a computed margin, but otherwise guess if there's no guaranteed spacing.
    private var computedLeadingMargin =
      2 * longestNumberedItemPrefix.length + spacingBeforeText

    private val isRtl by lazy {
      displayLocale.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
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
        val textWidth = Rect().also {
          paint.getTextBounds(
            numberedItemPrefix, /* start= */ 0, /* end= */ numberedItemPrefix.length, it
          )
        }.width()
        val longestTextWidth = Rect().also {
          paint.getTextBounds(
            longestNumberedItemPrefix,
            /* start= */ 0,
            /* end= */ longestNumberedItemPrefix.length,
            it
          )
        }.width()
        computedLeadingMargin = longestTextWidth + spacingBeforeNumberPrefix + spacingBeforeText

        // Compute the prefix's start x value such that it is right-aligned with other numbers in
        // the list.
        val indentedX = parentAbsoluteLeadingMargin + spacingBeforeNumberPrefix
        val endAlignedX = (max(textWidth, longestTextWidth) - textWidth) + indentedX
        val prefixStartX = if (isRtl) canvas.width - endAlignedX - 1 else endAlignedX
        canvas.drawText(numberedItemPrefix, prefixStartX.toFloat(), baseline.toFloat(), paint)
      }
    }

    override fun getLeadingMargin(first: Boolean) = computedLeadingMargin
  }
}
