package org.oppia.util.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.net.URL

// TODO (#169) : Replace this with exploration asset downloader
/** UrlImage Parser for android TextView to load Html Image tag. */
class UrlImageParser(
  internal var tvContents: TextView,
  internal var context: Context,
  private val entity_type: String,
  private val entity_id: String
) : Html.ImageGetter {

  val GCS_PREFIX: String = "https://storage.googleapis.com/"
  val GCS_RESOURCE_BUCKET_NAME = "oppiaserver-resources/"
  var IMAGE_DOWNLOAD_URL_TEMPLATE = "/<entity_type>/<entity_id>/assets/image/<filename>"

  /***
   * This method is called when the HTML parser encounters an <img> tag
   * @param urlString : urlString argument is the string from the "src" attribute
   * @return Drawable : Drawable representation of the image
   */
  override fun getDrawable(urlString: String): Drawable {
    IMAGE_DOWNLOAD_URL_TEMPLATE = entity_type + "/" + entity_id + "/assets/image/"
    val urlDrawable = UrlDrawable()
    val load = Glide.with(context).asBitmap()
      .load(URL(GCS_PREFIX + GCS_RESOURCE_BUCKET_NAME + IMAGE_DOWNLOAD_URL_TEMPLATE + urlString))
    val target = BitmapTarget(urlDrawable)
    load.into(target)
    return urlDrawable
  }

  inner class BitmapTarget(private val urlDrawable: UrlDrawable) : SimpleTarget<Bitmap>() {
    internal var drawable: Drawable? = null
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      drawable = BitmapDrawable(context.resources, resource)
      tvContents.post {
        val textviewWidth = tvContents.width
        val drawableHeight = (drawable as BitmapDrawable).intrinsicHeight
        val drawableWidth = (drawable as BitmapDrawable).intrinsicWidth
        if (drawableWidth > textviewWidth) {
          val calculatedHeight = textviewWidth * drawableHeight / drawableWidth;
          val rect = Rect(0, 0, textviewWidth, calculatedHeight)
          (drawable as BitmapDrawable).bounds = rect
          urlDrawable.bounds = rect
        } else {
          val rect = Rect(0, 0, drawableWidth, drawableHeight)
          (drawable as BitmapDrawable).bounds = rect
          urlDrawable.bounds = rect
        }
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
