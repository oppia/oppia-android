package org.oppia.android.util.image

import android.content.Context
import android.graphics.Picture
import android.text.TextPaint

/**
 * A [SvgPictureDrawable] for in-line rendering. This must be used in conjunction with
 * [UrlImageParser].
 *
 * [computeTextPicture] must be called before this drawable can be drawn.
 */
class TextPictureDrawable internal constructor(
  context: Context,
  scalableVectorGraphic: ScalableVectorGraphic
) : SvgPictureDrawable(context, scalableVectorGraphic) {
  /**
   * Initializes this drawable with a text-based [Picture]. See
   * [ScalableVectorGraphic.renderToTextPicture] for specifics.
   */
  fun computeTextPicture(textPaint: TextPaint): Unit = reinitialize(textPaint)
}
