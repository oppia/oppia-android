package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import org.oppia.util.caching.AssetRepository
import javax.inject.Inject

/** An [ImageLoader] that uses Glide. */
class GlideImageLoader @Inject constructor(
  private val context: Context, private val assetRepository: AssetRepository
) : ImageLoader {
  override fun load(imageUrl: String, target: CustomTarget<Bitmap>) {
    Glide.with(context)
      .asBitmap()
      .load(object : ImageAssetFetcher {
        override fun fetchImage(): ByteArray = assetRepository.loadRemoteBinaryAsset(imageUrl)()

        override fun getImageIdentifier(): String = imageUrl
      })
      .into(target)
  }
}
