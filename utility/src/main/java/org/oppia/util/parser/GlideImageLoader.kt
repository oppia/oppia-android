package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import javax.inject.Inject

/** An [ImageLoader] that uses Glide. */
class GlideImageLoader @Inject constructor(private val context: Context) : ImageLoader {
  override fun load(imageUrl: String, target: CustomTarget<Bitmap>) {
    Glide.with(context)
      .asBitmap()
      .load(imageUrl)
      .into(target)
  }
}
