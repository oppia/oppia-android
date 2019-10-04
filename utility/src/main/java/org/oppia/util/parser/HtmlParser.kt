package org.oppia.util.parser;

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.widget.TextView

const val CUSTOM_TAG = "oppia-noninteractive-image"
const val HTML_TAG = "img"
const val CUSTOM_ATTRIBUTE = "filepath-with-value"
const val HTML_ATTRIBUTE = "src"

/** Html Parser for android TextView to parse Html tag. */
class HtmlParser(
  internal var context: Context,
  val entity_type: String,
  val entity_id: String
) {

  fun parseHtml(rawString: String?, tvContents: TextView): Spannable {
    val html: Spannable
    var htmlContent = rawString
    if (htmlContent!!.contains(CUSTOM_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_TAG, HTML_TAG, false);
      htmlContent = htmlContent.replace(
        CUSTOM_ATTRIBUTE,
        HTML_ATTRIBUTE, false);
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }
    var imageGetter = UrlImageParser(tvContents, context, entity_type, entity_id)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, imageGetter, null) as Spannable
    }
    return html
  }
}
