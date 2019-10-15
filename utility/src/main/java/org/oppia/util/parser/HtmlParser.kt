package org.oppia.util.data;

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.widget.TextView

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

/** Html Parser for android TextView to parse Html tag. */
class HtmlParser(
  private val context: Context,
  private val entityType: String,
  private val entityId: String
) {

  fun parseHtml(rawString: String?, tvContents: TextView): Spannable {
    val html: Spannable
    var htmlContent = rawString
    if (htmlContent!!.contains(CUSTOM_IMG_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG, false);
      htmlContent = htmlContent.replace(
        CUSTOM_IMG_FILE_PATH_ATTRIBUTE,
        REPLACE_IMG_FILE_PATH_ATTRIBUTE, false);
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }
    // TODO(#205): Integrate UrlImageParser below once it's available.
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      html = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, /*imageGetter= */null, /*tagHandler= */null) as Spannable
    } else {
      html = Html.fromHtml(htmlContent, /*imageGetter= */null, /*tagHandler= */null) as Spannable
    }
    return html
  }
}
