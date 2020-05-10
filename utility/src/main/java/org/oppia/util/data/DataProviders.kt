package org.oppia.util.data

import androidx.annotation.GuardedBy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.oppia.util.threading.BackgroundDispatcher

/**
 * Various functions to create or manipulate [DataProvider]s.
 *
 * It's recommended to transform providers rather than [LiveData] since the latter occurs on the main thread, and the
 * former can occur safely on background threads to reduce UI lag and user perceived latency.
 */
@Singleton
class DataProviders @Inject constructor(
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
) {
  /**
   * Returns a new [DataProvider] that applies the specified function each time new data is available to it, and
   * provides it to its own subscribers.
   *
   * Notifications to the original data provider will also notify subscribers to the transformed data provider of
   * changes, but not vice versa.
   *
   * Note that the input transformation function should be non-blocking, have no side effects, and be thread-safe since
   * it may be called on different background threads at different times. It should perform no UI operations or
   * otherwise interact with UI components.
   */
  fun <T1, T2> transform(newId: Any, dataProvider: DataProvider<T1>, function: (T1) -> T2): DataProvider<T2> {
    asyncDataSubscriptionManager.associateIds(newId, dataProvider.getId())
    return object : DataProvider<T2> {
      override fun getId(): Any {
        return newId
      }

      override suspend fun retrieveData(): AsyncResult<T2> {
        return try {
          dataProvider.retrieveData().transform(function)
        } catch (t: Throwable) {
          AsyncResult.failed(t)
        }
      }
    }
  }

  /**
   * Returns a transformed [DataProvider] in the same way as [transform] except the transformation function can be
   * blocking.
   */
  fun <T1, T2> transformAsync(
    newId: Any,
    dataProvider: DataProvider<T1>,
    function: suspend (T1) -> AsyncResult<T2>
  ): DataProvider<T2> {
    asyncDataSubscriptionManager.associateIds(newId, dataProvider.getId())
    return object : DataProvider<T2> {
      override fun getId(): Any {
        return newId
      }

      override suspend fun retrieveData(): AsyncResult<T2> {
        return dataProvider.retrieveData().transformAsync(function)
      }
    }
  }

  /**
   * Returns a new [DataProvider] that combines two other providers by applying the specified function to produce a new
   * value each time either data provider changes.
   *
   * Notifications to the original data providers will also notify subscribers to the combined data provider of
   * changes, but not vice versa.
   *
   * Note that the combine function should be non-blocking, have no side effects, and be thread-safe since it may be
   * called on different background threads at different times. It should perform no UI operations or otherwise interact
   * with UI components.
   */
  fun <O, T1, T2> combine(
    newId: Any,
    dataProvider1: DataProvider<T1>,
    dataProvider2: DataProvider<T2>,
    function: (T1, T2) -> O
  ): DataProvider<O> {
    asyncDataSubscriptionManager.associateIds(newId, dataProvider1.getId())
    asyncDataSubscriptionManager.associateIds(newId, dataProvider2.getId())
    return object : DataProvider<O> {
      override fun getId(): Any {
        return newId
      }

      override suspend fun retrieveData(): AsyncResult<O> {
        return try {
          dataProvider1.retrieveData().combineWith(dataProvider2.retrieveData(), function)
        } catch (t: Throwable) {
          AsyncResult.failed(t)
        }
      }
    }
  }

  /**
   * Returns a transformed [DataProvider] in the same way as [combine] except the combine function can be blocking.
   */
  fun <O, T1, T2> combineAsync(
    newId: Any,
    dataProvider1: DataProvider<T1>,
    dataProvider2: DataProvider<T2>,
    function: suspend (T1, T2) -> AsyncResult<O>
  ): DataProvider<O> {
    asyncDataSubscriptionManager.associateIds(newId, dataProvider1.getId())
    asyncDataSubscriptionManager.associateIds(newId, dataProvider2.getId())
    return object : DataProvider<O> {
      override fun getId(): Any {
        return newId
      }

      override suspend fun retrieveData(): AsyncResult<O> {
        return dataProvider1.retrieveData().combineWithAsync(dataProvider2.retrieveData(), function)
      }
    }
  }

  /**
   * Returns a new in-memory [DataProvider] with the specified function being called each time the provider's data is
   * retrieved, and the specified identifier.
   *
   * Note that the loadFromMemory function should be non-blocking, and have no side effects. It should also be thread
   * safe since it can be called from different background threads. It also should never interact with UI components or
   * perform UI operations.
   *
   * Changes to the returned data provider can be propagated using calls to [AsyncDataSubscriptionManager.notifyChange]
   * with the in-memory provider's identifier.
   */
  fun <T> createInMemoryDataProvider(id: Any, loadFromMemory: () -> T): DataProvider<T> {
    return object : DataProvider<T> {
      override fun getId(): Any {
        return id
      }

      override suspend fun retrieveData(): AsyncResult<T> {
        return try {
          AsyncResult.success(loadFromMemory())
        } catch (t: Throwable) {
          AsyncResult.failed(t)
        }
      }
    }
  }

  /**
   * Returns a new in-memory [DataProvider] in the same way as [createInMemoryDataProvider] except the load function can
   * be blocking.
   */
  fun <T> createInMemoryDataProviderAsync(id: Any, loadFromMemoryAsync: suspend () -> AsyncResult<T>): DataProvider<T> {
    return object : DataProvider<T> {
      override fun getId(): Any {
        return id
      }

      override suspend fun retrieveData(): AsyncResult<T> {
        return loadFromMemoryAsync()
      }
    }
  }

  /**
   * Converts a [DataProvider] to [LiveData]. This will use a background executor to handle processing of the coroutine,
   * but [LiveData] guarantees that final delivery of the result will happen on the main thread.
   */
  fun <T> convertToLiveData(dataProvider: DataProvider<T>): LiveData<AsyncResult<T>> {
    return NotifiableAsyncLiveData(backgroundDispatcher, asyncDataSubscriptionManager, dataProvider)
  }

  /**
   * A version of [LiveData] which can be notified to execute a specified coroutine if there is a pending update.
   *
   * This [LiveData] also reports the pending, succeeding, and failing state of the [AsyncResult]. Note that it will
   * immediately execute the specified async function upon initialization, so it's recommended to only initialize this
   * object upon when its result is actually needed to avoid kicking off many async tasks with results that may never be
   * used.
   */
  private class NotifiableAsyncLiveData<T>(
    private val dispatcher: CoroutineDispatcher,
    private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
    private val dataProvider: DataProvider<T>
  ) : MediatorLiveData<AsyncResult<T>>() {
    private val coroutineLiveDataLock = ReentrantLock()
    @GuardedBy("coroutineLiveDataLock") private var pendingCoroutineLiveData: LiveData<AsyncResult<T>>? = null
    @GuardedBy("coroutineLiveDataLock") private var cachedValue: AsyncResult<T>? = null

    // This field is only access on the main thread, so no additional locking is necessary.
    private var dataProviderSubscriber: ObserveAsyncChange? = null

    init {
      // Schedule to retrieve data from the provider immediately.
      enqueueAsyncFunctionAsLiveData()
    }

    override fun onActive() {
      if (dataProviderSubscriber == null) {
        val subscriber: ObserveAsyncChange = {
          notifyUpdate()
        }
        asyncDataSubscriptionManager.subscribe(dataProvider.getId(), subscriber)
        dataProviderSubscriber = subscriber
      }
      super.onActive()
    }

    override fun onInactive() {
      super.onInactive()
      dataProviderSubscriber?.let {
        asyncDataSubscriptionManager.unsubscribe(dataProvider.getId(), it)
        dataProviderSubscriber = null
      }
      dequeuePendingCoroutineLiveData()
    }

    /**
     * Notifies this live data that it should re-run its asynchronous function and propagate any results.
     *
     * Note that if an existing operation is pending, it may complete but its results will not be propagated in favor
     * of the run started by this call. Note also that regardless of the current [AsyncResult] value of this live data,
     * the new value will overwrite it (e.g. it's possible to go from a failed to success state or vice versa).
     *
     * This needs to be run on the main thread due to [LiveData] limitations.
     */
    private fun notifyUpdate() {
      dequeuePendingCoroutineLiveData()
      enqueueAsyncFunctionAsLiveData()
    }

    /**
     * Enqueues the async function, but execution is based on whether this [LiveData] is active. See [MediatorLiveData]
     * docs for context.
     */
    private fun enqueueAsyncFunctionAsLiveData() {
      val coroutineLiveData = CoroutineLiveData(dispatcher) {
        dataProvider.retrieveData()
      }
      coroutineLiveDataLock.withLock {
        pendingCoroutineLiveData = coroutineLiveData
        addSource(coroutineLiveData) { computedValue ->
          // Only notify LiveData subscriptions if the value is actually different.
          if (cachedValue != computedValue) {
            value = computedValue
            cachedValue = value
          }
        }
      }
    }

    private fun dequeuePendingCoroutineLiveData() {
      coroutineLiveDataLock.withLock {
        pendingCoroutineLiveData?.let {
          removeSource(it) // This can trigger onInactive() situations for long-standing operations, leading to them being cancelled.
          pendingCoroutineLiveData = null
        }
      }
    }
  }

  // TODO(#72): Replace this with AndroidX's CoroutineLiveData once the corresponding LiveData suspend job bug is fixed
  //  and available.
  /** A [LiveData] whose value is derived from a suspended function. */
  private class CoroutineLiveData<T>(
    private val dispatcher: CoroutineDispatcher,
    private val function: suspend () -> T
  ) : MutableLiveData<T>() {
    private var runningJob: Job? = null

    override fun onActive() {
      super.onActive()
      if (runningJob == null) {
        val scope = CoroutineScope(dispatcher)
        runningJob = scope.launch {
          postValue(function())
          runningJob = null
        }
      }
    }

    override fun onInactive() {
      super.onInactive()
      runningJob?.cancel() // This can cancel downstream operations that may want to complete side effects.
      runningJob = null
    }
  }
}
