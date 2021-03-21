package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadImagesFromAssets
import javax.inject.Inject
import javax.inject.Singleton

/** An [ImageLoader] that uses Glide. */
@Singleton
class GlideImageLoader @Inject constructor(
  context: Context,
  @CacheAssetsLocally private val cacheAssetsLocally: Boolean,
  @LoadImagesFromAssets private val loadImagesFromAssets: Boolean,
  private val assetRepository: AssetRepository
) : ImageLoader {
  private val glide by lazy { Glide.with(context) }
  private val bitmapBlurTransformation by lazy { BitmapBlurTransformation(context) }
  private val pictureBitmapBlurTransformation by lazy { SvgBlurTransformation() }

  override fun loadBitmap(
    imageUrl: String,
    target: ImageTarget<Bitmap>,
    transformations: List<ImageTransformation>
  ) {
    glide
      .asBitmap()
      .load(loadImage(imageUrl))
      .transform(*transformations.toBitmapGlideTransformations().toTypedArray())
      .intoTarget(target)
  }

  override fun loadBlockSvg(
    imageUrl: String,
    target: ImageTarget<TransformablePictureDrawable>,
    transformations: List<ImageTransformation>
  ) = loadSvgWithGlide<BlockPictureDrawable>(imageUrl, target.specialize(), transformations)

  override fun loadTextSvg(
    imageUrl: String,
    target: ImageTarget<TransformablePictureDrawable>,
    transformations: List<ImageTransformation>
  ) = loadSvgWithGlide<TextPictureDrawable>(imageUrl, target.specialize(), transformations)

  override fun loadDrawable(
    imageDrawableResId: Int,
    target: ImageTarget<Drawable>,
    transformations: List<ImageTransformation>
  ) {
    glide
      .asDrawable()
      .load(imageDrawableResId)
      .transform(*transformations.toBitmapGlideTransformations().toTypedArray())
      .intoTarget(target)
  }

  private inline fun <reified T: TransformablePictureDrawable> loadSvgWithGlide(
    imageUrl: String,
    target: ImageTarget<T>,
    transformations: List<ImageTransformation>
  ) {
    // TODO(#45): Ensure the image caching flow is properly hooked up.
    glide
      .`as`(T::class.java)
      .fitCenter()
      .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
      .apply(SvgDecoder.createLoadOppiaSvgOption())
      .load(loadImage(imageUrl))
      .transformWithAll(transformations.toPictureGlideTransformations())
      .intoTarget(target)
  }

  private fun loadImage(imageUrl: String): Any = when {
    cacheAssetsLocally -> object : ImageAssetFetcher {
      override fun fetchImage(): ByteArray = assetRepository.loadRemoteBinaryAsset(imageUrl)()

      override fun getImageIdentifier(): String = imageUrl
    }
    loadImagesFromAssets -> object : ImageAssetFetcher {
      override fun fetchImage(): ByteArray =
        assetRepository.loadImageAssetFromLocalAssets(imageUrl)()

      override fun getImageIdentifier(): String = imageUrl
    }
    else -> imageUrl
  }

  private fun <T> RequestBuilder<T>.intoTarget(target: ImageTarget<T>) = when (target) {
    is CustomImageTarget -> into(target.customTarget)
    is ImageViewTarget -> into(target.imageView)
  }

  private fun <T> RequestBuilder<T>.transformWithAll(
    transformations: List<Transformation<OppiaSvg>>
  ): RequestBuilder<T> {
    transformations.forEach { transform(OppiaSvg::class.java, it) }
    return this
  }

  private fun List<ImageTransformation>.toBitmapGlideTransformations():
    List<Transformation<Bitmap>> {
    return map {
      when (it) {
        ImageTransformation.BLUR -> bitmapBlurTransformation
      }
    }
  }

  private fun List<ImageTransformation>.toPictureGlideTransformations():
    List<Transformation<OppiaSvg>> {
    return map {
      when (it) {
        ImageTransformation.BLUR -> pictureBitmapBlurTransformation
      }
    }
  }

  // This function is needed since Glide requires strict typing to be kept throughout the request
  // options. However, we need to support swapping to a generic TransformablePictureDrawable in
  // order to support transformations. Only using the generic version doesn't let Glide properly
  // select between base classes (e.g. block & text variants), and only using the specialized type
  // leads to an unsafe cast calling into the final ImageTarget (though it does build).
  private fun <T: TransformablePictureDrawable>
    ImageTarget<TransformablePictureDrawable>.specialize(): ImageTarget<T> {
    return when (this) {
      is CustomImageTarget -> CustomImageTarget(ConvertibleCustomTarget(customTarget))
      // ImageViewTarget can be converted without issue since the type is the same.
      is ImageViewTarget -> ImageViewTarget(imageView)
    }
  }

  private class ConvertibleCustomTarget<T: TransformablePictureDrawable>(
    private val destinationTarget: CustomTarget<TransformablePictureDrawable>
  ): CustomTarget<T>() {
    override fun onResourceReady(resource: T, transition: Transition<in T>?) {
      check(transition == null) {
        "Safely converting transitions is not supported when loading SVGs"
      }
      destinationTarget.onResourceReady(resource, /* transition= */ null)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
      destinationTarget.onLoadCleared(placeholder)
    }

    override fun onStart() = destinationTarget.onStart()

    override fun onStop() = destinationTarget.onStop()

    override fun onDestroy() = destinationTarget.onDestroy()

    override fun onLoadStarted(placeholder: Drawable?) =
      destinationTarget.onLoadStarted(placeholder)

    override fun onLoadFailed(errorDrawable: Drawable?) =
      destinationTarget.onLoadFailed(errorDrawable)
  }
}
