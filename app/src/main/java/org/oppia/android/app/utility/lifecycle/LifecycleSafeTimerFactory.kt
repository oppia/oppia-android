package org.oppia.android.app.utility.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/**
 * Injectable factory for creating lifecycle-safe timers that use [LiveData]. This should always be used instead of
 * Android's Handler class because this guarantees that observers will not be triggered if its lifecycle is no longer
 * valid, but will trigger upon the lifecycle becoming active again.
 */
class LifecycleSafeTimerFactory @Inject constructor(
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  // TODO(#59): Add tests for this class once it's possible to swap the runtime dispatcher in app module tests.

  private val backgroundCoroutineScope = CoroutineScope(backgroundCoroutineDispatcher)

  /**
   * Returns a new [LiveData] that will be triggered exactly once after the specified timeout in
   * milliseconds. This should be used instead of Android's Handler class because this guarantees
   * that observers will not be triggered if its lifecycle is no longer valid, but will trigger upon
   * the lifecycle becoming active again. No assumptions should be made about the payload of the
   * live data.
   */
  fun createTimer(timeoutMillis: Long): LiveData<Any> {
    val liveData = MutableLiveData<Any>()
    backgroundCoroutineScope.launch {
      delay(timeoutMillis)
      liveData.postValue(Any())
    }
    return liveData
  }

  /**
   * A convenience version of [createTimer] that runs the specified [block] for this
   * [LifecycleOwner] with the specified millisecond delay of [delayMillis].
   *
   * Note that [block] is guaranteed to run at a lifecycle-safe time, though it will be run on the
   * main thread so it should not perform any I/O or expensive operations.
   */
  fun LifecycleOwner.runWithDelay(delayMillis: Long, block: () -> Unit) {
    val liveData = createTimer(delayMillis)
    liveData.observe(
      this,
      object : Observer<Any> {
        override fun onChanged(value: Any?) {
          liveData.removeObserver(this)
          block()
        }
      }
    )
  }

  /**
   * Runs a [block] of code with an initial delay of [delayMillis] (default 0) at a periodic rate of
   * [periodMillis] milliseconds for this [LifecycleOwner].
   *
   * Like [runWithDelay], [block] is run at a lifecycle-safe time on the main thread. The loop will
   * continue until either the [LifecycleOwner]'s lifecycle ends, or [block] returns false.
   *
   * Note that [periodMillis] is how much time should be run between calls to [block]. If [block]
   * schedules something that will happen in parallel (such as an animation), then the period time
   * will likely represent the time between each animation start, rather than between one animation
   * end and the next start.
   */
  fun LifecycleOwner.runPeriodically(
    delayMillis: Long = 0,
    periodMillis: Long,
    block: () -> Boolean
  ) {
    runWithDelay(delayMillis) {
      if (block()) {
        runPeriodically(periodMillis, periodMillis, block)
      }
    }
  }
}
