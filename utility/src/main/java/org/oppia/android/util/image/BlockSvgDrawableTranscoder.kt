package org.oppia.android.util.image

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/** Transcoder for [ScalableVectorGraphic]s to [BlockPictureDrawable]s. */
class BlockSvgDrawableTranscoder(
  private val context: Context
) : ResourceTranscoder<ScalableVectorGraphic?, BlockPictureDrawable?> {
  override fun transcode(
    toTranscode: Resource<ScalableVectorGraphic?>,
    options: Options
  ): Resource<BlockPictureDrawable?> =
    SimpleResource(BlockPictureDrawable(context, toTranscode.get()))
}
