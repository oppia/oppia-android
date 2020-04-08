package org.oppia.util.datetime

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.system.OppiaClock
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000
// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000
// Time: Tue Apr 23 2019 14:22:00
private const val AFTERNOON_TIMESTAMP = 1556029320000

/** Tests for [DateTimeUtil]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class DateTimeUtilTest {

  @Inject lateinit var dateTimeUtil: DateTimeUtil
  @Inject lateinit var context: Context
  @Inject lateinit var oppiaClock: OppiaClock

  // Reference document: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerDateTimeUtilTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testGreetingMessageBasedOnTime_goodEveningMessageSucceeded() {
    oppiaClock.setCurrentTimeMs(EVENING_TIMESTAMP)
    assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good evening,")
  }

  @Test
  fun testGreetingMessageBasedOnTime_goodMorningMessageSucceeded() {
    oppiaClock.setCurrentTimeMs(MORNING_TIMESTAMP)
    assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good morning,")
  }

  @Test
  fun testGreetingMessageBasedOnTime_goodAfternoonMessageSucceeded() {
    oppiaClock.setCurrentTimeMs(AFTERNOON_TIMESTAMP)
    assertThat(dateTimeUtil.getGreetingMessage()).isEqualTo("Good afternoon,")
  }

  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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
  @Component(modules = [TestModule::class])
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
