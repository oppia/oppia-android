package org.oppia.android.app.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.OnboardingProfileTypeActivityBinding
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

private const val TAG_PROFILE_TYPE_FRAGMENT = "TAG_PROFILE_TYPE_FRAGMENT"

/** The Presenter for [OnboardingProfileTypeActivity]. */
@ActivityScope
class OnboardingProfileTypeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: OnboardingProfileTypeActivityBinding

  /** Handle creation and binding of the  OnboardingProfileTypeActivity layout. */
  fun handleOnCreate(profileId: ProfileId) {
    binding = DataBindingUtil.setContentView(activity, R.layout.onboarding_profile_type_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    if (getOnboardingProfileTypeFragment() == null) {
      val onboardingProfileTypeFragment = OnboardingProfileTypeFragment()
      val args = Bundle().apply {
        decorateWithUserProfileId(profileId)
      }
      onboardingProfileTypeFragment.arguments = args

      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_type_fragment_placeholder,
        onboardingProfileTypeFragment,
        TAG_PROFILE_TYPE_FRAGMENT
      ).commitNow()
    }
  }

  private fun getOnboardingProfileTypeFragment(): OnboardingProfileTypeFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_PROFILE_TYPE_FRAGMENT
    ) as? OnboardingProfileTypeFragment
  }
}
