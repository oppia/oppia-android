package org.oppia.android.util.parser.html

import android.app.Application
import android.content.res.AssetManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.ReplacementSpan
import androidx.core.content.res.ResourcesCompat
import io.github.karino2.kotlitex.view.MathExpressionSpan
import java.lang.ref.WeakReference
import org.json.JSONObject
import org.oppia.android.util.R
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever.Type.BLOCK_IMAGE
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever.Type.INLINE_TEXT_IMAGE
import org.xml.sax.Attributes

/** The custom tag corresponding to [MathTagHandler]. */
const val CUSTOM_MATH_TAG = "oppia-noninteractive-math"
private const val CUSTOM_MATH_MATH_CONTENT_ATTRIBUTE = "math_content-with-value"
private const val CUSTOM_MATH_RENDER_TYPE_ATTRIBUTE = "render-type"

/**
 * A custom tag handler for properly formatting math items in HTML parsed with
 * [CustomHtmlContentHandler].
 */
class MathTagHandler(
  private val consoleLogger: ConsoleLogger,
  private val assetManager: AssetManager,
  private val lineHeight: Float,
  private val cacheLatexRendering: Boolean,
  private val application: Application
) : CustomHtmlContentHandler.CustomTagHandler, CustomHtmlContentHandler.ContentDescriptionProvider {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever?
  ) {
    // Only insert the image tag if it's parsed correctly.
    val content = MathContent.parseMathContent(
      attributes.getJsonObjectValue(CUSTOM_MATH_MATH_CONTENT_ATTRIBUTE)
    )
    // TODO(#4170): Fix vertical alignment centering for inline cached LaTeX.
    val useInlineRendering = when (attributes.getValue(CUSTOM_MATH_RENDER_TYPE_ATTRIBUTE)) {
      "inline" -> true
      "block" -> false
      else -> true
    }
    checkNotNull(imageRetriever) { "Expected imageRetriever to be not null." }
    val newSpan = when (content) {
      is MathContent.MathAsSvg -> {
        ImageSpan(
          imageRetriever.loadDrawable(
            content.svgFilename,
            INLINE_TEXT_IMAGE
          ),
          content.svgFilename
        )
      }
      is MathContent.MathAsLatex -> {
        if (cacheLatexRendering) {
          LatexImageSpan(
            imageRetriever.loadMathDrawable(
              content.rawLatex,
              lineHeight,
              type = if (useInlineRendering) INLINE_TEXT_IMAGE else BLOCK_IMAGE
            ),
            useInlineRendering
          )
        } else {
          MathExpressionSpan(
            content.rawLatex,
            lineHeight,
            assetManager,
            isMathMode = !useInlineRendering,
            ResourcesCompat.getColor(
              application.resources,
              R.color.component_color_shared_equation_color,
              /* theme = */ null
            )
          )
        }
      }
      null -> {
        consoleLogger.e("MathTagHandler", "Failed to parse math tag")
        return
      }
    }

    // Insert an image span where the custom tag currently is to load the SVG/LaTeX span. Note that
    // this approach is based on Android's HTML parser.
    val (startIndex, endIndex) = output.run {
      // Use a control character to ensure that there's at least 1 character on which to
      // "attach" the image when rendering the HTML.
      val startIndex = length
      append('\uFFFC')
      return@run startIndex to length
    }
    output.setSpan(
      newSpan,
      startIndex,
      endIndex,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }

  private sealed class MathContent {
    data class MathAsSvg(val svgFilename: String) : MathContent()

    data class MathAsLatex(val rawLatex: String) : MathContent()

    companion object {
      internal fun parseMathContent(obj: JSONObject?): MathContent? {
        // Kotlitex expects escaped backslashes.
        val rawLatex = obj?.getOptionalString("raw_latex")
        val svgFilename = obj?.getOptionalString("svg_filename")
        return when {
          svgFilename != null -> MathAsSvg(svgFilename)
          rawLatex != null -> MathAsLatex(rawLatex)
          else -> null
        }
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

  override fun getContentDescription(attributes: Attributes): String {
    val mathVal = attributes.getJsonObjectValue(CUSTOM_MATH_MATH_CONTENT_ATTRIBUTE)
    return mathVal?.let { "Math content $it" } ?: ""
  }
}

/** An [ImageSpan] that vertically centers a LaTeX drawable within the surrounding text. */
private class LatexImageSpan(
  private val drawable: Drawable,
  private val isInline: Boolean
) : ReplacementSpan() {
  companion object {
    private const val INLINE_SHIFT_FACTOR = 0.9f // Adjust this value (0.2-0.4) as needed
  }

  override fun getSize(
    paint: Paint,
    text: CharSequence,
    start: Int,
    end: Int,
    fm: Paint.FontMetricsInt?
  ): Int {
    val bounds = drawable.bounds
    val imageHeight = bounds.height()
    val paintMetrics = paint.fontMetricsInt
    val textHeight = paintMetrics.descent - paintMetrics.ascent

    fm?.let { metrics ->
      if (isInline) {
        // Reserve space for inline shift
        val verticalShift = (imageHeight - textHeight) / 2 +
          (paintMetrics.descent * INLINE_SHIFT_FACTOR).toInt()
        metrics.ascent = paintMetrics.ascent - verticalShift
        metrics.top = metrics.ascent
        metrics.descent = paintMetrics.descent + verticalShift
        metrics.bottom = metrics.descent
      } else {
        // Block mode calculations remain unchanged
        val totalHeight = (imageHeight * 1.2).toInt()
        metrics.ascent = -totalHeight / 2
        metrics.top = metrics.ascent
        metrics.descent = totalHeight / 2
        metrics.bottom = metrics.descent
      }
    }
    return bounds.right
  }

  override fun draw(
    canvas: Canvas,
    text: CharSequence,
    start: Int,
    end: Int,
    x: Float,
    lineTop: Int,
    baseline: Int,
    lineBottom: Int,
    paint: Paint
  ) {
    canvas.save()

    val imageHeight = drawable.bounds.height()
    val yPosition = when {
      isInline -> {
        // Apply downward shift for inline equations
        val textMidline = baseline - (paint.fontMetrics.descent - paint.fontMetrics.ascent) / 2
        val shiftOffset = (paint.fontMetricsInt.descent * INLINE_SHIFT_FACTOR).toInt()
        textMidline - (imageHeight / 2) + shiftOffset
      }
      else -> {
        // Block mode remains centered
        lineTop + (lineBottom - lineTop - imageHeight) / 2
      }
    }

    canvas.translate(x, yPosition.toFloat())
    drawable.draw(canvas)
    canvas.restore()
  }
}
