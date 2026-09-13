package org.oppia.android.util.parser.math

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumptech.glide.Priority
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyFloat
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.ConsoleLoggerInjector
import org.oppia.android.util.logging.ConsoleLoggerInjectorProvider
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.threading.DispatcherInjector
import org.oppia.android.util.threading.DispatcherInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MathBitmapModelLoader]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MathBitmapModelLoaderTest.TestApplication::class)
class MathBitmapModelLoaderTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var application: Application

  @Before
  fun setUp() {
    application = ApplicationProvider.getApplicationContext()
    (application as TestApplication).inject(this)
  }

  @Test
  fun testMathCanvasSurface_drawLine_setsAntiAliasToTrue() {
    val canvas = mock(Canvas::class.java)
    val mathCanvasSurface = MathBitmapModelLoader.MathCanvasSurface(canvas, density = 1.0f)
    val paint = Paint().apply { isAntiAlias = false }

    mathCanvasSurface.drawLine(0f, 0f, 10f, 0f, paint)

    assertThat(paint.isAntiAlias).isTrue()
  }

  @Test
  fun testMathCanvasSurface_drawLine_thinStroke_enforcesMinimumStrokeWidth() {
    val canvas = mock(Canvas::class.java)
    val density = 2.0f
    val mathCanvasSurface = MathBitmapModelLoader.MathCanvasSurface(canvas, density)
    val paint = Paint().apply { strokeWidth = 0.5f }
    var strokeWidthDuringDraw = 0f

    doAnswer {
      val paintArg = it.getArgument<Paint>(4)
      strokeWidthDuringDraw = paintArg.strokeWidth
      null
    }.`when`(canvas).drawLine(
      anyFloat(), anyFloat(), anyFloat(), anyFloat(), any(Paint::class.java)
    )

    mathCanvasSurface.drawLine(0f, 10f, 20f, 10f, paint)

    val expectedMinStrokeWidth = MathBitmapModelLoader.MIN_FRACTION_BAR_THICKNESS_DP * density
    assertThat(strokeWidthDuringDraw).isEqualTo(expectedMinStrokeWidth)
    // Original paint's stroke width is restored after drawLine completes.
    assertThat(paint.strokeWidth).isEqualTo(0.5f)
  }

  @Test
  fun testMathCanvasSurface_drawLine_thickStroke_preservesLargerStrokeWidth() {
    val canvas = mock(Canvas::class.java)
    val density = 2.0f
    val mathCanvasSurface = MathBitmapModelLoader.MathCanvasSurface(canvas, density)
    val paint = Paint().apply { strokeWidth = 5.0f }
    var strokeWidthDuringDraw = 0f

    doAnswer {
      val paintArg = it.getArgument<Paint>(4)
      strokeWidthDuringDraw = paintArg.strokeWidth
      null
    }.`when`(canvas).drawLine(
      anyFloat(), anyFloat(), anyFloat(), anyFloat(), any(Paint::class.java)
    )

    mathCanvasSurface.drawLine(0f, 10f, 20f, 10f, paint)

    assertThat(strokeWidthDuringDraw).isEqualTo(5.0f)
    assertThat(paint.strokeWidth).isEqualTo(5.0f)
  }

  @Test
  fun testMathCanvasSurface_delegatesOtherMethodsToCanvas() {
    val canvas = mock(Canvas::class.java)
    val mathCanvasSurface = MathBitmapModelLoader.MathCanvasSurface(canvas, density = 1.0f)
    val paint = Paint()
    val rect = RectF(0f, 0f, 10f, 10f)
    val path = Path()

    mathCanvasSurface.save()
    verify(canvas).save()

    mathCanvasSurface.restore()
    verify(canvas).restore()

    mathCanvasSurface.clipRect(rect)
    verify(canvas).clipRect(rect)

    mathCanvasSurface.drawRect(rect, paint)
    verify(canvas).drawRect(rect, paint)

    mathCanvasSurface.drawPath(path, paint)
    verify(canvas).drawPath(path, paint)

    mathCanvasSurface.drawText("test", 5f, 5f, paint)
    verify(canvas).drawText("test", 5f, 5f, paint)
  }

  @Test
  fun testBoundsCalculatingSurface_drawLine_accountsForMinStrokeWidthExpansion() {
    val density = 2.0f
    val surface = MathBitmapModelLoader.BoundsCalculatingSurface(density)
    val paint = Paint().apply { strokeWidth = 0.5f }

    // Draw along baseline (y = 0f) where math elements are aligned.
    surface.drawLine(0f, 0f, 30f, 0f, paint)

    val bounds = surface.computeTotalBounds()
    val expectedMinStrokeWidth = MathBitmapModelLoader.MIN_FRACTION_BAR_THICKNESS_DP * density
    val expectedHalfStroke = expectedMinStrokeWidth / 2f

    // Total bounds is offset to (0, 0), so width is right and height is bottom.
    // X goes from 0 to 30 -> width with +1 offset is 30 - 0 + 1 = 31
    assertThat(bounds.left).isEqualTo(0f)
    assertThat(bounds.top).isEqualTo(0f)
    assertThat(bounds.width()).isEqualTo(31f)
    // Height should accommodate the top and bottom vertical extensions (+1 for inclusive right/bottom):
    // y0 - halfStroke to y1 + halfStroke = 0 - 1.5 to 0 + 1.5 = -1.5 to 1.5 -> span = 3.0 + 1 = 4.0
    val expectedHeight = (expectedHalfStroke * 2f) + 1f
    assertThat(bounds.height()).isEqualTo(expectedHeight)
  }

  @Test
  fun testBoundsCalculatingSurface_clipRect_restrictsBounds() {
    val surface = MathBitmapModelLoader.BoundsCalculatingSurface(density = 1.0f)
    val paint = Paint()

    surface.clipRect(RectF(0f, 0f, 50f, 50f))
    surface.drawRect(RectF(10f, 10f, 100f, 100f), paint)

    val bounds = surface.computeTotalBounds()
    assertThat(bounds.width()).isAtMost(51f)
    assertThat(bounds.height()).isAtMost(51f)
  }

  @Test
  fun testBoundsCalculatingSurface_saveAndRestore_restoresPreviousClip() {
    val surface = MathBitmapModelLoader.BoundsCalculatingSurface(density = 1.0f)
    val paint = Paint()

    surface.save()
    surface.clipRect(RectF(0f, 0f, 10f, 10f))
    surface.restore()

    surface.drawRect(RectF(0f, 0f, 50f, 50f), paint)

    val bounds = surface.computeTotalBounds()
    assertThat(bounds.width()).isGreaterThan(10f)
  }

  @Test
  fun testFactory_build_createsModelLoader() {
    val factory = MathBitmapModelLoader.Factory(application)
    val mockMultiFactory = mock(MultiModelLoaderFactory::class.java)

    val modelLoader = factory.build(mockMultiFactory)

    assertThat(modelLoader).isNotNull()
    assertThat(modelLoader).isInstanceOf(MathBitmapModelLoader::class.java)
  }

  @Test
  fun testModelLoader_handles_returnsTrue() {
    val factory = MathBitmapModelLoader.Factory(application)
    val mockMultiFactory = mock(MultiModelLoaderFactory::class.java)
    val modelLoader = factory.build(mockMultiFactory)
    val model = MathModel(
      rawLatex = "\\frac{1}{7}",
      lineHeight = 20f,
      useInlineRendering = true,
      equationColor = Color.BLACK
    )

    assertThat(modelLoader.handles(model)).isTrue()
  }

  @Test
  fun testModelLoader_buildLoadData_returnsExpectedKeySignature() {
    val factory = MathBitmapModelLoader.Factory(application)
    val mockMultiFactory = mock(MultiModelLoaderFactory::class.java)
    val modelLoader = factory.build(mockMultiFactory)
    val model = MathModel(
      rawLatex = "\\frac{1}{7}",
      lineHeight = 20f,
      useInlineRendering = true,
      equationColor = Color.BLACK
    )

    val loadData = modelLoader.buildLoadData(model, /* width= */ 100, /* height= */ 100, Options())

    assertThat(loadData).isNotNull()
    assertThat(loadData?.sourceKey).isEqualTo(model.toKeySignature())
  }

  @Test
  fun testModelLoader_loadData_withFraction_rendersValidBitmap() {
    val factory = MathBitmapModelLoader.Factory(application)
    val mockMultiFactory = mock(MultiModelLoaderFactory::class.java)
    val modelLoader = factory.build(mockMultiFactory)
    val model = MathModel(
      rawLatex = "\\frac{1}{7}",
      lineHeight = 20f,
      useInlineRendering = true,
      equationColor = Color.BLACK
    )

    val loadData = modelLoader.buildLoadData(model, /* width= */ 100, /* height= */ 100, Options())
    @Suppress("UNCHECKED_CAST")
    val callback: DataFetcher.DataCallback<ByteBuffer> =
      mock(DataFetcher.DataCallback::class.java) as DataFetcher.DataCallback<ByteBuffer>
    val byteBufferCaptor = ArgumentCaptor.forClass(ByteBuffer::class.java)

    loadData?.fetcher?.loadData(Priority.NORMAL, callback)
    testCoroutineDispatchers.runCurrent()

    verify(callback).onDataReady(byteBufferCaptor.capture())
    val byteBuffer = byteBufferCaptor.value
    assertThat(byteBuffer).isNotNull()
    val bytes = ByteArray(byteBuffer.remaining()).also { byteBuffer.get(it) }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    assertThat(bitmap).isNotNull()
    assertThat(bitmap.width).isGreaterThan(0)
    assertThat(bitmap.height).isGreaterThan(0)
  }

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      LoggerModule::class,
      FakeOppiaClockModule::class,
      RobolectricModule::class,
      LocaleProdModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent : DispatcherInjector, ConsoleLoggerInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: MathBitmapModelLoaderTest)
  }

  class TestApplication :
    Application(), DispatcherInjectorProvider, ConsoleLoggerInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMathBitmapModelLoaderTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: MathBitmapModelLoaderTest) {
      component.inject(test)
    }

    override fun getDispatcherInjector(): DispatcherInjector = component

    override fun getConsoleLoggerInjector(): ConsoleLoggerInjector = component
  }
}
