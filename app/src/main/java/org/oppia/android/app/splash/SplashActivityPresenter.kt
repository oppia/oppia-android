package org.oppia.android.app.splash

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.BuildFlavorNoticeMode
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.notice.AutomaticAppDeprecationNoticeDialogFragment
import org.oppia.android.app.notice.BetaNoticeDialogFragment
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeDialogFragment
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.translation.AppLanguageLocaleHandler
import org.oppia.android.app.utility.LifecycleSafeTimerFactory
import org.oppia.android.databinding.SplashActivityBinding
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.PrimeTopicAssetsController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

private const val AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG = "auto_deprecation_notice_dialog"
private const val BETA_NOTICE_DIALOG_FRAGMENT_TAG = "beta_notice_dialog"
private const val GA_UPDATE_NOTICE_DIALOG_FRAGMENT_TAG = "general_availability_update_notice_dialog"
private const val SPLASH_INIT_STATE_DATA_PROVIDER_ID = "splash_init_state_data_provider"

/** The presenter for [SplashActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val appStartupStateController: AppStartupStateController,
  private val primeTopicAssetsController: PrimeTopicAssetsController,
  private val translationController: TranslationController,
  private val localeController: LocaleController,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler,
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val currentBuildFlavor: BuildFlavor
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

    // Initiate download support before any additional processing begins.
    primeTopicAssetsController.downloadAssets(R.style.OppiaAlertDialogTheme)
    subscribeToOnboardingFlow()
  }

  fun handleOnCloseAppButtonClicked() {
    // If the app close button is clicked for the deprecation notice, finish the activity to close
    // the app.
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
              lifecycleSafeTimerFactory.createTimer(timeoutMillis = 5000).observe(
                activity,
                {
                  processInitState(SplashInitState.computeDefault(localeController))
                }
              )
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
    // First, initialize the app's initial locale.
    appLanguageLocaleHandler.initializeLocale(initState.displayLocale)

    // Second, prepare to route the user to the correct destination.
    startupMode = initState.appStartupState.startupMode

    // Third, show any dismissible notices.
    when (initState.appStartupState.buildFlavorNoticeMode) {
      BuildFlavorNoticeMode.FLAVOR_NOTICE_MODE_UNSPECIFIED, BuildFlavorNoticeMode.NO_NOTICE,
      BuildFlavorNoticeMode.UNRECOGNIZED, null -> {
        // No notice should be shown. However, when a pre-release version of the app is active that
        // changes the splash screen have it wait a bit longer so that the build flavor can be
        // clearly seen. The developer build isn't part of the wait to ensure fast startup times
        // (for development purposes).
        when (currentBuildFlavor) {
          BuildFlavor.BUILD_FLAVOR_UNSPECIFIED, BuildFlavor.UNRECOGNIZED,
          BuildFlavor.DEVELOPER, BuildFlavor.GENERAL_AVAILABILITY -> processStartupMode()
          BuildFlavor.ALPHA, BuildFlavor.BETA -> {
            lifecycleSafeTimerFactory.createTimer(timeoutMillis = 2000).observe(
              activity,
              {
                processStartupMode()
              }
            )
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
  }

  private fun processStartupMode() {
    when (startupMode) {
      StartupMode.USER_IS_ONBOARDED -> {
        activity.startActivity(ProfileChooserActivity.createProfileChooserActivity(activity))
        activity.finish()
      }
      StartupMode.APP_IS_DEPRECATED -> {
        showDialog(
          AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG,
          AutomaticAppDeprecationNoticeDialogFragment::newInstance
        )
      }
      else -> {
        // In all other cases (including errors when the startup state fails to load or is
        // defaulted), assume the user needs to be onboarded.
        activity.startActivity(OnboardingActivity.createOnboardingActivity(activity))
        activity.finish()
      }
    }
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
