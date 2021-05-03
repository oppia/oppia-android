package org.oppia.android.util.parser

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import org.oppia.android.util.caching.AssetRepository
import java.io.InputStream

/** Custom [AppGlideModule] to enable loading images from [AssetRepository] via Glide. */
@GlideModule
class RepositoryGlideModule : AppGlideModule() {
  override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
    // TODO(#1039): Introduce custom type OppiaImage for rendering Bitmap and Svg.
    registry.register(
      ScalableVectorGraphic::class.java,
      TextPictureDrawable::class.java,
      TextSvgDrawableTranscoder(context)
    )
    registry.register(
      ScalableVectorGraphic::class.java,
      BlockPictureDrawable::class.java,
      BlockSvgDrawableTranscoder(context)
    )

    registry.append(InputStream::class.java, ScalableVectorGraphic::class.java, SvgDecoder())
    registry.append(
      ImageAssetFetcher::class.java,
      InputStream::class.java,
      RepositoryModelLoader.Factory()
    )
  }
}
