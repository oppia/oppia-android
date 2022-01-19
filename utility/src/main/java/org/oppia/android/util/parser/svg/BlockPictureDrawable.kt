package org.oppia.android.util.parser.svg

import android.content.Context

/**
 * A [SvgPictureDrawable] for block-based rendering, that is, an image that should be centered near
 * blocks of text (which must be used in conjunction with [UrlImageParser]), or as a standalone
 * image.
 */
class BlockPictureDrawable constructor(
  context: Context,
  scalableVectorGraphic: ScalableVectorGraphic
) : SvgPictureDrawable(context, scalableVectorGraphic) {
  init {
    // Initialize this drawable for block rendering.
    reinitialize(textPaint = null)
  }
}
