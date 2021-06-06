package org.oppia.android.util.image

import android.content.Context
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/** Transcoder for [ScalableVectorGraphic]s to [TextPictureDrawable]s. */
class TextSvgDrawableTranscoder(
  private val context: Context
) : ResourceTranscoder<ScalableVectorGraphic?, TextPictureDrawable?> {
  override fun transcode(
    toTranscode: Resource<ScalableVectorGraphic?>,
    options: Options
  ): Resource<TextPictureDrawable?> =
    SimpleResource(TextPictureDrawable(context, toTranscode.get()))
}
