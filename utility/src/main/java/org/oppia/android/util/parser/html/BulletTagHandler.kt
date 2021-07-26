package org.oppia.android.util.parser.html

import android.graphics.Color
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
  private class Bullet

  override fun handleOpeningTag(output: Editable) {
    output.setSpan(Bullet(), output.length, output.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
  }

  override fun handleClosingTag(output: Editable) {
    output.append("\n")
    output.getSpans(0, output.length, Bullet::class.java).lastOrNull()?.let {
      val spanEnd = output.getSpanEnd(it)
      val start = output.getSpanStart(it)
      // no need for newline if empty span. This fix 'PARAGRAPH span must start at paragraph boundary'.
      if (start == spanEnd) {
        output.removeSpan(it)
      }
      if (start != output.length) {
        output.setSpan(BulletSpan(),
        start, output.length-1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
      }
    }
  }
}
