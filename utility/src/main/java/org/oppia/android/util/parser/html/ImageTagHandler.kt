package org.oppia.android.util.parser.html

import android.text.Editable
import android.text.Spannable
import android.text.style.ImageSpan
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [ImageTagHandler]. */
const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"

/**
 * A custom tag handler for supporting custom Oppia images parsed with [CustomHtmlContentHandler].
 */
class ImageTagHandler(
  private val consoleLogger: ConsoleLogger
) : CustomHtmlContentHandler.CustomTagHandler {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever?
  ) {
    val source = attributes.getJsonStringValue(CUSTOM_IMG_FILE_PATH_ATTRIBUTE)
    if (source != null) {
      val (startIndex, endIndex) = output.run {
        // Use a control character to ensure that there's at least 1 character on which to "attach"
        // the image when rendering the HTML. Note that this approach is based on Android's Html
        // parser.
        val startIndex = length
        append('\uFFFC')
        return@run startIndex to length
      }
      val drawable =
        imageRetriever?.loadDrawable(
          source, CustomHtmlContentHandler.ImageRetriever.Type.BLOCK_IMAGE
        )
      output.setSpan(
        drawable?.let { ImageSpan(it, source) },
        startIndex,
        endIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    } else consoleLogger.e("ImageTagHandler", "Failed to parse image tag")
  }
}
