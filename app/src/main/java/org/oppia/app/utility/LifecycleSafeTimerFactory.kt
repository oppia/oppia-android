package org.oppia.app.utility

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.util.threading.BackgroundDispatcher
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
}
