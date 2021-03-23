package org.oppia.android.util.parser

import android.content.Context
import android.text.TextPaint

class TextPictureDrawable internal constructor(
  context: Context,
  oppiaSvg: OppiaSvg
) : SvgPictureDrawable(context, oppiaSvg) {
  fun initialize(textPaint: TextPaint) {
    computeTextPicture(textPaint)
  }
}
