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

// Reference: https://bumptech.github.io/glide/tut/custom-modelloader.html#writing-the-modelloader.
class MathBitmapModelLoader private constructor(
  private val applicationContext: Context
): ModelLoader<MathModel, ByteBuffer> {
  override fun buildLoadData(
    model: MathModel, width: Int, height: Int, options: Options
  ): ModelLoader.LoadData<ByteBuffer> {
    return ModelLoader.LoadData(
      model.toKeySignature(), LatexModelDataFetcher(applicationContext, model, width, height)
    )
  }

  override fun handles(model: MathModel): Boolean = true

  private class LatexModelDataFetcher(
    private val applicationContext: Context,
    private val model: MathModel,
    private val targetWidth: Int,
    private val targetHeight: Int
  ): DataFetcher<ByteBuffer> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ByteBuffer>) {
      val span = MathExpressionSpan(
        model.rawLatex, model.lineHeight, applicationContext.assets, !model.useInlineRendering
      )
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
          Layout.Alignment.ALIGN_LEFT,
          /* spacingmult= */ 1f,
          /* spacingadd= */ 0f,
          /* includepad= */ true
        )

      // Reference: https://stackoverflow.com/a/27631737/3689782.
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
    }

    override fun cleanup() {}

    override fun cancel() {}

    override fun getDataClass(): Class<ByteBuffer> = ByteBuffer::class.java

    // 'Retrieval' is expensive in this case since a rendering operation is needed.
    override fun getDataSource(): DataSource = DataSource.REMOTE
  }

  class Factory(
    private val applicationContext: Context
  ): ModelLoaderFactory<MathModel, ByteBuffer> {
    override fun build(factory: MultiModelLoaderFactory): ModelLoader<MathModel, ByteBuffer> {
      return MathBitmapModelLoader(applicationContext)
    }

    override fun teardown() {}
  }
}
