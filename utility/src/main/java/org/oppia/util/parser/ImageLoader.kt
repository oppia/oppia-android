package org.oppia.util.parser

import android.graphics.Bitmap

/** Loads an image from the provided URL into the specified target, optionally caching it. */
interface ImageLoader {

  fun load(imageUrl: String, target: CustomTarget<Bitmap>)
}
