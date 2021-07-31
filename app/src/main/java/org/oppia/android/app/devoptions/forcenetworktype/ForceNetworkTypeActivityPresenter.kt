package org.oppia.android.app.devoptions.forcenetworktype

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [ForceNetworkTypeActivity]. */
@ActivityScope
class ForceNetworkTypeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Called when [ForceNetworkTypeActivity] is created. Handles UI for the activity. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.force_network_type_activity)

    if (getForceNetworkTypeFragment() == null) {
      val forceNetworkTypeFragment = ForceNetworkTypeFragment.newInstance()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.force_network_type_container,
        forceNetworkTypeFragment
      ).commitNow()
    }
  }

  private fun getForceNetworkTypeFragment(): ForceNetworkTypeFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.force_network_type_container) as ForceNetworkTypeFragment?
  }
}
