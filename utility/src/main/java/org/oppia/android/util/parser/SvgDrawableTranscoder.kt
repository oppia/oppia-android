package org.oppia.android.util.parser

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/** SvgDrawableTranscoder converts SVG to PictureDrawable. */
class SvgDrawableTranscoder(
  private val context: Context
) : ResourceTranscoder<OppiaSvg?, BlockPictureDrawable?> {
  override fun transcode(
    toTranscode: Resource<OppiaSvg?>,
    options: Options
  ): Resource<BlockPictureDrawable?> =
    SimpleResource(BlockPictureDrawable(context, toTranscode.get()))
}
