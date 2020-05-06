package org.oppia.util.parser

import android.content.Context
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import java.io.InputStream
import org.oppia.util.caching.AssetRepository

/** Custom [AppGlideModule] to enable loading images from [AssetRepository] via Glide. */
@GlideModule
class RepositoryGlideModule : AppGlideModule() {
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    // TODO #1039: Introduce custom type OppiaImage for rendering Bitmap and Svg.
    registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
      .prepend(SVG::class.java, SvgEncoder())
      .append(InputStream::class.java, SVG::class.java, SvgDecoder())
      .append(ImageAssetFetcher::class.java, InputStream::class.java, RepositoryModelLoader.Factory())
  }
}
