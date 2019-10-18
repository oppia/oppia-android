package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget

class GlideImageLoader : ImageLoader {
  override fun load(context: Context, path: String, target: SimpleTarget<Bitmap>) {
    Glide.with(context)
      .asBitmap()
      .load(path)
      .into(target)
  }
}
