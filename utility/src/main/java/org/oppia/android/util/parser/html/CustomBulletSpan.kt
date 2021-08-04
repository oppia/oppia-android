package org.oppia.android.util.parser.html

import android.content.Context
import android.graphics.Color
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
class CustomBulletSpan {
  /**
   * Update and return the [SpannableStringBuilder] by replacing all the [BulletSpan]s in the
   * [SpannableStringBuilder] with [CustomBulletSpan].
   */
  companion object {
    fun replaceBulletSpan(
      spannableStringBuilder: SpannableStringBuilder,
      context: Context
    ): SpannableStringBuilder {
      val resources = context.resources
      val bulletRadius = resources.getDimensionPixelSize(R.dimen.bullet_radius)
      val gapWidth = resources.getDimensionPixelSize(R.dimen.bullet_gap_width)
      // The space between the start of the line and the bullet.
      val spacingBeforeBullet = resources.getDimensionPixelSize(R.dimen.spacing_before_bullet)

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
          LeadingMarginSpan.Standard(spacingBeforeBullet), start, end,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.setSpan(
          BulletSpan(
            gapWidth, Color.BLACK, bulletRadius
          ),
          start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
      }
      return spannableStringBuilder
    }
  }
}
