package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import javax.inject.Inject

/** The presenter for arranging the UI of [ProfileAndDeviceIdActivity]. */
class ProfileAndDeviceIdActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  /** Handles [ProfileAndDeviceIdActivity]'s creation flow. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.profile_and_device_id_activity)
    setToolbar()
    if (getProfileAndDeviceIdFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_and_device_id_fragment_placeholder,
        ProfileAndDeviceIdFragment()
      ).commitNow()
    }
  }

  private fun setToolbar() {
    val appVersionToolbar = activity.findViewById(R.id.profile_and_device_id_toolbar) as Toolbar
    activity.setSupportActionBar(appVersionToolbar)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  private fun getProfileAndDeviceIdFragment(): ProfileAndDeviceIdFragment? {
    val fragManager = activity.supportFragmentManager
    val fragment = fragManager.findFragmentById(R.id.profile_and_device_id_fragment_placeholder)
    return fragment as? ProfileAndDeviceIdFragment
  }
}
