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
import javax.inject.Inject
import javax.inject.Provider

@ActivityScope
class SplashTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
//  private val platformParameterController: PlatformParameterController,
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.splash_test_activity)
    //loadPlatformParameters()
  }

//  fun loadPlatformParameters() {
//    getPlatformParameterLoadingStatus().observe(
//      activity,
//      Observer {
//        val splashScreenWelcomeMsgParam = (activity as SplashTestActivity)
//          .splashScreenWelcomeMsgParam
//        showToastIfAllowed(splashScreenWelcomeMsgParam)
//      }
//    )
//  }
//
//  private fun getPlatformParameterLoadingStatus(): LiveData<Boolean> {
//    return Transformations.map(
//      platformParameterController.getParameterDatabase().toLiveData(),
//      ::processPlatformParameterLoadingStatus
//    )
//  }
//
//  private fun processPlatformParameterLoadingStatus(loadingStatus: AsyncResult<Unit>): Boolean {
//    return loadingStatus.isSuccess()
//  }
//
//  private fun showToastIfAllowed(
//    splashScreenWelcomeMsgParam: Provider<PlatformParameterValue<Boolean>>
//  ) {
//    if (splashScreenWelcomeMsgParam.get().value) {
//      Toast.makeText(activity, SplashTestActivity.WELCOME_MSG, Toast.LENGTH_SHORT).show()
//    }
//  }
}
