package org.oppia.android.util.parser.image

import android.app.Application
import android.content.Context
import android.os.Looper
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.testing.TestImageLoaderModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever.Type.BLOCK_IMAGE
import org.oppia.android.util.parser.html.CustomHtmlContentHandler.ImageRetriever.Type.INLINE_TEXT_IMAGE
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [UrlImageParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class UrlImageParserTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var urlImageParserFactory: UrlImageParser.Factory
  @Inject lateinit var context: Context
  @Inject lateinit var testGlideImageLoader: TestGlideImageLoader

  private lateinit var testView: TextView
  private lateinit var urlImageParser: UrlImageParser

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testView = TextView(context)
    urlImageParser = urlImageParserFactory.create(
      testView,
      gcsResourceName = "test_gcs_bucket",
      entityType = "test_entity_type",
      entityId = "test_entity_id",
      imageCenterAlign = true
    )
  }

  // TODO(#277): Add more test cases for loading images. The below doesn't include layout or
  //  sizing/positioning.

  @Test
  fun testGetDrawable_bitmap_loadsBitmapImage() {
    urlImageParser.getDrawable("test_image.png")

    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.png")
  }

  @Test
  fun testGetDrawable_svg_loadsSvgBlockImage() {
    urlImageParser.getDrawable("test_image.svg")

    val loadedBitmaps = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svg")
  }

  @Test
  fun testLoadDrawable_bitmapFromUrl_loadsBlockImage() {
    val imageUrl = "https://example.com/test_image.png"
    urlImageParser.loadDrawable(imageUrl, BLOCK_IMAGE)

    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.png")
  }

  @Test
  fun testLoadDrawable_svgFromUrlBlockType_loadsBlockSvgImage() {
    val imageUrl = "https://example.com/test_image.svg"
    urlImageParser.loadDrawable(imageUrl, BLOCK_IMAGE)

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    val loadedSvgs = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedSvgs).hasSize(1)
    assertThat(loadedSvgs.first()).contains("test_image.svg")
  }

  @Test
  fun testLoadDrawable_svgzFromUrlBlockType_loadsBlockSvgImage() {
    val imageUrl = "https://example.com/test_image.svgz"
    urlImageParser.loadDrawable(imageUrl, BLOCK_IMAGE)

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    val loadedSvgs = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedSvgs).hasSize(1)
    assertThat(loadedSvgs.first()).contains("test_image.svgz")
  }

  @Test
  fun testLoadMathDrawable_latexInlineType_loadsInlineLatexImage() {
    val rawLatex = "\\frac{2}{6}"
    urlImageParser.loadMathDrawable(
      rawLatex = rawLatex,
      lineHeight = 20f,
      type = INLINE_TEXT_IMAGE
    )

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    val loadedMathDrawables = testGlideImageLoader.getLoadedMathDrawables()
    assertThat(loadedMathDrawables).hasSize(1)
    assertThat(loadedMathDrawables.first().rawLatex).isEqualTo("\\frac{2}{6}")
    assertThat(loadedMathDrawables.first().useInlineRendering).isTrue()
  }

  @Test
  fun testLoadDrawable_multipleImages_loadsAllImages() {
    val imageUrl1 = "https://example.com/test_image1.png"
    val imageUrl2 = "https://example.com/test_image2.svg"
    val imageUrl3 = "https://example.com/test_image3.svgz"

    urlImageParser.loadDrawable(imageUrl1, BLOCK_IMAGE)
    urlImageParser.loadDrawable(imageUrl2, BLOCK_IMAGE)
    urlImageParser.loadDrawable(imageUrl3, BLOCK_IMAGE)

    Shadows.shadowOf(Looper.getMainLooper()).idle()

    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    val loadedBlockSvgs = testGlideImageLoader.getLoadedBlockSvgs()

    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBlockSvgs).hasSize(2)
    assertThat(loadedBitmaps.first()).contains("test_image1.png")
    assertThat(loadedBlockSvgs.first()).contains("test_image2.svg")
    assertThat(loadedBlockSvgs[1]).contains("test_image3.svgz")
  }

  @Test
  fun testGetDrawable_svgz_loadsSvgzBlockImage() {
    urlImageParser.getDrawable("test_image.svgz")

    val loadedBitmaps = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svgz")
  }

  @Test
  fun testLoadDrawable_bitmap_blockType_loadsBitmapImage() {
    urlImageParser.loadDrawable("test_image.png", BLOCK_IMAGE)

    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.png")
  }

  @Test
  fun testLoadDrawable_bitmap_inlineType_loadsBitmapImage() {
    urlImageParser.loadDrawable("test_image.png", INLINE_TEXT_IMAGE)

    // The request to load the bitmap inline is ignored since inline bitmaps aren't supported. The
    // bitmap is instead loaded in block format.
    // TODO(#3085): Introduce test for verifying that the warning log is logged in this case.
    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.png")
  }

  @Test
  fun testLoadDrawable_svg_blockType_loadsSvgBlockImage() {
    urlImageParser.loadDrawable("test_image.svg", BLOCK_IMAGE)

    val loadedBitmaps = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svg")
  }

  @Test
  fun testLoadDrawable_svgz_blockType_loadsSvgzBlockImage() {
    urlImageParser.loadDrawable("test_image.svgz", BLOCK_IMAGE)

    val loadedBitmaps = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svgz")
  }

  @Test
  fun testLoadDrawable_svg_inlineType_loadsSvgTextImage() {
    urlImageParser.loadDrawable("test_image.svg", INLINE_TEXT_IMAGE)

    // The request to load the bitmap inline is ignored since inline bitmaps aren't supported.
    val loadedBitmaps = testGlideImageLoader.getLoadedTextSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svg")
  }

  @Test
  fun testLoadDrawable_svgz_inlineType_loadsSvgzTextImage() {
    urlImageParser.loadDrawable("test_image.svgz", INLINE_TEXT_IMAGE)

    // The request to load the bitmap inline is ignored since inline bitmaps aren't supported.
    val loadedBitmaps = testGlideImageLoader.getLoadedTextSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svgz")
  }

  @Test
  fun testLoadDrawable_latex_inlineType_loadsInlineLatexImage() {
    urlImageParser.loadMathDrawable(
      rawLatex = "\\frac{2}{6}", lineHeight = 20f, type = INLINE_TEXT_IMAGE
    )

    val mathDrawables = testGlideImageLoader.getLoadedMathDrawables()
    assertThat(mathDrawables).hasSize(1)
    assertThat(mathDrawables.first().rawLatex).isEqualTo("\\frac{2}{6}")
    assertThat(mathDrawables.first().lineHeight).isWithin(1e-5f).of(20f)
    assertThat(mathDrawables.first().useInlineRendering).isTrue()
  }

  @Test
  fun testLoadDrawable_latex_blockType_loadsBlockLatexImage() {
    urlImageParser.loadMathDrawable(
      rawLatex = "\\frac{2}{6}", lineHeight = 20f, type = BLOCK_IMAGE
    )

    val mathDrawables = testGlideImageLoader.getLoadedMathDrawables()
    assertThat(mathDrawables).hasSize(1)
    assertThat(mathDrawables.first().rawLatex).isEqualTo("\\frac{2}{6}")
    assertThat(mathDrawables.first().lineHeight).isWithin(1e-5f).of(20f)
    assertThat(mathDrawables.first().useInlineRendering).isFalse()
  }

  @Test
  fun testLoadDrawable_latex_multiple_loadsEachLatexImage() {
    urlImageParser.loadMathDrawable(
      rawLatex = "\\frac{1}{6}", lineHeight = 20f, type = INLINE_TEXT_IMAGE
    )
    urlImageParser.loadMathDrawable(
      rawLatex = "\\frac{2}{6}", lineHeight = 20f, type = INLINE_TEXT_IMAGE
    )
    urlImageParser.loadMathDrawable(
      rawLatex = "\\frac{2}{6}", lineHeight = 19f, type = INLINE_TEXT_IMAGE
    )
    urlImageParser.loadMathDrawable(
      rawLatex = "\\frac{2}{6}", lineHeight = 20f, type = BLOCK_IMAGE
    )

    val mathDrawables = testGlideImageLoader.getLoadedMathDrawables()
    assertThat(mathDrawables).hasSize(4)
  }

  private fun setUpTestApplicationComponent() {
    DaggerUrlImageParserTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, RobolectricModule::class,
      FakeOppiaClockModule::class, LoggerModule::class, TestImageLoaderModule::class,
      CachingTestModule::class, ImageParsingModule::class, AssetModule::class,
      LocaleProdModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(urlImageParserTest: UrlImageParserTest)
  }
}
