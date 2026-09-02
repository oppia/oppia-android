package org.oppia.android.app.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.ui.R
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** The presenter for [CreateAdminPinActivity]. */
@ActivityScope
class CreateAdminPinActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private companion object {
    private const val TAG_CREATE_ADMIN_PIN_FRAGMENT = "TAG_CREATE_ADMIN_PIN_FRAGMENT"
  }

  /** Creates the view for [CreateAdminPinActivity]. */
  fun handleOnCreate(profileId: LegacyProfileId) {
    activity.setContentView(R.layout.create_admin_pin_activity)

    if (getPinSetupFragment() == null) {
      val createAdminPinFragment = CreateAdminPinFragment().apply {
        arguments = Bundle().also { it.decorateWithUserProfileId(profileId) }
      }

      activity.supportFragmentManager.beginTransaction()
        .add(
          R.id.pin_setup_fragment_placeholder,
          createAdminPinFragment,
          TAG_CREATE_ADMIN_PIN_FRAGMENT
        )
        .commitNow()
    }
  }

  private fun getPinSetupFragment(): CreateAdminPinFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_CREATE_ADMIN_PIN_FRAGMENT
    ) as? CreateAdminPinFragment
  }
}
