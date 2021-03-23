package org.oppia.android.util.parser

import android.graphics.Picture
import android.text.TextPaint
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.utils.RenderOptionsBase

class OppiaSvg {
  private val parsedSvg: Lazy<SVG>
  val transformations: List<ImageTransformation>

  constructor(svgSource: String) {
    parsedSvg = lazy { SVG.getFromString(svgSource) }
    transformations = listOf()
  }

  private constructor(parsedSvg: SVG, transformations: List<ImageTransformation>) {
    this.parsedSvg = lazy { parsedSvg }
    this.transformations = transformations.distinct()
  }

  internal fun computeSizeSpecs(textPaint: TextPaint?): SvgSizeSpecs {
    return parsedSvg.value.let { svg ->
      if (textPaint != null) {
        val options = RenderOptionsBase().textPaint(textPaint)
        val width = svg.getDocumentWidth(options)
        val height = svg.getDocumentHeight(options)
        val verticalAlignment = adjustAlignmentForAndroid(svg.getVerticalAlignment(options))
        SvgSizeSpecs(width, height, verticalAlignment)
      } else {
        val options = RenderOptionsBase()
        val width = svg.getDocumentWidth(options)
        val height = svg.getDocumentHeight(options)
        SvgSizeSpecs(width, height, verticalAlignment = 0f)
      }
    }
  }

  fun renderToTextPicture(textPaint: TextPaint): Picture {
    return parsedSvg.value.let { svg ->
      computeSizeSpecs(textPaint).let { (width, height, _) ->
        val options =
          RenderOptions().textPaint(textPaint).viewPort(0f, 0f, width, height) as RenderOptions
        svg.renderToPicture(options)
      }
    }
  }

  fun renderToBlockPicture(): Picture = parsedSvg.value.renderToPicture()

  fun transform(transformations: List<ImageTransformation>): OppiaSvg {
    return OppiaSvg(parsedSvg.value, this.transformations + transformations)
  }

  // It seems that vertical alignment needs to be halved to work in Android's coordinate system as
  // compared with SVGs. This might be due to SVGs seemingly using an origin in the middle of the
  // image vs. Android using an upper-left origin. Further, negative alignment pushes the image down
  // in the SVG coordinate system, whereas Android's has positive y going down (requiring the y
  // value to be reversed).
  private fun adjustAlignmentForAndroid(value: Float) = value * -0.5f

  data class SvgSizeSpecs(val width: Float, val height: Float, val verticalAlignment: Float)
}
