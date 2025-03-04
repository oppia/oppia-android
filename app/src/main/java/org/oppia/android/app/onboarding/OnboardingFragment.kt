package org.oppia.android.app.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.FeatureFlagId.ONBOARDING_FLOW_V2
import org.oppia.android.domain.platformparameter.FeatureFlag
import javax.inject.Inject
import javax.inject.Provider

/** Fragment that contains an onboarding flow of the app. */
class OnboardingFragment : InjectableFragment() {
  @Inject
  lateinit var onboardingFragmentPresenterV1: OnboardingFragmentPresenterV1

  @Inject
  lateinit var onboardingFragmentPresenter: OnboardingFragmentPresenter

  @Inject
  @field:FeatureFlag(ONBOARDING_FLOW_V2)
  lateinit var enableOnboardingFlowV2: Provider<Boolean>

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return if (enableOnboardingFlowV2.get()) {
      onboardingFragmentPresenter.handleCreateView(inflater, container, savedInstanceState)
    } else {
      onboardingFragmentPresenterV1.handleCreateView(inflater, container)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (enableOnboardingFlowV2.get()) {
      onboardingFragmentPresenter.saveToSavedInstanceState(outState)
    }
  }
}
