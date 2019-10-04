package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.util.R

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
  var IMAGE_DOWNLOAD_URL_TEMPLATE = "%s/%s/assets/image/%s"

  /***
   * This method is called when the HTML parser encounters an <img> tag
   * @param urlString : urlString argument is the string from the "src" attribute
   * @return Drawable : Drawable representation of the image
   */
  override fun getDrawable(urlString: String): Drawable {

    IMAGE_DOWNLOAD_URL_TEMPLATE = String.format(IMAGE_DOWNLOAD_URL_TEMPLATE, entity_type, entity_id, urlString)
    val urlDrawable = UrlDrawable()
    val target = BitmapTarget(urlDrawable)
    ImageLoader.load(
      context,
      GCS_PREFIX + GCS_RESOURCE_BUCKET_NAME + IMAGE_DOWNLOAD_URL_TEMPLATE,
      R.drawable.abc_ab_share_pack_mtrl_alpha,
      target
    );
    return urlDrawable
  }

  inner class BitmapTarget(private val urlDrawable: UrlDrawable) : SimpleTarget<Bitmap>() {
    internal var drawable: Drawable? = null
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      drawable = BitmapDrawable(context.resources, resource)
      tvContents.post {
        val textViewWidth = tvContents.width
        val drawableHeight = (drawable as BitmapDrawable).intrinsicHeight
        val drawableWidth = (drawable as BitmapDrawable).intrinsicWidth
        // To resize the image keeping aspect ratio.
        if (drawableWidth > textViewWidth) {
          val calculatedHeight = textViewWidth * drawableHeight / drawableWidth;
          val rect = Rect(0, 0, textViewWidth, calculatedHeight)
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
