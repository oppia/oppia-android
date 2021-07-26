package org.oppia.android.util.parser.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import org.oppia.android.util.R

// TODO(#562): Add screenshot tests to check whether the drawing logic works correctly on all devices.

/**
 * Copy of [android.text.style.BulletSpan] from android SDK 28 with removed internal code.
 * This class helps us to customise bullet radius, gap width and offset present in rich-text.
 * Reference: https://github.com/davidbilik/bullet-span-sample
 */
class CustomBulletSpan  {
  /**
   * Update and return the [SpannableStringBuilder] by replacing all the [BulletSpan]s in the
   * [SpannableStringBuilder] with [CustomBulletSpan].
   */
  companion object {
    fun replaceBulletSpan(
      spannableStringBuilder: SpannableStringBuilder
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
          BulletSpan(
            40, Color.BLACK, 20),
          start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
      return spannableStringBuilder
    }
  }
}
