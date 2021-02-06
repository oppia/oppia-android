package org.oppia.android.testing.time

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.collect.Range
import com.google.common.truth.IntegerSubject
import com.google.common.truth.LongSubject
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.time.FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME
import org.oppia.android.testing.time.FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS
import org.oppia.android.testing.time.FakeOppiaClock.FakeTimeMode.MODE_WALL_CLOCK_TIME
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000

/** Tests for [FakeOppiaClock]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FakeOppiaClockTest.TestApplication::class)
class FakeOppiaClockTest {
  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetFakeTimeMode_initialState_isWallClockTime() {
    val timeMode = fakeOppiaClock.getFakeTimeMode()

    assertThat(timeMode).isEqualTo(MODE_WALL_CLOCK_TIME)
  }

  @Test
  fun testSetFakeTimeMode_wallClockMode_getTimeMode_returnsWallClockMode() {
    fakeOppiaClock.setFakeTimeMode(MODE_WALL_CLOCK_TIME)

    val timeMode = fakeOppiaClock.getFakeTimeMode()
    assertThat(timeMode).isEqualTo(MODE_WALL_CLOCK_TIME)
  }

  @Test
  fun testSetFakeTimeMode_fixedTimeMode_getTimeMode_returnsFixedMode() {
    fakeOppiaClock.setFakeTimeMode(MODE_FIXED_FAKE_TIME)

    val timeMode = fakeOppiaClock.getFakeTimeMode()
    assertThat(timeMode).isEqualTo(MODE_FIXED_FAKE_TIME)
  }

  @Test
  fun testSetFakeTimeMode_uptimeMillisMode_getTimeMode_returnsUptimeMillisMode() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)

    val timeMode = fakeOppiaClock.getFakeTimeMode()
    assertThat(timeMode).isEqualTo(MODE_UPTIME_MILLIS)
  }

  @Test
  fun testSetFakeTimeMode_wallClockMode_afterOtherMode_getTimeMode_returnsWallClockMode() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)

    fakeOppiaClock.setFakeTimeMode(MODE_WALL_CLOCK_TIME)

    val timeMode = fakeOppiaClock.getFakeTimeMode()
    assertThat(timeMode).isEqualTo(MODE_WALL_CLOCK_TIME)
  }

  @Test
  fun testSetCurrentTimeMs_wallClockMode_throwsException() {
    fakeOppiaClock.setFakeTimeMode(MODE_WALL_CLOCK_TIME)

    val exception = assertThrows(IllegalStateException::class) {
      fakeOppiaClock.setCurrentTimeMs(0)
    }
    assertThat(exception).hasMessageThat().contains("MODE_FIXED_FAKE_TIME")
  }

  @Test
  fun testSetCurrentTimeMs_fixedTimeMode_doesNotThrowException() {
    fakeOppiaClock.setFakeTimeMode(MODE_FIXED_FAKE_TIME)

    // Verify: an exception should not be thrown.
    fakeOppiaClock.setCurrentTimeMs(0)
  }

  @Test
  fun testSetCurrentTimeMs_uptimeMillisMode_throwsException() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)

    val exception = assertThrows(IllegalStateException::class) {
      fakeOppiaClock.setCurrentTimeMs(0)
    }
    assertThat(exception).hasMessageThat().contains("MODE_FIXED_FAKE_TIME")
  }

  @Test
  fun testGetCurrentTimeMs_wallClockMode_returnsCurrentTimeMillis() {
    fakeOppiaClock.setFakeTimeMode(MODE_WALL_CLOCK_TIME)

    val currentTimeMs = System.currentTimeMillis()
    val reportedTimeMs = fakeOppiaClock.getCurrentTimeMs()

    // Verify that the reported time is within 4ms of actual time (to provide some guard against
    // flakiness since this is testing real clock time).
    assertThat(reportedTimeMs).isWithin((currentTimeMs - 2)..(currentTimeMs + 2))
    // Sanity check that the fake is, indeed, using real clock time
    assertThat(reportedTimeMs).isNotEqualTo(0)
  }

  @Test
  fun testGetCurrentTimeMs_fixedTimeMode_initialState_returnsZero() {
    fakeOppiaClock.setFakeTimeMode(MODE_FIXED_FAKE_TIME)

    val reportedTimeMs = fakeOppiaClock.getCurrentTimeMs()

    assertThat(reportedTimeMs).isEqualTo(0)
  }

  @Test
  fun testGetCurrentTimeMs_fixedTimeMode_afterSettingTime_returnsSetTime() {
    fakeOppiaClock.setFakeTimeMode(MODE_FIXED_FAKE_TIME)

    fakeOppiaClock.setCurrentTimeMs(123)
    val reportedTimeMs = fakeOppiaClock.getCurrentTimeMs()

    assertThat(reportedTimeMs).isEqualTo(123)
  }

  @Test
  fun testGetCurrentTimeMs_uptimeMillisMode_returnsUptimeMillis() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)

    val reportedTimeMs = fakeOppiaClock.getCurrentTimeMs()

    assertThat(reportedTimeMs).isEqualTo(SystemClock.uptimeMillis())
    // Sanity check to make sure time has progressed in the test.
    assertThat(reportedTimeMs).isNotEqualTo(0)
  }

  @Test
  fun testGetCurrentTimeMs_uptimeMillisMode_afterCoroutineDelay_returnsMillisWithDelay() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)
    val timeBeforeDelaying = SystemClock.uptimeMillis()

    testCoroutineDispatchers.advanceTimeBy(delayTimeMillis = 10)
    val reportedTimeMs = fakeOppiaClock.getCurrentTimeMs()

    // The newly reported time should factor in the time delay from the coroutine dispatcher.
    assertThat(reportedTimeMs).isEqualTo(timeBeforeDelaying + 10)
  }

  @Test
  fun testGetCurrentCalendar_wallClockMode_returnsCalendarWithCurrentTimeMillis() {
    fakeOppiaClock.setFakeTimeMode(MODE_WALL_CLOCK_TIME)

    val currentTimeMs = System.currentTimeMillis()
    val calendar = fakeOppiaClock.getCurrentCalendar()

    // The calendar should be inited to a value close to the clock time at the time it was created.
    assertThat(calendar.timeInMillis).isWithin((currentTimeMs - 2)..(currentTimeMs + 2))
  }

  @Test
  fun testGetCurrentCalendar_fixedTimeMode_afterSettingTime_returnsCalnedarWithSetTime() {
    fakeOppiaClock.setFakeTimeMode(MODE_FIXED_FAKE_TIME)

    fakeOppiaClock.setCurrentTimeMs(123)
    val calendar = fakeOppiaClock.getCurrentCalendar()

    // The calendar should be initialized to the explicitly set fake time in this time mode.
    assertThat(calendar.timeInMillis).isEqualTo(123)
  }

  @Test
  fun testGetCurrentCalendar_uptimeMillisMode_returnsCalendarWithUptimeMillis() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)

    val calendar = fakeOppiaClock.getCurrentCalendar()

    // The calendar should be initialized to the uptime millis value in this time mode.
    assertThat(calendar.timeInMillis).isEqualTo(SystemClock.uptimeMillis())
  }

  @Test
  fun testSetCurrentTimeToSameDateTime_wallClockMode_throwsException() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)

    val exception = assertThrows(IllegalStateException::class) {
      fakeOppiaClock.setCurrentTimeToSameDateTime(0)
    }
    assertThat(exception).hasMessageThat().contains("MODE_FIXED_FAKE_TIME")
  }

  @Test
  fun testSetCurrentTimeToSameDateTime_fixedTimeMode_morningUtcTime_getTimeReturnsAdjustedTime() {
    fakeOppiaClock.setFakeTimeMode(MODE_FIXED_FAKE_TIME)

    fakeOppiaClock.setCurrentTimeToSameDateTime(MORNING_TIMESTAMP)

    // The special helper adjusts the timestamp such that it's morning time in the local timezone.
    val calendar = fakeOppiaClock.getCurrentCalendar()
    assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isWithin(4..11)
  }

  @Test
  fun testSetCurrentTimeToSameDateTime_fixedTimeMode_eveningUtcTime_getTimeReturnsAdjustedTime() {
    fakeOppiaClock.setFakeTimeMode(MODE_FIXED_FAKE_TIME)

    fakeOppiaClock.setCurrentTimeToSameDateTime(EVENING_TIMESTAMP)

    // The special helper adjusts the timestamp such that it's evening time in the local timezone.
    val calendar = fakeOppiaClock.getCurrentCalendar()
    assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isIn(17..23)
  }

  @Test
  fun testSetCurrentTimeToSameDateTime_uptimeMillisMode_throwsException() {
    fakeOppiaClock.setFakeTimeMode(MODE_UPTIME_MILLIS)

    val exception = assertThrows(IllegalStateException::class) {
      fakeOppiaClock.setCurrentTimeToSameDateTime(0)
    }
    assertThat(exception).hasMessageThat().contains("MODE_FIXED_FAKE_TIME")
  }

  private fun IntegerSubject.isWithin(range: IntRange) = isIn(range.toGuavaRange())

  private fun LongSubject.isWithin(range: LongRange) = isIn(range.toGuavaRange())

  private fun <T : Comparable<T>> ClosedRange<T>.toGuavaRange(): Range<T> {
    return Range.open(/* lower= */ start, /* upper= */ endInclusive)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ImageParsingModule::class, CachingTestModule::class, LoggerModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(fakeOppiaClockTest: FakeOppiaClockTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFakeOppiaClockTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(fakeOppiaClockTest: FakeOppiaClockTest) {
      component.inject(fakeOppiaClockTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
