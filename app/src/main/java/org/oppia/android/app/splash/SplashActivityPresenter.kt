package org.oppia.android.app.splash

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileOnboardingMode
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.notice.AutomaticAppDeprecationNoticeDialogFragment
import org.oppia.android.app.notice.BetaNoticeDialogFragment
import org.oppia.android.app.notice.DeprecationNoticeActionResponse
import org.oppia.android.app.notice.ForcedAppDeprecationNoticeDialogFragment
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeDialogFragment
import org.oppia.android.app.notice.OptionalAppDeprecationNoticeDialogFragment
import org.oppia.android.app.notice.OsDeprecationNoticeDialogFragment
import org.oppia.android.app.onboarding.IntroActivity
import org.oppia.android.app.onboarding.IntroActivity.Companion.PARAMS_KEY
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.utility.lifecycle.LifecycleSafeTimerFactory
import org.oppia.android.databinding.SplashActivityBinding
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.onboarding.DeprecationController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

private const val AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG = "auto_deprecation_notice_dialog"
private const val FORCED_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG = "forced_deprecation_notice_dialog"
private const val BETA_NOTICE_DIALOG_FRAGMENT_TAG = "beta_notice_dialog"
private const val GA_UPDATE_NOTICE_DIALOG_FRAGMENT_TAG = "general_availability_update_notice_dialog"
private const val OPTIONAL_UPDATE_NOTICE_DIALOG_FRAGMENT_TAG = "optional_update_notice_dialog"
private const val OS_UPDATE_NOTICE_DIALOG_FRAGMENT_TAG = "os_update_notice_dialog"
private const val SPLASH_INIT_STATE_DATA_PROVIDER_ID = "splash_init_state_data_provider"

/** The presenter for [SplashActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val appStartupStateController: AppStartupStateController,
  private val translationController: TranslationController,
  private val localeController: LocaleController,
  private val deprecationController: DeprecationController,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler,
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val currentBuildFlavor: BuildFlavor,
  @EnableAppAndOsDeprecation
  private val enableAppAndOsDeprecation: PlatformParameterValue<Boolean>,
  private val profileManagementController: ProfileManagementController,
  @EnableOnboardingFlowV2 private val enableOnboardingFlowV2: PlatformParameterValue<Boolean>,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>
) {
  lateinit var startupMode: StartupMode

  fun handleOnCreate() {
    DataBindingUtil.setContentView<SplashActivityBinding>(
      activity, R.layout.splash_activity
    ).apply {
      isOnDeveloperFlavor = currentBuildFlavor == BuildFlavor.DEVELOPER
      isOnAlphaFlavor = currentBuildFlavor == BuildFlavor.ALPHA
      isOnBetaFlavor = currentBuildFlavor == BuildFlavor.BETA
    }

    subscribeToOnboardingFlow()
  }

  fun handleOnDeprecationNoticeActionClicked(
    noticeActionResponse: DeprecationNoticeActionResponse
  ) {
    when (noticeActionResponse) {
      is DeprecationNoticeActionResponse.Close -> handleOnDeprecationNoticeCloseAppButtonClicked()
      is DeprecationNoticeActionResponse.Dismiss -> handleOnDeprecationNoticeDialogDismissed(
        deprecationNoticeType = noticeActionResponse.deprecationNoticeType,
        deprecatedVersion = noticeActionResponse.deprecatedVersion
      )
      is DeprecationNoticeActionResponse.Update -> handleOnDeprecationNoticeUpdateButtonClicked()
    }
  }

  /** Handles cases where the user clicks the close app option on a deprecation notice dialog. */
  fun handleOnDeprecationNoticeCloseAppButtonClicked() {
    // If the app close button is clicked for the deprecation notice, finish the activity to close
    // the app.
    activity.finish()
  }

  /** Handles cases where the user clicks the update button on a deprecation notice dialog. */
  private fun handleOnDeprecationNoticeUpdateButtonClicked() {
    // If the Update button is clicked for the deprecation notice, launch the Play Store and open
    // the Oppia app's page.
    val packageName = activity.packageName

    try {
      activity.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
      )
    } catch (e: ActivityNotFoundException) {
      activity.startActivity(
        Intent(
          Intent.ACTION_VIEW,
          Uri.parse(
            "https://play.google.com/store/apps/details?id=$packageName"
          )
        )
      )
    }

    // Finish splash activity to close the app in anticipation of an update.
    activity.finish()
  }

  /** Handles cases where the user dismisses the deprecation notice dialog. */
  private fun handleOnDeprecationNoticeDialogDismissed(
    deprecationNoticeType: DeprecationNoticeType,
    deprecatedVersion: Int
  ) {
    val deprecationResponse = DeprecationResponse.newBuilder()
      .setDeprecationNoticeType(deprecationNoticeType)
      .setDeprecatedVersion(deprecatedVersion)
      .build()

    deprecationController.saveDeprecationResponse(deprecationResponse)

    // If the Dismiss button is clicked for the deprecation notice, the dialog is automatically
    // dismissed. Navigate to profile chooser activity.
    activity.startActivity(ProfileChooserActivity.createProfileChooserActivity(activity))
    activity.finish()
  }

  /** Handles cases when the user dismisses the beta notice dialog. */
  fun handleOnBetaNoticeOkayButtonClicked(permanentlyDismiss: Boolean) {
    if (permanentlyDismiss) {
      appStartupStateController.dismissBetaNoticesPermanently()
    }
    processStartupMode()
  }

  /** Handles cases when the user dismisses the general availability update notice dialog. */
  fun handleOnGaUpgradeNoticeOkayButtonClicked(permanentlyDismiss: Boolean) {
    if (permanentlyDismiss) {
      appStartupStateController.dismissGaUpgradeNoticesPermanently()
    }
    processStartupMode()
  }

  private fun subscribeToOnboardingFlow() {
    val liveData = computeInitStateDataProvider().toLiveData()
    liveData.observe(
      activity,
      object : Observer<AsyncResult<SplashInitState>> {
        override fun onChanged(initStateResult: AsyncResult<SplashInitState>) {
          when (initStateResult) {
            is AsyncResult.Pending -> {
              // Ensure that pending states last no longer than 5 seconds. In cases where the app
              // enters a bad state, this ensures that the user doesn't become stuck on the splash
              // screen.
              lifecycleSafeTimerFactory.createTimer(timeoutMillis = 5000).observe(activity) {
                processInitState(SplashInitState.computeDefault(localeController))
              }
            }
            is AsyncResult.Failure -> {
              oppiaLogger.e(
                "SplashActivity", "Failed to compute initial state", initStateResult.error
              )
            }
            is AsyncResult.Success -> {
              // It's possible for the observer to still be active & change due to the next activity
              // causing a notification to be posted. That's always invalid to process here: the
              // splash activity should never do anything after its initial state since it always
              // finishes (or in the case of the deprecation dialog, blocks) the activity.
              liveData.removeObserver(this)
              processInitState(initStateResult.value)
            }
          }
        }
      }
    )
  }

  private fun processInitState(initState: SplashInitState) {
    // First, initialize the app's initial locale. Note that since the activity can be
    // reopened, it's possible for this to be initialized more than once.
    if (!appLanguageLocaleHandler.isInitialized()) {
      appLanguageLocaleHandler.initializeLocale(initState.displayLocale)
    }

    // Second, prepare to route the user to the correct destination.
    startupMode = initState.appStartupState.startupMode

    // Third, show any dismissible notices (if the app isn't deprecated).
    if (startupMode != StartupMode.APP_IS_DEPRECATED) {
      when (initState.appStartupState.buildFlavorNoticeMode) {
        BuildFlavorNoticeMode.FLAVOR_NOTICE_MODE_UNSPECIFIED, BuildFlavorNoticeMode.NO_NOTICE,
        BuildFlavorNoticeMode.UNRECOGNIZED, null -> {
          // No notice should be shown. However, when a pre-release version of the app is active
          // that changes the splash screen have it wait a bit longer so that the build flavor can
          // be clearly seen. The developer build isn't part of the wait to ensure fast startup
          // times (for development purposes).
          when (currentBuildFlavor) {
            BuildFlavor.BUILD_FLAVOR_UNSPECIFIED, BuildFlavor.UNRECOGNIZED,
            BuildFlavor.TESTING, BuildFlavor.DEVELOPER, BuildFlavor.GENERAL_AVAILABILITY ->
              processStartupMode()
            BuildFlavor.ALPHA, BuildFlavor.BETA -> {
              lifecycleSafeTimerFactory.createTimer(timeoutMillis = 2000).observe(activity) {
                processStartupMode()
              }
            }
          }
        }
        BuildFlavorNoticeMode.SHOW_BETA_NOTICE ->
          showDialog(BETA_NOTICE_DIALOG_FRAGMENT_TAG, BetaNoticeDialogFragment::newInstance)
        BuildFlavorNoticeMode.SHOW_UPGRADE_TO_GENERAL_AVAILABILITY_NOTICE -> {
          showDialog(
            GA_UPDATE_NOTICE_DIALOG_FRAGMENT_TAG,
            GeneralAvailabilityUpgradeNoticeDialogFragment::newInstance
          )
        }
      }
    } else processStartupMode()
  }

  private fun processStartupMode() {
    if (enableAppAndOsDeprecation.value) {
      processAppAndOsDeprecationEnabledStartUpMode()
    } else {
      processLegacyStartupMode()
    }
  }

  private fun processAppAndOsDeprecationEnabledStartUpMode() {
    when (startupMode) {
      StartupMode.USER_IS_ONBOARDED -> handleUserOnboarded()
      StartupMode.APP_IS_DEPRECATED -> {
        showDialog(
          FORCED_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG,
          ForcedAppDeprecationNoticeDialogFragment::newInstance
        )
      }
      StartupMode.OPTIONAL_UPDATE_AVAILABLE -> {
        showDialog(
          OPTIONAL_UPDATE_NOTICE_DIALOG_FRAGMENT_TAG,
          OptionalAppDeprecationNoticeDialogFragment::newInstance
        )
      }
      StartupMode.OS_IS_DEPRECATED -> {
        showDialog(
          OS_UPDATE_NOTICE_DIALOG_FRAGMENT_TAG,
          OsDeprecationNoticeDialogFragment::newInstance
        )
      }
      StartupMode.USER_NOT_YET_ONBOARDED -> fetchProfile()
      else -> {
        // In all other cases (including errors when the startup state fails to load or is
        // defaulted), assume the user needs to be onboarded.
        launchOnboardingActivity()
        activity.finish()
      }
    }
  }

  private fun processLegacyStartupMode() {
    when (startupMode) {
      StartupMode.USER_IS_ONBOARDED -> handleUserOnboarded()
      StartupMode.APP_IS_DEPRECATED -> {
        showDialog(
          AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG,
          AutomaticAppDeprecationNoticeDialogFragment::newInstance
        )
      }
      StartupMode.USER_NOT_YET_ONBOARDED -> fetchProfile()
      else -> {
        // In all other cases (including errors when the startup state fails to load or is
        // defaulted), assume the user needs to be onboarded.
        launchOnboardingActivity()
      }
    }
  }

  private fun handleUserOnboarded() {
    if (enableOnboardingFlowV2.value) {
      getProfileOnboardingState()
    } else {
      activity.startActivity(ProfileChooserActivity.createProfileChooserActivity(activity))
      activity.finish()
    }
  }

  private fun getProfileOnboardingState() {
    profileManagementController.getProfileOnboardingMode().toLiveData().observe(
      activity,
      { result ->
        when (result) {
          is AsyncResult.Success -> computeLoginRoute(result.value)
          is AsyncResult.Failure -> oppiaLogger.e(
            "SplashActivity",
            "Encountered unexpected non-successful result when fetching onboarding state",
            result.error
          )
          is AsyncResult.Pending -> {}
        }
      }
    )
  }

  private fun computeLoginRoute(onboardingMode: ProfileOnboardingMode) {
    when (onboardingMode) {
      ProfileOnboardingMode.NEW_INSTALL -> {
        launchOnboardingActivity()
      }
      ProfileOnboardingMode.SOLE_LEARNER_PROFILE_ONLY -> fetchProfile()
      else -> {
        activity.startActivity(ProfileChooserActivity.createProfileChooserActivity(activity))
        activity.finish()
      }
    }
  }

  private fun fetchProfile() {
    profileManagementController.getProfiles().toLiveData().observe(activity) { result ->
      when (result) {
        is AsyncResult.Success -> handleProfiles(result.value)
        is AsyncResult.Failure -> oppiaLogger.e(
          "SplashActivity", "Failed to retrieve the list of profiles", result.error
        )
        is AsyncResult.Pending -> {} // no-op
      }
    }
  }

  private fun handleProfiles(profiles: List<Profile>) {
    val soleLearnerProfile = profiles.find { it.profileType == ProfileType.SOLE_LEARNER }
    if (soleLearnerProfile != null) {
      proceedBasedOnProfileState(soleLearnerProfile)
    } else {
      launchOnboardingActivity()
    }
  }

  private fun proceedBasedOnProfileState(profile: Profile) {
    when {
      profile.startedProfileOnboarding && !profile.completedProfileOnboarding -> {
        resumeOnboarding(profile.id, profile.name)
      }
      profile.startedProfileOnboarding && profile.completedProfileOnboarding -> {
        loginToProfile(profile.id)
      }
      else -> launchOnboardingActivity()
    }
  }

  private fun resumeOnboarding(profileId: ProfileId, profileName: String) {
    val introActivityParams = IntroActivityParams.newBuilder()
      .setProfileNickname(profileName)
      .build()

    val intent = IntroActivity.createIntroActivity(activity).apply {
      putProtoExtra(PARAMS_KEY, introActivityParams)
      decorateWithUserProfileId(profileId)
    }

    activity.startActivity(intent)
  }

  private fun loginToProfile(profileId: ProfileId) {
    profileManagementController.loginToProfile(profileId).toLiveData().observe(activity) { result ->
      if (result is AsyncResult.Success && !activity.isFinishing) {
        launchHomeScreen(profileId)
      }
    }
  }

  private fun launchHomeScreen(profileId: ProfileId) {
    val intent = if (enableMultipleClassrooms.value) {
      ClassroomListActivity.createClassroomListActivity(activity, profileId)
    } else {
      HomeActivity.createHomeActivity(activity, profileId)
    }
    activity.startActivity(intent)
    activity.finish()
  }

  private fun launchOnboardingActivity() {
    activity.startActivity(OnboardingActivity.createOnboardingActivity(activity))
    activity.finish()
  }

  private fun computeInitStateDataProvider(): DataProvider<SplashInitState> {
    val startupStateDataProvider = appStartupStateController.getAppStartupState()
    val systemAppLanguageLocaleDataProvider = translationController.getSystemLanguageLocale()
    return startupStateDataProvider.combineWith(
      systemAppLanguageLocaleDataProvider, SPLASH_INIT_STATE_DATA_PROVIDER_ID
    ) { startupState, systemAppLanguageLocale ->
      SplashInitState(startupState, systemAppLanguageLocale)
    }
  }

  private inline fun <reified T : DialogFragment> showDialog(tag: String, createFragment: () -> T) {
    if (activity.supportFragmentManager.findFragmentByTag(tag) as? T == null) {
      activity.supportFragmentManager.beginTransaction().add(createFragment(), tag).commitNow()
    }
  }

  private data class SplashInitState(
    val appStartupState: AppStartupState,
    val displayLocale: OppiaLocale.DisplayLocale
  ) {
    companion object {
      fun computeDefault(localeController: LocaleController): SplashInitState {
        return SplashInitState(
          appStartupState = AppStartupState.getDefaultInstance(),
          displayLocale = localeController.reconstituteDisplayLocale(
            localeController.getLikelyDefaultAppStringLocaleContext()
          )
        )
      }
    }
  }
}
