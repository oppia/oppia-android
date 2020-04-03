package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
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
}
