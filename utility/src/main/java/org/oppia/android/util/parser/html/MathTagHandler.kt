package org.oppia.android.util.parser.html

import android.text.Editable
import android.text.Spannable
import android.text.style.ImageSpan
import org.json.JSONObject
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [MathTagHandler]. */
const val CUSTOM_MATH_TAG = "oppia-noninteractive-math"
private const val CUSTOM_MATH_SVG_PATH_ATTRIBUTE = "math_content-with-value"

/**
 * A custom tag handler for properly formatting math items in HTML parsed with
 * [CustomHtmlContentHandler].
 */
class MathTagHandler(
  private val consoleLogger: ConsoleLogger
) : CustomHtmlContentHandler.CustomTagHandler {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever?
  ) {
    // Only insert the image tag if it's parsed correctly.
    val content = MathContent.parseMathContent(
      attributes.getJsonObjectValue(CUSTOM_MATH_SVG_PATH_ATTRIBUTE)
    )
    if (content != null) {
      // Insert an image span where the custom tag currently is to load the SVG. In the future, this
      // could also load a LaTeX span, instead. Note that this approach is based on Android's Html
      // parser.
      val drawable =
        checkNotNull(imageRetriever!!.loadDrawable(
          content.svgFilename,
          CustomHtmlContentHandler.ImageRetriever.Type.INLINE_TEXT_IMAGE
        )) {
          "Expected imageRetriever to be not null."
        }

      val (startIndex, endIndex) = output.run {
        // Use a control character to ensure that there's at least 1 character on which to "attach"
        // the image when rendering the HTML.
        val startIndex = length
        append('\uFFFC')
        return@run startIndex to length
      }
      output.setSpan(
        ImageSpan(drawable, content.svgFilename),
        startIndex,
        endIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    } else consoleLogger.e("MathTagHandler", "Failed to parse math tag")
  }

  private data class MathContent(val rawLatex: String, val svgFilename: String) {
    companion object {
      internal fun parseMathContent(obj: JSONObject?): MathContent? {
        val rawLatex = obj?.getOptionalString("raw_latex")
        val svgFilename = obj?.getOptionalString("svg_filename")
        return if (rawLatex != null && svgFilename != null) {
          MathContent(rawLatex, svgFilename)
        } else null
      }

      /**
       * Returns a string corresponding to the specified name, or null if a mapping doesn't exist or
       * its value is null. There is no functionality in [JSONObject] that supports this exact
       * operation without relying on exceptions for control flow.
       */
      private fun JSONObject.getOptionalString(name: String): String? = opt(name)?.toJsonString()

      private fun Any?.toJsonString(): String? {
        // Based on JSON.toString() which is not available for public use.
        return when {
          this is String -> this
          this != null -> toString()
          else -> null
        }
      }
    }
  }
}
