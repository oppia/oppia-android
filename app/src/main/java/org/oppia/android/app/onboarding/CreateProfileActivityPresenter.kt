package org.oppia.android.app.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.model.CreateProfileFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.databinding.CreateProfileActivityBinding
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Argument key for [CreateProfileFragment] arguments. */
const val CREATE_PROFILE_FRAGMENT_ARGS = "CreateProfileFragment.args"

private const val TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT = "TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT"

/** Presenter for [CreateProfileActivity]. */
class CreateProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: CreateProfileActivityBinding

  /** Handle creation and binding of the CreateProfileActivity layout. */
  fun handleOnCreate(profileId: ProfileId, profileType: ProfileType) {
    binding = DataBindingUtil.setContentView(activity, R.layout.create_profile_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    if (getNewLearnerProfileFragment() == null) {
      val createLearnerProfileFragment = CreateProfileFragment()

      val args = Bundle().apply {
        val fragmentArgs =
          CreateProfileFragmentArguments.newBuilder().setProfileType(profileType).build()
        putProto(CREATE_PROFILE_FRAGMENT_ARGS, fragmentArgs)
        decorateWithUserProfileId(profileId)
      }

      createLearnerProfileFragment.arguments = args

      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_fragment_placeholder,
        createLearnerProfileFragment,
        TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT
      ).commitNow()
    }
  }

  private fun getNewLearnerProfileFragment(): CreateProfileFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_CREATE_PROFILE_ACTIVITY_FRAGMENT
    ) as? CreateProfileFragment
  }
}
