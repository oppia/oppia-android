package org.oppia.android.util.parser

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import androidx.annotation.DrawableRes

/** Represents transformations for images loaded using [ImageLoader]. */
enum class ImageTransformation {
  /** Represents Blur Transformation on an [ImageTarget]. */
  BLUR
}

/** Loads an image from the provided URL into the specified target, optionally caching it. */
interface ImageLoader {
  /**
   * Loads a bitmap at the specified [imageUrl] into the specified [target]. Note that this is an
   * asynchronous operation, and may take a while if the image needs to be downloaded from the
   * internet. Optional [transformations] may be applied to the image.
   */
  fun loadBitmap(
    imageUrl: String,
    target: ImageTarget<Bitmap>,
    transformations: List<ImageTransformation> = listOf()
  )

  /**
   * Loads a vector drawable at the specified [imageUrl] into the specified [target]. Note that this
   * is an asynchronous operation, and may take a while if the image needs to be downloaded from the
   * internet. Optional [transformations] may be applied to the image.
   */
  fun loadSvg(
    imageUrl: String,
    target: ImageTarget<PictureDrawable>,
    transformations: List<ImageTransformation> = listOf()
  )

  /**
   * Loads the specified [imageDrawable] resource into the specified [target].
   * Optional [transformations] may be applied to the image.
   */
  fun loadDrawable(
    @DrawableRes imageDrawableResId: Int,
    target: ImageTarget<Drawable>,
    transformations: List<ImageTransformation> = listOf()
  )
}
