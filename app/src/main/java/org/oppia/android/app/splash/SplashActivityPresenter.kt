package org.oppia.android.app.splash

import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.deprecation.AutomaticAppDeprecationNoticeDialogFragment
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.translation.AppLanguageLocaleHandler
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
import org.oppia.android.util.logging.CurrentAppScreenNameWrapper
import javax.inject.Inject

private const val AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG = "auto_deprecation_notice_dialog"
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
  private val currentAppScreenNameWrapper: CurrentAppScreenNameWrapper
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_activity)
    activity.window.setFlags(
      WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
    // Initiate download support before any additional processing begins.
    primeTopicAssetsController.downloadAssets(R.style.OppiaAlertDialogTheme)
    subscribeToOnboardingFlow()
  }

  fun handleOnCloseAppButtonClicked() {
    // If the app close button is clicked for the deprecation notice, finish the activity to close
    // the app.
    activity.finish()
  }

  /** Returns an intent that wraps up a proto object carrying the screen name of the activity. */
  fun getCurrentAppScreenNameIntent(): Intent =
    currentAppScreenNameWrapper.getCurrentAppScreenNameIntent(ScreenName.SPLASH_ACTIVITY)

  private fun subscribeToOnboardingFlow() {
    val liveData = computeInitStateLiveData()
    liveData.observe(
      activity,
      object : Observer<SplashInitState> {
        override fun onChanged(initState: SplashInitState) {
          // It's possible for the observer to still be active & change due to the next activity
          // causing a notification to be posted. That's always invalid to process here: the splash
          // activity should never do anything after its initial state since it always finishes (or
          // in the case of the deprecation dialog, blocks) the activity.
          liveData.removeObserver(this)

          // First, initialize the app's initial locale.
          appLanguageLocaleHandler.initializeLocale(initState.displayLocale)

          // Second, route the user to the correct destination.
          when (initState.startupMode) {
            StartupMode.USER_IS_ONBOARDED -> {
              activity.startActivity(ProfileChooserActivity.createProfileChooserActivity(activity))
              activity.finish()
            }
            StartupMode.APP_IS_DEPRECATED -> {
              if (getDeprecationNoticeDialogFragment() == null) {
                activity.supportFragmentManager.beginTransaction()
                  .add(
                    AutomaticAppDeprecationNoticeDialogFragment.newInstance(),
                    AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG
                  ).commitNow()
              }
            }
            else -> {
              // In all other cases (including errors when the startup state fails to load or is
              // defaulted), assume the user needs to be onboarded.
              activity.startActivity(OnboardingActivity.createOnboardingActivity(activity))
              activity.finish()
            }
          }
        }
      }
    )
  }

  private fun computeInitStateDataProvider(): DataProvider<SplashInitState> {
    val startupStateDataProvider = appStartupStateController.getAppStartupState()
    val systemAppLanguageLocaleDataProvider = translationController.getSystemLanguageLocale()
    return startupStateDataProvider.combineWith(
      systemAppLanguageLocaleDataProvider, SPLASH_INIT_STATE_DATA_PROVIDER_ID
    ) { startupState, systemAppLanguageLocale ->
      SplashInitState(startupState.startupMode, systemAppLanguageLocale)
    }
  }

  private fun computeInitStateLiveData(): LiveData<SplashInitState> =
    Transformations.map(computeInitStateDataProvider().toLiveData(), ::processInitState)

  private fun processInitState(
    initStateResult: AsyncResult<SplashInitState>
  ): SplashInitState {
    // If there's an error loading the data, assume the default.
    return when (initStateResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("SplashActivity", "Failed to compute initial state", initStateResult.error)
        SplashInitState.computeDefault(localeController)
      }
      is AsyncResult.Pending -> SplashInitState.computeDefault(localeController)
      is AsyncResult.Success -> initStateResult.value
    }
  }

  private fun getDeprecationNoticeDialogFragment(): AutomaticAppDeprecationNoticeDialogFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG
    ) as? AutomaticAppDeprecationNoticeDialogFragment
  }

  private data class SplashInitState(
    val startupMode: StartupMode,
    val displayLocale: OppiaLocale.DisplayLocale
  ) {
    companion object {
      fun computeDefault(localeController: LocaleController): SplashInitState {
        return SplashInitState(
          startupMode = AppStartupState.getDefaultInstance().startupMode,
          displayLocale = localeController.reconstituteDisplayLocale(
            localeController.getLikelyDefaultAppStringLocaleContext()
          )
        )
      }
    }
  }
}
