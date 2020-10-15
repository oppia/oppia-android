package org.oppia.android.util.parser

import android.content.Context
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.CacheAssetsLocally
import javax.inject.Inject

/** An [ImageLoader] that uses Glide. */
class GlideImageLoader @Inject constructor(
  private val context: Context,
  @CacheAssetsLocally private val cacheAssetsLocally: Boolean,
  private val assetRepository: AssetRepository
) : ImageLoader {

  override fun loadOppiaImage(imageUrl: String, target: ImageTarget<OppiaImage>) {
    val model: Any = if (cacheAssetsLocally) {
      object : ImageAssetFetcher {
        override fun fetchImage(): ByteArray = assetRepository.loadRemoteBinaryAsset(imageUrl)()

        override fun getImageIdentifier(): String = imageUrl
      }
    } else imageUrl

    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      // TODO(#45): Ensure the image caching flow is properly hooked up.
      Glide.with(context)
        .`as`(PictureDrawable::class.java)
        .fitCenter()
        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
        .load(model)
        .intoTarget(target)
    } else {
      Glide.with(context)
        .asBitmap()
        .load(model)
        .intoTarget(target)
    }
  }


  private fun <T> RequestBuilder<T>.intoTarget(target: ImageTarget<T>) {
    when (target) {
      is CustomImageTarget -> into(target.customTarget)
      is ImageViewTarget -> into(target.imageView)
    }
  }
}
