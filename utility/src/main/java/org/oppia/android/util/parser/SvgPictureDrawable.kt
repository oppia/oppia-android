package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Picture
import android.text.TextPaint

// TODO: combine with TransformablePictureDrawable?
open class SvgPictureDrawable(
  context: Context,
  private val oppiaSvg: OppiaSvg
) : TransformablePictureDrawable() {
  private val bitmapBlurrer by lazy { BitmapBlurrer(context) }

  /**
   * The [Paint] that should be used when rendering this drawable. Note that the flags are based
   * on the defaults set by Android's BitmapDrawable.
   */
  private val bitmapPaint by lazy { Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG) }

  private var picture: Picture? = null
  private var bitmap: Bitmap? = null
  private var intrinsicSize = IntrinsicSize(width = -1f, height = -1f)

  override fun draw(canvas: Canvas) {
    // The rendering approach here is loosely based on Android's PictureDrawable.
    canvas.apply {
      // Save current transformation state.
      save()

      if (oppiaSvg.hasTransformations()) {
        bitmap?.let { bitmap ->
          drawBitmap(bitmap, /* src= */ null, bounds, bitmapPaint)
        }
      } else {
        picture?.let { picture ->
          // Apply the picture's bounds so that it's positioned/clipped correctly.
          bounds.apply {
            clipRect(this)
            translate(left.toFloat(), top.toFloat())
          }

          drawPicture(picture)
        }
      }

      // Restore previous transformation state.
      restore()
    }
  }

  override fun getPicture(): Picture? = picture

  // TODO: consider delegating initialization to the child class to better utilize inheritance.
  override fun computeBlockPicture() {
    picture = oppiaSvg.renderToBlockPicture()
    recomputeIntrinsicSize { oppiaSvg.computeSizeSpecs(textPaint = null) }
    if (oppiaSvg.hasTransformations()) recomputeBitmap()
  }

  override fun computeTextPicture(textPaint: TextPaint) {
    picture = oppiaSvg.renderToTextPicture(textPaint)
    recomputeIntrinsicSize { oppiaSvg.computeSizeSpecs(textPaint) }
    if (oppiaSvg.hasTransformations()) recomputeBitmap()
  }

  override fun getIntrinsicSize(): IntrinsicSize = intrinsicSize

  private fun recomputeIntrinsicSize(computeSizeSpecs: () -> OppiaSvg.SvgSizeSpecs) {
    val (width, height, verticalAlignment) = computeSizeSpecs()
    intrinsicSize = IntrinsicSize(width, height, verticalAlignment)
  }

  private fun recomputeBitmap() {
    bitmap = picture?.let { picture ->
      val renderedPictureBitmap =
        Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(renderedPictureBitmap)
      canvas.drawPicture(picture)
      return@let transformBitmap(renderedPictureBitmap)
    }
  }

  private fun transformBitmap(bitmap: Bitmap): Bitmap {
    var transformedBitmap = bitmap
    for (imageTransformation in oppiaSvg.transformations) {
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

private fun OppiaSvg.hasTransformations() = transformations.isNotEmpty()
