package org.oppia.android.util.parser

import android.graphics.drawable.PictureDrawable

class BlockPictureDrawable(oppiaSvg: OppiaSvg) : PictureDrawable(oppiaSvg.renderToBlockPicture())
