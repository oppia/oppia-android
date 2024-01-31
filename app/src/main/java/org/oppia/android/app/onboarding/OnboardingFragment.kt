package org.oppia.android.app.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import org.oppia.android.app.onboarding.onboardingv2.OnboardingFragmentPresenter as OnboardingFragmentPresenterV2

/** Fragment that contains an onboarding flow of the app. */
class OnboardingFragment : InjectableFragment() {
  @Inject
  lateinit var onboardingFragmentPresenter: OnboardingFragmentPresenter

  @Inject
  lateinit var onboardingFragmentPresenterV2: OnboardingFragmentPresenterV2

  @Inject
  @field:EnableOnboardingFlowV2
  lateinit var enableOnboardingFlowV2: PlatformParameterValue<Boolean>

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return if (enableOnboardingFlowV2.value) {
      onboardingFragmentPresenterV2.handleCreateView(inflater, container)
    } else {
      onboardingFragmentPresenter.handleCreateView(inflater, container)
    }
  }
}
