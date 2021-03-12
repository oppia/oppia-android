package org.oppia.android.util.parser

import android.graphics.Picture
import android.text.TextPaint
import com.caverock.androidsvg.SVG

class OppiaSvg(private val svgSource: String) {
  private val parsedSvg by lazy { SVG.getFromString(svgSource) }
  private val widthRegex by lazy { Regex("width=\"([\\d.]+)ex\"") }
  private val heightRegex by lazy { Regex("height=\"([\\d.]+)ex\"") }

  // TODO: perform this parsing during SVG saving (e.g. by representing the SVG in proto form or
  // wrapping it) to avoid needing expensive regex matches just for image rendering.
  private val parsedWidth by lazy { parseDimension(widthRegex) }
  private val parsedHeight by lazy { parseDimension(heightRegex) }

  fun computeSize(textPaint: TextPaint): SvgSize? {
    val width = parsedWidth
    val height = parsedHeight
    return if (width != null && height != null) {
      // Follows CSS3 specification that 1ex=0.5em if the height of 'x' can't be easily computed. See
      // also: https://stackoverflow.com/q/42416622.
      val ex = textPaint.textSize * 0.5f
      SvgSize(width * ex, height * ex)
    } else null
  }

  fun renderToPicture(textPaint: TextPaint): Picture {
    return computeSize(textPaint)?.let { (width, height) ->
      parsedSvg.renderToPicture(width.toInt(), height.toInt())
    } ?: parsedSvg.renderToPicture()
  }

  private fun parseDimension(regex: Regex): Float? =
    regex.find(svgSource)?.destructured?.let { (parsedValue) -> parsedValue.toFloatOrNull() }

  data class SvgSize(val width: Float, val height: Float)
}
