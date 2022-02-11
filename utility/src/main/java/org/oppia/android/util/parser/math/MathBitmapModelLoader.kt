package org.oppia.android.util.parser.math

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
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
import io.github.karino2.kotlitex.view.MathExpressionSpan
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ConsoleLoggerInjectorProvider
import org.oppia.android.util.threading.DispatcherInjectorProvider

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
        // constants are derived from TextView's defaults (except width which is defaulted to 0 since
        // the width isn't necessarily known ahead of time).
        @Suppress("DEPRECATION") // This call is necessary for the supported min API version.
        val staticTextLayout =
          StaticLayout(
            renderableText,
            TextPaint(), // Any TextPaint can be used since the span will use its own.
            /* width= */ 0,
            Layout.Alignment.ALIGN_NORMAL,
            /* spacingmult= */ 1f,
            /* spacingadd= */ 0f,
            /* includepad= */ true
          )

        // Reference for how Android manages different parts of text during rendering:
        // https://stackoverflow.com/a/27631737/3689782. Note that the specifics of how text
        // properties are used to compute these bounds are in the modified KotliTeX implementation
        // (see the getter implementation for the property below).
        val bounds = span.drawableBounds
        val canvasBitmap =
          Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(canvasBitmap)
        staticTextLayout.draw(bitmapCanvas)

        val finalWidth = if (targetWidth == Target.SIZE_ORIGINAL) bounds.width() else targetWidth
        val finalHeight = if (targetHeight == Target.SIZE_ORIGINAL) bounds.height() else targetHeight

        // Compute the final bitmap (which might need to be scaled depending on options).
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
