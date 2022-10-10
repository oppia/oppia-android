package org.oppia.android.testing.time

import android.annotation.SuppressLint
import android.os.SystemClock
import org.oppia.android.testing.time.FakeOppiaClock.FakeTimeMode
import org.oppia.android.util.system.OppiaClock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
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
  private var fakeTimeMode: FakeTimeMode = FakeTimeMode.MODE_WALL_CLOCK_TIME

  init {
    // Ensure tests that rely on this clock are always operating in UTC.
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  override fun getCurrentTimeMs(): Long {
    return when (fakeTimeMode) {
      FakeTimeMode.MODE_WALL_CLOCK_TIME -> System.currentTimeMillis()
      FakeTimeMode.MODE_FIXED_FAKE_TIME -> fixedFakeTimeMs
      FakeTimeMode.MODE_UPTIME_MILLIS -> SystemClock.uptimeMillis()
    }
  }

  /**
   * Sets the current wall-clock time in milliseconds since the Unix epoch, in UTC.
   *
   * This can only be used if the current time mode is [FakeTimeMode.MODE_FIXED_FAKE_TIME].
   */
  fun setCurrentTimeMs(currentTimeMs: Long) {
    check(fakeTimeMode == FakeTimeMode.MODE_FIXED_FAKE_TIME) {
      "Cannot change set time unless time mode is MODE_FIXED_FAKE_TIME."
    }
    fixedFakeTimeMs = currentTimeMs
  }

  /**
   * Sets the clock's current time to a new UTC timestamp based on the specified one that takes
   * place at the same time of day on the same date as the specified timestamp, except accounting
   * for the local user's timezone.
   *
   * This is useful for ensuring that local calculations which depend on the device's timezone can
   * be tested for timestamps that must occur at a particular time of day or on a particular date.
   *
   * Note that this has the same restrictions as [setCurrentTimeMs] with regards to this clock's
   * time mode.
   */
  fun setCurrentTimeToSameDateTime(utcTimeMs: Long) {
    setCurrentTimeMs(getUtcTimeOfDayAsAdjustedUtcTimestampAccountForTimezone(utcTimeMs))
  }

  /** Sets the current mode used to compute time for [getCurrentTimeMs] and [getCurrentCalendar]. */
  fun setFakeTimeMode(fakeTimeMode: FakeTimeMode) {
    this.fakeTimeMode = fakeTimeMode
  }

  /** Returns the current time mode set by [setFakeTimeMode] or defaulted upon clock init. */
  fun getFakeTimeMode() = fakeTimeMode

  @SuppressLint("SimpleDateFormat")
  private fun getUtcTimeOfDayAsAdjustedUtcTimestampAccountForTimezone(utcTimeMs: Long): Long {
    val format = SimpleDateFormat("yyyy-MM-dd hh:mm a")
    val utcTimeAsDateTimeString = format.format(utcTimeMs)
    val date =
      format.parse(utcTimeAsDateTimeString)
        ?: error("Expected to parse date for time: $utcTimeAsDateTimeString")
    return getLocalToUtcDate(date) ?: error("Failed to convert date to UTC: $date")
  }

  @SuppressLint("SimpleDateFormat")
  private fun getLocalToUtcDate(date: Date): Long? {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.timeZone = TimeZone.getTimeZone("UTC")
    val time = calendar.time
    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    outputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val newDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(outputFormat.format(time))
    return newDate?.time
  }

  /** Determines the type of mode the clock should operate in. */
  enum class FakeTimeMode {
    /**
     * Indicates that the fake clock should use the system wall clock for tracking time. This is the
     * default behavior since it matches the production version of [OppiaClock].
     */
    MODE_WALL_CLOCK_TIME,

    /**
     * Indicates that the fake clock should default to an initial time of 0 and only return the time
     * set by [setCurrentTimeMs]. This is the only mode that can be used if the test wishes to
     * change time.
     */
    MODE_FIXED_FAKE_TIME,

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
    MODE_UPTIME_MILLIS
  }
}
