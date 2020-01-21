package org.oppia.app.options

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [OptionsActivity]. */
@ActivityScope
class OptionsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: Logger,
  private val profileManagementController: ProfileManagementController) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.option_activity)
    if (getOptionFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.option_fragment_placeholder,
        OptionsFragment(activity,profileManagementController,logger)
      ).commitNow()
    }
  }

  private fun getOptionFragment(): OptionsFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.option_fragment_placeholder) as OptionsFragment?
  }
}
