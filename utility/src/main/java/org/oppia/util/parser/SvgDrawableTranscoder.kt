package org.oppia.util.parser

import android.graphics.Picture
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.caverock.androidsvg.SVG

/** SvgDrawableTranscoder converts SVG to PictureDrawable. */
class SvgDrawableTranscoder : ResourceTranscoder<SVG?, Picture?> {
  override fun transcode(
    toTranscode: Resource<SVG?>,
    options: Options
  ): Resource<Picture?>? {
    val svg: SVG = toTranscode.get()
    val picture = svg.renderToPicture()
    return SimpleResource(picture)
  }
}
