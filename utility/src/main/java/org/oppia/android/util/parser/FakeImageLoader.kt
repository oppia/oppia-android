package org.oppia.android.util.parser

import android.graphics.Bitmap
import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import javax.inject.Inject

/** A [FakeImageLoader] that is used in tests. */
class FakeImageLoader @Inject constructor() : ImageLoader {

  override fun loadBitmap(
    imageUrl: String,
    target: ImageTarget<Bitmap>,
    transformations: List<ImageTransformation>
  ) {
  }

  override fun loadSvg(
    imageUrl: String,
    target: ImageTarget<PictureDrawable>,
    transformations: List<ImageTransformation>
  ) {
  }

  override fun loadDrawable(
    imageDrawableResId: Int,
    imageView: ImageView,
    transformations: List<ImageTransformation>
  ) {
    imageView.setImageResource(imageDrawableResId)
  }
}
