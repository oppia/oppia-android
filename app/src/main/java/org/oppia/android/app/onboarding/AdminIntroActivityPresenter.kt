package org.oppia.android.app.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.AdminIntroFragmentArguments
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.ui.R
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Argument key for [AdminIntroFragment] arguments. */
const val ADMIN_INTRO_FRAGMENT_ARGS = "AdminIntroFragment.args"

private const val TAG_ADMIN_INTRO_FRAGMENT = "TAG_ADMIN_INTRO_FRAGMENT"

/** The presenter for [AdminIntroActivity]. */
@ActivityScope
class AdminIntroActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Creates the view for [AdminIntroActivity]. */
  fun handleOnCreate(profileId: ProfileId, profileType: ProfileType) {
    activity.setContentView(R.layout.admin_intro_activity)

    if (getAdminIntroFragment() == null) {
      val adminIntroFragment = AdminIntroFragment()

      val args = Bundle().apply {
        val fragmentArgs =
          AdminIntroFragmentArguments.newBuilder().setProfileType(profileType).build()
        putProto(ADMIN_INTRO_FRAGMENT_ARGS, fragmentArgs)
        decorateWithUserProfileId(profileId)
      }

      adminIntroFragment.arguments = args

      activity.supportFragmentManager.beginTransaction()
        .add(
          R.id.admin_intro_fragment_placeholder,
          adminIntroFragment,
          TAG_ADMIN_INTRO_FRAGMENT
        )
        .commitNow()
    }
  }

  private fun getAdminIntroFragment(): AdminIntroFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_ADMIN_INTRO_FRAGMENT
    ) as? AdminIntroFragment
  }
}
