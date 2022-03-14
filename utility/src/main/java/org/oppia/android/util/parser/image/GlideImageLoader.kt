package org.oppia.android.util.parser.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.request.RequestOptions
import java.security.MessageDigest
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
      .transformWithAll(transformations.toPictureGlideTransformations(imageUrl))
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
    return transformations.fold(this) { builder, transformation ->
      builder.transform(ScalableVectorGraphic::class.java, transformation)
    }
  }

  private fun List<ImageTransformation>.toBitmapGlideTransformations():
    List<Transformation<Bitmap>> {
    return map {
      when (it) {
        ImageTransformation.BLUR -> bitmapBlurTransformation
      }
    }
  }

  private fun List<ImageTransformation>.toPictureGlideTransformations(imageUrl: String):
    List<Transformation<ScalableVectorGraphic>> {
    return map {
      when (it) {
        ImageTransformation.BLUR -> pictureBitmapBlurTransformation
      }
    } + UpdatePictureDrawableSize(imageUrl)
  }

  private class UpdatePictureDrawableSize(
    private val url: String
  ) : Transformation<ScalableVectorGraphic> {
    private val filename by lazy { Uri.parse(url).lastPathSegment }

    private val extractedWidth by lazy {
      filename?.let(WIDTH_REGEX::find)?.destructured?.component1()?.toIntOrNull()
    }
    private val extractedHeight by lazy {
      filename?.let(HEIGHT_REGEX::find)?.destructured?.component1()?.toIntOrNull()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
      messageDigest.update(ID.toByteArray())
      messageDigest.update(url.toByteArray())
    }

    override fun transform(
      context: Context,
      toTransform: Resource<ScalableVectorGraphic>,
      outWidth: Int,
      outHeight: Int
    ): Resource<ScalableVectorGraphic> {
      return SimpleResource(toTransform.get().also {
        it.initializeWithExtractedDimensions(extractedWidth, extractedHeight)
      })
    }

    private companion object {
      // See: https://bumptech.github.io/glide/doc/transformations.html#required-methods.
      private val ID = UpdatePictureDrawableSize::class.java.name

      private val WIDTH_REGEX by lazy { "width_(\\d+)".toRegex() }
      private val HEIGHT_REGEX by lazy { "height_(\\d+)".toRegex() }
    }
  }
}
