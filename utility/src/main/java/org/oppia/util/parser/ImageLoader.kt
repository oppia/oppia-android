package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import androidx.annotation.DrawableRes

/** Helper that handles loading and caching images from remote URLs.*/
interface ImageLoader {

  fun load(context: Context, path: String, target: SimpleTarget<Bitmap>)
}
