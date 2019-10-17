package org.oppia.util.parser

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.widget.TextView

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

/** Html Parser for android TextView to parse Html tag. */
class HtmlParser(private val context: Context, entityType: String, entityId: String) {

  /**
   * This method replaces custom Oppia tags with Android-compatible versions for a given raw HTML string, and returns the HTML [Spannable].
   * @param rawString : rawString argument is the string from the string-content
   * @param htmlContentTextView : htmlContentTextView argument is the TextView, that need to be passed as argument to ImageGetter class for image parsing
   * @return Spannable : Spannable represents the styled text.
   */
  fun parseOppiaHtml(rawString: String?, htmlContentTextView: TextView): Spannable {
    var htmlContent = rawString
    if (htmlContent!!.contains(CUSTOM_IMG_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG, false);
      htmlContent = htmlContent.replace(
        CUSTOM_IMG_FILE_PATH_ATTRIBUTE,
        REPLACE_IMG_FILE_PATH_ATTRIBUTE, false
      );
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }
    // TODO(#205): Integrate UrlImageParser below once it's available.
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, /* imageGetter= */null, /* tagHandler= */null) as Spannable
    } else {
      Html.fromHtml(htmlContent, /* imageGetter= */null, /* tagHandler= */null) as Spannable
    }

  }
}
