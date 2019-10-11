package org.oppia.util.data

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.widget.TextView

private const val CUSTOM_TAG = "oppia-noninteractive-image"
private const val HTML_TAG = "img"
private const val CUSTOM_ATTRIBUTE = "filepath-with-value"
private const val HTML_ATTRIBUTE = "src"

/** Html Parser for android TextView to parse Html tag. */
class HtmlParser(internal var context: Context) {

  /***
   * This method is called to parse HTML from a string-content, it manipulates the HTML elements, attributes and text
   * @param rawString : rawString argument is the string from the string-content
   * @param tvContents : tvContents argument is the TextView, that need to be passed as argument to ImageGetter class for image parsing
   * @return Spannable : Spannable represents the styled text.
   */
  fun parseHtml(rawString: String?, tvContents: TextView): Spannable {
    val html: Spannable
    var htmlContent = rawString
    if (htmlContent!!.contains(CUSTOM_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_TAG, HTML_TAG, false);
      htmlContent = htmlContent.replace(CUSTOM_ATTRIBUTE, HTML_ATTRIBUTE, false);
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, null, null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, null, null) as Spannable
    }
    return html
  }
}
