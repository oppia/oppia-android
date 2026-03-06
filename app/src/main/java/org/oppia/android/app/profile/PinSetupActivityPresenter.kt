package org.oppia.android.app.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.ui.R
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** The presenter for [PinSetupActivity]. */
@ActivityScope
class PinSetupActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private companion object {
    private const val TAG_PIN_SETUP_FRAGMENT = "TAG_PIN_SETUP_FRAGMENT"
  }

  /** Creates the view for [PinSetupActivity]. */
  fun handleOnCreate(profileId: LegacyProfileId) {
    activity.setContentView(R.layout.pin_setup_activity)

    if (getPinSetupFragment() == null) {
      val pinSetupFragment = PinSetupFragment().apply {
        arguments = Bundle().also { it.decorateWithUserProfileId(profileId) }
      }

      activity.supportFragmentManager.beginTransaction()
        .add(
          R.id.pin_setup_fragment_placeholder,
          pinSetupFragment,
          TAG_PIN_SETUP_FRAGMENT
        )
        .commitNow()
    }
  }

  private fun getPinSetupFragment(): PinSetupFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_PIN_SETUP_FRAGMENT
    ) as? PinSetupFragment
  }
}
