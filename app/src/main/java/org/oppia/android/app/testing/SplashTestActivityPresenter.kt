package org.oppia.android.app.testing

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import javax.inject.Inject
import javax.inject.Provider

/** The presenter for [SplashTestActivity]. */
@ActivityScope
class SplashTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val platformParameterController: PlatformParameterController,
  @SplashScreenWelcomeMsg
  private val splashScreenWelcomeMsgParam: Provider<PlatformParameterValue<Boolean>>
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_test_activity)
  }

  /**
   * Triggers the loading process for getting platform parameters from cache store to the platform
   * parameter singleton.
   */
  fun loadPlatformParameters() {
    platformParameterController.getParameterInitializationStatus().toLiveData().observe(activity) {
      if (it is AsyncResult.Success && it.value) {
        showToastIfAllowed()
      }
    }
  }

  private fun showToastIfAllowed() {
    if (splashScreenWelcomeMsgParam.get().value) {
      Toast.makeText(activity, SplashTestActivity.WELCOME_MSG, Toast.LENGTH_SHORT).show()
    }
  }
}
