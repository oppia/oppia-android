package org.oppia.android.util.parser

import android.content.Context
import android.graphics.drawable.Drawable
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.CacheAssetsLocally
import javax.inject.Inject

/** A [TestGlideImageLoader] that is used in tests. */
class TestGlideImageLoader @Inject constructor(
  context: Context,
  @CacheAssetsLocally private val cacheAssetsLocally: Boolean,
  assetRepository: AssetRepository
) : GlideImageLoader(context, cacheAssetsLocally, assetRepository) {

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
