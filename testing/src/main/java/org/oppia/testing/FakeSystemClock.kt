package org.oppia.testing

import android.os.SystemClock
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

// TODO(#89): Actually finish this implementation so that it properly works across Robolectric and
//  Espresso, and add tests for it.
/**
 * A Robolectric-specific fake for the system clock that can be used to manipulate time in a
 * consistent way.
 */
@Singleton
class FakeSystemClock @Inject constructor(@IsOnRobolectric private val isOnRobolectric: Boolean) {
  private val timeCoordinator by lazy { TimeCoordinator.retrieveTimeCoordinator(isOnRobolectric) }
  private val currentTimeMillis: AtomicLong

  init {
    val initialMillis = timeCoordinator.getCurrentTime()
    timeCoordinator.setCurrentTime(initialMillis)
    currentTimeMillis = AtomicLong(initialMillis)
  }

  /** Returns the current time of the fake clock, in milliseconds. */
  fun getTimeMillis(): Long = currentTimeMillis.get()

  /**
   * Advances the clock time by the specific number of milliseconds, and returns the new value. It's
   * recommended to *never* use this method directly as it may result in UI-scheduled tasks
   * executing before background tasks, and may cause background tasks to execute at the wrong time.
   * If a test needs time to be advanced, it should use [TestCoroutineDispatchers.advanceTimeBy].
   */
  fun advanceTime(millis: Long): Long {
    val newTime = currentTimeMillis.addAndGet(millis)
    timeCoordinator.advanceTimeTo(newTime)
    return newTime
  }

  private sealed class TimeCoordinator {
    abstract fun getCurrentTime(): Long

    abstract fun advanceTimeTo(timeMillis: Long)

    abstract fun setCurrentTime(timeMillis: Long)

    internal companion object {
      internal fun retrieveTimeCoordinator(isOnRobolectric: Boolean): TimeCoordinator {
        return if (isOnRobolectric) {
          RobolectricTimeCoordinator
        } else {
          EspressoTimeCoordinator
        }
      }
    }

    private object RobolectricTimeCoordinator : TimeCoordinator() {
      private val robolectricClass by lazy { loadRobolectricClass() }
      private val foregroundScheduler by lazy { loadForegroundScheduler() }
      private val retrieveCurrentTimeMethod by lazy { loadRetrieveCurrentTimeMethod() }
      private val retrieveAdvanceToMethod by lazy { loadAdvanceToMethod() }

      override fun getCurrentTime(): Long {
        return retrieveCurrentTimeMethod.invoke(foregroundScheduler) as Long
      }

      override fun advanceTimeTo(timeMillis: Long) {
        retrieveAdvanceToMethod.invoke(foregroundScheduler, timeMillis)
        setCurrentTime(timeMillis)
      }

      override fun setCurrentTime(timeMillis: Long) {
        SystemClock.setCurrentTimeMillis(timeMillis)
      }

      private fun loadRobolectricClass(): Class<*> {
        val classLoader = FakeSystemClock::class.java.classLoader!!
        return classLoader.loadClass("org.robolectric.Robolectric")
      }

      private fun loadForegroundScheduler(): Any {
        val retrieveSchedulerMethod =
          robolectricClass.getDeclaredMethod("getForegroundThreadScheduler")
        return retrieveSchedulerMethod.invoke(/* obj= */ null)
      }

      private fun loadRetrieveCurrentTimeMethod(): Method {
        val schedulerClass = foregroundScheduler.javaClass
        return schedulerClass.getDeclaredMethod("getCurrentTime")
      }

      private fun loadAdvanceToMethod(): Method {
        val schedulerClass = foregroundScheduler.javaClass
        return schedulerClass.getDeclaredMethod("advanceTo", Long::class.java)
      }
    }

    private object EspressoTimeCoordinator : TimeCoordinator() {
      override fun getCurrentTime(): Long {
        // Assume that time remains fixed.
        return 0
      }

      override fun advanceTimeTo(timeMillis: Long) {
        // Espresso runs in real-time. Delays don't actually work in the same way. Callers should
        // make use of idling resource to properly communicate to Espresso when coroutines have
        // finished executing.
      }

      override fun setCurrentTime(timeMillis: Long) {
        // Don't override the system time on Espresso since devices require apps to have special
        // permissions to do so. It's also unnecessary since the coroutine dispatchers only need to
        // synchronize on the fake clock's internal time.
      }
    }
  }
}
