package org.oppia.util.parser

import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.style.BulletSpan
import org.xml.sax.XMLReader

/**
 * [Html.TagHandler] implementation that processes <li> tags and creates bullets.
 *
 * Reference: https://github.com/davidbilik/bullet-span-sample
 */
class LiTagHandler : Html.TagHandler {
  /**
   * Helper marker class. Based on [Html.fromHtml] implementation.
   */
  class Bullet

  override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
    if (tag == "li" && opening) {
      output.setSpan(Bullet(), output.length, output.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    if (tag == "li" && !opening) {
      output.append("\n")
      val lastMark = output.getSpans(0, output.length, Bullet::class.java).lastOrNull()
      lastMark?.let {
        val start = output.getSpanStart(it)
        output.removeSpan(it)
        if (start != output.length) {
          output.setSpan(BulletSpan(), start, output.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
      }
    }
  }
}
