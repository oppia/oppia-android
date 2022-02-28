package org.oppia.android.util.parser.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import org.oppia.android.util.R

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.

/**
 * A version of [LeadingMarginSpan] that shows text inside the margin.
 *
 * @param context the activity context.
 * @param indentation The zero-based indentation level of this item.
 * @param string String to show inside the margin.
 * @param tag shows the custom tag.
 * Reference: https://medium.com/swlh/making-nested-lists-with-android-spannables-in-kotlin-4ad00052912c
 */
class CustomBulletSpan(
  private val context: Context,
  private val indentation: Int,
  private val string: String,
  private val tag: String,
) : LeadingMarginSpan {
  private val resources = context.resources
  private val bulletRadius = resources.getDimensionPixelSize(R.dimen.bullet_radius)
  private val gapWidth = resources.getDimensionPixelSize(R.dimen.bullet_gap_width)

  /** The space between the start of the line and the bullet. */
  private val spacingBeforeBullet = resources.getDimensionPixelSize(R.dimen.spacing_before_bullet)

  /** The space between the bullet and the text. */
  private val spacingBeforeText = resources.getDimensionPixelSize(R.dimen.spacing_before_text)

  override fun drawLeadingMargin(
    c: Canvas,
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
    l: Layout
  ) {
    val startCharOfSpan = (text as Spanned).getSpanStart(this)
    val isFirstCharacter = startCharOfSpan == start

    if (isFirstCharacter) {
      val trueX = gapWidth * indentation + spacingBeforeBullet

      val yPosition = (top + bottom) / 2f
      when (tag) {
        CUSTOM_BULLET_UL_LIST_TAG -> {
          val style = paint.style
          if (trueX == spacingBeforeBullet) {
            paint.style = Paint.Style.FILL
          } else {
            paint.style = Paint.Style.STROKE
          }
          c.drawCircle(trueX.toFloat(), yPosition, bulletRadius.toFloat(), paint)
          paint.style = style
        }
        CUSTOM_BULLET_OL_LIST_TAG -> {
          c.drawText(string, trueX.toFloat(), baseline.toFloat(), paint)
        }
      }
    }

  }

  override fun getLeadingMargin(first: Boolean): Int = spacingBeforeText
}
