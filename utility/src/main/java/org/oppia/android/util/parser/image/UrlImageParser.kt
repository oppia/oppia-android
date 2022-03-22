package org.oppia.android.util.parser.image

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
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever.Type.BLOCK_IMAGE
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever.Type.INLINE_TEXT_IMAGE
import org.oppia.android.util.parser.svg.BlockPictureDrawable
import javax.inject.Inject
import kotlin.math.max

// TODO(#169): Replace this with exploration asset downloader.

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
  private val imageLoader: ImageLoader,
  private val consoleLogger: ConsoleLogger,
  private val machineLocale: OppiaLocale.MachineLocale
) : Html.ImageGetter, ImageRetriever {
  override fun getDrawable(urlString: String): Drawable {
    // Only block images can be loaded through the standard ImageGetter.
    return loadDrawable(urlString, BLOCK_IMAGE)
  }

  override fun loadDrawable(filename: String, type: ImageRetriever.Type): Drawable {
    val imagePath = machineLocale.run {
      imageDownloadUrlTemplate.formatForMachines(entityType, entityId, filename)
    }
    val imageUrl = "$gcsPrefix/$gcsResourceName/$imagePath"
    val proxyDrawable = ProxyDrawable()
    // TODO(#1039): Introduce custom type OppiaImage for rendering Bitmap and Svg.
    val isSvg = machineLocale.run { imageUrl.endsWithIgnoreCase("svg") }
    val adjustedType = if (type == INLINE_TEXT_IMAGE && !isSvg) {
      // Treat non-svg in-line images as block, instead, since only SVG is supported.
      consoleLogger.w("UrlImageParser", "Forcing image $filename to block image")
      BLOCK_IMAGE
    } else type

    return when (adjustedType) {
      INLINE_TEXT_IMAGE -> {
        imageLoader.loadTextSvg(
          imageUrl,
          createCustomTarget(proxyDrawable) {
            AutoAdjustingImageTarget.InlineTextImage.createForSvg(it)
          }
        )
        proxyDrawable
      }
      BLOCK_IMAGE -> {
        if (isSvg) {
          imageLoader.loadBlockSvg(
            imageUrl,
            createCustomTarget(
              proxyDrawable,
              AutoAdjustingImageTarget.BlockImageTarget.SvgTarget::create
            )
          )
        } else {
          imageLoader.loadBitmap(
            imageUrl,
            createCustomTarget(
              proxyDrawable,
              AutoAdjustingImageTarget.BlockImageTarget.BitmapTarget::create
            )
          )
        }
        proxyDrawable
      }
    }
  }

  override fun loadMathDrawable(
    rawLatex: String,
    lineHeight: Float,
    type: ImageRetriever.Type
  ): Drawable {
    return ProxyDrawable().also { drawable ->
      imageLoader.loadMathDrawable(
        rawLatex,
        lineHeight,
        useInlineRendering = type == INLINE_TEXT_IMAGE,
        createCustomTarget(drawable) {
          when (type) {
            INLINE_TEXT_IMAGE -> AutoAdjustingImageTarget.InlineTextImage.createForMath(context, it)
            BLOCK_IMAGE -> {
              // Render the LaTeX as a block image, but don't automatically resize it since it's
              // text (which means resizing may make it unreadable).
              AutoAdjustingImageTarget.BlockImageTarget.BitmapTarget.create(
                it, autoResizeImage = false
              )
            }
          }
        }
      )
    }
  }

  private fun <T, D : Drawable, C : AutoAdjustingImageTarget<T, D>> createCustomTarget(
    proxyDrawable: ProxyDrawable,
    createTarget: (AutoAdjustingImageTarget.TargetConfiguration) -> C
  ): CustomImageTarget<T> {
    val configuration = AutoAdjustingImageTarget.TargetConfiguration(
      context, htmlContentTextView, imageCenterAlign, proxyDrawable
    )
    return CustomImageTarget(createTarget(configuration))
  }

  // T must be bounded to a non-null value per https://youtrack.jetbrains.com/issue/KT-50961 and
  // https://youtrack.jetbrains.com/issue/KT-26245 to ensure that the Kotlin compiler can be
  // confident an NPE can't unwittingly happen.
  /**
   * A [CustomTarget] that can automatically resized, or align, the loaded image as needed. This
   * class coordinates with a [ProxyDrawable] defined as part of the specified
   * [TargetConfiguration], and ensures that the drawable is only adjusted when it's safe to do so
   * per the holding TextView's lifecycle.
   */
  private sealed class AutoAdjustingImageTarget<T : Any, D : Drawable>(
    private val targetConfiguration: TargetConfiguration
  ) : CustomTarget<T>() {

    protected val context by lazy { targetConfiguration.context }
    protected val htmlContentTextView by lazy { targetConfiguration.htmlContentTextView }
    protected val imageCenterAlign by lazy { targetConfiguration.imageCenterAlign }
    protected val proxyDrawable by lazy { targetConfiguration.proxyDrawable }

    override fun onLoadCleared(placeholder: Drawable?) {
      // No resources to clear.
    }

    override fun onResourceReady(resource: T, transition: Transition<in T?>?) {
      val drawable = retrieveDrawable(resource)
      htmlContentTextView.post {
        htmlContentTextView.width { viewWidth ->
          proxyDrawable.initialize(drawable, computeBounds(drawable, viewWidth))
          htmlContentTextView.text = htmlContentTextView.text
          htmlContentTextView.invalidate()
        }
      }
    }

    /** Returns the drawable corresponding to the specified loaded resource. */
    protected abstract fun retrieveDrawable(resource: T): D

    /**
     * Returns the bounds and/or alignment of the specified drawable, given the current text view's
     * width.
     */
    protected abstract fun computeBounds(drawable: D, viewWidth: Int): Rect

    /**
     * A [AutoAdjustingImageTarget] that may automatically center and/or resize loaded images to
     * display them in a "block" fashion.
     */
    sealed class BlockImageTarget<T : Any, D : Drawable>(
      targetConfiguration: TargetConfiguration,
      private val autoResizeImage: Boolean
    ) : AutoAdjustingImageTarget<T, D>(targetConfiguration) {

      override fun computeBounds(drawable: D, viewWidth: Int): Rect {
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

        var drawableWidth = drawable.intrinsicWidth.toFloat()
        var drawableHeight = drawable.intrinsicHeight.toFloat()
        if (autoResizeImage) {
          val minimumImageSize = context.resources.getDimensionPixelSize(R.dimen.minimum_image_size)
          if (drawableHeight <= minimumImageSize || drawableWidth <= minimumImageSize) {
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
          }
          val maxContentItemPadding =
            context.resources.getDimensionPixelSize(R.dimen.maximum_content_item_padding)
          val maximumImageSize = maxAvailableWidth - maxContentItemPadding
          if (drawableWidth >= maximumImageSize) {
            // The multipleFactor value is used to make sure that the aspect ratio of the image
            // remains the same. Example: Height is 420, width is 440 and maximumImageSize is 200.
            // Then multipleFactor will be (200/440). The new height will be 191 and new width will
            // be 200.
            val multipleFactor = if (drawableHeight >= drawableWidth) {
              // If height is greater then the width, multipleFactor value is determined by height.
              (maximumImageSize.toFloat() / drawableHeight)
            } else {
              // If height is greater then the width, multipleFactor value is determined by width.
              (maximumImageSize.toFloat() / drawableWidth)
            }
            drawableHeight *= multipleFactor
            drawableWidth *= multipleFactor
          }
        }
        val drawableLeft = if (imageCenterAlign) {
          calculateInitialMargin(maxAvailableWidth, drawableWidth)
        } else {
          0f
        }
        val drawableTop = 0f
        val drawableRight = drawableLeft + drawableWidth
        val drawableBottom = drawableTop + drawableHeight
        return Rect(
          drawableLeft.toInt(), drawableTop.toInt(), drawableRight.toInt(), drawableBottom.toInt()
        )
      }

      /** A [BlockImageTarget] used to load & arrange SVGs. */
      internal class SvgTarget(
        targetConfiguration: TargetConfiguration
      ) : BlockImageTarget<BlockPictureDrawable, BlockPictureDrawable>(
        targetConfiguration, autoResizeImage = true
      ) {
        override fun retrieveDrawable(resource: BlockPictureDrawable): BlockPictureDrawable =
          resource

        companion object {
          /** Returns a new [SvgTarget] for the specified configuration. */
          fun create(targetConfiguration: TargetConfiguration) = SvgTarget(targetConfiguration)
        }
      }

      /** A [BlockImageTarget] used to load & arrange bitmaps. */
      internal class BitmapTarget(
        targetConfiguration: TargetConfiguration,
        autoResizeImage: Boolean
      ) : BlockImageTarget<Bitmap, BitmapDrawable>(targetConfiguration, autoResizeImage) {
        override fun retrieveDrawable(resource: Bitmap): BitmapDrawable {
          return BitmapDrawable(context.resources, resource)
        }

        companion object {
          /** Returns a new [BitmapTarget] for the specified configuration. */
          fun create(targetConfiguration: TargetConfiguration, autoResizeImage: Boolean = true) =
            BitmapTarget(targetConfiguration, autoResizeImage)
        }
      }
    }

    /**
     * A [AutoAdjustingImageTarget] that should be used for in-line SVG images and math expressions
     * that will not be resized or aligned beyond what the target itself requires, and what the
     * system performs automatically.
     */
    class InlineTextImage<T: Any, D : Drawable>(
      targetConfiguration: TargetConfiguration,
      private val computeDrawable: (T) -> D,
      private val computeDimensions: (D, TextView) -> Unit,
    ) : AutoAdjustingImageTarget<T, D>(targetConfiguration) {
      override fun retrieveDrawable(resource: T): D = computeDrawable(resource)

      override fun computeBounds(drawable: D, viewWidth: Int): Rect {
        computeDimensions(drawable, htmlContentTextView)
        return Rect(/* left= */ 0, /* top= */ 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
      }

      companion object {
        /** Returns a new [InlineTextImage] for the specified SVG configuration. */
        fun createForSvg(
          targetConfiguration: TargetConfiguration
        ): InlineTextImage<TextPictureDrawable, TextPictureDrawable> {
          return InlineTextImage(
            targetConfiguration,
            computeDrawable = { it },
            computeDimensions = { drawable, textView ->
              drawable.computeTextPicture(textView.paint)
            }
          )
        }

        /** Returns a new [InlineTextImage] for the specified math configuration. */
        fun createForMath(
          applicationContext: Context,
          targetConfiguration: TargetConfiguration
        ): InlineTextImage<Bitmap, Drawable> {
          return InlineTextImage(
            targetConfiguration,
            computeDrawable = { BitmapDrawable(applicationContext.resources, it) },
            computeDimensions = { _, _ -> }
          )
        }
      }
    }

    /**
     * Configures a [AutoAdjustingImageTarget]. See the specified parameters for what needs to be
     * provided.
     */
    data class TargetConfiguration(
      /** The application context. */
      val context: Context,
      /** The [TextView] in which the retrieved images are being rendered. */
      val htmlContentTextView: TextView,
      /** Whether to automatically align block-displayed images. */
      val imageCenterAlign: Boolean,
      /** The [ProxyDrawable] corresponding to the drawable that will be loaded for displaying. */
      val proxyDrawable: ProxyDrawable
    )
  }

  /**
   * A [Drawable] that can be created & used immediately, but whose drawing properties will be
   * defined later, asynchronously.
   */
  private class ProxyDrawable : Drawable() {
    private var drawable: Drawable? = null

    /** Initializes the drawable with the specified root [Drawable] and [bounds]. */
    fun initialize(drawable: Drawable, bounds: Rect) {
      this.drawable = drawable
      this.bounds = bounds
    }

    override fun draw(canvas: Canvas) {
      val currentDrawable = drawable
      drawable?.apply {
        bounds = this@ProxyDrawable.bounds
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

  /** Factory to create new [UrlImageParser]s. This is injectable in any component. */
  class Factory @Inject constructor(
    private val context: Context,
    @DefaultGcsPrefix private val gcsPrefix: String,
    @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
    private val imageLoader: ImageLoader,
    private val consoleLogger: ConsoleLogger,
    private val machineLocale: OppiaLocale.MachineLocale
  ) {
    /**
     * Creates a new [UrlImageParser] based on the specified settings.
     *
     * @param htmlContentTextView the [TextView] that will contain [Drawable]s loaded by the
     *     returned [UrlImageParser]. The [TextView]'s paint and size settings will affect the
     *     drawable (which means text resizing or other layout changes may cause slight "jittering"
     *     effects as images are automatically adjusted in subsequent layout & draw steps after the
     *     [TextView] changes).
     * @param gcsResourceName the GCS resource bucket that should be used when loading images
     * @param entityType the entity type corresponding to loaded images (such as explorations)
     * @param entityId the ID of the entity for retrieving images (such as an exploration ID)
     * @param imageCenterAlign whether to center-align block images within the [TextView]
     * @return the new [UrlImageParser] configured with the provided parameters
     */
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
        imageLoader,
        consoleLogger,
        machineLocale
      )
    }
  }
}

private fun calculateInitialMargin(availableAreaWidth: Int, drawableWidth: Float): Float {
  val margin = (availableAreaWidth - drawableWidth) / 2
  return margin.coerceAtLeast(0f)
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
