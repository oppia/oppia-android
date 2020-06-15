package org.oppia.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.profile.ProfileActivity
import javax.inject.Inject

/** Activity that contains the onboarding flow for learners. */
class OnboardingActivity : InjectableAppCompatActivity(), RouteToProfileListListener {
  @Inject lateinit var onboardingActivityPresenter: OnboardingActivityPresenter

  companion object {
    fun createOnboardingActivity(context: Context): Intent {
      val intent = Intent(context, OnboardingActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    onboardingActivityPresenter.handleOnCreate()
  }

  override fun routeToProfileList() {
    startActivity(ProfileActivity.createProfileActivity(this))
    finish()
  }
}
