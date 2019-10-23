package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import javax.inject.Inject

/** An [ImageLoader] that uses Glide. */
class GlideImageLoader @Inject constructor(private val context: Context) : ImageLoader {
  override fun load(imageUrl: String, target: SimpleTarget<Bitmap>) {
    Glide.with(context)
      .asBitmap()
      .load(imageUrl)
      .into(target)
  }
}
