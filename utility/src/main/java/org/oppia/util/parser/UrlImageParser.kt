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
import android.opengl.ETC1.getWidth



// TODO(#169): Replace this with exploration asset downloader.
// TODO(#277): Add test cases for loading image.

/** UrlImage Parser for android TextView to load Html Image tag. */
class UrlImageParser private constructor(
  private val context: Context,
  @DefaultGcsPrefix private val gcsPrefix: String,
  @DefaultGcsResource private val gcsResource: String,
  @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
  private val htmlContentTextView: TextView,
  private val entityType: String,
  private val entityId: String,
  private val imageLoader: ImageLoader
) : Html.ImageGetter {
  /**
   * This method is called when the HTML parser encounters an <img> tag.
   * @param urlString : urlString argument is the string from the "src" attribute.
   * @return Drawable : Drawable representation of the image.
   */
  override fun getDrawable(urlString: String): Drawable {
    val imageUrl =  String.format(imageDownloadUrlTemplate, entityType, entityId, urlString)
    val urlDrawable = UrlDrawable()
    val target = BitmapTarget(urlDrawable)
    imageLoader.load(
      gcsPrefix + gcsResource + imageUrl,
      target
    )
    return urlDrawable
  }

  private inner class BitmapTarget(private val urlDrawable: UrlDrawable) : CustomTarget<Bitmap>() {
    override fun onLoadCleared(placeholder: Drawable?) {
      // No resources to clear.
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      val  drawable = BitmapDrawable(context.resources, resource)
      htmlContentTextView.post {
        val drawableHeight = drawable.intrinsicHeight
        val drawableWidth = drawable.intrinsicWidth
        val width : Int
        val height : Int
        if (htmlContentTextView.getWidth() >= drawableWidth) {
          width = drawableWidth
          height = drawableHeight
        } else {
          width = htmlContentTextView.getWidth()
          height = (drawableHeight / (1.0 * drawableWidth / width)).toInt()
        }

        val rect = Rect(0, 0, width, height)
        drawable.bounds = rect
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
