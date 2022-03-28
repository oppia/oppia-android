package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for fetching and updating platform parameters in the database. */
@Singleton
class PlatformParameterController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  platformParameterSingleton: PlatformParameterSingleton
) {
  private val platformParameterDatabaseStore = cacheStoreFactory.create(
    PLATFORM_PARAMETER_DATABASE_NAME,
    RemotePlatformParameterDatabase.getDefaultInstance()
  )

  /** The state of the platform parameter caching process. */
  private enum class PlatformParameterCachingStatus {
    /** Whether the caching operation corresponding to this status was successful. */
    SUCCESS
  }

  companion object {
    /** ID for all the data providers used in [PlatformParameterController]. */
    private const val PLATFORM_PARAMETER_DATA_PROVIDER_ID = "platform_parameter_data_provider_id"
    /** Name of the platform parameter database in cache store. */
    private const val PLATFORM_PARAMETER_DATABASE_NAME = "platform_parameter_database"
  }

  private val platformParameterDataProvider by lazy {
    // After this transformation the cached List of Platform Parameters gets converted into a simple
    // map where the keys corresponds to the name of Platform Parameter and value will correspond to
    // the PlatformParameter object itself
    platformParameterDatabaseStore.transform(PLATFORM_PARAMETER_DATA_PROVIDER_ID) {
      platformParameterDatabase ->
      val platformParameterMap = mutableMapOf<String, PlatformParameter>()
      platformParameterDatabase.platformParameterList.forEach {
        platformParameterMap[it.name] = it
      }
      platformParameterSingleton.setPlatformParameterMap(platformParameterMap)
      return@transform
    }
  }

  /**
   * Updates the platform parameter database in cache store.
   *
   * @param platformParameterList list of [PlatformParameter] objects which needs to be cached
   * @return a [DataProvider] that indicates the success/failure of this update operation
   */
  fun updatePlatformParameterDatabase(
    platformParameterList: List<PlatformParameter>
  ): DataProvider<Any?> {
    val deferredTask = platformParameterDatabaseStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = false
    ) {
      Pair(
        it.toBuilder().addAllPlatformParameter(platformParameterList).build(),
        PlatformParameterCachingStatus.SUCCESS
      )
    }
    return dataProviders.createInMemoryDataProviderAsync(PLATFORM_PARAMETER_DATA_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferredTask)
    }
  }

  /**
   * Executes and transforms the [Deferred] task into an [AsyncResult].
   *
   * @param deferred task which needs to be executed
   * @return async result for success or failure after the execution of deferred task
   */
  private suspend fun getDeferredResult(
    deferred: Deferred<PlatformParameterCachingStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      PlatformParameterCachingStatus.SUCCESS -> AsyncResult.Success(null)
    }
  }

  /**
   * Returns a [DataProvider] which can be used to confirm that PlatformParameterDatabase read
   * process has been completed.
   */
  fun getParameterDatabase(): DataProvider<Unit> = platformParameterDataProvider
}
