package org.oppia.android.util.parser

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.text.TextPaint

abstract class TransformablePictureDrawable : Drawable() {
  // Force children to specifically implement this function.
  abstract override fun draw(canvas: Canvas)

  override fun getIntrinsicWidth(): Int = getIntrinsicSize().width.toInt()

  override fun getIntrinsicHeight(): Int = getIntrinsicSize().height.toInt()

  override fun setAlpha(alpha: Int) { /* Unsupported. */ }

  override fun setColorFilter(colorFilter: ColorFilter?) { /* Unsupported. */ }

  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  /** Returns this drawable's current [Picture] if it's been computed, or null if otherwise. */
  abstract fun getPicture(): Picture?

  /**
   * (Re)-initializes this drawable with a text-based [Picture]. See [OppiaSvg.renderToBlockPicture]
   * for specifics.
   */
  abstract fun computeBlockPicture()

  /**
   * (Re)-initializes this drawable with a text-based [Picture]. See [OppiaSvg.renderToTextPicture]
   * for specifics.
   */
  abstract fun computeTextPicture(textPaint: TextPaint)

  /**
   * Returns the computed intrinsic size of this drawable, based on prior calls to [computePicture].
   * Note that the returned data class includes the same values as [getIntrinsicWidth] and
   * [getIntrinsicHeight], except in float form and including vertical alignment.
   */
  abstract fun getIntrinsicSize(): IntrinsicSize

  /** Corresponds to the intrinsic size of the drawable. */
  data class IntrinsicSize(
    /** The width in pixels needed to contain the entire SVG picture. */
    val width: Float,
    /** The height in pixels needed to contain the entire SVG picture. */
    val height: Float,
    // TODO: consider just immediately applying the vertical alignment & other computations during draw.
    /** The amount of vertical pixels that should be translated when drawing the picture. */
    val verticalAlignment: Float = 0f
  )
}
