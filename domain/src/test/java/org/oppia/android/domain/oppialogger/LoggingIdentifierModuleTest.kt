package org.oppia.android.domain.oppialogger

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.system.OppiaClock
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import com.google.common.truth.Truth.assertThat

/** Tests for [LoggingIdentifierModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class LoggingIdentifierModuleTest {
  private companion object {
    private const val FIXED_CURRENT_TIME_MS = 12345L
  }

  @field:[JvmField Inject ApplicationIdSeed] var applicationIdSeed: Long = Long.MIN_VALUE

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testInjectApplicationIdSeed_isTheCurrentTimeInMillis() {
    assertThat(applicationIdSeed).isEqualTo(FIXED_CURRENT_TIME_MS)
  }

  private fun setUpTestApplicationComponent() {
    DaggerLoggingIdentifierModuleTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // FakeOppiaClock can't be used since this test suite needs to verify an injection-time clock
    // call, and the fake defaults to wall-clock time and can't be configured until after injection
    // time.
    @Provides
    fun provideOppiaClock(): OppiaClock = object: OppiaClock {
      override fun getCurrentTimeMs(): Long = FIXED_CURRENT_TIME_MS
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, LoggingIdentifierModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: LoggingIdentifierModuleTest)
  }
}
