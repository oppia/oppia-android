package org.oppia.android.testing.threading

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.IsOnRobolectric
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Tests for [TestCoroutineDispatcher] (particularly its injectability; specific behaviors are
 * tested in implementation-specific test classes.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = TestCoroutineDispatcherTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class TestCoroutineDispatcherTest {
  @Inject
  @field:BackgroundTestDispatcher
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  @Inject
  @field:BlockingTestDispatcher
  lateinit var blockingTestDispatcher: TestCoroutineDispatcher

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Inject
  @field:BlockingDispatcher
  lateinit var blockingDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    TestModule.isOnRobolectric = null
  }

  @Test
  fun testBackgroundTestDispatcher_onRobolectric_isRobolectricImpl() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    assertThat(backgroundTestDispatcher).isInstanceOf(TestCoroutineDispatcherRobolectricImpl::class)
  }

  @Test
  fun testBackgroundTestDispatcher_onEspresso_isEpressoImpl() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    assertThat(backgroundTestDispatcher).isInstanceOf(TestCoroutineDispatcherEspressoImpl::class)
  }

  @Test
  fun testBlockingDispatcher_onRobolectric_isRobolectricImpl() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    assertThat(blockingTestDispatcher).isInstanceOf(TestCoroutineDispatcherRobolectricImpl::class)
  }

  @Test
  fun testBlockingDispatcher_onEspresso_isEpressoImpl() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    // Verify that the real background dispatcher is replaced by the correct test dispatcher.
    assertThat(blockingTestDispatcher).isInstanceOf(TestCoroutineDispatcherEspressoImpl::class)
  }

  @Test
  fun testBackgroundDispatcher_onRobolectric_isRobolectricImpl() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    // Verify that the real background dispatcher is replaced by the correct test dispatcher.
    assertThat(backgroundDispatcher).isInstanceOf(TestCoroutineDispatcherRobolectricImpl::class)
  }

  @Test
  fun testBackgroundDispatcher_onEspresso_isEpressoImpl() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    // Verify that the real blocking dispatcher is replaced by the correct test dispatcher.
    assertThat(backgroundDispatcher).isInstanceOf(TestCoroutineDispatcherEspressoImpl::class)
  }

  @Test
  fun testBlockingTestDispatcher_onRobolectric_isRobolectricImpl() {
    arrangeOnRobolectric()

    setUpTestApplicationComponent()

    // Verify that the real blocking dispatcher is replaced by the correct test dispatcher.
    assertThat(blockingDispatcher).isInstanceOf(TestCoroutineDispatcherRobolectricImpl::class)
  }

  @Test
  fun testBlockingTestDispatcher_onEspresso_isEpressoImpl() {
    arrangeOnEspresso()

    setUpTestApplicationComponent()

    assertThat(blockingDispatcher).isInstanceOf(TestCoroutineDispatcherEspressoImpl::class)
  }

  @Test
  fun testDispatcher_defaultTimeout_robolectric_noDebugger_isTenSeconds() {
    arrangeOnRobolectric()
    simulateIntellijDebuggerEnabled(false)
    setUpTestApplicationComponent()

    val defaultTime = backgroundTestDispatcher.DEFAULT_TIMEOUT_SECONDS
    val defaultTimeUnit = backgroundTestDispatcher.DEFAULT_TIMEOUT_UNIT

    // NB: this cannot be reliably tested on Espresso due to how the timeout is computed.
    assertThat(defaultTime).isEqualTo(10)
    assertThat(defaultTimeUnit).isEqualTo(TimeUnit.SECONDS)
  }

  @Test
  fun testDispatcher_defaultTimeout_robolectric_withIntellijDebugger_isOneHour() {
    arrangeOnRobolectric()
    simulateIntellijDebuggerEnabled(true)
    setUpTestApplicationComponent()

    val defaultTime = backgroundTestDispatcher.DEFAULT_TIMEOUT_SECONDS
    val defaultTimeUnit = backgroundTestDispatcher.DEFAULT_TIMEOUT_UNIT
    val defaultTimeMinutes = TimeUnit.MINUTES.convert(defaultTime, defaultTimeUnit)

    // NB: this cannot be reliably tested on Espresso due to how the timeout is computed.
    assertThat(defaultTimeMinutes).isEqualTo(60)
  }

  private fun arrangeOnRobolectric() {
    TestModule.isOnRobolectric = { true }
  }

  private fun arrangeOnEspresso() {
    TestModule.isOnRobolectric = { false }
  }

  private fun simulateIntellijDebuggerEnabled(enabled: Boolean) {
    System.setProperty("intellij.debug.agent", enabled.toString())
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun Subject.isInstanceOf(type: KClass<*>) = isInstanceOf(type.java)

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    internal companion object {
      var isOnRobolectric: (() -> Boolean)? = null
    }

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

    @Provides
    @IsOnRobolectric
    fun provideIsOnRobolectric(): Boolean =
      checkNotNull(isOnRobolectric) { "Test is not correctly initialized" }()
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      TestModule::class,
      TestLogReportingModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: TestCoroutineDispatcherTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestCoroutineDispatcherTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: TestCoroutineDispatcherTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
