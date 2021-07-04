package org.oppia.android.app.splash

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
import org.oppia.android.app.onboarding.OnboardingActivity
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.domain.onboarding.AppStartupStateController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.topic.PrimeTopicAssetsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG = "auto_deprecation_notice_dialog"

/** The presenter for [SplashActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
  private val appStartupStateController: AppStartupStateController,
  private val platformParameterController: PlatformParameterController,
  private val primeTopicAssetsController: PrimeTopicAssetsController
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_activity)
    activity.window.setFlags(
      WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
    // Initiate download support before any additional processing begins.
    primeTopicAssetsController.downloadAssets(R.style.AlertDialogTheme)
    loadPlatformParameters()
  }

  fun handleOnCloseAppButtonClicked() {
    // If the app close button is clicked for the deprecation notice, finish the activity to close
    // the app.
    activity.finish()
  }

  fun loadPlatformParameters() {
    getParameterLoadingStatus().observe(
      activity,
      Observer { loadSuccessful ->
        if (!loadSuccessful) {
          oppiaLogger.w(
            "SplashActivity",
            "Default PlatformParameters will be used",
          )
        }
        subscribeToOnboardingFlow()
      }
    )
  }

  private fun subscribeToOnboardingFlow() {
    getOnboardingFlow().observe(
      activity,
      Observer { startupMode ->
        when (startupMode) {
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
    )
  }

  private fun getOnboardingFlow(): LiveData<StartupMode> {
    return Transformations.map(
      appStartupStateController.getAppStartupState().toLiveData(),
      ::processStartupState
    )
  }

  private fun getParameterLoadingStatus(): LiveData<Boolean> {
    return Transformations.map(
      platformParameterController.getParameterDatabase().toLiveData(),
      ::processParameterLoadingStatus
    )
  }

  private fun processParameterLoadingStatus(loadingStatus: AsyncResult<Unit>): Boolean {
    if (loadingStatus.isFailure()) {
      oppiaLogger.e(
        "SplashActivity",
        "Failed to read platform parameter database",
        loadingStatus.getErrorOrNull()
      )
      return false
    }
    return true
  }

  private fun processStartupState(
    startupStateResult: AsyncResult<AppStartupState>
  ): StartupMode {
    if (startupStateResult.isFailure()) {
      oppiaLogger.e(
        "SplashActivity",
        "Failed to retrieve startup state",
        startupStateResult.getErrorOrNull()
      )
    }
    // If there's an error loading the data, assume the default.
    return startupStateResult.getOrDefault(AppStartupState.getDefaultInstance()).startupMode
  }

  private fun getDeprecationNoticeDialogFragment(): AutomaticAppDeprecationNoticeDialogFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG
    ) as? AutomaticAppDeprecationNoticeDialogFragment
  }
}
