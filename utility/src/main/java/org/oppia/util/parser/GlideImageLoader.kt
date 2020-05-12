package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import javax.inject.Inject
import org.oppia.util.caching.AssetRepository
import org.oppia.util.caching.CacheAssetsLocally

/** An [ImageLoader] that uses Glide. */
class GlideImageLoader @Inject constructor(
  private val context: Context,
  @CacheAssetsLocally private val cacheAssetsLocally: Boolean,
  private val assetRepository: AssetRepository
) : ImageLoader {

  override fun load(imageUrl: String, target: CustomTarget<Bitmap>) {
    val model: Any = if (cacheAssetsLocally) {
      object : ImageAssetFetcher {
        override fun fetchImage(): ByteArray = assetRepository.loadRemoteBinaryAsset(imageUrl)()

        override fun getImageIdentifier(): String = imageUrl
      }
    } else imageUrl
    Glide.with(context)
      .asBitmap()
      .load(model)
      .into(target)
  }

  override fun loadSvg(imageUrl: String, target: CustomTarget<Picture>) {
    val model: Any = if (cacheAssetsLocally) {
      object : ImageAssetFetcher {
        override fun fetchImage(): ByteArray = assetRepository.loadRemoteBinaryAsset(imageUrl)()

        override fun getImageIdentifier(): String = imageUrl
      }
    } else imageUrl

    // TODO(#45): Ensure the image caching flow is properly hooked up.
    Glide.with(context)
      .`as`(Picture::class.java)
      .fitCenter()
      .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
      .load(model)
      .into(target)
  }
}
