package org.oppia.android.util.parser

/**
 * A [SvgPictureDrawable] for block-based rendering, that is, an image that should be centered near
 * blocks of text (which must be used in conjunction with [UrlImageParser]), or as a standalone
 * image.
 */
class BlockPictureDrawable internal constructor(
  scalableVectorGraphic: ScalableVectorGraphic
) : SvgPictureDrawable(scalableVectorGraphic) {
  init {
    // Initialize this drawable for block rendering.
    reinitialize(textPaint = null)
  }
}
