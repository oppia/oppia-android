package org.oppia.android.util.parser

import android.text.TextPaint

class TextPictureDrawable internal constructor(oppiaSvg: OppiaSvg) : SvgPictureDrawable(oppiaSvg) {
  fun initialize(textPaint: TextPaint) {
    computeTextPicture(textPaint)
  }
}
