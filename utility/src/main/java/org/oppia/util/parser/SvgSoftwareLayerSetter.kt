package org.oppia.util.parser

import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * Listener which updates the [ImageView] to be software rendered, because {@link
 * [SVG]/[Picture] can't render on a hardware backed [Canvas]. Prior to API level 23 a picture cannot
 * be replayed on a hardware accelerated canvas. On SDK 23+ use a hardware-accelerated layout to improve image rendering performance.
 */
class SvgSoftwareLayerSetter : RequestListener<PictureDrawable> {
  override fun onLoadFailed(
    e: GlideException?,
    model: Any?,
    target: Target<PictureDrawable?>?,
    isFirstResource: Boolean
  ): Boolean {
    return false
  }

  override fun onResourceReady(
    resource: PictureDrawable?,
    model: Any?,
    target: Target<PictureDrawable?>?,
    dataSource: DataSource?,
    isFirstResource: Boolean
  ): Boolean {
    return false
  }
}
