package org.oppia.util.parser

import android.graphics.Bitmap
import com.bumptech.glide.request.target.CustomTarget

/** Loads an image from the provided URL into the specified target, optionally caching it. */
interface ImageLoader {

  fun load(imageUrl: String, target: CustomTarget<Bitmap>)
}
