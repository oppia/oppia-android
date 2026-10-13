package org.oppia.android.app.devoptions.forcenetworktype

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ForceNetworkTypeActivity]. */
@ActivityScope
class ForceNetworkTypeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  /** Called when [ForceNetworkTypeActivity] is created. Handles UI for the activity. */
  fun handleOnCreate() {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
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
