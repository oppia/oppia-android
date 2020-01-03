package org.oppia.app.onBoarding

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.profile.ProfileActivity
import javax.inject.Inject

/** An activity that shows a temporary Onboardinging page until the app is onborarded then navigates to [ProfileActivity]. */
class OnBoardingingActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var onBoardingActivityPresenter: OnBoardingingActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    onBoardingActivityPresenter.handleOnCreate()
  }
}

