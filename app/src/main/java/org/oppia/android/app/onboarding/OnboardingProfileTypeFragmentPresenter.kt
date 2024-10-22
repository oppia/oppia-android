package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.databinding.OnboardingProfileTypeFragmentBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Argument key for [CreateProfileActivity] intent parameters. */
const val CREATE_PROFILE_PARAMS_KEY = "CreateProfileActivity.params"

/** Argument key for [ProfileChooserActivity] intent parameters. */
const val PROFILE_CHOOSER_PARAMS_KEY = "ProfileChooserActivity.params"

/** The presenter for [OnboardingProfileTypeFragment]. */
class OnboardingProfileTypeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var binding: OnboardingProfileTypeFragmentBinding

  /** Handle creation and binding of the  OnboardingProfileTypeFragment layout. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId
  ): View {
    binding = OnboardingProfileTypeFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.apply {
      lifecycleOwner = fragment

      profileTypeLearnerNavigationCard.setOnClickListener {
        val intent = CreateProfileActivity.createProfileActivityIntent(activity)
        intent.apply {
          decorateWithUserProfileId(profileId)
          putProtoExtra(
            CREATE_PROFILE_PARAMS_KEY,
            CreateProfileActivityParams.newBuilder()
              .setProfileType(ProfileType.SOLE_LEARNER)
              .build()
          )
        }
        fragment.startActivity(intent)
      }

      profileTypeSupervisorNavigationCard.setOnClickListener {
        // TODO(#4938): Remove once admin profile onboarding is implemented.
        profileManagementController.markProfileOnboardingStarted(profileId)

        val intent = ProfileChooserActivity.createProfileChooserActivity(activity)
        intent.apply {
          decorateWithUserProfileId(profileId)
          putProtoExtra(
            PROFILE_CHOOSER_PARAMS_KEY,
            ProfileChooserActivityParams.newBuilder()
              .setProfileType(ProfileType.SUPERVISOR)
              .build()
          )
        }
        fragment.startActivity(intent)
        // Clear back stack so that user cannot go back to the onboarding flow.
        fragment.activity?.finishAffinity()
      }

      onboardingNavigationBack.setOnClickListener {
        activity.finish()
      }
    }

    return binding.root
  }
}
