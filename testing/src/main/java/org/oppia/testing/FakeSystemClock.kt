package org.oppia.testing

import android.os.SystemClock
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

// TODO(#89): Add tests for this implementation.
/**
 * A fake for the system clock that can be used to manipulate time in a consistent way.
 *
 * Note that time manipulation only applies to Robolectric--this clock will no-op in Espresso tests
 * since those tests always run in real-time.
 */
@Singleton
class FakeSystemClock @Inject constructor(@IsOnRobolectric private val isOnRobolectric: Boolean) {
  private val timeCoordinator by lazy { TimeCoordinator.retrieveTimeCoordinator(isOnRobolectric) }
  private val currentTimeMillis: AtomicLong

  init {
    val initialMillis = timeCoordinator.getCurrentTimeMillis()
    timeCoordinator.setCurrentTimeMillis(initialMillis)
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
    timeCoordinator.advanceTimeMillisTo(newTime)
    return newTime
  }

  /** A test platform-specific time coordinator. */
  private sealed class TimeCoordinator {
    /** Returns the current wall time, in milliseconds. */
    abstract fun getCurrentTimeMillis(): Long

    /**
     * Advances the fake clock to the specified time in milliseconds, including running any
     * operations that are pending up to this point.
     */
    abstract fun advanceTimeMillisTo(timeMillis: Long)

    /**
     * Advances the fake clock to the specified time in milliseconds, but does not explicitly
     * execute any tasks scheduled between now and the new time.
     */
    abstract fun setCurrentTimeMillis(timeMillis: Long)

    internal companion object {
      /** Returns the [TimeCoordinator] based on the current test platform being used. */
      internal fun retrieveTimeCoordinator(isOnRobolectric: Boolean): TimeCoordinator {
        return if (isOnRobolectric) {
          RobolectricTimeCoordinator
        } else {
          EspressoTimeCoordinator
        }
      }
    }

    /**
     * Robolectric-specific [TimeCoordinator] that manages fake time to simplify test execution
     * coordination.
     */
    private object RobolectricTimeCoordinator : TimeCoordinator() {
      private val robolectricClass by lazy { loadRobolectricClass() }
      private val foregroundScheduler by lazy { loadForegroundScheduler() }
      private val retrieveCurrentTimeMethod by lazy { loadRetrieveCurrentTimeMethod() }
      private val retrieveAdvanceToMethod by lazy { loadAdvanceToMethod() }

      override fun getCurrentTimeMillis(): Long {
        return retrieveCurrentTimeMethod.invoke(foregroundScheduler) as Long
      }

      override fun advanceTimeMillisTo(timeMillis: Long) {
        retrieveAdvanceToMethod.invoke(foregroundScheduler, timeMillis)
        setCurrentTimeMillis(timeMillis)
      }

      override fun setCurrentTimeMillis(timeMillis: Long) {
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

    /** Espresso-specific [TimeCoordinator] that no-ops in favor of using the real clock. */
    private object EspressoTimeCoordinator : TimeCoordinator() {
      override fun getCurrentTimeMillis(): Long {
        // Assume that time remains fixed.
        return 0
      }

      override fun advanceTimeMillisTo(timeMillis: Long) {
        // Espresso runs in real-time. Delays don't actually work in the same way. Callers should
        // make use of idling resource to properly communicate to Espresso when coroutines have
        // finished executing.
      }

      override fun setCurrentTimeMillis(timeMillis: Long) {
        // Don't override the system time on Espresso since devices require apps to have special
        // permissions to do so. It's also unnecessary since the coroutine dispatchers only need to
        // synchronize on the fake clock's internal time.
      }
    }
  }
}
