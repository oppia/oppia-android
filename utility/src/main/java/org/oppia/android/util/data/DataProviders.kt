package org.oppia.android.util.data

import android.content.Context
import androidx.lifecycle.LiveData
import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Various functions to create or manipulate [DataProvider]s.
 *
 * It's recommended to transform providers rather than [LiveData] since the latter occurs on the
 * main thread, and the former can occur safely on background threads to reduce UI lag and user
 * perceived latency.
 */
@Reusable // Since otherwise a new provider will be created for each companion object call.
class DataProviders @Inject constructor(
  private val context: Context,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val exceptionLogger: ExceptionLogger
) {
  companion object {
    /**
     * Returns a new [DataProvider] that applies the specified function each time new data is
     * available to this data provider, and provides the transformed data to its own subscribers.
     *
     * Notifications to the original data provider will also notify subscribers to the transformed
     * data provider of changes, but not vice versa.
     *
     * Note that the input transformation function should be non-blocking, have no side effects, and
     * be thread-safe since it may be called on different background threads at different times. It
     * should perform no UI operations or otherwise interact with UI components.
     */
    fun <I, O> DataProvider<I>.transform(newId: Any, function: (I) -> O): DataProvider<O> {
      val dataProviders = getDataProviders()
      dataProviders.asyncDataSubscriptionManager.associateIds(newId, getId())
      return object : DataProvider<O>(context) {
        override fun getId(): Any = newId

        override suspend fun retrieveData(): AsyncResult<O> {
          return try {
            this@transform.retrieveData().transform(function)
          } catch (e: Exception) {
            dataProviders.exceptionLogger.logException(e)
            AsyncResult.failed(e)
          }
        }
      }
    }

    /**
     * Returns a transformed [DataProvider] in the same way as [transform] except the transformation
     * function can be blocking.
     */
    fun <I, O> DataProvider<I>.transformAsync(
      newId: Any,
      function: suspend (I) -> AsyncResult<O>
    ): DataProvider<O> {
      val dataProviders = getDataProviders()
      dataProviders.asyncDataSubscriptionManager.associateIds(newId, getId())
      return object : DataProvider<O>(context) {
        override fun getId(): Any = newId

        override suspend fun retrieveData(): AsyncResult<O> {
          return this@transformAsync.retrieveData().transformAsync(function)
        }
      }
    }

    /**
     * Returns a new [NestedTransformedDataProvider] based on the current provider. By default, the
     * data provider returned by this function behaves the same as [transformAsync]'s, except this
     * one supports changing its based provider. If callers do not plan to change the underlying
     * base provider, [transformAsync] should be used, instead.
     */
    fun <I, O> DataProvider<I>.transformNested(
      newId: Any,
      function: suspend (I) -> AsyncResult<O>
    ): NestedTransformedDataProvider<O> {
      return NestedTransformedDataProvider.createNestedTransformedDataProvider(
        context, newId, this, function, getDataProviders().asyncDataSubscriptionManager
      )
    }

    /**
     * Returns a new [DataProvider] that combines this provider with another by applying the
     * specified function to produce a new value each time either data provider changes.
     *
     * Notifications to the original data providers will also notify subscribers to the combined
     * data provider of changes, but not vice versa.
     *
     * Note that the combine function should be non-blocking, have no side effects, and be
     * thread-safe since it may be called on different background threads at different times. It
     * should perform no UI operations or otherwise interact with UI components.
     */
    fun <O, T1, T2> DataProvider<T1>.combineWith(
      dataProvider: DataProvider<T2>,
      newId: Any,
      function: (T1, T2) -> O
    ): DataProvider<O> {
      val dataProviders = getDataProviders()
      dataProviders.asyncDataSubscriptionManager.associateIds(newId, getId())
      dataProviders.asyncDataSubscriptionManager.associateIds(newId, dataProvider.getId())
      return object : DataProvider<O>(context) {
        override fun getId(): Any {
          return newId
        }

        override suspend fun retrieveData(): AsyncResult<O> {
          return try {
            this@combineWith.retrieveData().combineWith(dataProvider.retrieveData(), function)
          } catch (e: Exception) {
            dataProviders.exceptionLogger.logException(e)
            AsyncResult.failed(e)
          }
        }
      }
    }

    /**
     * Returns a transformed [DataProvider] in the same way as [combineWith] except the combine
     * function can be blocking.
     */
    fun <O, T1, T2> DataProvider<T1>.combineWithAsync(
      dataProvider: DataProvider<T2>,
      newId: Any,
      function: suspend (T1, T2) -> AsyncResult<O>
    ): DataProvider<O> {
      val dataProviders = getDataProviders()
      dataProviders.asyncDataSubscriptionManager.associateIds(newId, getId())
      dataProviders.asyncDataSubscriptionManager.associateIds(newId, dataProvider.getId())
      return object : DataProvider<O>(context) {
        override fun getId(): Any {
          return newId
        }

        override suspend fun retrieveData(): AsyncResult<O> {
          return this@combineWithAsync.retrieveData().combineWithAsync(
            dataProvider.retrieveData(), function
          )
        }
      }
    }

    /**
     * Converts a [DataProvider] to [LiveData]. This will use a background executor to handle
     * processing of the coroutine, but [LiveData] guarantees that final delivery of the result will
     * happen on the main thread.
     */
    fun <T> DataProvider<T>.toLiveData(): LiveData<AsyncResult<T>> {
      val dataProviders = getDataProviders()
      return NotifiableAsyncLiveData(
        dataProviders.backgroundDispatcher, dataProviders.asyncDataSubscriptionManager, this
      )
    }

    private fun <T> DataProvider<T>.getDataProviders(): DataProviders {
      val injectorProvider = context.applicationContext as DataProvidersInjectorProvider
      return injectorProvider.getDataProvidersInjector().getDataProviders()
    }
  }

  /**
   * Returns a new in-memory [DataProvider] with the specified function being called each time the
   * provider's data is retrieved, and the specified identifier.
   *
   * Note that the loadFromMemory function should be non-blocking, and have no side effects. It
   * should also be thread safe since it can be called from different background threads. It also
   * should never interact with UI components or perform UI operations.
   *
   * Changes to the returned data provider can be propagated using calls to
   * [AsyncDataSubscriptionManager.notifyChange] with the in-memory provider's identifier.
   */
  fun <T> createInMemoryDataProvider(id: Any, loadFromMemory: () -> T): DataProvider<T> {
    return object : DataProvider<T>(context) {
      override fun getId(): Any {
        return id
      }

      override suspend fun retrieveData(): AsyncResult<T> {
        return try {
          AsyncResult.success(loadFromMemory())
        } catch (e: Exception) {
          exceptionLogger.logException(e)
          AsyncResult.failed(e)
        }
      }
    }
  }

  /**
   * Returns a new in-memory [DataProvider] in the same way as [createInMemoryDataProvider] except
   * the load function can be blocking.
   */
  fun <T> createInMemoryDataProviderAsync(
    id: Any,
    loadFromMemoryAsync: suspend () -> AsyncResult<T>
  ): DataProvider<T> {
    return object : DataProvider<T>(context) {
      override fun getId(): Any {
        return id
      }

      override suspend fun retrieveData(): AsyncResult<T> {
        return loadFromMemoryAsync()
      }
    }
  }

  /**
   * A [DataProvider] that acts in the same way as [transformAsync] except the underlying base data
   * provider can change.
   */
  class NestedTransformedDataProvider<O> private constructor(
    context: Context,
    private val id: Any,
    private var baseId: Any,
    private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
    private var retrieveTransformedData: suspend () -> AsyncResult<O>
  ) : DataProvider<O>(context) {
    init {
      initializeTransformer()
    }

    override fun getId(): Any = id

    override suspend fun retrieveData(): AsyncResult<O> {
      return retrieveTransformedData()
    }

    /**
     * Sets a new base [DataProvider] and transform function from which to derive this data
     * provider.
     *
     * Note that this will notify any observers of this provider so that they receive the latest
     * transformed value.
     */
    fun <I> setBaseDataProvider(
      dataProvider: DataProvider<I>,
      transform: suspend (I) -> AsyncResult<O>
    ) {
      asyncDataSubscriptionManager.dissociateIds(id, baseId)
      baseId = dataProvider.getId()
      retrieveTransformedData = { dataProvider.retrieveData().transformAsync(transform) }
      initializeTransformer()

      // Notify subscribers that the base provider has changed.
      asyncDataSubscriptionManager.notifyChangeAsync(id)
    }

    private fun initializeTransformer() {
      asyncDataSubscriptionManager.associateIds(id, baseId)
    }

    companion object {
      /** Returns a new [NestedTransformedDataProvider]. */
      internal fun <I, O> createNestedTransformedDataProvider(
        context: Context,
        id: Any,
        baseDataProvider: DataProvider<I>,
        transform: suspend (I) -> AsyncResult<O>,
        asyncDataSubscriptionManager: AsyncDataSubscriptionManager
      ): NestedTransformedDataProvider<O> {
        return NestedTransformedDataProvider(
          context, id, baseDataProvider.getId(), asyncDataSubscriptionManager
        ) {
          baseDataProvider.retrieveData().transformAsync(transform)
        }
      }
    }
  }

  /**
   * A version of [LiveData] which automatically pipes data from a specified [DataProvider] to
   * LiveData observers in a thread-safe and lifecycle-safe way.
   *
   * This class will immediately retrieve the latest state of its input [DataProvider] at the first
   * occurrence of an active observer, but not before then. It guarantees that all active observers
   * (including new ones) will receive an eventually consistent state of the data provider. It also
   * will not deliver the same value more than once in a row to avoid over-alerting observers of
   * changes.
   */
  private class NotifiableAsyncLiveData<T>(
    private val dispatcher: CoroutineDispatcher,
    private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
    private val dataProvider: DataProvider<T>
  ) : LiveData<AsyncResult<T>>() {
    private val asyncSubscriber: ObserveAsyncChange = this::handleDataProviderUpdate
    private val isActive = AtomicBoolean(false)
    private val runningJob = AtomicReference<Job?>(null)
    private var cache: AsyncResult<T>? = null // only accessed on the main thread

    override fun onActive() {
      super.onActive()
      // Subscribe to the ID immediately in case there's a value in the data provider already ready.
      asyncDataSubscriptionManager.subscribe(dataProvider.getId(), asyncSubscriber)
      isActive.set(true)

      // If there's no currently cached value or soon-to-be cached value, kick-off a data retrieval
      // so that new observers can receive the most up-to-date value.
      if (runningJob.get() == null) {
        val job = CoroutineScope(dispatcher).launch {
          handleDataProviderUpdate()
        }
        // Note that this can race against handleDataProviderUpdate() clearing the job, but in
        // either outcome the behavior should still be correct (eventual consistency).
        runningJob.set(job)
      }
    }

    override fun onInactive() {
      super.onInactive()
      // Stop watching for updates immediately, then cancel any existing operations.
      asyncDataSubscriptionManager.unsubscribe(dataProvider.getId(), asyncSubscriber)
      isActive.set(false)
      // This can cancel downstream operations that may want to complete side effects.
      runningJob.getAndSet(null)?.cancel()
    }

    override fun setValue(value: AsyncResult<T>?) {
      checkNotNull(value) { "Null values should not be posted to NotifiableAsyncLiveData." }
      val currentCache = cache // This is safe because cache can only be changed on the main thread.
      if (currentCache != null) {
        if (value.isNewerThanOrSameAgeAs(currentCache) && currentCache != value) {
          // Only propagate the value if it's changed and is newer since it's possible for observer
          // callbacks to happen out-of-order.
          cache = value
          super.setValue(value)
        }
      } else {
        cache = value
        super.setValue(value)
      }
    }

    private suspend fun handleDataProviderUpdate() {
      // This doesn't guarantee that retrieveData() is only called when the live data is active
      // (e.g. it can become inactive right after the value is posted & before it's dispatched), but
      // it does guarantee that it won't be called when the live data is currently inactive. This
      // also safely passes the value to the main thread and relies on LiveData's own internal
      // mechanism which in turn always calls setValue(), even if there are no active observers. See
      // the override of setValue() above for the adjusted semantics this class requires to ensure
      // its own cache remains up-to-date.
      retrieveFromDataProvider()?.let {
        super.postValue(it)
        runningJob.set(null)
      }
    }

    private suspend fun retrieveFromDataProvider(): AsyncResult<T>? {
      return if (isActive.get()) {
        // Although it's possible for the live data to become inactive after this point, this
        // follows the expected contract of the data provider (it may have its data retrieved and
        // not delivered), and it guarantees eventual consistency since the class still caches the
        // results in case a new observer is added later.
        dataProvider.retrieveData()
      } else null
    }
  }
}
