package org.oppia.android.util.parser.html

import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.style.BulletSpan

/** The custom tag corresponding to [BulletTagHandler]. */
const val CUSTOM_BULLET_LIST_TAG = "oppia-li"

/**
 * A custom tag handler for properly formatting bullet items in HTML parsed with
 * [CustomHtmlContentHandler].
 */
class BulletTagHandler : CustomHtmlContentHandler.CustomTagHandler {
  /** Helper marker class. */
  private class BulletMark

  override fun handleOpeningTag(output: Editable) {
    output.ensureEndingNewlines(count = 2)
    output.setSpan(BulletMark(), output.length, output.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
  }

  override fun handleClosingTag(output: Editable) {
    output.ensureEndingNewlines(count = 2)
    output.getSpans(0, output.length, BulletMark::class.java).lastOrNull()?.let {
      val start = output.getSpanStart(it)
      output.removeSpan(it)
      if (start != output.length) {
        output.setSpan(BulletSpan(), start, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
  }

  private fun Editable.ensureEndingNewlines(count: Int) {
    val newlinesToAdd = (count - takeLastWhile { it == '\n' }.length).coerceAtLeast(0)
    append("\n".repeat(newlinesToAdd))
  }
}
