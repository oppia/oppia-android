package org.oppia.android.util.parser.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import org.oppia.android.util.parser.math.MathModel
import org.oppia.android.util.parser.svg.BlockPictureDrawable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TestGlideImageLoader] is designed to be used in tests. It uses real [GlideImageLoader]
 * except for [loadDrawable]. [loadDrawable] function is overridden to work with drawable matchers
 * in unit tests.
 */
@Singleton
class TestGlideImageLoader @Inject constructor(
  private val glideImageLoader: GlideImageLoader,
  private val context: Context
) : ImageLoader {
  private val availableBitmaps = mutableMapOf<String, @DrawableRes Int>()
  private val loadedBitmaps = mutableListOf<String>()
  private val loadedBlockSvgs = mutableListOf<String>()
  private val loadedTextSvgs = mutableListOf<String>()
  private val loadedMathDrawables = mutableListOf<MathModel>()

  override fun loadBitmap(
    imageUrl: String,
    target: ImageTarget<Bitmap>,
    transformations: List<ImageTransformation>
  ) {
    loadedBitmaps += imageUrl
    val filename = imageUrl.substringAfterLast('/')
    if (filename in availableBitmaps) {
      check(target is ImageViewTarget) {
        "Only ImageViewTarget-type loads are supported to be overwritten in TestGlideImageLoader."
      }
      target.imageView.setImageResource(availableBitmaps.getValue(filename))
    } else glideImageLoader.loadBitmap(imageUrl, target, transformations)
  }

  override fun loadBlockSvg(
    imageUrl: String,
    target: ImageTarget<BlockPictureDrawable>,
    transformations: List<ImageTransformation>
  ) {
    loadedBlockSvgs += imageUrl
    glideImageLoader.loadBlockSvg(imageUrl, target, transformations)
  }

  override fun loadTextSvg(
    imageUrl: String,
    target: ImageTarget<TextPictureDrawable>,
    transformations: List<ImageTransformation>
  ) {
    loadedTextSvgs += imageUrl
    glideImageLoader.loadTextSvg(imageUrl, target, transformations)
  }

  /**
   * [loadDrawable] can be used in tests to match drawable ids:
   * `matches(withDrawable([imageDrawableResId]))`.
   *
   * Real [loadDrawable] in [GlideImageLoader] cannot be tested using such drawable matchers.
   */
  override fun loadDrawable(
    imageDrawableResId: Int,
    target: ImageTarget<Drawable>,
    transformations: List<ImageTransformation>
  ) {
    if (target is ImageViewTarget) {
      target.imageView.setImageResource(imageDrawableResId)
    }
  }

  override fun loadMathDrawable(
    rawLatex: String,
    lineHeight: Float,
    useInlineRendering: Boolean,
    target: ImageTarget<Bitmap>
  ) {
    loadedMathDrawables += MathModel(rawLatex, lineHeight, useInlineRendering)
    glideImageLoader.loadMathDrawable(rawLatex, lineHeight, useInlineRendering, target)
  }

  /**
   * Sets a test bitmap to load when [loadbitmap] is called, based on a specified filename.
   *
   * The image loaded will correspond to [imageDrawableResId] instead of being loaded from the
   * requested image URL.
   *
   * Subsequent calls to this method will override any previous arrangements. Multiple filenames may
   * point to the same drawable IDs. Referenced drawables do not actually need to be bitmaps (they
   * can be any types of drawable).
   */
  fun arrangeBitmap(filename: String, @DrawableRes imageDrawableResId: Int) {
    availableBitmaps[filename] = imageDrawableResId
  }

  /**
   * Returns the list of image URLs that have been loaded as bitmaps since the start of the
   * application.
   */
  fun getLoadedBitmaps(): List<String> = loadedBitmaps

  /**
   * Returns the list of image URLs that have been loaded as SVGs (in block format) since the start
   * of the application.
   */
  fun getLoadedBlockSvgs(): List<String> = loadedBlockSvgs

  /**
   * Returns the list of image URLs that have been loaded as SVGs (in inline text format) since the
   * start of the application.
   */
  fun getLoadedTextSvgs(): List<String> = loadedTextSvgs

  /** Returns the list of renderable math LaTeX [MathModel]s that have been loaded as drawables. */
  fun getLoadedMathDrawables(): List<MathModel> = loadedMathDrawables
}
