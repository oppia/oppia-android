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

/** UrlImage Parser for android TextView to load Html Image tag. */
class UrlImageParser(
  internal var tvContents: TextView,
  internal var context: Context,
  entity_type: String,
  entity_id: String
) : Html.ImageGetter {

  var targets: ArrayList<BitmapTarget>? = null
  /***
   * This method is called when the HTML parser encounters an <img> tag
   * @param urlString : urlString argument is the string from the "src" attribute
   * @return Drawable : Drawable representation of the image
   */
  override fun getDrawable(urlString: String): Drawable {
    val urlDrawable = UrlDrawable()
    val load = Glide.with(context).asBitmap().load(URL(urlString))
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
        val textviewWidth = tvContents.width
        val drawableHeight = (drawable as BitmapDrawable).intrinsicHeight
        val drawableWidth = (drawable as BitmapDrawable).intrinsicWidth
        val newHeight = drawableHeight * textviewWidth / drawableWidth
        val rect = Rect(0, 0, textviewWidth, newHeight)
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
