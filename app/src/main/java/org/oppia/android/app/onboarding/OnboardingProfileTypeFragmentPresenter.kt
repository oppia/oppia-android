package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.databinding.OnboardingProfileTypeFragmentBinding
import javax.inject.Inject

/** The presenter for [OnboardingProfileTypeFragment]. */
class OnboardingProfileTypeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity
) {
  private lateinit var binding: OnboardingProfileTypeFragmentBinding

  /** Handle creation and binding of the  OnboardingProfileTypeFragment layout. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = OnboardingProfileTypeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.apply {
      lifecycleOwner = fragment

      profileTypeLearnerNavigationCard.setOnClickListener {
        val intent = CreateProfileActivity.createProfileActivityIntent(activity)
        fragment.startActivity(intent)
      }

      profileTypeSupervisorNavigationCard.setOnClickListener {
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity)
        fragment.startActivity(intent)
      }

      onboardingNavigationBack.setOnClickListener {
        activity.finish()
      }
    }

    return binding.root
  }
}
