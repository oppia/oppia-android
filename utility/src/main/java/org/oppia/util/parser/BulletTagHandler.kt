package org.oppia.util.parser

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import org.xml.sax.Attributes

/** The custom tag corresponding to [BulletTagHandler]. */
const val CUSTOM_BULLET_LIST_TAG = "oppia-li"

/**
 * A custom tag handler for properly formatting bullet items in HTML parsed with
 * [CustomHtmlContentHandler].
 */
class BulletTagHandler : CustomHtmlContentHandler.CustomTagHandler {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable
  ) {
    val spannableBuilder = SpannableStringBuilder(
      output.subSequence(
        openIndex,
        closeIndex
      )
    )
    spannableBuilder.append("\n")
    if (openIndex != closeIndex) {
      spannableBuilder.setSpan(
        BulletSpan(), 0, spannableBuilder.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
      )
    }
    output.replace(openIndex, closeIndex, spannableBuilder)
  }
}
