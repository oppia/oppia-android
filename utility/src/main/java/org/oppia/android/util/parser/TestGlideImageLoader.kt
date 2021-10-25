package org.oppia.android.util.parser

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import javax.inject.Inject

/**
 * [TestGlideImageLoader] is designed to be used in tests. It uses real [GlideImageLoader]
 * except for [loadDrawable]. [loadDrawable] function is overridden to work with drawable matchers
 * in unit tests.
 */
class TestGlideImageLoader @Inject constructor(
  private val glideImageLoader: GlideImageLoader
) : ImageLoader {

  override fun loadBitmap(
    imageUrl: String,
    target: ImageTarget<Bitmap>,
    transformations: List<ImageTransformation>
  ) = glideImageLoader.loadBitmap(imageUrl, target, transformations)

  override fun loadSvg(
    imageUrl: String,
    target: ImageTarget<PictureDrawable>,
    transformations: List<ImageTransformation>
  ) = glideImageLoader.loadSvg(imageUrl, target, transformations)

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
}
