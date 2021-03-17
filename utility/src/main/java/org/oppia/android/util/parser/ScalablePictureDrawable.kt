package org.oppia.android.util.parser

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint

// TODO: specialize this for text.
class ScalablePictureDrawable(private val oppiaSvg: OppiaSvg) : Drawable() {
  private lateinit var textPaint: TextPaint
  private lateinit var renderedPicture: Picture

  fun computeIntrinsicSize(): OppiaSvg.SvgSizeSpecs = oppiaSvg.computeSizeSpecs(textPaint)

  fun initialize(textPaint: TextPaint) {
    this.textPaint = textPaint
    renderedPicture = oppiaSvg.renderToPicture(textPaint)
  }

  override fun draw(canvas: Canvas) {
    if (this::renderedPicture.isInitialized) {
      canvas.apply {
        // Set new clip bounds/translation to prepare to draw the image. This will be replaced with
        // the current bounds/translation after the picture is drawn.
        save()
        bounds.apply {
          clipRect(this)
          translate(left.toFloat(), top.toFloat())
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
}
