package org.oppia.android.util.parser.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadImagesFromAssets
import org.oppia.android.util.parser.svg.BlockPictureDrawable
import org.oppia.android.util.parser.svg.ScalableVectorGraphic
import org.oppia.android.util.parser.svg.SvgBlurTransformation
import org.oppia.android.util.parser.svg.SvgDecoder
import org.oppia.android.util.parser.svg.SvgPictureDrawable
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.parser.math.MathModel

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
    target: ImageTarget<BlockPictureDrawable>,
    transformations: List<ImageTransformation>
  ) = loadSvgWithGlide(imageUrl, target, transformations)

  override fun loadTextSvg(
    imageUrl: String,
    target: ImageTarget<TextPictureDrawable>,
    transformations: List<ImageTransformation>
  ) = loadSvgWithGlide(imageUrl, target, transformations)

  override fun loadDrawable(
    imageDrawableResId: Int,
    target: ImageTarget<Drawable>,
    transformations: List<ImageTransformation>
  ) {
    // TODO(#3887): Investigate why this has a native crash on KitKat & find a fix.
    glide
      .asDrawable()
      .load(imageDrawableResId)
      .transform(*transformations.toBitmapGlideTransformations().toTypedArray())
      .intoTarget(target)
  }

  override fun loadMathDrawable(
    rawLatex: String,
    lineHeight: Float,
    useInlineRendering: Boolean,
    target: ImageTarget<Bitmap>
  ) {
    glide
      .asBitmap()
      .load(MathModel(rawLatex, lineHeight, useInlineRendering))
      .intoTarget(target)
  }

  private inline fun <reified T : SvgPictureDrawable> loadSvgWithGlide(
    imageUrl: String,
    target: ImageTarget<T>,
    transformations: List<ImageTransformation>
  ) {
    // TODO(#45): Ensure the image caching flow is properly hooked up.
    glide
      .`as`(T::class.java)
      .fitCenter()
      .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
      .apply(SvgDecoder.createLoadSvgFromPipelineOption())
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
    transformations: List<Transformation<ScalableVectorGraphic>>
  ): RequestBuilder<T> {
    transformations.forEach { transform(ScalableVectorGraphic::class.java, it) }
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
    List<Transformation<ScalableVectorGraphic>> {
      return map {
        when (it) {
          ImageTransformation.BLUR -> pictureBitmapBlurTransformation
        }
      }
    }
}
