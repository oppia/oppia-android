package org.oppia.android.util.parser

import android.widget.ImageView
import com.bumptech.glide.request.target.CustomTarget

/**
 * Represents a target that can receive a loaded Glide image. The type must correspond to either
 * bitmaps or vector drawables as required by [ImageLoader].
 */
sealed class ImageTarget<T>

/** A type of [ImageTarget] that has a [CustomTarget] set up to process the loaded image. */
data class CustomImageTarget<T>(val customTarget: CustomTarget<T>) : ImageTarget<T>()

/** A type of [ImageTarget] that loads images into an [ImageView]. */
data class ImageViewTarget<T>(val imageView: ImageView) : ImageTarget<T>()
