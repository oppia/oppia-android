package org.oppia.util.parser

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.widget.TextView
import javax.inject.Inject

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

/** Html Parser to parse custom Oppia tags with Android-compatible versions. */
class HtmlParser private constructor(
  private val context: Context,
  private val urlImageParserFactory : UrlImageParser.Factory,
  private val entityType: String,
  private val entityId: String
) {

  /**
   * This method replaces custom Oppia tags with Android-compatible versions for a given raw HTML string, and returns the HTML [Spannable].
   * @param rawString rawString argument is the string from the string-content
   * @param htmlContentTextView htmlContentTextView argument is the TextView, that need to be passed as argument to ImageGetter class for image parsing
   * @return Spannable Spannable represents the styled text.
   */
  fun parseOppiaHtml(rawString: String, htmlContentTextView: TextView): Spannable {
    var htmlContent = rawString
    if (htmlContent.contains(CUSTOM_IMG_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG, /* ignoreCase= */false);
      htmlContent = htmlContent.replace(
        CUSTOM_IMG_FILE_PATH_ATTRIBUTE,
        REPLACE_IMG_FILE_PATH_ATTRIBUTE, /* ignoreCase= */false
      );
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    val imageGetter =  urlImageParserFactory.create(htmlContentTextView, entityType, entityId)
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, /* imageGetter= */imageGetter, /* tagHandler= */null) as Spannable
    } else {
      Html.fromHtml(htmlContent, /* imageGetter= */imageGetter, /* tagHandler= */null) as Spannable
    }

  }

  class Factory @Inject constructor(
    @ApplicationContext private val context: Context, private val urlImageParserFactory: UrlImageParser.Factory
  ) {
    fun create(entityType: String, entityId: String): HtmlParser {
      return HtmlParser(context,urlImageParserFactory, entityType, entityId)
    }
  }
}
