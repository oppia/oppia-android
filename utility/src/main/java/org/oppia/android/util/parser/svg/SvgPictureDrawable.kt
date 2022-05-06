package org.oppia.android.util.parser.svg

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextPaint
import org.oppia.android.util.parser.image.BitmapBlurrer
import org.oppia.android.util.parser.image.ImageTransformation

/**
 * A [Drawable] for rendering [ScalableVectorGraphic]s. See subclasses for specific drawables &
 * rendering methods available.
 */
abstract class SvgPictureDrawable(
  private val context: Context,
  private val scalableVectorGraphic: ScalableVectorGraphic
) : Drawable() {
  // TODO(#1523): Once Glide can be orchestrated, add tests for verifying this drawable's state.
  // TODO(#1815): Add screenshot tests to verify this drawable is rendered correctly.

  /**
   * The [Paint] that should be used when rendering this drawable. Note that the flags are based
   * on the defaults set by Android's BitmapDrawable.
   */
  private val bitmapPaint by lazy { Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG) }
  private val bitmapBlurrer by lazy { BitmapBlurrer(context) }

  private var picture: Picture? = null
  private var intrinsicSize = ScalableVectorGraphic.SvgSizeSpecs(width = -1f, height = -1f)
  private var bitmap: Bitmap? = null

  override fun draw(canvas: Canvas) {
    // The rendering approach here is loosely based on Android's PictureDrawable.
    canvas.apply {
      // Save current transformation state.
      save()

      if (scalableVectorGraphic.shouldBeRenderedAsBitmap()) {
        bitmap?.let { bitmap ->
          drawBitmap(bitmap, /* src= */ null, bounds, bitmapPaint)
        }
      } else {
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

          // If the destination is larger than the intrinsic size (such as when the app needs the
          // image to be larger for accessibility) then ensure the picture is correspondingly
          // scaled.
          val scaleX = bounds.width().toFloat() / intrinsicWidth
          val scaleY = bounds.height().toFloat() / intrinsicHeight
          scale(scaleX, scaleY)
          drawPicture(picture)
        }
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
    // TODO(#4246): Fix both SVG rendering performance and upscaling to ensure images aren't blurry.
    intrinsicSize = scalableVectorGraphic.computeSizeSpecs(textPaint)
    if (scalableVectorGraphic.shouldBeRenderedAsBitmap()) {
      recomputeBitmap()
    }
  }

  private fun recomputeBitmap() {
    bitmap = picture?.let { picture ->
      val renderedPictureBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, ARGB_8888)
      val canvas = Canvas(renderedPictureBitmap)
      canvas.drawPicture(picture)
      return@let transformBitmap(renderedPictureBitmap)
    }
  }

  private fun transformBitmap(bitmap: Bitmap): Bitmap {
    var transformedBitmap = bitmap
    for (imageTransformation in scalableVectorGraphic.transformations) {
      transformedBitmap = transformBitmap(transformedBitmap, imageTransformation)
    }
    return transformedBitmap
  }

  private fun transformBitmap(bitmap: Bitmap, imageTransformation: ImageTransformation): Bitmap {
    return when (imageTransformation) {
      ImageTransformation.BLUR -> bitmapBlurrer.blur(bitmap)
    }
  }
}

private fun ScalableVectorGraphic.shouldBeRenderedAsBitmap() =
  hasTransformations() || isUsingAndroidSdkWithSvgRenderingIssues()

private fun ScalableVectorGraphic.hasTransformations() = transformations.isNotEmpty()

// TODO(#3961): Remove this & instead rely on native SVG rendering for older SDK versions.
// See #3938 for context on why these OS versions are being forced to bitmap rendering.
@SuppressLint("ObsoleteSdkInt") // Incorrect warning.
private fun isUsingAndroidSdkWithSvgRenderingIssues() =
  Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
