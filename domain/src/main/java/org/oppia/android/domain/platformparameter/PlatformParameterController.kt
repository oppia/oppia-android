package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import javax.inject.Inject
import javax.inject.Singleton

private const val PLATFORM_PARAMETER_DATA_PROVIDER_ID = "platform_parameter_data_provider_id"
private const val PLATFORM_PARAMETER_DATABASE_NAME = "platform_parameter_database"

/** Controller for fetching and updating platform parameters in the database. */
@Singleton
class PlatformParameterController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  platformParameterSingleton: PlatformParameterSingleton
) {
  private val platformParameterDatabaseStore = cacheStoreFactory.create(
    PLATFORM_PARAMETER_DATABASE_NAME,
    RemotePlatformParameterDatabase.getDefaultInstance()
  )

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
   * @param platformParameterList [List] of [PlatformParameter] objects which needs to be cached
   * @return [Unit]
   */
  fun updatePlatformParameterDatabase(
    platformParameterList: List<PlatformParameter>
  ) {
    platformParameterDatabaseStore.storeDataAsync(
      updateInMemoryCache = false
    ) {
      it.toBuilder()
        .addAllPlatformParameter(platformParameterList)
        .build()
    }.invokeOnCompletion {
      it?.let {
        oppiaLogger.e("DOMAIN", "Failed when storing platform parameter values list", it)
      }
    }
  }

  /**
   * Returns a [DataProvider] which can be used to confirm that PlatformParameterDatabase read
   * process has been completed.
   */
  fun getParameterDatabase(): DataProvider<Unit> = platformParameterDataProvider
}
