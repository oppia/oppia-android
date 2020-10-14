package org.oppia.android.util.parser

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat.UNKNOWN
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

// TODO: add docs. Mention that bounds have to be set with setBounds() and not directly on the
//  bounds object otherwise changes won't be reflected in the proxied drawable.
class DeferredImageDrawable<T> private constructor(drawableFactory: (T) -> Drawable): Drawable() {
  private val loadedDrawable = MutableLiveData<Drawable>()
  private val drawableTarget = DrawableTarget(loadedDrawable, drawableFactory)
  private val imageTarget = CustomImageTarget(drawableTarget)

  override fun draw(canvas: Canvas) {
    loadedDrawable.value?.draw(canvas)
  }

  override fun setAlpha(alpha: Int) {
    loadedDrawable.value?.alpha = alpha
  }

  override fun getOpacity(): Int {
    @Suppress("DEPRECATION") // Needed to implement getOpacity().
    return loadedDrawable.value?.opacity ?: UNKNOWN
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    loadedDrawable.value?.colorFilter = colorFilter
  }

  override fun getIntrinsicWidth(): Int {
    return loadedDrawable.value?.intrinsicWidth ?: 0
  }

  override fun getIntrinsicHeight(): Int {
    return loadedDrawable.value?.intrinsicHeight ?: 0
  }

  override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
    super.setBounds(left, top, right, bottom)
    loadedDrawable.value?.setBounds(left, top, right, bottom)
  }

  // TODO: add docs.
  fun getImageTarget(): ImageTarget<T> = imageTarget

  // TODO: add docs.
  fun getLoadedDrawable(): LiveData<Drawable> = loadedDrawable

  // TODO: consider moving to a factory. Could replace new DeferredUrlImageParser (& old one).
  companion object {
    // TODO: add docs.
    fun createDeferredImageDrawableForBitmap(resources: Resources): DeferredImageDrawable<Bitmap> {
      return DeferredImageDrawable() {
          resource -> BitmapDrawable(resources, resource)
      }
    }

    // TODO: add docs.
    fun createDeferredImageDrawableForPicture(): DeferredImageDrawable<PictureDrawable> {
      return DeferredImageDrawable() { it }
    }
  }

  private inner class DrawableTarget<T>(
    private val liveData: MutableLiveData<Drawable>,
    private val drawableFactory: (T) -> Drawable
  ): CustomTarget<T>() {
    override fun onLoadCleared(placeholder: Drawable?) {}

    override fun onResourceReady(resource: T, transition: Transition<in T>?) {
      val drawableResource = drawableFactory(resource)
      drawableResource.bounds = bounds
      liveData.postValue(drawableResource)
    }
  }
}
