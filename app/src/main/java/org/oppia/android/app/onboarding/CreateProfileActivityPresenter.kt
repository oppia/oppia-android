package org.oppia.android.app.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.model.CreateProfileFragmentArguments
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.ui.R
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Argument key for [CreateProfileFragment] arguments. */
const val CREATE_PROFILE_FRAGMENT_ARGS = "CreateProfileFragment.args"

private const val TAG_CREATE_PROFILE_FRAGMENT = "TAG_CREATE_PROFILE_FRAGMENT"

/** Presenter for [CreateProfileActivity]. */
class CreateProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  /** Handle creation and binding of the CreateProfileActivity layout. */
  fun handleOnCreate(profileId: LegacyProfileId, profileType: ProfileType, avatarColor: Int = 0) {
    activity.setContentView(R.layout.create_profile_activity)
    if (getCreateProfileFragment() == null) {
      val createProfileFragment = CreateProfileFragment().apply {
        arguments = Bundle().also {
          it.decorateWithUserProfileId(profileId)
          it.putProto(
            CREATE_PROFILE_ARGUMENTS_KEY,
            CreateProfileFragmentArguments.newBuilder()
              .setProfileType(profileType)
              .setAvatarColor(avatarColor)
              .build()
          )
        }
      }
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.profile_fragment_placeholder, createProfileFragment)
        .commitNow()
    }
  }

  private fun getCreateProfileFragment(): CreateProfileFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_CREATE_PROFILE_FRAGMENT
    ) as? CreateProfileFragment
  }
}
