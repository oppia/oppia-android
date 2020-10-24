package org.oppia.android.util.parser

import android.graphics.Bitmap
import android.graphics.drawable.PictureDrawable

/** Loads an image from the provided URL into the specified target, optionally caching it. */
interface ImageLoader {

  /**
   * Loads a bitmap at the specified [imageUrl] into the specified [target]. Note that this is an
   * asynchronous operation, and may take a while if the image needs to be downloaded from the
   * internet.
   */
  fun loadBitmap(imageUrl: String, target: ImageTarget<Bitmap>)

  /**
   * Loads a vector drawable at the specified [imageUrl] into the specified [target]. Note that this
   * is an asynchronous operation, and may take a while if the image needs to be downloaded from the
   * internet.
   */
  fun loadSvg(imageUrl: String, target: ImageTarget<PictureDrawable>)
}
