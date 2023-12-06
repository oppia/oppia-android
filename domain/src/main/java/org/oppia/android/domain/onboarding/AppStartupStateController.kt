package org.oppia.android.domain.onboarding

import android.os.Build
import kotlinx.coroutines.runBlocking
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.DeprecationResponseDatabase
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
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
  private val enableAppAndOsDeprecation: PlatformParameterValue<Boolean>,
  @OptionalAppUpdateVersionCode
  private val optionalAppUpdateVersionCode: PlatformParameterValue<Int>,
  @ForcedAppUpdateVersionCode
  private val forcedAppUpdateVersionCode: PlatformParameterValue<Int>,
  @LowestSupportedApiLevel
  private val lowestSupportedApiLevel: PlatformParameterValue<Int>
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
  fun markOnboardingFlowCompleted() {
    updateOnboardingState { alreadyOnboardedApp = true }
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
    return onboardingFlowStore.transform(APP_STARTUP_STATE_PROVIDER_ID) { onboardingState ->
      AppStartupState.newBuilder().apply {
        startupMode = computeAppStartupMode(onboardingState)
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

  private fun computeAppStartupMode(onboardingState: OnboardingState): StartupMode {
    // Return old logic if app and os feature flag is not enabled
    if (!enableAppAndOsDeprecation.value) {
      return when {
        hasAppExpired() -> StartupMode.APP_IS_DEPRECATED
        onboardingState.alreadyOnboardedApp -> StartupMode.USER_IS_ONBOARDED
        else -> StartupMode.USER_NOT_YET_ONBOARDED
      }
    }

    val deprecationDataProvider = deprecationController.getDeprecationDatabase()

    var deprecationDatabase = DeprecationResponseDatabase.newBuilder().build()

    runBlocking {
      deprecationDataProvider.retrieveData().transform {
        deprecationDatabase = it
      }
    }

    val appVersionCode = Build.VERSION.SDK_INT

    val osIsDeprecated = lowestSupportedApiLevel.value > appVersionCode &&
      deprecationDatabase.osDeprecationResponse.deprecatedVersion != appVersionCode
    val appUpdateIsAvailable = optionalAppUpdateVersionCode.value > appVersionCode ||
      forcedAppUpdateVersionCode.value > appVersionCode

    if (onboardingState.alreadyOnboardedApp) {
      if (osIsDeprecated) {
        return StartupMode.OS_IS_DEPRECATED
      }

      if (appUpdateIsAvailable) {
        if (forcedAppUpdateVersionCode.value > appVersionCode) {
          return StartupMode.APP_IS_DEPRECATED
        }

        if (deprecationDatabase.appDeprecationResponse.deprecatedVersion !=
          optionalAppUpdateVersionCode.value
        ) {
          return StartupMode.OPTIONAL_UPDATE_AVAILABLE
        }
      }

      return StartupMode.USER_IS_ONBOARDED
    } else return StartupMode.USER_NOT_YET_ONBOARDED
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
}
