package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.PoliciesActivityParams
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ScreenName.ONBOARDING_ACTIVITY
import org.oppia.android.app.policies.RouteToPoliciesListener
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that contains the onboarding flow for learners. */
class OnboardingActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToPoliciesListener {
  @Inject lateinit var onboardingActivityPresenter: OnboardingActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, OnboardingActivity::class.java).apply {
        decorateWithScreenName(ONBOARDING_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    onboardingActivityPresenter.handleOnCreate()
  }

  override fun routeToProfileList() {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        profileChooserActivityParams = ProfileChooserActivityParams.getDefaultInstance()
      }.build()
    )
    finish()
  }

  override fun onRouteToPolicies(policyPage: PolicyPage) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        policiesActivityParams = PoliciesActivityParams.newBuilder().apply {
          this.policyPage = policyPage
        }.build()
      }.build()
    )
  }

  interface Injector {
    fun inject(activity: OnboardingActivity)
  }
}
