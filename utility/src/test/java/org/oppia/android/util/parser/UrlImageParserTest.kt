package org.oppia.android.util.parser

import android.app.Application
import android.content.Context
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
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.CustomHtmlContentHandler.ImageRetriever
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
  fun testLoadDrawable_bitmap_blockType_loadsBitmapImage() {
    urlImageParser.loadDrawable("test_image.png", ImageRetriever.Type.BLOCK_IMAGE)

    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.png")
  }

  @Test
  fun testLoadDrawable_bitmap_inlineType_loadsBitmapImage() {
    urlImageParser.loadDrawable("test_image.png", ImageRetriever.Type.INLINE_TEXT_IMAGE)

    // The request to load the bitmap inline is ignored since inline bitmaps aren't supported. The
    // bitmap is instead loaded in block format.
    // TODO(#3085): Introduce test for verifying that the warning log is logged in this case.
    val loadedBitmaps = testGlideImageLoader.getLoadedBitmaps()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.png")
  }

  @Test
  fun testLoadDrawable_svg_blockType_loadsSvgBlockImage() {
    urlImageParser.loadDrawable("test_image.svg", ImageRetriever.Type.BLOCK_IMAGE)

    val loadedBitmaps = testGlideImageLoader.getLoadedBlockSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svg")
  }

  @Test
  fun testLoadDrawable_svg_inlineType_loadsSvgTextImage() {
    urlImageParser.loadDrawable("test_image.svg", ImageRetriever.Type.INLINE_TEXT_IMAGE)

    // The request to load the bitmap inline is ignored since inline bitmaps aren't supported.
    val loadedBitmaps = testGlideImageLoader.getLoadedTextSvgs()
    assertThat(loadedBitmaps).hasSize(1)
    assertThat(loadedBitmaps.first()).contains("test_image.svg")
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
      CachingTestModule::class, ImageParsingModule::class
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
