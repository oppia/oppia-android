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
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.gcsresource.QuestionResourceBucketName
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = EndToEndTestGcsResourceModuleTest.TestApplication::class)
class EndToEndTestGcsResourceModuleTest {

  @field:[Inject DefaultResourceBucketName]
  lateinit var defaultResourceBucketName: String

  @field:[Inject QuestionResourceBucketName]
  lateinit var questionResourceBucketName: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_defaultGcsResource_isAssetsDevHandler() {
    assertThat(defaultResourceBucketName).isEqualTo("assetsdevhandler")
  }

  @Test
  fun testModule_questionResourceBucketName_isAssetsDevHandler() {
    assertThat(questionResourceBucketName).isEqualTo("assetsdevhandler")
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
      TestModule::class, EndToEndTestGcsResourceModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(endTestGcsResourceModuleTest: EndToEndTestGcsResourceModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerEndToEndTestGcsResourceModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(endTestGcsResourceModuleTest: EndToEndTestGcsResourceModuleTest) {
      component.inject(endTestGcsResourceModuleTest)
    }
  }
}
