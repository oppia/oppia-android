package org.oppia.util.parser

import android.content.Context
import android.graphics.Picture
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import org.oppia.util.caching.AssetRepository
import java.io.InputStream

/** Custom [AppGlideModule] to enable loading images from [AssetRepository] via Glide. */
@GlideModule
class RepositoryGlideModule : AppGlideModule() {
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    // TODO(#1039): Introduce custom type OppiaImage for rendering Bitmap and Svg.
    registry.register(SVG::class.java, Picture::class.java, SvgDrawableTranscoder())
      .append(InputStream::class.java, SVG::class.java, SvgDecoder())
      .append(
        ImageAssetFetcher::class.java, InputStream::class.java, RepositoryModelLoader.Factory()
      )
  }
}
