package org.oppia.android.util.parser

import android.content.Context

class BlockPictureDrawable internal constructor(
  context: Context,
  oppiaSvg: OppiaSvg
) : SvgPictureDrawable(context, oppiaSvg) {
  init {
    computeBlockPicture()
  }
}
