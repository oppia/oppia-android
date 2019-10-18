package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget

interface ImageLoader {
  companion object {

    fun load(context: Context, path: String, placeholder: Int, target: SimpleTarget<Bitmap>) {
      Glide.with(context)
        .asBitmap()
        .load(path)
        .placeholder(placeholder)
        .into(target)
    }
  }
}
