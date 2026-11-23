package org.oppia.android.util.parser.svg

import android.graphics.Picture
import android.graphics.RectF
import android.text.TextPaint
import com.caverock.androidsvg.androidrendering.RenderOptions
import com.caverock.androidsvg.androidrendering.RenderOptionsBase
import com.caverock.androidsvg.androidrendering.SVG
import com.caverock.androidsvg.model.PreserveAspectRatio
import org.oppia.android.util.parser.image.ImageTransformation

/**
 * The app's representation of SVG images. This should be used analogically with Android's bitmap
 * class when loading and rendering scalable vector graphics.
 *
 * [SvgPictureDrawable] should be used to render instances of this class.
 */
class ScalableVectorGraphic {
  private val parsedSvg: Lazy<SVG>
  private var extractedWidth: Int? = null
  private var extractedHeight: Int? = null
  private var shouldPreserveAspectRatio: Boolean = false
  val transformations: List<ImageTransformation>

  /** Constructs a new [ScalableVectorGraphic] from the specified SVG source code. */
  constructor(svgSource: String) {
    parsedSvg = lazy { SVG.getFromString(svgSource) }
    transformations = listOf()
  }

  private constructor(
    parsedSvg: SVG,
    transformations: List<ImageTransformation>,
    extractedWidth: Int?,
    extractedHeight: Int?,
    shouldPreserveAspectRatio: Boolean
  ) {
    this.parsedSvg = lazy { parsedSvg }
    this.transformations = transformations.distinct()
    this.extractedWidth = extractedWidth
    this.extractedHeight = extractedHeight
    this.shouldPreserveAspectRatio = shouldPreserveAspectRatio
  }

  /**
   * Returns the [SvgSizeSpecs] corresponding to this SVG.
   *
   * The returned specs will be arranged for rendering the SVG in a standalone manner.
   */
  fun computeSizeSpecs(): SvgSizeSpecs {
    val options = RenderOptionsBase()
    val documentWidth = parsedSvg.value.getDocumentWidthOrNull(options)
    val documentHeight = parsedSvg.value.getDocumentHeightOrNull(options)

    val viewBox: RectF? = parsedSvg.value.documentViewBox
    val viewBoxWidth = viewBox?.width()
    val viewBoxHeight = viewBox?.height()

    val imageFileNameWidth = extractedWidth?.toFloat()
    val imageFileNameHeight = extractedHeight?.toFloat()

    // Prioritize the document size, then view box, then image filename. From observation, the
    // document size seems correct in cases where it's available, and otherwise the viewbox is
    // generally correct. The image filename can sometimes be wrong (as it actually represents the
    // size the image should be rendered at one web rather than its actual size), but it's a
    // reasonable fallback if the other two are unavailable. If no dimension is available, default
    // to a value that can hopefully be scaled (though it's likely at that point the image will not
    // render well). However, the filename is the best indicator of what size to which the image
    // *should* be rendered.
    val intrinsicWidth = documentWidth ?: viewBoxWidth ?: imageFileNameWidth ?: DEFAULT_SIZE_PX
    val intrinsicHeight = documentHeight ?: viewBoxHeight ?: imageFileNameHeight ?: DEFAULT_SIZE_PX

    return SvgSizeSpecs(
      intrinsicWidth,
      intrinsicHeight,
      renderedWidth = imageFileNameWidth ?: intrinsicWidth,
      renderedHeight = imageFileNameHeight ?: intrinsicHeight,
      verticalAlignment = 0f
    )
  }

  /**
   * Returns the [SvgSizeSpecs] corresponding to this SVG, based on the specified [textPaint].
   * Based on the supplied [TextPaint], the returned specs will include text-based adjustments
   * (for in-line images).
   */
  fun computeSizeSpecsForTextPicture(textPaint: TextPaint?): SvgSizeSpecs {
    val options = textPaint?.let { RenderOptionsBase().textPaint(it) } ?: RenderOptionsBase()
    val documentWidth = parsedSvg.value.getDocumentWidthOrNull(options)
    val documentHeight = parsedSvg.value.getDocumentHeightOrNull(options)

    val imageFileNameWidth = extractedWidth?.toFloat()
    val imageFileNameHeight = extractedHeight?.toFloat()

    val fontMetrics = textPaint?.fontMetrics
    val fontHeight = fontMetrics?.descent?.minus(fontMetrics.ascent) ?: 0f

    val adjustedWidth =
      imageFileNameWidth?.convertExToPx(fontHeight) ?: documentWidth ?: DEFAULT_SIZE_PX
    val adjustedHeight =
      imageFileNameHeight?.convertExToPx(fontHeight) ?: documentHeight ?: DEFAULT_SIZE_PX

    val verticalAlignment = textPaint?.let {
      adjustAlignmentForAndroid(parsedSvg.value.getVerticalAlignment(options))
    } ?: 0f

    return SvgSizeSpecs(
      adjustedWidth,
      adjustedHeight,
      renderedWidth = adjustedWidth,
      renderedHeight = adjustedHeight,
      verticalAlignment
    )
  }

  private fun Float.convertExToPx(fontHeight: Float): Float {
    return this * fontHeight * 0.5f
  }

  /**
   * Returns an Android [Picture] including the draw instructions for rendering this SVG within a
   * line of text whose size and style is configured by the provided [textPaint].
   */
  fun renderToTextPicture(textPaint: TextPaint): Picture {
    return computeSizeSpecsForTextPicture(textPaint).let { (width, height, _) ->
      val options =
        RenderOptions().textPaint(textPaint).viewPort(0f, 0f, width, height) as RenderOptions
      parsedSvg.value.renderToPicture(options)
    }
  }

  /**
   * Returns an Android [Picture] including the draw instructions for rendering this SVG in a block
   * or standalone format (that is, it's not meant to be rendered within text).
   *
   * By default, the picture has the SVG's own size and so it's stretched when drawn at a rendered
   * size (from [computeSizeSpecs]) with a different aspect ratio. If the graphic was initialized
   * with 'preserveAspectRatio' set (see [initializeWithExtractedDimensions]), the picture instead
   * has exactly the rendered size, with the SVG scaled evenly to fit inside it and centered (see
   * [computeLetterboxedPictureSize]). This matches how browsers (and thus Oppia web) fit an SVG
   * image into a box of a different shape, per the default 'xMidYMid meet' value of the SVG's
   * preserveAspectRatio attribute. SVGs that explicitly opt into stretching
   * (preserveAspectRatio="none") are still stretched.
   */
  fun renderToBlockPicture(): Picture {
    val svg = parsedSvg.value
    val naturalPicture = svg.renderToPicture()
    if (!shouldPreserveAspectRatio || svg.isStretchingAllowed()) return naturalPicture
    val letterboxSize = computeLetterboxedPictureSize(naturalPicture) ?: return naturalPicture

    val (pictureWidth, pictureHeight, documentBounds, documentScale) = letterboxSize
    return Picture().also { picture ->
      picture.beginRecording(pictureWidth, pictureHeight).apply {
        translate(documentBounds.left, documentBounds.top)
        scale(documentScale, documentScale)
        // Note that the natural picture isn't clipped to its own bounds, so any content that
        // overflows the SVG's viewBox stays visible in the padding (like it does on web).
        drawPicture(naturalPicture)
      }
      picture.endRecording()
    }
  }

  /**
   * Returns the [LetterboxedPictureSize] needed to render this SVG in a block format without
   * distorting it, or null if no letterboxing is needed (either because the SVG's own size has the
   * same aspect ratio as its rendered size, or because the SVG has no intrinsic size to preserve).
   */
  fun computeLetterboxedPictureSize(): LetterboxedPictureSize? =
    computeLetterboxedPictureSize(naturalPicture = parsedSvg.value.renderToPicture())

  private fun computeLetterboxedPictureSize(naturalPicture: Picture): LetterboxedPictureSize? {
    val (documentWidth, documentHeight) = computeDocumentSize(naturalPicture) ?: return null
    val (_, _, renderedWidth, renderedHeight) = computeSizeSpecs()

    // The picture has exactly the size that SvgPictureDrawable draws it at, so that drawing it is
    // an even scale (rounding the picture to a different size would stretch it a little, and make
    // the padding uneven).
    val pictureWidth = renderedWidth.toInt()
    val pictureHeight = renderedHeight.toInt()
    if (pictureWidth <= 0 || pictureHeight <= 0) return null

    val documentScale = minOf(pictureWidth / documentWidth, pictureHeight / documentHeight)
    val horizontalPadding = (pictureWidth - documentWidth * documentScale) / 2f
    val verticalPadding = (pictureHeight - documentHeight * documentScale) / 2f

    // Avoid changing how images are drawn when the difference is smaller than a rendered pixel
    // (which is expected since the sizes in image filenames are rounded).
    if (maxOf(horizontalPadding, verticalPadding) < MIN_LETTERBOX_PADDING_RENDERED_PX) return null

    val documentBounds = RectF(
      horizontalPadding,
      verticalPadding,
      pictureWidth - horizontalPadding,
      pictureHeight - verticalPadding
    )
    return LetterboxedPictureSize(pictureWidth, pictureHeight, documentBounds, documentScale)
  }

  /**
   * Returns the size of the SVG's content within [naturalPicture] (the picture that AndroidSVG
   * renders for the SVG by default), or null if the SVG has no intrinsic size.
   */
  private fun computeDocumentSize(naturalPicture: Picture): Pair<Float, Float>? {
    val svg = parsedSvg.value
    val options = RenderOptionsBase()
    val documentWidth = svg.getDocumentWidthOrNull(options)
    val documentHeight = svg.getDocumentHeightOrNull(options)
    return when {
      // Prefer the exact (unrounded) document size when AndroidSVG can provide it.
      documentWidth != null && documentHeight != null -> documentWidth to documentHeight
      // Otherwise, AndroidSVG sized the picture from the viewBox, and from the root 'height'
      // attribute for SVGs that only specify a height (which AndroidSVG doesn't otherwise expose).
      svg.documentViewBox != null && naturalPicture.width > 0 && naturalPicture.height > 0 ->
        naturalPicture.width.toFloat() to naturalPicture.height.toFloat()
      else -> null
    }
  }

  /**
   * Returns a new [ScalableVectorGraphic] that will be transformed by the specified
   * transformations. Any existing transformations on the graphic will also be included.
   */
  fun transform(transformations: List<ImageTransformation>): ScalableVectorGraphic {
    return ScalableVectorGraphic(
      parsedSvg.value,
      this.transformations + transformations,
      extractedWidth,
      extractedHeight,
      shouldPreserveAspectRatio
    )
  }

  /**
   * Initializes the size at which this graphic should be rendered, as extracted from the image's
   * filename.
   *
   * @param width the rendered width, or null if unknown
   * @param height the rendered height, or null if unknown
   * @param preserveAspectRatio whether [renderToBlockPicture] should keep the SVG's own aspect
   *     ratio (rather than stretching it) when it differs from that of the rendered size
   */
  fun initializeWithExtractedDimensions(
    width: Int?,
    height: Int?,
    preserveAspectRatio: Boolean = false
  ) {
    extractedWidth = width
    extractedHeight = height
    shouldPreserveAspectRatio = preserveAspectRatio
  }

  // It seems that vertical alignment needs to be halved to work in Android's coordinate system as
  // compared with SVGs. This might be due to SVGs seemingly using an origin in the middle of the
  // image vs. Android using an upper-left origin (though the SVG spec also indicates an upper left
  // origin). Further, negative alignment pushes the image down in the SVG coordinate system,
  // whereas Android's has positive y going down (requiring the y value to be reversed). This may
  // simply be a change of coordinate space occurring somewhere along the transformation pipeline;
  // this correction has been determined based on visual observation and may need to be iterated on
  // in the future.
  private fun adjustAlignmentForAndroid(value: Float) = value * -0.5f

  /**
   * Corresponds to the intrinsic size of the drawable.
   *
   * @property intrinsicWidth the width in pixels needed to contain the entire SVG picture
   * @property intrinsicHeight the height in pixels needed to contain the entire SVG picture
   * @property renderedWidth the desired width to render the SVG, in Oppia web pixels
   * @property renderedHeight the desired height to render the SVG, in Oppia web pixels
   * @property verticalAlignment he amount of vertical pixels that should be translated when drawing
   *     the picture
   */
  data class SvgSizeSpecs(
    val intrinsicWidth: Float,
    val intrinsicHeight: Float,
    val renderedWidth: Float = intrinsicWidth,
    val renderedHeight: Float = intrinsicHeight,
    val verticalAlignment: Float = 0f
  )

  /**
   * The size of a [Picture] that letterboxes this SVG so that it can be drawn at its rendered size
   * without being distorted.
   *
   * @property pictureWidth the width of the picture, in rendered pixels (i.e. the rendered width)
   * @property pictureHeight the height of the picture, in rendered pixels (i.e. the rendered height)
   * @property documentBounds where the SVG is drawn within the picture (it's centered, and scaled
   *     evenly to fit)
   * @property documentScale how much the SVG's own size is scaled to fit in [documentBounds]
   */
  data class LetterboxedPictureSize(
    val pictureWidth: Int,
    val pictureHeight: Int,
    val documentBounds: RectF,
    val documentScale: Float
  )

  private companion object {
    // Without knowing what size to default to, just pick 1 and let the app & other utilities try to
    // scale the image.
    private const val DEFAULT_SIZE_PX = 1f

    // Letterboxing with less padding than this on each side (in rendered pixels) is ignored since
    // it wouldn't visibly change the image.
    private const val MIN_LETTERBOX_PADDING_RENDERED_PX = 1f

    private fun SVG.isStretchingAllowed(): Boolean =
      documentPreserveAspectRatio?.alignment == PreserveAspectRatio.Alignment.none

    private fun SVG.getDocumentWidthOrNull(options: RenderOptionsBase): Float? =
      getDocumentWidth(options).takeIf { it > 0 }

    private fun SVG.getDocumentHeightOrNull(options: RenderOptionsBase): Float? =
      getDocumentHeight(options).takeIf { it > 0 }
  }
}
