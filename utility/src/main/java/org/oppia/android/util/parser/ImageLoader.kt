package org.oppia.android.util.parser

/** Loads an image from the provided URL into the specified target, optionally caching it. */
interface ImageLoader {

  /**
   * Loads an oppiaImage at the specified [imageUrl] into the specified [target]. Note that this is an
   * asynchronous operation, and may take a while if the image needs to be downloaded from the
   * internet.
   */
  fun loadOppiaImage(imageUrl: String, target: ImageTarget<OppiaImage>)
}
