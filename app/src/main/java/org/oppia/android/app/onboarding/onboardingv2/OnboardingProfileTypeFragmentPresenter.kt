package org.oppia.android.app.onboarding.onboardingv2

import android.content.res.Configuration
import android.content.res.Resources
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

  private val orientation = Resources.getSystem().configuration.orientation

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

    binding.profileTypeLearnerNavigationCard.setOnClickListener {
      val intent = NewLearnerProfileActivity.createNewLearnerProfileActivity(activity)
      fragment.startActivity(intent)
    }

    binding.profileTypeSupervisorNavigationCard.setOnClickListener {
      val intent = ProfileChooserActivity.createProfileChooserActivity(activity)
      fragment.startActivity(intent)
    }

    binding.onboardingNavigationBack.setOnClickListener {
      activity.finish()
    }

    binding.onboardingStepsCount.visibility =
      if (orientation == Configuration.ORIENTATION_PORTRAIT) View.VISIBLE else View.GONE

    return binding.root
  }
}
