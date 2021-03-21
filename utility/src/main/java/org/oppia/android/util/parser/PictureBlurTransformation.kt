package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Picture
import android.text.TextPaint
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import java.security.MessageDigest

class PictureBlurTransformation(context: Context): Transformation<TransformablePictureDrawable> {
  private val bitmapBlurrer by lazy { BitmapBlurrer(context) }
  private companion object {
    // See: https://bumptech.github.io/glide/doc/transformations.html#required-methods.
    private val ID = PictureBlurTransformation::class.java.name
  }

  override fun transform(
    context: Context,
    toTransform: Resource<TransformablePictureDrawable>,
    outWidth: Int,
    outHeight: Int
  ): Resource<TransformablePictureDrawable> {
    return SimpleResource(BlurredPictureDrawable(toTransform.get(), bitmapBlurrer))
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    messageDigest.update(ID.toByteArray())
  }

  override fun hashCode(): Int = ID.hashCode()

  override fun equals(other: Any?): Boolean = other is PictureBlurTransformation

  private class BlurredPictureDrawable(
    private val baseDrawable: TransformablePictureDrawable,
    private val bitmapBlurrer: BitmapBlurrer
  ): TransformablePictureDrawable() {
    /**
     * The [Paint] that should be used when rendering this drawable. Note that the flags are based
     * on the defaults set by Android's BitmapDrawable.
     */
    private val paint by lazy { Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG) }

    private var bitmap: Bitmap? = null

    init {
      // Recompute the bitmap if a picture is initialized.
      recomputeBitmap()
    }

    override fun draw(canvas: Canvas) {
      // Based on SvgPictureDrawable's draw procedure.
      bitmap?.let { bitmap ->
        canvas.apply {
          // Save current transformation state.
          save()

          // Apply the picture's bounds so that it's positioned/clipped correctly.
          bounds.apply {
            clipRect(this)
            translate(left.toFloat(), top.toFloat())
          }

          drawBitmap(bitmap, /* src= */ null, bounds, paint)

          // Restore previous transformation state.
          restore()
        }
      }
    }

    override fun getPicture(): Picture? = baseDrawable.getPicture()

    override fun computeBlockPicture() {
      baseDrawable.computeBlockPicture()
      recomputeBitmap()
    }

    override fun computeTextPicture(textPaint: TextPaint) {
      baseDrawable.computeTextPicture(textPaint)
      recomputeBitmap()
    }

    override fun getIntrinsicSize(): IntrinsicSize = baseDrawable.getIntrinsicSize()

    private fun recomputeBitmap() {
      bitmap = getPicture()?.let { picture ->
        val renderedPictureBitmap =
          Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(renderedPictureBitmap)
        canvas.drawPicture(picture)
        return@let bitmapBlurrer.blur(renderedPictureBitmap)
      }
    }
  }
}
