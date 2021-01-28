package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Transformation
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

  override fun loadBitmap(
    imageUrl: String,
    target: ImageTarget<Bitmap>,
    transformations: List<ImageTransformation>
  ) {
    val model: Any = if (cacheAssetsLocally) {
      object : ImageAssetFetcher {
        override fun fetchImage(): ByteArray = assetRepository.loadRemoteBinaryAsset(imageUrl)()

        override fun getImageIdentifier(): String = imageUrl
      }
    } else imageUrl

    Glide.with(context)
      .asBitmap()
      .load(model)
      .transform(*transformations.toGlideTransformations())
      .intoTarget(target)
  }

  override fun loadSvg(
    imageUrl: String,
    target: ImageTarget<PictureDrawable>,
    transformations: List<ImageTransformation>
  ) {
    val model: Any = if (cacheAssetsLocally) {
      object : ImageAssetFetcher {
        override fun fetchImage(): ByteArray = assetRepository.loadRemoteBinaryAsset(imageUrl)()

        override fun getImageIdentifier(): String = imageUrl
      }
    } else imageUrl

    // TODO(#45): Ensure the image caching flow is properly hooked up.
    Glide.with(context)
      .`as`(PictureDrawable::class.java)
      .fitCenter()
      .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
      .load(model)
      .transform(*transformations.toGlideTransformations())
      .intoTarget(target)
  }

  override fun loadDrawable(
    imageDrawableResId: Int,
    target: ImageTarget<Drawable>,
    transformations: List<ImageTransformation>
  ) {
    Glide.with(context)
      .asDrawable()
      .load(imageDrawableResId)
      .transform(*transformations.toGlideTransformations())
      .intoTarget(target)
  }

  private fun <T> RequestBuilder<T>.intoTarget(target: ImageTarget<T>) {
    when (target) {
      is CustomImageTarget -> into(target.customTarget)
      is ImageViewTarget -> into(target.imageView)
    }
  }

  private fun List<ImageTransformation>.toGlideTransformations(): Array<Transformation<Bitmap>> {
    return map {
      when (it) {
        ImageTransformation.BLUR -> BlurTransformation(context)
      }
    }.toTypedArray()
  }
}
