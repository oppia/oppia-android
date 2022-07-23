package org.oppia.android.domain.onboarding

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject
import javax.inject.Singleton

private const val UPDATE_ONBOARDING_STATE_PROVIDER_ID = "update_onboarding_state_data_provider_id"
private const val APP_STARTUP_STATE_PROVIDER_ID = "app_startup_state_data_provider_id"

/** Controller for persisting and retrieving the user's initial app state upon opening the app. */
@Singleton
class AppStartupStateController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  private val expirationMetaDataRetriever: ExpirationMetaDataRetriever,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val dataProviders: DataProviders,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher,
  private val currentBuildFlavor: BuildFlavor
) {
  private val onboardingFlowStore =
    cacheStoreFactory.create("on_boarding_flow", OnboardingState.getDefaultInstance())

  private val appStartupStateDataProvider by lazy { computeAppStartupStateProvider() }

  init {
    // Prime the cache ahead of time so that any existing history is read prior to any calls to
    // markOnboardingFlowCompleted().
    onboardingFlowStore.primeInMemoryCacheAsync().invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "StartupController",
          "Failed to prime cache ahead of data retrieval for user onboarding data.",
          failure
        )
      }
    }
  }

  /**
   * Saves that the user has completed the app onboarding flow.
   *
   * Note that this does not notify existing subscribers of the changed state, nor can future
   * subscribers observe this state until the app restarts.
   */
  fun markOnboardingFlowCompleted() {
    updateOnboardingState { onboardingState ->
      onboardingState.toBuilder().apply {
        alreadyOnboardedApp = true
      }.build()
    }
  }

  /**
   * Saves that the user never wants to see beta notices again.
   *
   * Note that this does not notify existing subscribers of the changed state, nor can future
   * subscribers observe this state until the app restarts.
   */
  fun dismissBetaNoticesPermanently() {
    updateOnboardingState { onboardingState ->
      onboardingState.toBuilder().apply {
        permanentlyDismissedBetaNotice = true
      }.build()
    }
  }

  /**
   * Saves that the user never wants to notices for cases when their app has updated from a
   * pre-release version of the app to the general availability version.
   *
   * Note that this does not notify existing subscribers of the changed state, nor can future
   * subscribers observe this state until the app restarts.
   */
  fun dismissGaUpgradeNoticesPermanently() {
    updateOnboardingState { onboardingState ->
      onboardingState.toBuilder().apply {
        permanentlyDismissedGaUpgradeNotice = true
      }.build()
    }
  }

  /**
   * Returns a [DataProvider] containing the user's startup state, which in turn affect what initial
   * app flow the user is directed to.
   */
  fun getAppStartupState(): DataProvider<AppStartupState> = appStartupStateDataProvider

  private fun computeAppStartupStateProvider(): DataProvider<AppStartupState> {
    val updateProvider = dataProviders.run {
      onboardingFlowStore.storeDataAsync(updateInMemoryCache = false) { state ->
        state.toBuilder().apply { lastUsedBuildFlavor = currentBuildFlavor }.build()
      }.toStateFlow().convertAsyncToSimpleDataProvider(UPDATE_ONBOARDING_STATE_PROVIDER_ID)
    }
    // Combining a cache read and update like this may seem like it could introduce a data race,
    // however it won't because: (1) the update doesn't alter the in-memory cache, and (2) the
    // in-memory cache should already be primed before this point.
    return updateProvider.combineWith(
      onboardingFlowStore, APP_STARTUP_STATE_PROVIDER_ID
    ) { _, onboardingState ->
      AppStartupState.newBuilder().apply {
        startupMode = computeAppStartupMode(onboardingState)
        buildFlavorNoticeMode = computeBuildNoticeMode(onboardingState)
      }.build()
    }
  }

  private fun updateOnboardingState(updateState: (OnboardingState) -> OnboardingState) {
    val deferred = onboardingFlowStore.storeDataAsync(updateInMemoryCache = false, updateState)
    deferred.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e("StartupController", "Failed to update onboarding state.", failure)
      }
    }
  }

  private fun computeAppStartupMode(onboardingState: OnboardingState): StartupMode {
    return when {
      hasAppExpired() -> StartupMode.APP_IS_DEPRECATED
      onboardingState.alreadyOnboardedApp -> StartupMode.USER_IS_ONBOARDED
      else -> StartupMode.USER_NOT_YET_ONBOARDED
    }
  }

  private fun computeBuildNoticeMode(onboardingState: OnboardingState): BuildFlavorNoticeMode {
    return when (currentBuildFlavor) {
      BuildFlavor.TESTING, BuildFlavor.BUILD_FLAVOR_UNSPECIFIED, BuildFlavor.UNRECOGNIZED ->
        BuildFlavorNoticeMode.FLAVOR_NOTICE_MODE_UNSPECIFIED
      // No notice is shown for developer & alpha builds.
      BuildFlavor.DEVELOPER, BuildFlavor.ALPHA -> BuildFlavorNoticeMode.NO_NOTICE
      BuildFlavor.BETA -> {
        if (!onboardingState.permanentlyDismissedBetaNotice &&
          onboardingState.lastUsedBuildFlavor != BuildFlavor.BETA
        ) {
          BuildFlavorNoticeMode.SHOW_BETA_NOTICE
        } else BuildFlavorNoticeMode.NO_NOTICE // The user doesn't want to see the notice again.
      }
      BuildFlavor.GENERAL_AVAILABILITY -> when (onboardingState.lastUsedBuildFlavor) {
        BuildFlavor.ALPHA, BuildFlavor.BETA, null -> {
          if (!onboardingState.permanentlyDismissedGaUpgradeNotice) {
            BuildFlavorNoticeMode.SHOW_UPGRADE_TO_GENERAL_AVAILABILITY_NOTICE
          } else BuildFlavorNoticeMode.NO_NOTICE // The user doesn't want to see the notice again.
        }
        // A brand new install should result in no notice, or an update from a developer build.
        BuildFlavor.BUILD_FLAVOR_UNSPECIFIED, BuildFlavor.UNRECOGNIZED, BuildFlavor.TESTING,
        BuildFlavor.DEVELOPER, BuildFlavor.GENERAL_AVAILABILITY -> BuildFlavorNoticeMode.NO_NOTICE
      }
    }
  }

  private fun hasAppExpired(): Boolean {
    val applicationMetadata = expirationMetaDataRetriever.getMetaData()
    val isAppExpirationEnabled =
      applicationMetadata?.getBoolean(
        "automatic_app_expiration_enabled", /* defaultValue= */ true
      ) ?: true
    return if (isAppExpirationEnabled) {
      val expirationDateString = applicationMetadata?.getStringFromBundle("expiration_date")
      val expirationDate = expirationDateString?.let { machineLocale.parseOppiaDate(it) }
      // Assume the app is in an expired state if something fails when comparing the date.
      expirationDate?.isBeforeToday() ?: true
    } else false
  }

  /**
   * Converts a [Deferred] to a [StateFlow] in a way that avoids potentially deadlocking when
   * asynchronously blocking on the [Deferred] since it leverages a background coroutine.
   *
   * The returned [StateFlow] will never be updated more than once, and will always start in a
   * pending state.
   */
  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }
}
