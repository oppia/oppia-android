package org.oppia.android.util.parser

import android.graphics.drawable.PictureDrawable
import android.text.TextPaint

class TextPictureDrawable(
  private val oppiaSvg: OppiaSvg
) : PictureDrawable(/* picture= */ null) {
  private lateinit var textPaint: TextPaint

  fun computeIntrinsicSize(): OppiaSvg.SvgSizeSpecs = oppiaSvg.computeSizeSpecs(textPaint)

  fun initialize(textPaint: TextPaint) {
    this.textPaint = textPaint
    this.picture = oppiaSvg.renderToTextPicture(textPaint)
  }
}
