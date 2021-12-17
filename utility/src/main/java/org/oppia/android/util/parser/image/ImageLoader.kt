package org.oppia.android.util.parser.image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import org.oppia.android.util.parser.svg.BlockPictureDrawable

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
  fun loadBlockSvg(
    imageUrl: String,
    target: ImageTarget<BlockPictureDrawable>,
    transformations: List<ImageTransformation> = listOf()
  )

  /**
   * Same as [loadBlockSvg] except this specifically loads a [TextPictureDrawable] which can be rendered
   * in-line with text.
   */
  fun loadTextSvg(
    imageUrl: String,
    target: ImageTarget<TextPictureDrawable>,
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
