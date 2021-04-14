package org.oppia.android.util.parser

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/** Transcoder for [OppiaSvg]s to [BlockPictureDrawable]s. */
class BlockSvgDrawableTranscoder(
  private val context: Context
) : ResourceTranscoder<OppiaSvg?, BlockPictureDrawable?> {
  override fun transcode(
    toTranscode: Resource<OppiaSvg?>,
    options: Options
  ): Resource<BlockPictureDrawable?> =
    SimpleResource(BlockPictureDrawable(context, toTranscode.get()))
}
