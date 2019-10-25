package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import androidx.annotation.DrawableRes
import com.bumptech.glide.request.target.CustomTarget

/** Loads an image from the provided URL into the specified target, optionally caching it. */
interface ImageLoader {

  fun load(imageUrl: String, target: CustomTarget<Bitmap>)
}
