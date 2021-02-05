package org.oppia.android.testing.time

import android.os.SystemClock
import org.oppia.android.util.system.OppiaClock
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A test-friendly fake for [OppiaClock] that provides better Robolectric/test time
 * interoperability.
 *
 * Note that this fake can operate in several distinct time modes. See [FakeTimeMode] for specifics.
 * This fake intentionally does not guarantee perfect interoperability with the production version
 * of [OppiaClock], but its default state (e.g. the initial clock without an altered time mode) does
 * guarantee that.
 */
@Singleton
class FakeOppiaClock @Inject constructor() : OppiaClock {
  private var fixedFakeTimeMs: Long = 0
  private var fakeTimeMode: FakeTimeMode = FakeTimeMode.USE_WALL_CLOCK_TIME

  override fun getCurrentTimeMs(): Long {
    return when (fakeTimeMode) {
      FakeTimeMode.USE_WALL_CLOCK_TIME -> System.currentTimeMillis()
      FakeTimeMode.USE_FIXED_FAKE_TIME -> fixedFakeTimeMs
      FakeTimeMode.USE_UPTIME_MILLIS -> SystemClock.uptimeMillis()
    }
  }

  override fun getCurrentCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = getCurrentTimeMs()
    return calendar
  }

  /**
   * Sets the current wall-clock time in milliseconds since the Unix epoch, in UTC.
   *
   * This can only be used if the current time mode is [FakeTimeMode.USE_FIXED_FAKE_TIME].
   */
  fun setCurrentTimeMs(currentTimeMs: Long) {
    check(fakeTimeMode == FakeTimeMode.USE_FIXED_FAKE_TIME) {
      "Cannot change set time unless time mode is USE_FIXED_FAKE_TIME."
    }
    fixedFakeTimeMs = currentTimeMs
  }

  /** Sets the current mode used to compute time for [getCurrentTimeMs] and [getCurrentCalendar]. */
  fun setFakeTimeMode(fakeTimeMode: FakeTimeMode) {
    this.fakeTimeMode = fakeTimeMode
  }

  /** Determines the type of mode the clock should operate in. */
  enum class FakeTimeMode {
    /**
     * Indicates that the fake clock should use the system wall clock for tracking time. This is the
     * default behavior since it matches the production version of [OppiaClock].
     */
    USE_WALL_CLOCK_TIME,

    /**
     * Indicates that the fake clock should default to an initial time of 0 and only return the time
     * set by [setCurrentTimeMs]. This is the only mode that can be used if the test wishes to
     * change time.
     */
    USE_FIXED_FAKE_TIME,

    /**
     * Indicates that the fake clock should use [SystemClock.uptimeMillis] to provide time, instead.
     * This does not match production behavior since uptime millis won't survive device restarts.
     * However, this generally will work better in Robolectric tests since those have a disparity
     * issue wherein [System.currentTimeMillis] uses real wall-clock time and uptime millis uses a
     * fake time. The latter is used to coordinate test timing (such as with the test coroutine
     * dispatcher utility). This mode should be used if the user wishes to enesure predictable time
     * advancement without relying on the actual speed at which the test runs.
     *
     * Any data that stores a copy of current time derived from this mode cannot be reliably shared
     * across test boundaries since each individual test is treated like a device restart from the
     * perspective of uptime tracking.
     */
    USE_UPTIME_MILLIS
  }
}
