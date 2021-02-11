package org.oppia.android.util.datetime

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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000
// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000
// Time: Tue Apr 23 2019 14:22:00
private const val AFTERNOON_TIMESTAMP = 1556029320000

/** Tests for [DateTimeUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DateTimeUtilTest {

  @Inject lateinit var dateTimeUtil: DateTimeUtil
  @Inject lateinit var context: Context
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
  }

  private fun setUpTestApplicationComponent() {
    DaggerDateTimeUtilTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testGreetingMessageBasedOnTime_goodEveningMessageSucceeded() {
    fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)
    assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good evening,")
  }

  @Test
  fun testGreetingMessageBasedOnTime_goodMorningMessageSucceeded() {
    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)
    assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good morning,")
  }

  @Test
  fun testGreetingMessageBasedOnTime_goodAfternoonMessageSucceeded() {
    fakeOppiaClock.setCurrentTimeToSameDateTime(AFTERNOON_TIMESTAMP)
    assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good afternoon,")
  }

  // TODO(#89): Move this to a common test application component.
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
      TestModule::class,
      RobolectricModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(dateTimeUtilTest: DateTimeUtilTest)
  }
}
