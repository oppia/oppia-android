package org.oppia.android.app.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.databinding.OnboardingProfileTypeFragmentBinding
import org.oppia.android.util.extensions.getProtoExtra
import javax.inject.Inject

/** The presenter for [OnboardingProfileTypeFragment]. */
class OnboardingProfileTypeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity
) {
  private lateinit var binding: OnboardingProfileTypeFragmentBinding
  private val args by lazy {
    activity.intent.getProtoExtra(
      CreateProfileActivity.CREATE_PROFILE_ACTIVITY_PARAMS_KEY,
      CreateProfileActivityParams.getDefaultInstance()
    )
  }

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
        val rgbColor = args?.colorRgb ?: -10710042
        val intent = CreateProfileActivity.createProfileActivityIntent(activity, rgbColor)
        fragment.startActivity(intent)
      }

      profileTypeSupervisorNavigationCard.setOnClickListener {
        val intent = ProfileChooserActivity.createProfileChooserActivity(activity)
        fragment.startActivity(intent)
        activity.finishAffinity()
      }

      onboardingNavigationBack.setOnClickListener {
        activity.finish()
      }
    }

    return binding.root
  }
}
