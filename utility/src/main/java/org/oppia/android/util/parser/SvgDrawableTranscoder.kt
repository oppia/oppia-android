package org.oppia.android.util.parser

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/** SvgDrawableTranscoder converts SVG to PictureDrawable. */
class SvgDrawableTranscoder : ResourceTranscoder<OppiaSvg?, ScalablePictureDrawable?> {
  override fun transcode(
    toTranscode: Resource<OppiaSvg?>,
    options: Options
  ): Resource<ScalablePictureDrawable?> = SimpleResource(ScalablePictureDrawable(toTranscode.get()))
}
