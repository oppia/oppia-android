package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.splash.SplashActivity
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/**
 * A test activity to verify the injection of [PlatformParameterValue] in the [SplashActivity].
 * This test activity is used in integration tests for platform parameters.
 */
class SplashTestActivity : InjectableSystemLocalizedAppCompatActivity() {

  @Inject
  lateinit var splashTestActivityPresenter: SplashTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    splashTestActivityPresenter.handleOnCreate()
  }

  companion object {
    val WELCOME_MSG = "Welcome User"
  }
}
