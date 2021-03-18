package org.oppia.android.util.parser

import android.graphics.Picture
import android.text.TextPaint
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.utils.RenderOptionsBase

class OppiaSvg(private val svgSource: String) {
  private val parsedSvg by lazy { SVG.getFromString(svgSource) }

  internal fun computeSizeSpecs(textPaint: TextPaint): SvgSizeSpecs {
    val options = RenderOptionsBase().textPaint(textPaint)
    val width = parsedSvg.getDocumentWidth(options)
    val height = parsedSvg.getDocumentHeight(options)
    val verticalAlignment = adjustAlignmentForAndroid(parsedSvg.getVerticalAlignment(options))
    return SvgSizeSpecs(width, height, verticalAlignment)
  }

  fun renderToTextPicture(textPaint: TextPaint): Picture {
    return computeSizeSpecs(textPaint).let { (width, height, _) ->
      val options =
        RenderOptions().textPaint(textPaint).viewPort(0f, 0f, width, height) as RenderOptions
      parsedSvg.renderToPicture(options)
    }
  }

  fun renderToBlockPicture(): Picture = parsedSvg.renderToPicture()

  // It seems that vertical alignment needs to be halved to work in Android's coordinate system as
  // compared with SVGs. This might be due to SVGs seemingly using an origin in the middle of the
  // image vs. Android using an upper-left origin. Further, negative alignment pushes the image down
  // in the SVG coordinate system, whereas Android's has positive y going down (requiring the y
  // value to be reversed).
  private fun adjustAlignmentForAndroid(value: Float) = value * -0.5f

  data class SvgSizeSpecs(val width: Float, val height: Float, val verticalAlignment: Float)
}
