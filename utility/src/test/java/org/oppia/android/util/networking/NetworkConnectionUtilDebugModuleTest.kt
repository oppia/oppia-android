package org.oppia.android.util.networking

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
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [NetworkConnectionUtilDebugModule]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NetworkConnectionUtilDebugModuleTest {

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testNetworkConnectionUtilProdModule_debugImplementationIsInjected() {
    assertThat(networkConnectionUtil).isInstanceOf(NetworkConnectionUtilDebugImpl::class.java)
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkConnectionUtilDebugModuleTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  /** Test specific dagger module for [NetworkConnectionUtilDebugModuleTest]. */
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, NetworkConnectionUtilDebugModule::class
    ]
  )
  /** Test specific [ApplicationComponent] for [NetworkConnectionUtilDebugModuleTest]. */
  interface TestApplicationComponent {
    /** Test specific [Component.Builder] for [TestApplicationComponent]. */
    @Component.Builder
    interface Builder {
      /** Binds [Application] to [TestApplicationComponent]. */
      @BindsInstance
      fun setApplication(application: Application): Builder

      /** Builds [TestApplicationComponent]. */
      fun build(): TestApplicationComponent
    }

    /**
     * Injects [TestApplicationComponent] to [NetworkConnectionUtilDebugModuleTest] providing the required
     * dagger modules.
     */
    fun inject(networkConnectionUtilDebugModuleTest: NetworkConnectionUtilDebugModuleTest)
  }
}
