package org.oppia.android.app.devoptions.platformparameters

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** The presenter for [PlatformParameterActivity]. */
@ActivityScope
class PlatformParameterActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Called when [PlatformParameterActivity] is created. Handles UI for the activity. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.feature_flag_activity)

    if (getPlatformParameterFragment() == null) {
      val PlatformParameterFragment = PlatformParameterFragment.newInstance()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.feature_flag_container,
        PlatformParameterFragment
      ).commitNow()
    }
  }

  private fun getPlatformParameterFragment(): PlatformParameterFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.feature_flag_container) as PlatformParameterFragment?
  }
}
