package org.oppia.util.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Log
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.net.URL

/** UrlImage Parser for android TextView to extract image from Html content. */
class UrlImageParser(internal var tvContents: TextView, internal var context: Context) : Html.ImageGetter {


  val GCS_PREFIX: String = "https://storage.googleapis.com/"
  val GCS_RESOURCE_BUCKET_NAME = "oppiaserver-resources/"
  var  IMAGE_DOWNLOAD_URL_TEMPLATE = "/<entity_type>/<entity_id>/assets/image/<filename>"


  var targets: ArrayList<BitmapTarget>? = null

  override fun getDrawable(url: String): Drawable {
    IMAGE_DOWNLOAD_URL_TEMPLATE = "exploration/umPkwp0L1M0-/assets/image/"

    Log.d("url","htmlContent: " + url)
    val urlDrawable = UrlDrawable()
    val load = Glide.with(context).asBitmap().load(URL(GCS_PREFIX+GCS_RESOURCE_BUCKET_NAME+IMAGE_DOWNLOAD_URL_TEMPLATE+url))
    val target = BitmapTarget(urlDrawable)
    targets?.add(target)
    load.into(target)
    return urlDrawable
  }

  inner class BitmapTarget(private val urlDrawable: UrlDrawable) : SimpleTarget<Bitmap>() {
    internal var drawable: Drawable? = null

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

      drawable = BitmapDrawable(context.resources, resource)

      tvContents.post {
        val w = tvContents.width
        val hh = (drawable as BitmapDrawable).intrinsicHeight
        val ww = (drawable as BitmapDrawable).intrinsicWidth
        val newHeight = hh * w / ww
        val rect = Rect(0, 0, w, newHeight)
        (drawable as BitmapDrawable).bounds = rect
        urlDrawable.bounds = rect
        urlDrawable.drawable = drawable
        tvContents.text = tvContents.text
        tvContents.invalidate()
      }

    }
  }

  inner class UrlDrawable : BitmapDrawable() {
    var drawable: Drawable? = null
    override fun draw(canvas: Canvas) {
      if (drawable != null)
        drawable!!.draw(canvas)
    }
  }

}
