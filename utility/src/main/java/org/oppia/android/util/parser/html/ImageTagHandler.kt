package org.oppia.android.util.parser.html

import android.graphics.Typeface
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [ImageTagHandler]. */
const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val CUSTOM_IMG_ALT_TEXT_ATTRIBUTE = "alt-with-value"
private const val CUSTOM_IMG_CAPTION_ATTRIBUTE = "caption-with-value"

/**
 * A custom tag handler for supporting custom Oppia images parsed with [CustomHtmlContentHandler].
 */
class ImageTagHandler(
  private val consoleLogger: ConsoleLogger
) : CustomHtmlContentHandler.CustomTagHandler, CustomHtmlContentHandler.ContentDescriptionProvider {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever?
  ) {
    val source = attributes.getJsonStringValue(CUSTOM_IMG_FILE_PATH_ATTRIBUTE)
    val contentDescription = attributes.getJsonStringValue(CUSTOM_IMG_ALT_TEXT_ATTRIBUTE)
    val caption = attributes.getJsonStringValue(CUSTOM_IMG_CAPTION_ATTRIBUTE)

    if (source != null) {
      val (startIndex, endIndex) = output.run {
        // Use a control character to ensure that there's at least 1 character on which to "attach"
        // the image when rendering the HTML. Note that this approach is based on Android's Html
        // parser.
        val startIndex = length
        append('\uFFFC')
        return@run startIndex to length
      }
      imageRetriever?.let {
        val drawable = imageRetriever.loadDrawable(
          source, CustomHtmlContentHandler.ImageRetriever.Type.BLOCK_IMAGE
        )
        output.setSpan(
          ImageSpan(drawable, source),
          startIndex,
          endIndex,
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
      }
    } else consoleLogger.e("ImageTagHandler", "Failed to parse image tag")
    if (!contentDescription.isNullOrBlank()) {
      val spannableBuilder = SpannableStringBuilder(contentDescription)
      spannableBuilder.setSpan(
        contentDescription,
        /* start = */ 0,
        /* end = */ contentDescription.length,
        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
      )
      output.replace(openIndex, output.length, spannableBuilder)
    } else consoleLogger.w(
      "ImageTagHandler",
      "Failed to parse $CUSTOM_IMG_ALT_TEXT_ATTRIBUTE"
    )
    if (!caption.isNullOrBlank()) {
      output.append("\n")
      val captionStart = output.length
      output.append(caption)
      val captionEnd = output.length
      output.setSpan(
        StyleSpan(Typeface.ITALIC),
        captionStart,
        captionEnd,
        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
      )
      output.setSpan(
        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
        captionStart,
        captionEnd,
        Spannable.SPAN_PARAGRAPH
      )

      // Insert a newline after the caption and reset alignment.
      output.append("\n")
      val resetStart = output.length
      output.append(" ")
      output.setSpan(
        AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
        resetStart,
        output.length,
        Spannable.SPAN_PARAGRAPH
      )
    } else consoleLogger.w(
      "ImageTagHandler",
      "Failed to parse $CUSTOM_IMG_CAPTION_ATTRIBUTE"
    )
  }

  override fun getContentDescription(attributes: Attributes): String {
    val altValue = attributes.getJsonStringValue(CUSTOM_IMG_ALT_TEXT_ATTRIBUTE)
    return if (!altValue.isNullOrBlank()) {
      "Image illustrating $altValue"
    } else ""
  }
}
