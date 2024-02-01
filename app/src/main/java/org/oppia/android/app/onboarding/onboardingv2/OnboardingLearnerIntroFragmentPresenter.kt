package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.databinding.OnboardingLearnerIntroFragmentBinding
import org.oppia.android.databinding.OnboardingProfileTypeFragmentBinding

/** The presenter for [OnboardingLearnerIntroFragment]. */
class OnboardingLearnerIntroFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: OnboardingLearnerIntroFragmentBinding

  /** Handle creation and binding of the  OnboardingProfileTypeFragment layout. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = OnboardingLearnerIntroFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
    }
    return binding.root
  }
}
