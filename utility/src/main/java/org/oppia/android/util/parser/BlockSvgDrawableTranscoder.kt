package org.oppia.android.util.parser

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/** Transcoder for [ScalableVectorGraphic]s to [BlockPictureDrawable]s. */
class BlockSvgDrawableTranscoder :
  ResourceTranscoder<ScalableVectorGraphic?, BlockPictureDrawable?> {
  override fun transcode(
    toTranscode: Resource<ScalableVectorGraphic?>,
    options: Options
  ): Resource<BlockPictureDrawable?> = SimpleResource(BlockPictureDrawable(toTranscode.get()))
}
