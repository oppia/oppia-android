package org.oppia.util.parser

import android.text.Html
import android.text.Spannable
import android.widget.TextView
import javax.inject.Inject
import android.text.SpannableStringBuilder



private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

/** Html Parser to parse custom Oppia tags with Android-compatible versions. */
class HtmlParser private constructor(
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
      htmlContent = htmlContent.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG, /* ignoreCase= */false)
      htmlContent = htmlContent.replace(
        CUSTOM_IMG_FILE_PATH_ATTRIBUTE,
        REPLACE_IMG_FILE_PATH_ATTRIBUTE, /* ignoreCase= */false
      )
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    val imageGetter =  urlImageParserFactory.create(htmlContentTextView, entityType, entityId)
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      trimSpannable(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, /* tagHandler= */ null) as SpannableStringBuilder)
    } else {
      trimSpannable(Html.fromHtml(htmlContent, imageGetter, /* tagHandler= */ null)as SpannableStringBuilder)
    }
  }

  class Factory @Inject constructor(private val urlImageParserFactory: UrlImageParser.Factory) {
    fun create(entityType: String, entityId: String): HtmlParser {
      return HtmlParser(urlImageParserFactory, entityType, entityId)
    }
  }

  private fun trimSpannable(spannable: SpannableStringBuilder): SpannableStringBuilder {
    checkNotNull(spannable)
    var trimStart = 0
    var trimEnd = 0

    var text = spannable.toString()

    while (text.length > 0 && text.startsWith("\n")) {
      text = text.substring(1)
      trimStart += 1
    }

    while (text.length > 0 && text.endsWith("\n")) {
      text = text.substring(0, text.length - 1)
      trimEnd += 1
    }

    return spannable.delete(0, trimStart).delete(spannable.length - trimEnd, spannable.length)
  }
}
