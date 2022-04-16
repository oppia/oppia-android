package org.oppia.android.util.parser.math

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.request.target.Target
import io.github.karino2.kotlitex.view.DrawableSurface
import io.github.karino2.kotlitex.view.MathExpressionSpan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ConsoleLoggerInjectorProvider
import org.oppia.android.util.threading.DispatcherInjectorProvider
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * [ModelLoader] for rendering and caching bitmap representations of LaTeX represented by
 * [MathModel]s.
 *
 * This loader provides support for loading a bitmap version of rendered LaTeX that's been
 * pre-rendered into a Glide-cacheable bitmap. Note that this is computationally more expensive to
 * use than direct rendering since it includes steps to encode the image on-disk, but it's far more
 * performant for repeated rendering of the the LaTeX (real-time LaTeX rendering is very expensive
 * and blocks the main thread).
 */
class MathBitmapModelLoader private constructor(
  private val applicationContext: Context
) : ModelLoader<MathModel, ByteBuffer> {
  // Ref: https://bumptech.github.io/glide/tut/custom-modelloader.html#writing-the-modelloader.

  private val backgroundDispatcher by lazy {
    val injectorProvider = applicationContext.applicationContext as DispatcherInjectorProvider
    val injector = injectorProvider.getDispatcherInjector()
    injector.getBackgroundDispatcher()
  }

  private val blockingDispatcher by lazy {
    val injectorProvider = applicationContext.applicationContext as DispatcherInjectorProvider
    val injector = injectorProvider.getDispatcherInjector()
    injector.getBlockingDispatcher()
  }

  private val consoleLogger by lazy {
    val injectorProvider = applicationContext as ConsoleLoggerInjectorProvider
    val injector = injectorProvider.getConsoleLoggerInjector()
    injector.getConsoleLogger()
  }

  override fun buildLoadData(
    model: MathModel,
    width: Int,
    height: Int,
    options: Options
  ): ModelLoader.LoadData<ByteBuffer> {
    return ModelLoader.LoadData(
      model.toKeySignature(),
      LatexModelDataFetcher(
        applicationContext,
        model,
        width,
        height,
        backgroundDispatcher,
        blockingDispatcher,
        consoleLogger
      )
    )
  }

  override fun handles(model: MathModel): Boolean = true

  private class LatexModelDataFetcher(
    private val applicationContext: Context,
    private val model: MathModel,
    private val targetWidth: Int,
    private val targetHeight: Int,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val blockingDispatcher: CoroutineDispatcher,
    private val consoleLogger: ConsoleLogger
  ) : DataFetcher<ByteBuffer> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ByteBuffer>) {
      // Defer execution to the app's dispatchers since synchronization is needed (and more
      // performant and easier to achieve with coroutines).
      CoroutineScope(backgroundDispatcher).launch {
        // KotliTeX drawable initialization loads shared static state that's susceptible to race
        // conditions. This synchronizes span creation so that the race condition can't happen,
        // though it will likely slow down LaTeX loading a bit. Fortunately, rendering & PNG
        // creation can still happen in parallel, and those are the more expensive steps.
        val span = withContext(CoroutineScope(blockingDispatcher).coroutineContext) {
          MathExpressionSpan(
            model.rawLatex, model.lineHeight, applicationContext.assets, !model.useInlineRendering
          ).also { it.ensureDrawable() }
        }
        val renderableText = SpannableStringBuilder("\uFFFC").apply {
          setSpan(span, /* start= */ 0, /* end= */ 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Use Android's StaticLayout to ensure the text is rendered correctly. Note that the
        // constants are derived from TextView's defaults (except width which is defaulted to 0
        // since the width isn't necessarily known ahead of time).
        // Any TextPaint can be used since the span will use its own.
        val textPaint = TextPaint()
        @Suppress("DEPRECATION") // This call is necessary for the supported min API version.
        val staticTextLayout =
          StaticLayout(
            renderableText,
            textPaint,
            /* width= */ 0,
            Layout.Alignment.ALIGN_NORMAL,
            /* spacingmult= */ 1f,
            /* spacingadd= */ 0f,
            /* includepad= */ true
          )

        // Estimate the surface necessary for rendering the LaTeX, then compute a tightly-packed
        // bitmap containing rendered pixels. See drawText in BoundsCalculatingSurface and
        // renderAutoSizingBitmap for more details.
        val surface = BoundsCalculatingSurface()
        val totalBounds = surface.also {
          // The x and y are mostly unused by the draw routine.
          span.draw(it, renderableText, x = 0f, y = 0, textPaint)
        }.computeTotalBounds()
        val boundsWidth = totalBounds.width().roundToInt()
        val boundsHeight = totalBounds.height().roundToInt()
        val canvasBitmap =
          renderToAutoSizingBitmap(estimatedWidth = boundsWidth, estimatedHeight = boundsHeight) {
            staticTextLayout.draw(it)
          }

        val finalWidth =
          if (targetWidth == Target.SIZE_ORIGINAL) canvasBitmap.width else targetWidth
        val finalHeight =
          if (targetHeight == Target.SIZE_ORIGINAL) canvasBitmap.height else targetHeight

        // Compute the final bitmap (which might need to be scaled depending on options). Note that
        // any actual scaling here is likely to distort the image since it can be automatically
        // cropped to minimize excess whitespace during rendering.
        val bitmap = if (canvasBitmap.width != finalWidth || canvasBitmap.height != finalHeight) {
          // Glide is requesting the image in a different size, so adjust it.
          Bitmap.createScaledBitmap(canvasBitmap, finalWidth, finalHeight, /* filter= */ true)
        } else canvasBitmap // Otherwise, the original bitmap is the correct size.

        // Convert the bitmap to a PNG to store within Glide's cache for later retrieval.
        val rawBitmap = ByteArrayOutputStream().also { outputStream ->
          bitmap.compress(Bitmap.CompressFormat.PNG, /* quality= */ 100, outputStream)
        }.toByteArray()
        callback.onDataReady(ByteBuffer.wrap(rawBitmap))
      }.invokeOnCompletion {
        if (it != null) {
          consoleLogger.e("ImageLoading", "Failed to convert LaTeX to SVG (model: $model)", it)
        }
      }
    }

    override fun cleanup() {}

    override fun cancel() {}

    override fun getDataClass(): Class<ByteBuffer> = ByteBuffer::class.java

    // 'Retrieval' is expensive in this case since a rendering operation is needed.
    override fun getDataSource(): DataSource = DataSource.REMOTE

    /**
     * A [DrawableSurface] which tracks the bounds necessary to draw each constituent part of LaTeX
     * (rendered by KotliTeX) in order to estimate the bounds necessary to render specific LaTeX.
     */
    private class BoundsCalculatingSurface : DrawableSurface {
      private val initialClipRect =
        RectF(-Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
      private var currentClip = initialClipRect
      private val pastClips = mutableListOf<RectF>()
      private val currentBounds = RectF()

      /**
       * Returns the [RectF] encompassing the estimated space required to render the entirety of all
       * previous render operations called to this surface.
       *
       * Note that the returned [RectF] is a copy so changes to it will not change this class's
       * internal state. Note also that the returned [RectF] is always starting at (0, 0) so its
       * right and bottom values represent the space's width and height, respectively.
       */
      fun computeTotalBounds(): RectF = RectF(currentBounds).apply { offsetTo(0f, 0f) }

      override fun clipRect(rect: RectF) {
        currentClip = currentClip.intersection(rect)
      }

      override fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) {
        currentBounds.ensureIncludes(x0, y0)
        currentBounds.ensureIncludes(x1, y1)
      }

      override fun drawPath(path: Path, paint: Paint) {
        val pathBounds = RectF().also { path.computeBounds(it, /* unusedExact= */ true) }
        currentBounds.union(pathBounds.intersection(currentClip))
      }

      override fun drawRect(rect: RectF, paint: Paint) {
        currentBounds.union(rect.intersection(currentClip))
      }

      override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
        /*
         * Text is particularly difficult to track size for since it's not obvious to actually get
         * the dimensions and position of the space that the actual rendered pixels will occupy.
         * https://stackoverflow.com/a/27631737/3689782 provides context both on how text is laid
         * out, and provides examples of glyphs that can exceed the expected size of a line.
         *
         * This problem is exacerbated by KotliTeX manually positioning glyphs both horizontally and
         * vertically rather than relying on built-in font kerning, tracking, and other rules (for
         * a high-level reference on these, see: https://proandroiddev.com/5f06722dd611).
         *
         * One way to measure text is by using the Paint object (see
         * https://stackoverflow.com/a/18260682/3689782), but this doesn't account for the extra
         * vertical or horizontal space needed for a specific glyph.
         *
         * The chosen solution is to approximate vertical alignment by appending a tall character
         * (such as a parenthesis) on a line below the glyph, then to compute the bounds of the
         * first line and treat this as the size of the glyph. The use of StaticLayout came as a
         * suggestion from https://stackoverflow.com/a/7643312/3689782 and
         * https://stackoverflow.com/a/42091739/3689782. While this still is generally an
         * under-approximation, it's close to the necessary space and pairs well with rendering to a
         * larger canvas that can be cropped down.
         */
        @Suppress("DEPRECATION") // This call is necessary for the supported min API version.
        val staticLayout =
          StaticLayout(
            "$text\n(",
            paint as TextPaint,
            /* width= */ 0,
            Layout.Alignment.ALIGN_NORMAL,
            /* spacingmult= */ 1f,
            /* spacingadd= */ 0f,
            /* includepad= */ true
          )
        val textBounds = staticLayout.getLineBounds().apply { offsetTo(x, y) }
        currentBounds.union(textBounds.intersection(currentClip))
      }

      override fun restore() {
        currentClip = pastClips.removeLast()
      }

      override fun save() {
        pastClips += currentClip
      }
    }

    private companion object {
      /**
       * Returns a new [Bitmap] with the contents produced by [render].
       *
       * This function is useful for cases when the exact dimension requirement of results from
       * [render] may not be known, but a close approximation can be computed.
       *
       * The size of the bitmap is initialized based on heuristic initial width/heights (defined by
       * [estimatedWidth] and [estimatedHeight]). Note that it's possible the rendered contents
       * exceed the size of the bitmap in which case they will be cut off. Otherwise, the returned
       * bitmap will be the smallest bitmap possible to hold the results [render] in a bitmap up to
       * 2x the initial specified dimensions.
       *
       * This method requires creating 2 [Bitmap]s at once, so it may utilize quite a bit of memory.
       */
      private fun renderToAutoSizingBitmap(
        estimatedWidth: Int,
        estimatedHeight: Int,
        render: (Canvas) -> Unit
      ): Bitmap {
        val fullWidth = estimatedWidth * 2
        val fullHeight = estimatedHeight * 2
        val drawX = (fullWidth.toFloat() / 2) - (estimatedWidth.toFloat() / 2)
        val drawY = (fullHeight.toFloat() / 2) - (estimatedHeight.toFloat() / 2)
        val fullRender = Bitmap.createBitmap(fullWidth, fullHeight, ARGB_8888).also { bitmap ->
          Canvas(bitmap).also { canvas ->
            canvas.save()
            // Move initial drawing such that there's a width/2 and height/2 boundary around the
            // entire drawing space for rendering that may overflow.
            canvas.translate(drawX, drawY)
            render(canvas)
            canvas.restore()
          }
        }

        // Initialize with the largest possible "empty" (inverted) rectangle so that *any* pixel
        // will become the entire initial rectangular region.
        val filledRegion =
          RectF(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)
        for (x in 0 until fullRender.width) {
          for (y in 0 until fullRender.height) {
            val pixel = fullRender.getPixel(x, y)
            if ((pixel.toLong() and 0xff000000L) != 0L) {
              // Any not-fully transparent pixels are considered "filled in" parts of the render.
              filledRegion.ensureIncludes(x.toFloat(), y.toFloat())
            }
          }
        }

        return if (!filledRegion.isEmpty) {
          // At least some pixels have been filled.
          val neededWidth = filledRegion.width().roundToInt()
          val neededHeight = filledRegion.height().roundToInt()
          if (neededWidth != fullWidth || neededHeight != fullHeight) {
            // Less space is needed than the original bitmap which means it can be cropped to save
            // on space & memory.
            Bitmap.createBitmap(
              fullRender,
              filledRegion.left.toInt(),
              filledRegion.top.toInt(),
              neededWidth,
              neededHeight
            )
          } else fullRender // Otherwise, just return the original (since the full space is needed).
        } else {
          // The entire render is empty so default to a 1x1 bitmap to conserve memory.
          Bitmap.createBitmap(/* width= */ 1, /* height= */ 1, ARGB_8888)
        }
      }

      private fun RectF.getActualLeft(): Float = min(left, right)
      private fun RectF.getActualRight(): Float = max(left, right)
      private fun RectF.getActualTop(): Float = min(top, bottom)
      private fun RectF.getActualBottom(): Float = max(top, bottom)

      private fun RectF.intersection(other: RectF): RectF {
        // https://stackoverflow.com/a/19754915/3689782 provided a simple approach.
        val intersectedLeft = max(getActualLeft(), other.getActualLeft())
        val intersectedTop = max(getActualTop(), other.getActualTop())
        val intersectedRight = min(getActualRight(), other.getActualRight())
        val intersectedBottom = min(getActualBottom(), other.getActualBottom())

        // Make sure that rectangles which don't at least partially overlap result in a degenerate
        // rectangle rather than a negative one (which would actually represent the union along
        // whichever axis doesn't overlap).
        val (actualLeft, actualRight) = if (intersectedRight < intersectedLeft) {
          0f to 0f
        } else intersectedLeft to intersectedRight
        val (actualTop, actualBottom) = if (intersectedBottom < intersectedTop) {
          0f to 0f
        } else intersectedTop to intersectedBottom
        return RectF(actualLeft, actualTop, actualRight, actualBottom)
      }

      private fun RectF.ensureIncludes(x: Float, y: Float) {
        // Note the '+1' here is necessary since 'right' and 'bottom' are exclusive bounds in the
        // rectangle class (in order for the 'width' and 'height' computations to operate
        // correctly).
        left = min(left, x)
        right = max(right, x + 1)
        top = min(top, y)
        bottom = max(bottom, y + 1)
      }

      private fun StaticLayout.getLineBounds(line: Int = 0): RectF {
        return RectF(
          getLineLeft(line),
          getLineTop(line).toFloat(),
          getLineRight(line),
          getLineBottom(line).toFloat()
        )
      }
    }
  }

  /** [ModelLoaderFactory] for creating new [MathBitmapModelLoader]s. */
  class Factory(
    private val applicationContext: Context
  ) : ModelLoaderFactory<MathModel, ByteBuffer> {
    override fun build(factory: MultiModelLoaderFactory): ModelLoader<MathModel, ByteBuffer> {
      return MathBitmapModelLoader(applicationContext)
    }

    override fun teardown() {}
  }
}
