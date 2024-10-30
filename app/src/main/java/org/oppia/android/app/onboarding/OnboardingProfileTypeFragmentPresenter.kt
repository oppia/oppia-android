package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.databinding.OnboardingProfileTypeFragmentBinding
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Argument key for [CreateProfileActivity] intent parameters. */
const val CREATE_PROFILE_PARAMS_KEY = "CreateProfileActivity.params"

/** The presenter for [OnboardingProfileTypeFragment]. */
class OnboardingProfileTypeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity
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
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity)
        // TODO(#4938): Add profileId and ProfileType to intent extras.
        fragment.startActivity(intent)
      }

      onboardingNavigationBack.setOnClickListener {
        activity.finish()
      }
    }

    return binding.root
  }
}
