package org.oppia.util.parser

import android.content.Context
import android.graphics.drawable.PictureDrawable
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
//  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//    registry.prepend(ImageAssetFetcher::class.java, InputStream::class.java, RepositoryModelLoader.Factory())
//  }

  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
      .prepend(SVG::class.java, SvgEncoder())
      .append(InputStream::class.java, SVG::class.java, SvgDecoder())
  }
}
