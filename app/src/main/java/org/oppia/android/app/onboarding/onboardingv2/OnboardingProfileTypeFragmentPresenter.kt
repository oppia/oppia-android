package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.databinding.OnboardingProfileTypeFragmentBinding
import javax.inject.Inject

/** The presenter for [OnboardingProfileTypeFragment]. */
class OnboardingProfileTypeFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: OnboardingProfileTypeFragmentBinding

  /** Handle creation and binding of the  OnboardingProfileTypeFragment layout. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = OnboardingProfileTypeFragmentBinding.inflate(
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
