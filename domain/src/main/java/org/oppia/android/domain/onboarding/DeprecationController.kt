package org.oppia.android.domain.onboarding

import android.os.Build
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.app.model.DeprecationResponseDatabase
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.BuildConfig
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_DEPRECATION_RESPONSE_PROVIDER_ID = "get_deprecation_response_provider_id"
private const val ADD_DEPRECATION_RESPONSE_PROVIDER_ID = "add_deprecation_response_provider_id"
private const val GET_DEPRECATION_RESPONSE_DATABASE_ID = "get_deprecation_response_database_id"

/**
 * Controller for persisting and retrieving the user's deprecation responses. This will be used to
 * handle deprecations once the user opens the app.
 */
@Singleton
class DeprecationController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  private val dataProviders: DataProviders,
  @OptionalAppUpdateVersionCode
  private val optionalAppUpdateVersionCode: PlatformParameterValue<Int>,
  @ForcedAppUpdateVersionCode
  private val forcedAppUpdateVersionCode: PlatformParameterValue<Int>,
  @LowestSupportedApiLevel
  private val lowestSupportedApiLevel: PlatformParameterValue<Int>
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
    /** Indicates that the deprecation response read/write operation succeeded. */
    SUCCESS
  }

  init {
    // Prime the cache ahead of time so that the deprecation response can be retrieved
    // synchronously.
    deprecationStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_ALWAYS,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion { primeFailure ->
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
    return deprecationStore.transform(
      GET_DEPRECATION_RESPONSE_PROVIDER_ID
    ) { deprecationResponsesDatabase ->
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

//  private fun getDeprecationResponseDatabase(): DeprecationResponseDatabase {
//    val deprecationDataProvider = fetchDeprecationProvider()
//
//    var deprecationDatabase = DeprecationResponseDatabase.newBuilder().build()
//
//    deprecationDataProvider.transform(GET_DEPRECATION_RESPONSE_DATABASE_ID) {
//      deprecationResponseDatabase ->
//      deprecationDatabase = deprecationResponseDatabase
//    }
//
//    return deprecationDatabase
//  }
//
//  private fun fetchDeprecationProvider(): DataProvider<DeprecationResponseDatabase> {
//    return deprecationStore.transform(
//      GET_DEPRECATION_RESPONSE_PROVIDER_ID
//    ) { deprecationResponsesDatabase ->
//      DeprecationResponseDatabase.newBuilder().apply {
//        appDeprecationResponse = deprecationResponsesDatabase.appDeprecationResponse
//        osDeprecationResponse = deprecationResponsesDatabase.osDeprecationResponse
//      }.build()
//    }
//  }

  /**
   * Stores a new [DeprecationResponse] to the cache.
   *
   * @param deprecationResponse the deprecation response to be stored
   * @return [AsyncResult] of the deprecation action
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
   * @param deferred a deferred instance of the [DeprecationResponseActionStatus]
   * @return [AsyncResult]
   */
  private suspend fun getDeferredResult(
    deferred: Deferred<DeprecationResponseActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      DeprecationResponseActionStatus.SUCCESS -> AsyncResult.Success(null)
    }
  }

  /**
   * Process and return either a [StartupMode.OS_IS_DEPRECATED], [StartupMode.APP_IS_DEPRECATED],
   * [StartupMode.OPTIONAL_UPDATE_AVAILABLE], [StartupMode.USER_IS_ONBOARDED] or
   * [StartupMode.USER_NOT_YET_ONBOARDED] based on the values of [lowestSupportedApiLevel],
   * [optionalAppUpdateVersionCode], [forcedAppUpdateVersionCode] and [onboardingState].
   */
  fun processStartUpMode(
    onboardingState: OnboardingState,
    deprecationDatabase: DeprecationResponseDatabase
  ): StartupMode {
    val previousDeprecatedAppVersion = deprecationDatabase.appDeprecationResponse.deprecatedVersion
    val previousDeprecatedOsVersion = deprecationDatabase.osDeprecationResponse.deprecatedVersion

    val appVersionCode = BuildConfig.VERSION_CODE
    val currentApiLevel = Build.VERSION.SDK_INT
    val osIsDeprecated = lowestSupportedApiLevel.value > currentApiLevel
    val osDeprecationDialogHasNotBeenShown =
      previousDeprecatedOsVersion < lowestSupportedApiLevel.value

    val forcedAppUpdateIsAvailable = forcedAppUpdateVersionCode.value > appVersionCode
    val optionalAppUpdateIsAvailable = optionalAppUpdateVersionCode.value > appVersionCode

    val optionalAppDeprecationDialogHasNotBeenShown =
      previousDeprecatedAppVersion < optionalAppUpdateVersionCode.value
    val forcedAppDeprecationDialogHasNotBeenShown =
      previousDeprecatedAppVersion < forcedAppUpdateVersionCode.value

    if (onboardingState.alreadyOnboardedApp) {
      if (osIsDeprecated && osDeprecationDialogHasNotBeenShown) {
        return StartupMode.OS_IS_DEPRECATED
      }

      if (forcedAppUpdateIsAvailable && forcedAppDeprecationDialogHasNotBeenShown) {
        return StartupMode.APP_IS_DEPRECATED
      }

      if (optionalAppUpdateIsAvailable && optionalAppDeprecationDialogHasNotBeenShown) {
        return StartupMode.OPTIONAL_UPDATE_AVAILABLE
      }

      return StartupMode.USER_IS_ONBOARDED
    } else return StartupMode.USER_NOT_YET_ONBOARDED
  }
}
