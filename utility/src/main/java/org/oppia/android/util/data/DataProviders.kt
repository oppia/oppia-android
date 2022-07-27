package org.oppia.android.util.data

import android.content.Context
import androidx.lifecycle.LiveData
import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
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
  private val exceptionLogger: ExceptionLogger,
  private val dynamicallyTransformedDataProviderFactory: DynamicallyTransformedDataProvider.Factory
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

        override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<O> {
          return try {
            this@transform.retrieveData(originNotificationIds).transform(function)
          } catch (e: Exception) {
            dataProviders.exceptionLogger.logException(e)
            AsyncResult.Failure(e)
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

        override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<O> {
          return this@transformAsync.retrieveData(originNotificationIds).transformAsync(function)
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
     * Returns a new [DataProvider] in the same way as [transformAsync] except the provided
     * [function] can return a different [DataProvider] that's used to source the value of the
     * returned [DataProvider].
     *
     * Note that this function has a particularly complex subscription contract and should thus be
     * very carefully used, and only in cases when no other [DataProviders] method can be used. The
     * subscription contract is as follows:
     * 1. If [this] provider is notified, compute a new provider via [function] and notify
     *   subscriptions registered to the returned provider.
     * 2. If the returned provider is notified as changed, follow the same process as (1).
     * 3. If the computed provider via [function] is notified as changed, notify the subscribers of
     *   the provider returned by [transformDynamic] but do not compute a new provider.
     *
     * The above flow is meant to ensure that in cases when [function] may want to compute a new
     * provider that it's able to, but it can avoid potential infinite loops (or unnecessary
     * recomputations) by not recomputing specifically when the dynamic provider has changed.
     */
    fun <I, O> DataProvider<I>.transformDynamic(
      newId: Any,
      function: (I) -> DataProvider<O>
    ): DataProvider<O> {
      return getDataProviders().dynamicallyTransformedDataProviderFactory.create(
        newId, baseProvider = this, function
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

        override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<O> {
          return try {
            this@combineWith.retrieveData(originNotificationIds).combineWith(
              dataProvider.retrieveData(originNotificationIds), function
            )
          } catch (e: Exception) {
            dataProviders.exceptionLogger.logException(e)
            AsyncResult.Failure(e)
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

        override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<O> {
          return this@combineWithAsync.retrieveData(originNotificationIds).combineWithAsync(
            dataProvider.retrieveData(originNotificationIds), function
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

      override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<T> {
        return try {
          AsyncResult.Success(loadFromMemory())
        } catch (e: Exception) {
          exceptionLogger.logException(e)
          AsyncResult.Failure(e)
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

      override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<T> {
        return loadFromMemoryAsync()
      }
    }
  }

  /**
   * Returns a [DataProvider] that sources its data from this [StateFlow] with the specified [id].
   *
   * Note that changes to the [StateFlow] will **not** result in changes to the returned
   * [DataProvider] (though callers can still notify that [id] has changes to re-pull data from the
   * [StateFlow]). If callers want the provider to automatically update with the [StateFlow] they
   * should use [convertToAutomaticDataProvider].
   *
   * The returned [DataProvider] will always be in a passing state.
   */
  fun <T> StateFlow<T>.convertToSimpleDataProvider(id: Any): DataProvider<T> =
    createInMemoryDataProvider(id) { value }

  /**
   * Returns a [DataProvider] for this [StateFlow] in the same way as [convertToSimpleDataProvider]
   * except the [StateFlow] has a payload of an [AsyncResult] to affect the pending/success/failure
   * state of the provider.
   */
  fun <T> StateFlow<AsyncResult<T>>.convertAsyncToSimpleDataProvider(id: Any): DataProvider<T> =
    createInMemoryDataProviderAsync(id) { value }

  /**
   * Returns a [DataProvider] for this [StateFlow] in the same way as [convertToSimpleDataProvider]
   * except changes to the [StateFlow]'s [StateFlow.value] will result in notifications being
   * triggered for the returned [DataProvider].
   *
   * Note that the subscription to the [StateFlow] never expires and will remain for the lifetime of
   * the flow (at least until both the [StateFlow] and [DataProvider] go out of memory and are
   * cleaned up).
   */
  fun <T> StateFlow<T>.convertToAutomaticDataProvider(id: Any): DataProvider<T> {
    return convertToSimpleDataProvider(id).also {
      onEach {
        // Synchronously notify subscribers whenever the flow's state changes.
        asyncDataSubscriptionManager.notifyChange(id)
      }.launchIn(CoroutineScope(backgroundDispatcher))
    }
  }

  /**
   * Returns a [DataProvider] for this [StateFlow] in the same way as
   * [convertToAutomaticDataProvider] with the same async behavior as
   * [convertAsyncToSimpleDataProvider].
   */
  fun <T> StateFlow<AsyncResult<T>>.convertAsyncToAutomaticDataProvider(id: Any): DataProvider<T> {
    return convertAsyncToSimpleDataProvider(id).also {
      onEach {
        // Synchronously notify subscribers whenever the flow's state changes.
        asyncDataSubscriptionManager.notifyChange(id)
      }.launchIn(CoroutineScope(backgroundDispatcher))
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
    private var retrieveTransformedData: suspend (Set<Any>) -> AsyncResult<O>
  ) : DataProvider<O>(context) {
    init {
      initializeTransformer()
    }

    override fun getId(): Any = id

    override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<O> {
      return retrieveTransformedData(originNotificationIds)
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
      retrieveTransformedData = { originNotificationId ->
        dataProvider.retrieveData(originNotificationId).transformAsync(transform)
      }
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
        ) { originNotificationIds ->
          baseDataProvider.retrieveData(originNotificationIds).transformAsync(transform)
        }
      }
    }
  }

  /**
   * A [DataProvider] whose base provider can change via [transform].
   *
   * This class should never be used directly. Instead, use [transformDynamic].
   */
  class DynamicallyTransformedDataProvider<I, O> private constructor(
    context: Context,
    private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
    private val inMemoryBlockingCacheFactory: InMemoryBlockingCache.Factory,
    private val id: Any,
    private val baseProvider: DataProvider<I>,
    private val transform: (I) -> DataProvider<O>
  ): DataProvider<O>(context) {
    private val currentProviderResult: InMemoryBlockingCache<AsyncResult<DataProvider<O>>> by lazy {
      inMemoryBlockingCacheFactory.create()
    }

    override fun getId(): Any = id

    override suspend fun retrieveData(originNotificationIds: Set<Any>): AsyncResult<O> {
      return currentProviderResult.createIfAbsentAsync {
        asyncDataSubscriptionManager.associateIds(id, baseProvider.getId())
        createNewDynamicProvider(originNotificationIds)
      }.await().transformAsync { dynamicProvider ->
        // If there's a dynamic provider available, determine if it needs to be reset.
        val dynamicProviderId = dynamicProvider.getId()
        val maybeNewProviderResult = if (originNotificationIds != setOf(dynamicProviderId)) {
          // If the notification originated from somewhere else (even if partially), create a new
          // provider.
          asyncDataSubscriptionManager.dissociateIds(id, dynamicProviderId)
          currentProviderResult.updateAsync {
            createNewDynamicProvider(originNotificationIds)
          }.await()
        } else AsyncResult.Success(dynamicProvider)

        return@transformAsync maybeNewProviderResult.transformAsync {
          it.retrieveData(originNotificationIds)
        }
      }
    }

    private suspend fun createNewDynamicProvider(
      originNotificationIds: Set<Any>
    ): AsyncResult<DataProvider<O>> {
      return baseProvider.retrieveData(originNotificationIds).transform(transform).transform {
        // Make sure that the new provider, if available, is properly connected to the outer
        // provider.
        asyncDataSubscriptionManager.associateIds(id, it.getId())
        it
      }
    }

    /** Application-injectable factory for creating new [DynamicallyTransformedDataProvider]s. */
    class Factory @Inject constructor(
      private val context: Context,
      private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
      private val inMemoryBlockingCacheFactory: InMemoryBlockingCache.Factory
    ) {
      /**
       * Returns a new [DynamicallyTransformedDataProvider] with the specified [id], [baseProvider],
       * and dynamic [transform] function.
       */
      fun <I, O> create(
        id: Any, baseProvider: DataProvider<I>, transform: (I) -> DataProvider<O>
      ): DynamicallyTransformedDataProvider<I, O> {
        return DynamicallyTransformedDataProvider(
          context,
          asyncDataSubscriptionManager,
          inMemoryBlockingCacheFactory,
          id,
          baseProvider,
          transform
        )
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
          handleDataProviderUpdate(originatingIds = setOf())
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
        if (value.isNewerThanOrSameAgeAs(currentCache) &&
          !currentCache.hasSameEffectiveValueAs(value)
        ) {
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

    private suspend fun handleDataProviderUpdate(originatingIds: Set<Any>) {
      // This doesn't guarantee that retrieveData() is only called when the live data is active
      // (e.g. it can become inactive right after the value is posted & before it's dispatched), but
      // it does guarantee that it won't be called when the live data is currently inactive. This
      // also safely passes the value to the main thread and relies on LiveData's own internal
      // mechanism which in turn always calls setValue(), even if there are no active observers. See
      // the override of setValue() above for the adjusted semantics this class requires to ensure
      // its own cache remains up-to-date.
      retrieveFromDataProvider(originatingIds)?.let {
        super.postValue(it)
        runningJob.set(null)
      }
    }

    private suspend fun retrieveFromDataProvider(originatingIds: Set<Any>): AsyncResult<T>? {
      return if (isActive.get()) {
        // Although it's possible for the live data to become inactive after this point, this
        // follows the expected contract of the data provider (it may have its data retrieved and
        // not delivered), and it guarantees eventual consistency since the class still caches the
        // results in case a new observer is added later.
        dataProvider.retrieveData(originatingIds)
      } else null
    }
  }
}
