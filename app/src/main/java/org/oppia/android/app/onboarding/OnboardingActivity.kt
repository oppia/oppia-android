package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.model.ScreenName.ONBOARDING_ACTIVITY
import org.oppia.android.app.policies.PoliciesActivity
import org.oppia.android.app.policies.RouteToPoliciesListener
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName

/** Activity that contains the onboarding flow for learners. */
class OnboardingActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToPoliciesListener {
  @Inject
  lateinit var onboardingActivityPresenter: OnboardingActivityPresenter

  companion object {
    fun createOnboardingActivity(context: Context): Intent {
      return Intent(context, OnboardingActivity::class.java).apply {
        decorateWithScreenName(ONBOARDING_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    titleColor = R.color.color_palette_section_title_divider_color
    onboardingActivityPresenter.handleOnCreate()
  }

  override fun routeToProfileList() {
    startActivity(ProfileChooserActivity.createProfileChooserActivity(this))
    finish()
  }

  override fun onRouteToPolicies(policyPage: PolicyPage) {
    startActivity(PoliciesActivity.createPoliciesActivityIntent(this, policyPage))
  }
}
