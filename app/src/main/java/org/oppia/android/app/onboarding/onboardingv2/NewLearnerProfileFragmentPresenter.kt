package org.oppia.android.app.onboarding.onboardingv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.databinding.CreateProfileFragmentBinding
import javax.inject.Inject

/** Presenter for [NewLearnerProfileFragment]. */
class NewLearnerProfileFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: CreateProfileFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = CreateProfileFragmentBinding.inflate(
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
