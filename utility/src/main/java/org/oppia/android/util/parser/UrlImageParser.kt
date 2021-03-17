package org.oppia.android.util.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.oppia.android.util.R
import javax.inject.Inject
import kotlin.math.max

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
    val proxyDrawable = ProxyDrawable()
    // TODO(#1039): Introduce custom type OppiaImage for rendering Bitmap and Svg.
    if (imageUrl.endsWith("svg", ignoreCase = true)) {
      val target = SvgTarget(proxyDrawable)
      imageLoader.loadSvg("$gcsPrefix/$gcsResourceName/$imageUrl", CustomImageTarget(target))
    } else {
      val target = BitmapTarget(proxyDrawable)
      imageLoader.loadBitmap("$gcsPrefix/$gcsResourceName/$imageUrl", CustomImageTarget(target))
    }
    return proxyDrawable
  }

  private inner class BitmapTarget(proxyDrawable: ProxyDrawable) : CustomImageTarget<Bitmap>(
      proxyDrawable, { resource -> BitmapDrawable(context.resources, resource) }
  )

  private inner class SvgTarget(
      proxyDrawable: ProxyDrawable
  ) : CustomImageTarget<ScalablePictureDrawable>(
      proxyDrawable, { it }
  )

  private open inner class CustomImageTarget<T>(
      private val proxyDrawable: ProxyDrawable,
      private val drawableFactory: (T) -> Drawable
  ) : CustomTarget<T>() {
    override fun onLoadCleared(placeholder: Drawable?) {
      // No resources to clear.
    }

    override fun onResourceReady(resource: T, transition: Transition<in T>?) {
      val drawable = drawableFactory(resource)
      htmlContentTextView.post {
        htmlContentTextView.width { viewWidth ->
          val layoutParams = htmlContentTextView.layoutParams
          val maxAvailableWidth = if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // Assume that wrap_content cases means that the view cannot exceed its parent's width
            // minus margins.
            val parent = htmlContentTextView.parent
            if (parent is View && layoutParams is ViewGroup.MarginLayoutParams) {
              // Only pick the computed space if it allows the view to expand to accommodate larger
              // images.
              max(viewWidth, parent.width - (layoutParams.leftMargin + layoutParams.rightMargin))
            } else viewWidth
          } else viewWidth

          // TODO: make this better by properly handling non-centering images and scalable
          //  drawables.
          var (drawableWidth, drawableHeight, verticalAlignment) = if (drawable is ScalablePictureDrawable) {
            drawable.initialize(htmlContentTextView.paint)
            drawable.computeIntrinsicSize()
          } else {
            OppiaSvg.SvgSizeSpecs(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat(), verticalAlignment = 0f)
          }
          val minimumImageSize = context.resources.getDimensionPixelSize(R.dimen.minimum_image_size)
          /*if (drawableHeight <= minimumImageSize || drawableWidth <= minimumImageSize) {
            // The multipleFactor value is used to make sure that the aspect ratio of the image
            // remains the same.
            // Example: Height is 90, width is 60 and minimumImageSize is 120.
            // Then multipleFactor will be 2 (120/60).
            // The new height will be 180 and new width will be 120.
            val multipleFactor = if (drawableHeight <= drawableWidth) {
              // If height is less then the width, multipleFactor value is determined by height.
              minimumImageSize.toFloat() / drawableHeight
            } else {
              // If height is less then the width, multipleFactor value is determined by width.
              minimumImageSize.toFloat() / drawableWidth
            }
            drawableHeight *= multipleFactor
            drawableWidth *= multipleFactor
          }*/
          val maxContentItemPadding =
            context.resources.getDimensionPixelSize(R.dimen.maximum_content_item_padding)
          val maximumImageSize = maxAvailableWidth - maxContentItemPadding
          /*if (drawableWidth >= maximumImageSize) {
            // The multipleFactor value is used to make sure that the aspect ratio of the image
            // remains the same. Example: Height is 420, width is 440 and maximumImageSize is 200.
            // Then multipleFactor will be (200/440). The new height will be 191 and new width will
            // be 200.
            val multipleFactor = if (drawableHeight >= drawableWidth) {
              // If height is greater then the width, multipleFactor value is determined by height.
              (maximumImageSize.toDouble() / drawableHeight.toDouble())
            } else {
              // If height is greater then the width, multipleFactor value is determined by width.
              (maximumImageSize.toDouble() / drawableWidth.toDouble())
            }
            drawableHeight = (drawableHeight.toDouble() * multipleFactor).toInt()
            drawableWidth = (drawableWidth.toDouble() * multipleFactor).toInt()
          }*/
          val drawableLeft = 0/*if (imageCenterAlign) {
            calculateInitialMargin(maxAvailableWidth, drawableWidth)
          } else {
            0
          }*/
          val drawableTop = 0
//          val drawableRight = (drawableLeft + drawableWidth).toInt()
//          val drawableBottom = (drawableTop + drawableHeight).toInt()
//          val rect = Rect(drawableLeft, drawableTop, drawableRight, drawableBottom)
//          proxyDrawable.bounds = expectedBounds
//          proxyDrawable.drawable = bitmapDrawable
          val bounds = Rect(0, 0, drawableWidth.toInt(), drawableHeight.toInt())
          proxyDrawable.initialize(drawable, bounds, verticalAlignment)

          htmlContentTextView.text = htmlContentTextView.text
          htmlContentTextView.invalidate()
        }
      }
    }
  }

  /**
   * A [Drawable] that can be created & used immediately, but whose drawing properties will be
   * defined later, asynchronously.
   */
  private class ProxyDrawable : Drawable() {
    private var drawable: Drawable? = null
    private var verticalAlignment: Float = 0f

    fun initialize(drawable: Drawable, bounds: Rect, verticalAlignment: Float) {
      this.drawable = drawable
      this.bounds = bounds
      this.verticalAlignment = verticalAlignment
    }

    override fun draw(canvas: Canvas) {
      val currentDrawable = drawable
      drawable?.apply {
        bounds = this@ProxyDrawable.bounds
        bounds.top += verticalAlignment.toInt()
        bounds.bottom += verticalAlignment.toInt()
      }
      currentDrawable?.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
      drawable?.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
      drawable?.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
      @Suppress("DEPRECATION") // Needed to pass along the call to the proxied drawable.
      return drawable?.opacity ?: PixelFormat.TRANSLUCENT
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
