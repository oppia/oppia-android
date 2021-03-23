package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Canvas
import android.graphics.Picture
import android.text.TextPaint

// TODO: combine with TransformablePictureDrawable?
open class SvgPictureDrawable(
  context: Context,
  private val oppiaSvg: OppiaSvg
) : TransformablePictureDrawable() {
  private var picture: Picture? = null
  private var intrinsicSize = IntrinsicSize(width = -1f, height = -1f)

  override fun draw(canvas: Canvas) {
    // The rendering approach here is loosely based on Android's PictureDrawable.
    canvas.apply {
      // Save current transformation state.
      save()

      picture?.let { picture ->
        // Apply the picture's bounds so that it's positioned/clipped correctly.
        bounds.apply {
          clipRect(this)
          translate(left.toFloat(), top.toFloat())
        }

        drawPicture(picture)
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
  }

  override fun computeTextPicture(textPaint: TextPaint) {
    picture = oppiaSvg.renderToTextPicture(textPaint)
    recomputeIntrinsicSize { oppiaSvg.computeSizeSpecs(textPaint) }
  }

  override fun getIntrinsicSize(): IntrinsicSize = intrinsicSize

  private fun recomputeIntrinsicSize(computeSizeSpecs: () -> OppiaSvg.SvgSizeSpecs) {
    val (width, height, verticalAlignment) = computeSizeSpecs()
    intrinsicSize = IntrinsicSize(width, height, verticalAlignment)
  }
}
