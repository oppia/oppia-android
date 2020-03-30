package org.oppia.util.parser

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream
import org.oppia.util.caching.AssetRepository

/** Custom [AppGlideModule] to enable loading images from [AssetRepository] via Glide. */
@GlideModule
class RepositoryGlideModule : AppGlideModule() {
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    registry.prepend(ImageAssetFetcher::class.java, InputStream::class.java, RepositoryModelLoader.Factory())
  }
}
