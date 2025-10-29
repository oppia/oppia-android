package org.oppia.android.app.devoptions.platformparameters

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** The presenter for [PlatformParametersActivity]. */
@ActivityScope
class PlatformParametersActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  /** Called when [PlatformParametersActivity] is created. Handles UI for the activity. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.platform_parameters_activity)

    if (getPlatformParametersFragment() == null) {
      val platformParametersFragment = PlatformParametersFragment.newInstance()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.platform_parameters_container,
        platformParametersFragment
      ).commitNow()
    }
  }

  private fun getPlatformParametersFragment(): PlatformParametersFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.platform_parameters_container) as? PlatformParametersFragment
  }
}
