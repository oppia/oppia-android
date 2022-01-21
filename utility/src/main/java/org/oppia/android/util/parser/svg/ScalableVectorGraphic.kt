package org.oppia.android.util.parser.svg

import android.graphics.Picture
import android.text.TextPaint
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.utils.RenderOptionsBase
import org.oppia.android.util.parser.image.ImageTransformation

/**
 * The app's representation of SVG images. This should be used analogically with Android's bitmap
 * class when loading and rendering scalable vector graphics.
 *
 * [SvgPictureDrawable] should be used to render instances of this class.
 */
class ScalableVectorGraphic {
  private val parsedSvg: Lazy<SVG>
  val transformations: List<ImageTransformation>

  /** Constructs a new [ScalableVectorGraphic] from the specified SVG source code. */
  constructor(svgSource: String) {
    parsedSvg = lazy { SVG.getFromString(svgSource) }
    transformations = listOf()
  }

  private constructor(parsedSvg: SVG, transformations: List<ImageTransformation>) {
    this.parsedSvg = lazy { parsedSvg }
    this.transformations = transformations.distinct()
  }

  /**
   * Returns the [SvgSizeSpecs] corresponding to this SVG, based on the specified [textPaint]. If a
   * [TextPaint] is supplied, the returns specs will include text-based adjustments (for in-line
   * images). Otherwise, the returned specs will be arranged for rendering the SVG in a standalone
   * manner.
   */
  fun computeSizeSpecs(textPaint: TextPaint?): SvgSizeSpecs {
    return if (textPaint != null) {
      val options = RenderOptionsBase().textPaint(textPaint)
      val width = parsedSvg.value.getDocumentWidth(options)
      val height = parsedSvg.value.getDocumentHeight(options)
      val verticalAlignment =
        adjustAlignmentForAndroid(parsedSvg.value.getVerticalAlignment(options))
      SvgSizeSpecs(width, height, verticalAlignment)
    } else {
      val options = RenderOptionsBase()
      val width = parsedSvg.value.getDocumentWidth(options)
      val height = parsedSvg.value.getDocumentHeight(options)
      SvgSizeSpecs(width, height, verticalAlignment = 0f)
    }
  }

  /**
   * Returns an Android [Picture] including the draw instructions for rendering this SVG within a
   * line of text whose size and style is configured by the provided [textPaint].
   */
  fun renderToTextPicture(textPaint: TextPaint): Picture {
    return computeSizeSpecs(textPaint).let { (width, height, _) ->
      val options =
        RenderOptions().textPaint(textPaint).viewPort(0f, 0f, width, height) as RenderOptions
      parsedSvg.value.renderToPicture(options)
    }
  }

  /**
   * Returns an Android [Picture] including the draw instructions for rendering this SVG in a block
   * or standalone format (that is, it's not meant to be rendered within text).
   */
  fun renderToBlockPicture(): Picture = parsedSvg.value.renderToPicture()

  /**
   * Returns a new [ScalableVectorGraphic] that will be transformed by the specified
   * transformations. Any existing transformations on the graphic will also be included.
   */
  fun transform(transformations: List<ImageTransformation>): ScalableVectorGraphic =
    ScalableVectorGraphic(parsedSvg.value, this.transformations + transformations)

  // It seems that vertical alignment needs to be halved to work in Android's coordinate system as
  // compared with SVGs. This might be due to SVGs seemingly using an origin in the middle of the
  // image vs. Android using an upper-left origin (though the SVG spec also indicates an upper left
  // origin). Further, negative alignment pushes the image down in the SVG coordinate system,
  // whereas Android's has positive y going down (requiring the y value to be reversed). This may
  // simply be a change of coordinate space occurring somewhere along the transformation pipeline;
  // this correction has been determined based on visual observation and may need to be iterated on
  // in the future.
  private fun adjustAlignmentForAndroid(value: Float) = value * -0.5f

  /** Corresponds to the intrinsic size of the drawable. */
  data class SvgSizeSpecs(
    /** The width in pixels needed to contain the entire SVG picture. */
    val width: Float,
    /** The height in pixels needed to contain the entire SVG picture. */
    val height: Float,
    /** The amount of vertical pixels that should be translated when drawing the picture. */
    val verticalAlignment: Float = 0f
  )
}
