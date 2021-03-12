package org.oppia.android.util.parser

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint

// TODO: specialize this for text.
class ScalablePictureDrawable(private val oppiaSvg: OppiaSvg) : Drawable() {
  private lateinit var textPaint: TextPaint
  private lateinit var renderedPicture: Picture

  fun computeIntrinsicSize(): IntrinsicSize {
    return oppiaSvg.computeSize(textPaint)?.let {
      IntrinsicSize(it.width, it.height)
    } ?: IntrinsicSize(intrinsicWidth.toFloat(), intrinsicHeight.toFloat())
  }

  fun initialize(textPaint: TextPaint) {
    this.textPaint = textPaint
//    renderedPicture = oppiaSvg.renderToPicture(textPaint)
  }

  override fun draw(canvas: Canvas) {
    if (this::renderedPicture.isInitialized) {
      canvas.apply {
        // Set new clip bounds/translation to prepare to draw the image. This will be replaced with
        // the current bounds/translation after the picture is drawn.
        save()
        computeBounds().apply {
          clipRect(this)
          translate(left, top)
        }
        drawPicture(renderedPicture)
        restore()
      }
    }
  }

  override fun setAlpha(alpha: Int) {
    // Unsupported.
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    // Unsupported.
  }

  // Unsupported.
  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  private fun computeBounds(): RectF {
    // TODO: factor in vertical alignment if needed.
    return RectF(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat())
//    return computeIntrinsicSize().let {
//      RectF(bounds.left.toFloat(), bounds.top.toFloat(), it.width, it.height)
//    }
  }
}

data class IntrinsicSize(val width: Float, val height: Float)
