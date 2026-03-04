package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.OnboardingProfileTypeFragmentBinding
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ProfileType
import javax.inject.Inject

/** The presenter for [OnboardingProfileTypeFragment]. */
class OnboardingProfileTypeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity
) {
  private lateinit var binding: OnboardingProfileTypeFragmentBinding

  /** Handle creation and binding of the [OnboardingProfileTypeFragment] layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: LegacyProfileId
  ): View {
    binding = OnboardingProfileTypeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.apply {
      lifecycleOwner = fragment

      profileTypeLearnerNavigationCard.setOnClickListener {
        val intent = CreateProfileActivity.createProfileActivityIntent(
          activity,
          profileId,
          ProfileType.SOLE_LEARNER
        )
        fragment.startActivity(intent)
      }

      profileTypeSupervisorNavigationCard.setOnClickListener {
        val intent = CreateProfileActivity.createProfileActivityIntent(
          activity,
          profileId,
          ProfileType.SUPERVISOR
        )

        fragment.startActivity(intent)
      }

      onboardingNavigationBack.setOnClickListener {
        activity.finish()
      }
    }

    return binding.root
  }
}
