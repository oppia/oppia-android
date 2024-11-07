package org.oppia.android.domain.onboarding

import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.DeprecationResponseDatabase
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val APP_STARTUP_STATE_PROVIDER_ID = "app_startup_state_data_provider_id"

/** Controller for persisting and retrieving the user's initial app state upon opening the app. */
@Singleton
class AppStartupStateController @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  private val expirationMetaDataRetriever: ExpirationMetaDataRetriever,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val currentBuildFlavor: BuildFlavor,
  private val deprecationController: DeprecationController,
  @EnableAppAndOsDeprecation
  private val enableAppAndOsDeprecation: Provider<PlatformParameterValue<Boolean>>,
  private val analyticsController: AnalyticsController,
) {
  private val onboardingFlowStore by lazy {
    cacheStoreFactory.create("on_boarding_flow", OnboardingState.getDefaultInstance())
  }

  private val appStartupStateDataProvider by lazy { computeAppStartupStateProvider() }

  init {
    // Prime the cache ahead of time so that any existing history is read prior to any calls to
    // markOnboardingFlowCompleted(). Note that this also ensures that the on-disk cache contains
    // the last used build flavor (but it doesn't update the in-memory copy as it's the *last* used
    // flavor, and thus requires an app restart in order to observe).
    onboardingFlowStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_ALWAYS,
      publishMode = PersistentCacheStore.PublishMode.DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
    ) { state ->
      state.toBuilder().apply { lastUsedBuildFlavor = currentBuildFlavor }.build()
    }.invokeOnCompletion { primeFailure ->
      if (primeFailure != null) {
        oppiaLogger.e(
          "StartupController",
          "Failed to prime cache ahead of data retrieval for user onboarding data.",
          primeFailure
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
  fun markOnboardingFlowCompleted(profileId: ProfileId? = null) {
    updateOnboardingState { alreadyOnboardedApp = true }
    logAppOnboardedEvent(profileId)
  }

  /**
   * Saves that the user never wants to see beta notices again.
   *
   * Note that this does not notify existing subscribers of the changed state, nor can future
   * subscribers observe this state until the app restarts.
   */
  fun dismissBetaNoticesPermanently() {
    updateOnboardingState { permanentlyDismissedBetaNotice = true }
  }

  /**
   * Saves that the user never wants to notices for cases when their app has updated from a
   * pre-release version of the app to the general availability version.
   *
   * Note that this does not notify existing subscribers of the changed state, nor can future
   * subscribers observe this state until the app restarts.
   */
  fun dismissGaUpgradeNoticesPermanently() {
    updateOnboardingState { permanentlyDismissedGaUpgradeNotice = true }
  }

  /**
   * Returns a [DataProvider] containing the user's startup state, which in turn affect what initial
   * app flow the user is directed to.
   */
  fun getAppStartupState(): DataProvider<AppStartupState> = appStartupStateDataProvider

  private fun computeAppStartupStateProvider(): DataProvider<AppStartupState> {
    val databaseProvider = deprecationController.getDeprecationDatabase()

    return onboardingFlowStore.combineWith(
      databaseProvider,
      APP_STARTUP_STATE_PROVIDER_ID
    ) { onboardingState, deprecationResponseDatabase ->
      AppStartupState.newBuilder().apply {
        startupMode = computeAppStartupMode(onboardingState, deprecationResponseDatabase)
        buildFlavorNoticeMode = computeBuildNoticeMode(onboardingState, startupMode)
      }.build()
    }
  }

  private fun updateOnboardingState(updateState: OnboardingState.Builder.() -> Unit) {
    // Note that the flavor must be written here since it only gets updated on-disk and never
    // in-memory (which means it will be inadvertently overwritten when updating onboarding state
    // here).
    val deferred = onboardingFlowStore.storeDataAsync(updateInMemoryCache = false) { state ->
      state.toBuilder().apply {
        updateState()
        lastUsedBuildFlavor = currentBuildFlavor
      }.build()
    }
    deferred.invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e("StartupController", "Failed to update onboarding state.", failure)
      }
    }
  }

  private fun computeAppStartupMode(
    onboardingState: OnboardingState,
    deprecationResponseDatabase: DeprecationResponseDatabase
  ): StartupMode {
    // Process and return either a StartupMode.APP_IS_DEPRECATED, StartupMode.USER_IS_ONBOARDED or
    // StartupMode.USER_NOT_YET_ONBOARDED if the app and OS deprecation feature flag is not enabled.
    if (!enableAppAndOsDeprecation.get().value) {
      return when {
        hasAppExpired() -> StartupMode.APP_IS_DEPRECATED
        onboardingState.alreadyOnboardedApp -> StartupMode.USER_IS_ONBOARDED
        else -> StartupMode.USER_NOT_YET_ONBOARDED
      }
    }

    return deprecationController.processStartUpMode(onboardingState, deprecationResponseDatabase)
  }

  private fun computeBuildNoticeMode(
    onboardingState: OnboardingState,
    startupMode: StartupMode
  ): BuildFlavorNoticeMode {
    return when (currentBuildFlavor) {
      BuildFlavor.TESTING, BuildFlavor.BUILD_FLAVOR_UNSPECIFIED, BuildFlavor.UNRECOGNIZED ->
        BuildFlavorNoticeMode.FLAVOR_NOTICE_MODE_UNSPECIFIED
      // No notice is shown for developer & alpha builds.
      BuildFlavor.DEVELOPER, BuildFlavor.ALPHA -> BuildFlavorNoticeMode.NO_NOTICE
      BuildFlavor.BETA -> {
        // Only show the beta notice if the user hasn't permanently dismissed it, and when it's
        // appropriate to show (i.e. they've recently changed to the beta flavor, and their app is
        // not force-deprecated).
        if (!onboardingState.permanentlyDismissedBetaNotice &&
          onboardingState.lastUsedBuildFlavor != BuildFlavor.BETA &&
          startupMode != StartupMode.APP_IS_DEPRECATED
        ) {
          BuildFlavorNoticeMode.SHOW_BETA_NOTICE
        } else BuildFlavorNoticeMode.NO_NOTICE
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

  private fun logAppOnboardedEvent(profileId: ProfileId?) {
    analyticsController.logAppOnboardedEvent(profileId)
  }
}
