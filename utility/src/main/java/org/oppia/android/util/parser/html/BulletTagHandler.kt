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
  private class Bullet

  override fun handleOpeningTag(output: Editable) {
    appendNewLine(output)
    output.setSpan(Bullet(), output.length, output.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
  }

  override fun handleClosingTag(output: Editable) {
    appendNewLine(output)
    output.getSpans(0, output.length, Bullet::class.java).lastOrNull()?.let {
      val start = output.getSpanStart(it)
      output.removeSpan(it)
      if (start != output.length) {
        output.setSpan(BulletSpan(), start, output.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
      }
    }
  }

  /**
   * Appends a new line to [output] if it doesn't already end in a new line.
   * We want the first line of list item to start on a separate line, and other content that comes
   * after the list to also be on a separate line. We avoid appending multiple newline characters
   * in a row by first checking if the last character in output is \n.
   * https://medium.com/swlh/making-nested-lists-with-android-spannables-in-kotlin-4ad00052912c
   */
  private fun appendNewLine(output: Editable) {
    if (output.isNotEmpty() && output.last() != '\n') {
      output.append("\n")
    }
  }
}
