package org.oppia.android.util.parser

class BlockPictureDrawable internal constructor(oppiaSvg: OppiaSvg) : SvgPictureDrawable(oppiaSvg) {
  init {
    computeBlockPicture()
  }
}
