package org.oppia.util.parser

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * Listener which updates the [ImageView] to be software rendered, because [ ]/[Picture][android.graphics.Picture] can't render on a
 * hardware backed [Canvas][android.graphics.Canvas].
 */
class SvgSoftwareLayerSetter : RequestListener<Bitmap> {
  override fun onLoadFailed(
    e: GlideException?,
    model: Any?,
    target: Target<Bitmap?>?,
    isFirstResource: Boolean
  ): Boolean {
//    val view = (target as ImageViewTarget<*>).view
//    view.setLayerType(ImageView.LAYER_TYPE_NONE, null)
    return false
  }

  override fun onResourceReady(
    resource: Bitmap?,
    model: Any?,
    target: Target<Bitmap?>?,
    dataSource: DataSource?,
    isFirstResource: Boolean
  ): Boolean {
//    val view = (target as ImageViewTarget<*>).view
//    view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
    return false
  }
}
