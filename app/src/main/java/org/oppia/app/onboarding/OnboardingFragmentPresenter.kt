package org.oppia.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.databinding.OnboardingFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [OnboardingFragment]. */
@FragmentScope
class OnboardingFragmentPresenter @Inject constructor() {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = OnboardingFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.

    return binding.root
  }
}
