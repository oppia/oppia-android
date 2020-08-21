package org.oppia.app.splash

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.deprecation.AutomaticAppDeprecationNoticeDialogFragment
import org.oppia.app.model.AppStartupState
import org.oppia.app.model.AppStartupState.StartupMode
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.profile.ProfileChooserActivity
import org.oppia.domain.onboarding.AppStartupStateController
import org.oppia.domain.topic.PrimeTopicAssetsController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

private const val AUTO_DEPRECATION_NOTICE_DIALOG_FRAGMENT_TAG = "auto_deprecation_notice_dialog"

/** The presenter for [SplashActivity]. */
@ActivityScope
class SplashActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: ConsoleLogger,
  private val appStartupStateController: AppStartupStateController,
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
    subscribeToOnboardingFlow()
  }

  fun handleOnCloseAppButtonClicked() {
    // If the app close button is clicked for the deprecation notice, finish the activity to close
    // the app.
    activity.finish()
  }

  private fun subscribeToOnboardingFlow() {
    getOnboardingFlow().observe(
      activity,
      Observer { startupMode ->
        when (startupMode) {
          StartupMode.USER_IS_ONBOARDED -> {
            activity.startActivity(ProfileChooserActivity.createProfileActivity(activity))
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
      appStartupStateController.getAppStartupState(),
      ::processStartupState
    )
  }

  private fun processStartupState(
    startupStateResult: AsyncResult<AppStartupState>
  ): StartupMode {
    if (startupStateResult.isFailure()) {
      logger.e(
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
