package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.transform
import javax.inject.Inject
import javax.inject.Singleton

private const val PLATFORM_PARAMETER_DATA_PROVIDER_ID = "platform_parameter_data_provider_id"

@Singleton
class PlatformParameterController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  platformParameterSingleton: PlatformParameterSingleton
) {
  private val platformParameterDatabaseStore = cacheStoreFactory.create(
    "platform_parameter_database",
    RemotePlatformParameterDatabase.getDefaultInstance()
  )

  private val platformParameterDatabaseProvider by lazy {
    platformParameterDatabaseStore.transform(PLATFORM_PARAMETER_DATA_PROVIDER_ID) {
      platformParameterDatabase ->
      val platformParameterMap = mutableMapOf<String, PlatformParameter>()
      platformParameterDatabase.platformParameterList.forEach {
        platformParameterMap[it.name] = it
      }
      platformParameterSingleton.setPlatformParameterMap(platformParameterMap)
    }
  }

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

  fun getParameterDatabase(): DataProvider<Unit> = platformParameterDatabaseProvider
}
