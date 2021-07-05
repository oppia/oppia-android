package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** This is a dummy activity to test the injection of Platform Parameters. */
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
