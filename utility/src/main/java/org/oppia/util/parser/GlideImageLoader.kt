package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import org.oppia.util.caching.AssetRepository
import org.oppia.util.caching.CacheAssetsLocally
import javax.inject.Inject

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
//    Glide.with(context)
//      .asBitmap()
//      .load(model)
//      .into(target)

    val requestBuilder: RequestBuilder<Bitmap?> = GlideApp.with(context)
      .`as`(Bitmap::class.java)
      .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
      .listener(SvgSoftwareLayerSetter())

    val uri: Uri = Uri.parse("https://cdn.shopify.com/s/files/1/0496/1029/files/Freesample.svg")
    requestBuilder.load(uri)
      .disallowHardwareConfig()
      .into(target)
  }
}
