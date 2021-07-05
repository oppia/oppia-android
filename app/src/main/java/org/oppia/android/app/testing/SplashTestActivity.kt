package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import javax.inject.Inject
import javax.inject.Provider

class SplashTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var splashTestActivityPresenter: SplashTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    splashTestActivityPresenter.handleOnCreate()
  }

  companion object {
    val WELCOME_MSG = "Welcome User"
  }
}
