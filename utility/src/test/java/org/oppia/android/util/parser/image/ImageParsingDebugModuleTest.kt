package org.oppia.android.util.parser.image

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [ImageParsingDebugModule]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ImageParsingDebugModuleTest {

  private lateinit var imageParsingDebugModule: ImageParsingDebugModule

  @Before
  fun setUp() {
    imageParsingDebugModule = ImageParsingDebugModule()
  }

  @Test
  fun testProvideDefaultGcsPrefix_returnsDevContentUri() {
    val prefix = imageParsingDebugModule.provideDefaultGcsPrefix()
    assertThat(prefix).isEqualTo("content://org.oppia.android.provider.gcs")
  }

  @Test
  fun testProvideImageDownloadUrlTemplate_returnsCorrectTemplate() {
    val template = imageParsingDebugModule.provideImageDownloadUrlTemplate()
    assertThat(template).isEqualTo("%s/%s/assets/image/%s")
  }

  @Test
  fun testProvideThumbnailDownloadUrlTemplate_returnsCorrectTemplate() {
    val template = imageParsingDebugModule.provideThumbnailDownloadUrlTemplate()
    assertThat(template).isEqualTo("%s/%s/assets/thumbnail/%s")
  }

  @Singleton
  @Component(modules = [ImageParsingDebugModule::class])
  interface TestComponent {
    @DefaultGcsPrefix
    fun getDefaultGcsPrefix(): String

    @ImageDownloadUrlTemplate
    fun getImageDownloadUrlTemplate(): String

    @ThumbnailDownloadUrlTemplate
    fun getThumbnailDownloadUrlTemplate(): String
  }
}
