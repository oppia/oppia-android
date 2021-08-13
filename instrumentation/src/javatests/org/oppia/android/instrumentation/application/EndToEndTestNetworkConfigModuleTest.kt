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
import org.oppia.android.data.backends.gae.BaseUrl
import org.oppia.android.data.backends.gae.XssiPrefix
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = EndToEndTestNetworkConfigModuleTest.TestApplication::class)
class EndToEndTestNetworkConfigModuleTest {

  @field:[Inject BaseUrl]
  lateinit var baseUrl: String

  @field:[Inject XssiPrefix]
  lateinit var xssiPrefix: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_baseUrl_isLocalhost() {
    assertThat(baseUrl).isEqualTo("http://localhost:8181")
  }

  @Test
  fun testModule_xssiPrefix_isXssiPrefix() {
    assertThat(xssiPrefix).isEqualTo(")]}'")
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
      TestModule::class, EndToEndTestNetworkConfigModule::class
    ]
  )

  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(endTestNetworkConfigModuleTest: EndToEndTestNetworkConfigModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerEndToEndTestNetworkConfigModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(endToEndTestNetworkConfigModuleTest: EndToEndTestNetworkConfigModuleTest) {
      component.inject(endToEndTestNetworkConfigModuleTest)
    }
  }
}
