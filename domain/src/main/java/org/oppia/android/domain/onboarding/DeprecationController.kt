package org.oppia.android.domain.onboarding

import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.app.model.DeprecationResponseDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import javax.inject.Inject
import javax.inject.Singleton

private const val DEPRECATION_PROVIDER_ID = "deprecation_data_provider_id"
private const val ADD_DEPRECATION_PROVIDER_ID = "add_deprecation_data_provider_id"

@Singleton
class DeprecationController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  private val dataProviders: DataProviders
) {
  /**
   * Create an instance of [PersistentCacheStore] that contains a [DeprecationResponseDatabase].
   */
  private val deprecationStore by lazy {
    cacheStoreFactory.create(
      "deprecation_store",
      DeprecationResponseDatabase.getDefaultInstance()
    )
  }

  /**
   * Enum states for the possible outcomes of a deprecation action.
   */
  private enum class DeprecationActionStatus {
    SUCCESS,
    FAILED_TO_STORE_DEPRECATION_RESPONSE,
    DEPRECATION_RESPONSE_NOT_FOUND
  }

  init {
    /**
     * Prime the cache ahead of time so that the deprecation response can be retrieved
     * synchronously.
     */
    deprecationStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_ALWAYS,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ) {
      it.toBuilder().build()
    }.invokeOnCompletion { primeFailure ->
      primeFailure?.let {
        oppiaLogger.e(
          "DeprecationController",
          "Failed to prime cache ahead of data retrieval for DeprecationController.",
          primeFailure
        )
      }
    }
  }

  val deprecationDataProvider by lazy { computeDeprecationProvider() }

  private fun computeDeprecationProvider(): DataProvider<DeprecationResponseDatabase> {
    return deprecationStore.transform(DEPRECATION_PROVIDER_ID) { deprecationResponsesDatabase ->
      DeprecationResponseDatabase.newBuilder().apply {
        appDeprecationResponse = deprecationResponsesDatabase.appDeprecationResponse
        osDeprecationResponse = deprecationResponsesDatabase.osDeprecationResponse
      }.build()
    }
  }

  /**
   * Returns a [DataProvider] containing the the [DeprecationResponseDatabase], which in turn
   * affects what initial app flow the user is directed to.
   */
  fun getDeprecationDatabase(): DataProvider<DeprecationResponseDatabase> = deprecationDataProvider

  /**
   * Stores a new [DeprecationResponse] to the cache.
   *
   * @param deprecationResponse the deprecation response to be stored.
   * @return [AsyncResult] of the deprecation action.
   */
  fun setDeprecationResponse(deprecationResponse: DeprecationResponse): DataProvider<Any?> {
    val deferred = deprecationStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val deprecationBuilder = if (deprecationResponse.deprecationNoticeType
        == DeprecationNoticeType.DEPRECATION_NOTICE_TYPE_APP_UPDATE
      ) {
        it.toBuilder()
          .setAppDeprecationResponse(deprecationResponse)
          .build()
      } else {
        it.toBuilder()
          .setOsDeprecationResponse(deprecationResponse)
          .build()
      }

      Pair(deprecationBuilder, DeprecationActionStatus.SUCCESS)
    }

    return dataProviders.createInMemoryDataProviderAsync(ADD_DEPRECATION_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  /**
   * Retrieves the [DeprecationResponse] from the cache.
   *
   * @param deferred a deferred instance of the [DeprecationActionStatus].
   * @return [AsyncResult].
   */
  private suspend fun getDeferredResult(
    deferred: Deferred<DeprecationActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      DeprecationActionStatus.SUCCESS -> AsyncResult.Success(null)
      DeprecationActionStatus.FAILED_TO_STORE_DEPRECATION_RESPONSE -> AsyncResult.Failure(
        Exception("Failed to store deprecation response")
      )
      DeprecationActionStatus.DEPRECATION_RESPONSE_NOT_FOUND -> AsyncResult.Failure(
        Exception("Deprecation response not found")
      )
    }
  }
}
