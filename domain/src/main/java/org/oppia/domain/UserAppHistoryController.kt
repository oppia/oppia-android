package org.oppia.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.oppia.app.model.UserAppHistory
import org.oppia.util.data.AsyncDataSource
import org.oppia.util.data.AsyncResult
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** Controller for persisting and retrieving the previous user history of using the app. */
class UserAppHistoryController(private val coroutineContext: CoroutineContext = EmptyCoroutineContext) {
  // TODO(#70): Persist this value.
  private var userOpenedApp = false

  /**
   * Saves that the user has opened the app. Note that this does not notify existing consumers that the change was made.
   */
  fun markUserOpenedApp() {
    userOpenedApp = true
  }

  /** Returns a [LiveData] result indicating whether the user has previously opened the app. */
  fun getUserAppHistory(): LiveData<AsyncResult<UserAppHistory>> {
    return NotifiableAsyncLiveData(coroutineContext) {
      createUserAppHistoryDataSource().executePendingOperation()
    }
  }

  private fun createUserAppHistoryDataSource(): AsyncDataSource<UserAppHistory> {
    return object : AsyncDataSource<UserAppHistory> {
      override suspend fun executePendingOperation(): UserAppHistory {
        return UserAppHistory.newBuilder().setAlreadyOpenedApp(userOpenedApp).build()
      }
    }
  }

  // TODO(#71): Move this to the correct module once the architecture for data sources is determined.
  /**
   * A version of [LiveData] which can be notified to execute a specified coroutine if there is a pending update.
   *
   * This [LiveData] also reports the pending, succeeding, and failing state of the [AsyncResult]. Note that it will
   * immediately execute the specified async function upon initialization, so it's recommended to only initialize this
   * object upon when its result is actually needed to avoid kicking off many async tasks with results that may never be
   * used.
   */
  private class NotifiableAsyncLiveData<T>(
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val function: suspend () -> T
  ) : MediatorLiveData<AsyncResult<T>>() {
    private val lock = Object()
    private var pendingCoroutineLiveData: LiveData<AsyncResult<T>>? = null

    init {
      // Assume that the specified block is ready to execute immediately.
      value = AsyncResult.pending()
      enqueueAsyncFunctionAsLiveData()
    }

    /**
     * Notifies this live data that it should re-run its asynchronous function and propagate any results.
     *
     * Note that if an existing operation is pending, it may complete but its results will not be propagated in favor
     * of the run started by this call. Note also that regardless of the current [AsyncResult] value of this live data,
     * the new value will overwrite it (e.g. it's possible to go from a failed to success state or vice versa).
     */
    fun notifyUpdate() {
      synchronized(lock) {
        if (pendingCoroutineLiveData != null) {
          removeSource(pendingCoroutineLiveData!!)
          pendingCoroutineLiveData = null
        }
        enqueueAsyncFunctionAsLiveData()
      }
    }

    /**
     * Enqueues the async function, but execution is based on whether this [LiveData] is active. See [MediatorLiveData]
     * docs for context.
     */
    private fun enqueueAsyncFunctionAsLiveData() {
      val coroutineLiveData = CoroutineLiveData(context) {
        try {
          AsyncResult.success(function())
        } catch (t: Throwable) {
          // Capture all failures for the downstream handler.
          AsyncResult.failed<T>(t)
        }
      }
      synchronized(lock) {
        pendingCoroutineLiveData = coroutineLiveData
        addSource(coroutineLiveData) { computedValue ->
          value = computedValue
        }
      }
    }
  }

  // TODO(#72): Replace this with AndroidX's CoroutineLiveData once the corresponding LiveData suspend job bug is fixed
  //  and available.
  /** A [LiveData] whose value is derived from a suspended function. */
  private class CoroutineLiveData<T>(
    private val context: CoroutineContext,
    private val function: suspend () -> T
  ) : MutableLiveData<T>() {
    private var runningJob: Job? = null

    override fun onActive() {
      super.onActive()
      if (runningJob == null) {
        val scope = CoroutineScope(Dispatchers.Main + context)
        runningJob = scope.launch {
          value = function()
        }
      }
    }

    override fun onInactive() {
      super.onInactive()
      runningJob?.cancel()
    }
  }
}
