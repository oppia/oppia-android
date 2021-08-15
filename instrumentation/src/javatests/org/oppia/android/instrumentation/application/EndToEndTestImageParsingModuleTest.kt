package org.oppia.android.instrumentation.application

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.parser.image.DefaultGcsPrefix
import org.oppia.android.util.parser.image.ImageDownloadUrlTemplate
import org.oppia.android.util.parser.image.ThumbnailDownloadUrlTemplate
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = EndToEndTestImageParsingModuleTest.TestApplication::class)
class EndToEndTestImageParsingModuleTest {

  @field:[Inject DefaultGcsPrefix]
  lateinit var defaultGcsPrefix: String

  @field:[Inject ImageDownloadUrlTemplate]
  lateinit var imageDownloadUrlTemplate: String

  @field:[Inject ThumbnailDownloadUrlTemplate]
  lateinit var thumbnailDownloadUrlTemplate: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_defaultGcsPrefix_isLocalHost() {
    assertThat(defaultGcsPrefix).isEqualTo("http://localhost:8181/")
  }

  @Test
  fun testModule_imageDownloadUrlTemplate_isImageTemplate() {
    assertThat(imageDownloadUrlTemplate).isEqualTo("%s/%s/assets/image/%s")
  }

  @Test
  fun testModule_thumbnailDownloadUrlTemplate_isThumbnailTemplate() {
    assertThat(thumbnailDownloadUrlTemplate).isEqualTo("%s/%s/assets/thumbnail/%s")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, EndToEndTestImageParsingModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(endToEndTestImageParsingModuleTest: EndToEndTestImageParsingModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerEndToEndTestImageParsingModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(endToEndTestImageParsingModuleTest: EndToEndTestImageParsingModuleTest) {
      component.inject(endToEndTestImageParsingModuleTest)
    }
  }
}
