package org.oppia.android.util.parser

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint

/**
 * A [Drawable] for rendering [ScalableVectorGraphic]s. See subclasses for specific drawables &
 * rendering methods available.
 */
abstract class SvgPictureDrawable(
  private val scalableVectorGraphic: ScalableVectorGraphic
) : Drawable() {
  private var picture: Picture? = null
  private var intrinsicSize = ScalableVectorGraphic.SvgSizeSpecs(width = -1f, height = -1f)

  override fun draw(canvas: Canvas) {
    // The rendering approach here is loosely based on Android's PictureDrawable.
    canvas.apply {
      // Save current transformation state.
      save()

      picture?.let { picture ->
        // Apply the picture's bounds so that it's positioned/clipped correctly.
        Rect(bounds).apply {
          // Shift the drawable's bounds to adjust for needed vertical alignment (sometimes needed
          // for in-line drawables). This is done here versus during size recomputing so that
          // external changes to the bounds don't mess up the vertical shift needed for rendering.
          offset(/* dx= */ 0, /* dy= */ intrinsicSize.verticalAlignment.toInt())
          clipRect(this)
          translate(left.toFloat(), top.toFloat())
        }

        drawPicture(picture)
      }

      // Restore previous transformation state.
      restore()
    }
  }

  /**
   * See the super class for specifics. Note that the returned width will not be valid until this
   * drawable is initialized (which is the responsibility of the subclass implementation).
   */
  override fun getIntrinsicWidth(): Int = intrinsicSize.width.toInt()

  /** See [getIntrinsicWidth]. */
  override fun getIntrinsicHeight(): Int = intrinsicSize.height.toInt()

  override fun setAlpha(alpha: Int) { /* Unsupported. */ }

  override fun setColorFilter(colorFilter: ColorFilter?) { /* Unsupported. */ }

  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  /**
   * Re-renders the [Picture] state & intrinsic size held by this drawable, using block rendering
   * when [textPaint] is null and text rendering when otherwise.
   */
  protected fun reinitialize(textPaint: TextPaint?) {
    picture = textPaint?.let {
      scalableVectorGraphic.renderToTextPicture(it)
    } ?: scalableVectorGraphic.renderToBlockPicture()
    intrinsicSize = scalableVectorGraphic.computeSizeSpecs(textPaint)
  }
}
