package org.oppia.android.util.parser.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import androidx.core.view.ViewCompat
import org.oppia.android.util.R
import org.oppia.android.util.locale.OppiaLocale

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.

/**
 * A version of [LeadingMarginSpan] that shows text inside the margin.
 * Reference: https://medium.com/swlh/making-nested-lists-with-android-spannables-in-kotlin-4ad00052912c
 */
sealed class ListItemLeadingMarginSpan : LeadingMarginSpan {

  /**
   * A subclass of [LeadingMarginSpan] that shows nested list span for <ul> tags.
   *
   * @param context the activity context.
   * @param indentation The zero-based indentation level of this item
   */
  class UlSpan(
    context: Context,
    private val indentation: Int,
    private val displayLocale: OppiaLocale.DisplayLocale,
  ) : LeadingMarginSpan {
    private val resources = context.resources
    private val bulletRadius = resources.getDimensionPixelSize(R.dimen.bullet_radius)
    private val gapWidth = resources.getDimensionPixelSize(R.dimen.bullet_gap_width)

    /** The space between the start of the line and the bullet. */
    private val spacingBeforeBullet =
      resources.getDimensionPixelSize(R.dimen.spacing_before_bullet)

    /** The space between the bullet and the text. */
    private val spacingBeforeText =
      resources.getDimensionPixelSize(R.dimen.spacing_before_text)

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
      val startCharOfSpan = (text as Spanned).getSpanStart(this)
      val isFirstCharacter = startCharOfSpan == start

      if (isFirstCharacter) {
        val trueX = gapWidth * indentation + spacingBeforeBullet

        val yPosition = (top + bottom) / 2f
        val style = paint.style
        if (indentation == 0) {
          paint.style = Paint.Style.FILL
        } else {
          paint.style = Paint.Style.STROKE
          paint.strokeWidth = 2f
        }
        val correctX = if (isRtl) canvas.width - trueX - 1 else trueX
        canvas.drawCircle(correctX.toFloat(), yPosition, bulletRadius.toFloat(), paint)
        paint.style = style
      }
    }

    override fun getLeadingMargin(first: Boolean): Int {
      return 2 * bulletRadius + spacingBeforeText
    }

    private val isRtl by lazy {
      displayLocale.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
    }
  }

  /**
   * A subclass of [LeadingMarginSpan] that shows nested list span for <ol> tags.
   *
   * @param context the activity context.
   * @param indentation The zero-based indentation level of this item
   */
  class OlSpan(
    context: Context,
    private val indentation: Int,
    private val numberedItemPrefix: String,
    private val displayLocale: OppiaLocale.DisplayLocale
  ) : LeadingMarginSpan {
    private val resources = context.resources
    private val gapWidth = resources.getDimensionPixelSize(R.dimen.bullet_gap_width)

    /** The space between the start of the line and the bullet. */
    private val spacingBeforeBullet =
      resources.getDimensionPixelSize(R.dimen.spacing_before_bullet)

    private val spacingBeforeNumberedText =
      resources.getDimensionPixelSize(R.dimen.spacing_before_numbered_text)

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
      val startCharOfSpan = (text as Spanned).getSpanStart(this)
      val isFirstCharacter = startCharOfSpan == start

      if (isFirstCharacter) {
        val trueX = gapWidth * indentation + spacingBeforeBullet
        val correctX = if (isRtl) canvas.width - trueX - 1 else trueX
        canvas.drawText(numberedItemPrefix, correctX.toFloat(), baseline.toFloat(), paint)
      }
    }

    override fun getLeadingMargin(first: Boolean): Int {
      return 2 * numberedItemPrefix.length + spacingBeforeNumberedText
    }

    private val isRtl by lazy {
      displayLocale.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
    }
  }
}
