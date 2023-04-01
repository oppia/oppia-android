package org.oppia.android.app.testing

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
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
    fetchPlatformParametersFromDatabase().observe(
      activity,
      Observer {
        showToastIfAllowed()
      }
    )
  }

  private fun fetchPlatformParametersFromDatabase(): LiveData<Boolean> {
    return Transformations.map(
      platformParameterController.getParameterDatabase().toLiveData(),
      ::processPlatformParameters
    )
  }

  private fun processPlatformParameters(loadingStatus: AsyncResult<Unit>): Boolean {
    return loadingStatus is AsyncResult.Success
  }

  private fun showToastIfAllowed() {
    if (splashScreenWelcomeMsgParam.get().value) {
      Toast.makeText(activity, SplashTestActivity.WELCOME_MSG, Toast.LENGTH_SHORT).show()
    }
  }
}
