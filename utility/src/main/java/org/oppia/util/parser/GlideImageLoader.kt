package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.RequestBuilder
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
      .listener(SvgSoftwareLayerSetter())

    val uri: Uri = Uri.parse("https://drive.google.com/a/google.com/file/d/1wqnklWExa7t926Yv2KRBG5q64Vv__X8m/view?usp=sharing")
    requestBuilder.load(uri).into(target)
  }
}
