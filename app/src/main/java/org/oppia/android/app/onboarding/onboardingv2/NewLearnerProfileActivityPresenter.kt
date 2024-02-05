package org.oppia.android.app.onboarding.onboardingv2

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.databinding.CreateProfileActivityBinding
import javax.inject.Inject

private const val TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT = "TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT"

/** Presenter for [NewLearnerProfileActivity]. */
class NewLearnerProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: CreateProfileActivityBinding

  /** Handle creation and binding of the NewLearnerProfileActivity layout. */
  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView(activity, R.layout.create_profile_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    if (getNewLearnerProfileFragment() == null) {
      val createLearnerProfileFragment = NewLearnerProfileFragment()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_fragment_placeholder,
        createLearnerProfileFragment,
        TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT
      )
        .commitNow()
    }
  }

  private fun getNewLearnerProfileFragment(): NewLearnerProfileFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT
    ) as? NewLearnerProfileFragment
  }
}
