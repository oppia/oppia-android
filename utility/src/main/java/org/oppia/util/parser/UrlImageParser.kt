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
import javax.inject.Inject
import javax.inject.Singleton

// TODO (#169) : Replace this with exploration asset downloader
/** UrlImage Parser for android TextView to load Html Image tag. */
@Singleton
class UrlImageParser @Inject constructor(
  @DefaultGcsPrefix private val gcsPrefix: String,
  @DefaultGCSResource private val gcsResource: String,
  @ImageDownloadUrlTemplate private var imageDownloadUrlTemplate: String
) : Html.ImageGetter {

  lateinit var htmlContentTextView: TextView
  lateinit var context: Context
  lateinit var entityType: String
  lateinit var entityId: String

  fun setImageData(htmlContentTextView: TextView, context: Context, entityType: String, entityId: String) {
    this.htmlContentTextView = htmlContentTextView
    this.context = context
    this.entityId = entityId
    this.entityType = entityType
  }

  /***
   * This method is called when the HTML parser encounters an <img> tag
   * @param urlString : urlString argument is the string from the "src" attribute
   * @return Drawable : Drawable representation of the image
   */
  override fun getDrawable(urlString: String): Drawable {

    imageDownloadUrlTemplate = String.format(imageDownloadUrlTemplate, entityType, entityId, urlString)
    val urlDrawable = UrlDrawable()
    val target = BitmapTarget(urlDrawable)
    ImageLoader.load(
      context,
      gcsPrefix + gcsResource + imageDownloadUrlTemplate,
      R.drawable.abc_ab_share_pack_mtrl_alpha,
      target
    );
    return urlDrawable
  }

  inner class BitmapTarget(private val urlDrawable: UrlDrawable) : SimpleTarget<Bitmap>() {
    internal var drawable: Drawable? = null
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      drawable = BitmapDrawable(context.resources, resource)
      htmlContentTextView.post {
        val drawableHeight = (drawable as BitmapDrawable).intrinsicHeight
        val drawableWidth = (drawable as BitmapDrawable).intrinsicWidth
        val rect = Rect(0, 0, drawableWidth, drawableHeight)
        (drawable as BitmapDrawable).bounds = rect
        urlDrawable.bounds = rect
        urlDrawable.drawable = drawable
        htmlContentTextView.text = htmlContentTextView.text
        htmlContentTextView.invalidate()
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
