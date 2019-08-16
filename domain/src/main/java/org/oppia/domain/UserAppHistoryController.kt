package org.oppia.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import org.oppia.app.model.UserAppHistory
import org.oppia.util.data.AsyncDataSource
import org.oppia.util.data.AsyncResult
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** Controller for persisting and retrieving the previous user history of using the app. */
class UserAppHistoryController(coroutineContext: CoroutineContext = EmptyCoroutineContext) {
  // TODO(BenHenning): Persist this value.
  private var userOpenedApp = true

  // Lazy initialize this so that its async function isn't started until it's actually needed.
  private val userAppHistoryData: NotifiableAsyncLiveData<UserAppHistory> by lazy {
    NotifiableAsyncLiveData(coroutineContext) {
      createUserAppHistoryDataSource().executePendingOperation()
    }
  }

  /** Saves that the user has opened the app. */
  fun markUserOpenedApp() {
    userOpenedApp = true
    userAppHistoryData.notifyUpdate()
  }

  /** Returns a [LiveData] result indicating whether the user has previously opened the app. */
  fun getUserAppHistory(): LiveData<AsyncResult<UserAppHistory>> {
    return userAppHistoryData
  }

  // TODO(BenHenning): Move this to a data source within the data source module.
  private fun createUserAppHistoryDataSource(): AsyncDataSource<UserAppHistory> {
    return object : AsyncDataSource<UserAppHistory> {
      override suspend fun executePendingOperation(): UserAppHistory {
        return UserAppHistory.newBuilder().setAlreadyOpenedApp(userOpenedApp).build()
      }
    }
  }

  // TODO(BenHenning): Move this to the correct module once the architecture for data sources is determined.
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
      }
    }

    /**
     * Enqueues the async function, but execution is based on whether this [LiveData] is active. See [MediatorLiveData]
     * docs for context.
     */
    private fun enqueueAsyncFunctionAsLiveData() {
      val coroutineLiveData: LiveData<AsyncResult<T>> by lazy {
        liveData(context) {
          try {
            emit(AsyncResult.success(function()))
          } catch (t: Throwable) {
            // Capture all failures for the downstream handler.
            emit(AsyncResult.failed<T>(t))
          }
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
}