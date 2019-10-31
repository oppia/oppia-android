package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import javax.inject.Inject

// TODO(#169,#277): Replace this with exploration asset downloader and add test cases for loading image
/** UrlImage Parser for android TextView to load Html Image tag. */
class UrlImageParser private constructor(
  private val context: Context,
  @DefaultGcsPrefix private val gcsPrefix: String,
  @DefaultGcsResource private val gcsResource: String,
  @ImageDownloadUrlTemplate private var imageDownloadUrlTemplate: String,
  private val htmlContentTextView: TextView,
  private val entityType: String,
  private val entityId: String,
  private val imageLoader:ImageLoader
) : Html.ImageGetter {

  /**
   * This method is called when the HTML parser encounters an <img> tag.
   * @param urlString : urlString argument is the string from the "src" attribute.
   * @return Drawable : Drawable representation of the image.
   */
  override fun getDrawable(urlString: String): Drawable {
    imageDownloadUrlTemplate = String.format(imageDownloadUrlTemplate, entityType, entityId, urlString)
    val urlDrawable = UrlDrawable()
    val target = BitmapTarget(urlDrawable)
    imageLoader.load(
      gcsPrefix + gcsResource + imageDownloadUrlTemplate,
      target
    )
    return urlDrawable
  }

  private inner class BitmapTarget(private val urlDrawable: UrlDrawable) : CustomTarget<Bitmap>() {
    override fun onLoadCleared(placeholder: Drawable?) {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      val  drawable = BitmapDrawable(context.resources, resource)
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

   class UrlDrawable : BitmapDrawable() {
    var drawable: Drawable? = null
    override fun draw(canvas: Canvas) {
      val currentDrawable = drawable
      if (currentDrawable != null) {
        currentDrawable.draw(canvas)
      }
    }
  }

  class Factory @Inject constructor(
    private val context: Context,
    @DefaultGcsPrefix private val gcsPrefix: String,
    @DefaultGcsResource private val gcsResource: String,
    @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
    private val imageLoader: ImageLoader
  ) {
    fun create(htmlContentTextView: TextView, entityType: String, entityId: String): UrlImageParser {
      return UrlImageParser(
        context,
        gcsPrefix,
        gcsResource,
        imageDownloadUrlTemplate,
        htmlContentTextView,
        entityType,
        entityId,
        imageLoader
      )
    }
  }
}
