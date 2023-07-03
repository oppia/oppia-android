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

private const val GET_DEPRECATION_RESPONSE_PROVIDER_ID = "get_deprecation_response_provider_id"
private const val ADD_DEPRECATION_RESPONSE_PROVIDER_ID = "add_deprecation_response_provider_id"

/**
 * Controller for persisting and retrieving the user's deprecation responses. This will be used to
 * handle deprecations once the user opens the app.
 */
@Singleton
class DeprecationController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  private val dataProviders: DataProviders
) {
  /** Create an instance of [PersistentCacheStore] that contains a [DeprecationResponseDatabase]. */
  private val deprecationStore by lazy {
    cacheStoreFactory.create(
      "deprecation_store",
      DeprecationResponseDatabase.getDefaultInstance()
    )
  }

  /** Enum states for the possible outcomes of a deprecation action. */
  private enum class DeprecationResponseActionStatus {
    /** Indicates that the deprecation operation succeeded. */
    SUCCESS,

    /** Indicates that the deprecation response write/store operation succeeded. */
    FAILED_TO_STORE_DEPRECATION_RESPONSE,

    /**
     * Indicates that a deprecation response read operation failed. This is usually the result when
     * a requested [DeprecationResponse] was not found.
     */
    DEPRECATION_RESPONSE_NOT_FOUND
  }

  init {
    // Prime the cache ahead of time so that the deprecation response can be retrieved
    // synchronously.
    deprecationStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_ALWAYS,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ) .invokeOnCompletion { primeFailure ->
      primeFailure?.let {
        oppiaLogger.e(
          "DeprecationController",
          "Failed to prime cache ahead of data retrieval for DeprecationController.",
          primeFailure
        )
      }
    }
  }

  private val deprecationDataProvider by lazy { fetchDeprecationProvider() }

  private fun fetchDeprecationProvider(): DataProvider<DeprecationResponseDatabase> {
    return deprecationStore.transform(GET_DEPRECATION_RESPONSE_PROVIDER_ID) { deprecationResponsesDatabase ->
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
  fun saveDeprecationResponse(deprecationResponse: DeprecationResponse): DataProvider<Any?> {
    val deferred = deprecationStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { deprecationResponseDb ->
      val deprecationBuilder = deprecationResponseDb.toBuilder().apply {
        if (deprecationResponse.deprecationNoticeType == DeprecationNoticeType.APP_DEPRECATION)
          appDeprecationResponse = deprecationResponse
        else
          osDeprecationResponse = deprecationResponse
      }
        .build()
      Pair(deprecationBuilder, DeprecationResponseActionStatus.SUCCESS)
    }

    return dataProviders.createInMemoryDataProviderAsync(ADD_DEPRECATION_RESPONSE_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  /**
   * Retrieves the [DeprecationResponse] from the cache.
   *
   * @param deferred a deferred instance of the [DeprecationResponseActionStatus].
   * @return [AsyncResult].
   */
  private suspend fun getDeferredResult(
    deferred: Deferred<DeprecationResponseActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      DeprecationResponseActionStatus.SUCCESS -> AsyncResult.Success(null)
      DeprecationResponseActionStatus.FAILED_TO_STORE_DEPRECATION_RESPONSE -> AsyncResult.Failure(
        Exception("Failed to store deprecation response")
      )
      DeprecationResponseActionStatus.DEPRECATION_RESPONSE_NOT_FOUND -> AsyncResult.Failure(
        Exception("Deprecation response not found")
      )
    }
  }
}
