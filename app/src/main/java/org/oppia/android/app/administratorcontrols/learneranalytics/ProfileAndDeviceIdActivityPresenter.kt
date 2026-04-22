package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for arranging the UI of [ProfileAndDeviceIdActivity]. */
class ProfileAndDeviceIdActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  /** Handles [ProfileAndDeviceIdActivity]'s creation flow. */
  fun handleOnCreate() {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.setContentView(R.layout.profile_and_device_id_activity)
    val toolbar = setToolbar()
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        toolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }
    if (getProfileAndDeviceIdFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_and_device_id_fragment_placeholder,
        ProfileAndDeviceIdFragment()
      ).commitNow()
    }
  }

  private fun setToolbar(): Toolbar {
    val toolbar = activity.findViewById(R.id.profile_and_device_id_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    return toolbar
  }

  private fun getProfileAndDeviceIdFragment(): ProfileAndDeviceIdFragment? {
    val fragManager = activity.supportFragmentManager
    val fragment = fragManager.findFragmentById(R.id.profile_and_device_id_fragment_placeholder)
    return fragment as? ProfileAndDeviceIdFragment
  }
}
