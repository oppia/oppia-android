package org.oppia.android.util.parser

import android.text.Editable
import android.text.Spannable
import android.text.style.ImageSpan
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [MathTagHandler]. */
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
    imageRetriever: CustomHtmlContentHandler.ImageRetriever
  ) {
    // Note that the attribute is actually an encoded JSON string (so it has escaped quotes around
    // it). Since it's only a source string, the quotes can simply be removed in order to extract
    // the string value.
    val source = attributes.getValue(CUSTOM_IMG_FILE_PATH_ATTRIBUTE)?.replace("&quot;", "")
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
        imageRetriever.loadDrawable(
          source, CustomHtmlContentHandler.ImageRetriever.Type.BLOCK_IMAGE
        )
      output.setSpan(
        ImageSpan(drawable, source),
        startIndex,
        endIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    } else consoleLogger.e("ImageTagHandler", "Failed to parse image tag")
  }
}
