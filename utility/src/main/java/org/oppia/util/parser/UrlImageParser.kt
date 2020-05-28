package org.oppia.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.text.Html
import android.view.ViewTreeObserver
import android.widget.TextView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.util.gcsresource.DefaultResourceBucketName
import javax.inject.Inject

// TODO(#169): Replace this with exploration asset downloader.
// TODO(#277): Add test cases for loading image.

/** UrlImage Parser for android TextView to load Html Image tag. */
class UrlImageParser private constructor(
  private val context: Context,
  private val gcsPrefix: String,
  private val gcsResourceName: String,
  private val imageDownloadUrlTemplate: String,
  private val htmlContentTextView: TextView,
  private val entityType: String,
  private val entityId: String,
  private val imageCenterAlign: Boolean,
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
    // TODO(#1039): Introduce custom type OppiaImage for rendering Bitmap and Svg.
    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      val target = SvgTarget(urlDrawable)
      imageLoader.loadSvg("$gcsPrefix/$gcsResourceName/$imageUrl", target)
    } else {
      val target = BitmapTarget(urlDrawable)
      imageLoader.load("$gcsPrefix/$gcsResourceName/$imageUrl", target)
    }
    return urlDrawable
  }

  private inner class BitmapTarget(urlDrawable: UrlDrawable) : CustomImageTarget<Bitmap>(
    urlDrawable, { resource -> BitmapDrawable(context.resources, resource) }
  )

  private inner class SvgTarget(urlDrawable: UrlDrawable) : CustomImageTarget<Picture>(
    urlDrawable, { resource -> PictureDrawable(resource) }
  )

  private open inner class CustomImageTarget<T>(
    private val urlDrawable: UrlDrawable,
    private val drawableFactory: (T) -> Drawable
  ) : CustomTarget<T>() {
    override fun onLoadCleared(placeholder: Drawable?) {
      // No resources to clear.
    }

    override fun onResourceReady(resource: T, transition: Transition<in T>?) {
      val drawable = drawableFactory(resource)
      htmlContentTextView.post {
        htmlContentTextView.width {
          val drawableHeight = drawable.intrinsicHeight
          val drawableWidth = drawable.intrinsicWidth
          val initialDrawableMargin = if (imageCenterAlign) {
            calculateInitialMargin(it, drawableWidth)
          } else {
            0
          }
          val rect =
            Rect(initialDrawableMargin, 0, drawableWidth + initialDrawableMargin, drawableHeight)
          drawable.bounds = rect
          urlDrawable.bounds = rect
          urlDrawable.drawable = drawable
          htmlContentTextView.text = htmlContentTextView.text
          htmlContentTextView.invalidate()
        }
      }
    }
  }

  class UrlDrawable : BitmapDrawable() {
    var drawable: Drawable? = null
    override fun draw(canvas: Canvas) {
      val currentDrawable = drawable
      currentDrawable?.draw(canvas)
    }
  }

  private fun calculateInitialMargin(availableAreaWidth: Int, drawableWidth: Int): Int {
    val margin = (availableAreaWidth - drawableWidth) / 2
    return if (margin > 0) {
      margin
    } else {
      0
    }
  }

  // Reference: https://stackoverflow.com/a/51865494
  private fun TextView.width(computeWidthOnGlobalLayout: (Int) -> Unit) {
    if (width == 0) {
      viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
          viewTreeObserver.removeOnGlobalLayoutListener(this)
          computeWidthOnGlobalLayout(width)
        }
      })
    } else {
      computeWidthOnGlobalLayout(width)
    }
  }

  class Factory @Inject constructor(
    private val context: Context,
    @DefaultGcsPrefix private val gcsPrefix: String,
    @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
    private val imageLoader: ImageLoader
  ) {
    fun create(
      htmlContentTextView: TextView,
      gcsResourceName: String,
      entityType: String,
      entityId: String,
      imageCenterAlign: Boolean
    ): UrlImageParser {
      return UrlImageParser(
        context,
        gcsPrefix,
        gcsResourceName,
        imageDownloadUrlTemplate,
        htmlContentTextView,
        entityType,
        entityId,
        imageCenterAlign,
        imageLoader
      )
    }
  }
}
