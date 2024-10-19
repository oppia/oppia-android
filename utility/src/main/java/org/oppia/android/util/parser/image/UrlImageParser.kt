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
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.ViewCompat
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
import kotlin.math.sqrt

// TODO(#169): Replace this with exploration asset downloader.

/**
 * The pixels-per-inch used on @seanlip's monitor with which Oppia content images have been
 * calibrated to be the correct scale.
 *
 * This measurement is used to convert image sizes such that they have the same physical display
 * size when rendered on a local Android device.
 */
private const val REFERENCE_MONITOR_PPI = 81.589f

/**
 * A factor to adjust for the fact that phones are physically smaller than the display used to
 * calibrate [REFERENCE_MONITOR_PPI] by downsizing the images to be more appropriately sized.
 *
 * Image sizes will still be consistent with the original calibration display.
 */
private const val RELATIVE_SIZE_ADJUSTMENT_FACTOR = 0.15f

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
  private val diagonalPixelsPerInch by lazy {
    context.resources.displayMetrics.computeDiagonalPpi()
  }
  private val oppiaLocalImageSpaceConversionFactor by lazy {
    // The conversion here is from Oppia pixel to MDPI pixel (which can be treated as dp since
    // 1px=1dp in MDPI) for later scaling according to the user's set display density.
    (diagonalPixelsPerInch / REFERENCE_MONITOR_PPI) * RELATIVE_SIZE_ADJUSTMENT_FACTOR
  }

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
    val isSvg = machineLocale.run {
      imageUrl.endsWithIgnoreCase("svg") || imageUrl.endsWithIgnoreCase("svgz")
    }
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
      context,
      htmlContentTextView,
      imageCenterAlign,
      proxyDrawable,
      oppiaLocalImageSpaceConversionFactor
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
    private val oppiaLocalImageSpaceConversionFactor by lazy {
      targetConfiguration.oppiaLocalImageSpaceConversionFactor
    }

    override fun onLoadCleared(placeholder: Drawable?) {
      // No resources to clear.
    }

    override fun onResourceReady(resource: T, transition: Transition<in T?>?) {
      val drawable = retrieveDrawable(resource)
      htmlContentTextView.post {
        htmlContentTextView.width { viewWidth ->
          val padding =
            Rect(
              htmlContentTextView.paddingLeft,
              htmlContentTextView.paddingTop,
              htmlContentTextView.paddingRight,
              htmlContentTextView.paddingBottom
            )
          proxyDrawable.initialize(drawable, computeBounds(context, drawable, viewWidth, padding))
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
    protected abstract fun computeBounds(
      context: Context,
      drawable: D,
      viewWidth: Int,
      padding: Rect
    ): Rect

    /**
     * Returns a conversion of [this] from "Oppia" image space to local Android screen space (to
     * ensure images take up the same physical space locally as they do on @seanlip's monitor).
     */
    protected fun Float.oppiaPxToLocalAndroidPx() =
      context.dpToPx(this * oppiaLocalImageSpaceConversionFactor)

    /**
     * A [AutoAdjustingImageTarget] that may automatically center and/or resize loaded images to
     * display them in a "block" fashion.
     */
    sealed class BlockImageTarget<T : Any, D : Drawable>(
      targetConfiguration: TargetConfiguration,
      private val autoResizeImage: Boolean
    ) : AutoAdjustingImageTarget<T, D>(targetConfiguration) {

      private fun isRTLMode(): Boolean {
        return ViewCompat.getLayoutDirection(htmlContentTextView) == ViewCompat
          .LAYOUT_DIRECTION_RTL
      }

      override fun computeBounds(
        context: Context,
        drawable: D,
        viewWidth: Int,
        padding: Rect
      ): Rect {
        val layoutParams = htmlContentTextView.layoutParams
        val maxAvailableWidth = when (layoutParams.width) {
          ViewGroup.LayoutParams.WRAP_CONTENT -> {
            // Assume that wrap_content cases means that the view cannot exceed its parent's width
            // minus margins.
            val parent = htmlContentTextView.parent
            if (parent is View && layoutParams is ViewGroup.MarginLayoutParams) {
              // Only pick the computed space if it allows the view to expand to accommodate larger
              // images.
              max(viewWidth, parent.width - (layoutParams.leftMargin + layoutParams.rightMargin))
            } else viewWidth
          }
          ViewGroup.LayoutParams.MATCH_PARENT -> {
            // match_parent means the view's width likely represents the maximum space that can be
            // taken up, but padding must be subtracted to avoid the image being cut-off.
            viewWidth - (padding.left + padding.right)
          }
          else -> viewWidth // The view's width
        }

        var drawableWidth = drawable.intrinsicWidth.toFloat()
        var drawableHeight = drawable.intrinsicHeight.toFloat()
        val maxContentItemPadding =
          context.resources.getDimensionPixelSize(R.dimen.maximum_content_item_padding)
        if (autoResizeImage) {
          // Treat the drawable's dimensions as dp so that the image scales for higher density
          // displays.
          drawableWidth = drawableWidth.oppiaPxToLocalAndroidPx()
          drawableHeight = drawableHeight.oppiaPxToLocalAndroidPx()

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

        if (drawableWidth >= (maxAvailableWidth - maxContentItemPadding)) {
          drawableWidth -= maxContentItemPadding
        }

        var drawableLeft = if (imageCenterAlign && !isRTLMode()) {
          calculateInitialMargin(maxAvailableWidth, drawableWidth)
        } else {
          0f
        }

        val drawableTop = 0f
        var drawableRight = drawableLeft + drawableWidth

        // If the image is getting cut off, recalculate the positions of the drawableLeft and drawableRight.
        if (drawableRight + maxContentItemPadding > maxAvailableWidth) {
          drawableLeft -= maxContentItemPadding
          drawableRight -= maxContentItemPadding
        }

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
    class InlineTextImage<T : Any, D : Drawable>(
      targetConfiguration: TargetConfiguration,
      private val computeDrawable: (T) -> D,
      private val computeDimensions: (D, TextView) -> Unit,
    ) : AutoAdjustingImageTarget<T, D>(targetConfiguration) {
      override fun retrieveDrawable(resource: T): D = computeDrawable(resource)

      override fun computeBounds(
        context: Context,
        drawable: D,
        viewWidth: Int,
        padding: Rect
      ): Rect {
        computeDimensions(drawable, htmlContentTextView)
        // Note that the original size is used here since inline images should be sized based on the
        // text line height (which is already adjusted for both font and display scaling/density).
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
     *
     * @property context the application context
     * @property htmlContentTextView the [TextView] in which the retrieved images are being rendered
     * @property imageCenterAlign whether to automatically align block-displayed images
     * @property proxyDrawable the [ProxyDrawable] corresponding to the drawable that will be loaded
     *     for displaying
     * @property oppiaLocalImageSpaceConversionFactor the conversion factor from Oppia image space
     *     pixels into the local Android device's screen space dp
     */
    data class TargetConfiguration(
      val context: Context,
      val htmlContentTextView: TextView,
      val imageCenterAlign: Boolean,
      val proxyDrawable: ProxyDrawable,
      val oppiaLocalImageSpaceConversionFactor: Float
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

// References: https://stackoverflow.com/a/51865494 and
// https://stackoverflow.com/a/35444014/12314934.
private fun TextView.width(computeWidthOnGlobalLayout: (Int) -> Unit) {
  if (width == 0) {
    // In a recyclerview, where there are images with content descriptions, onGlobalLayoutListener
    // will not be called because the view is created before the request to load images is
    // processed, so calling requestLayout() will ensure that onGlobalLayoutListener is called.
    requestLayout()
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

private fun Context.dpToPx(dp: Float): Float =
  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

private fun DisplayMetrics.computeDiagonalPpi() = sqrt(xdpi * xdpi + ydpi * ydpi)
