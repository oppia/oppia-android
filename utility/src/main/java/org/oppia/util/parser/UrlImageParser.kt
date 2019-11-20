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
    val imageUrl = String.format(imageDownloadUrlTemplate, entityType, entityId, urlString)
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
      val drawable = BitmapDrawable(context.resources, resource)
      htmlContentTextView.post {
        val drawableHeight = drawable.intrinsicHeight
        val drawableWidth = drawable.intrinsicWidth
        val result = calculateDrawableSize(drawableWidth, drawableHeight, htmlContentTextView)
        val width: Int = result.first
        val height: Int = result.second

        val initialDrawableMargin = calculateInitialMargin(drawableWidth)
        val rect = Rect(initialDrawableMargin, 0, width + initialDrawableMargin, height)
        drawable.bounds = rect
        urlDrawable.bounds = rect
        urlDrawable.drawable = drawable
        htmlContentTextView.text = htmlContentTextView.text
        htmlContentTextView.invalidate()
      }
    }
  }

  /**
   * Check whether textview's width is greater then drawable width,
   * if true use drawable width and drawable height
   * else adjust the height if the width of the  textview's  is smaller then drawable width
   */
  public fun calculateDrawableSize(
    drawableWidth: Int,
    drawableHeight: Int,
    htmlContentTextView: TextView
  ): Pair<Int, Int> {
    return if (htmlContentTextView.getWidth() >= drawableWidth) {
      val width = drawableWidth
      val height = drawableHeight
      Pair(width, height)
    } else {
      val width = htmlContentTextView.getWidth()
      val height = (drawableHeight * width) / drawableWidth
      Pair(width, height)
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

  private fun calculateInitialMargin(drawableWidth: Int): Int {
    val availableAreaWidth = htmlContentTextView.width
    return (availableAreaWidth - drawableWidth) / 2
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
