package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope

@ActivityScope
class ProfileAndDeviceIdActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
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
    val appVersionToolbar: Toolbar =
      activity.findViewById(R.id.profile_and_device_id_toolbar) as Toolbar
    activity.setSupportActionBar(appVersionToolbar)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  private fun getProfileAndDeviceIdFragment(): ProfileAndDeviceIdFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.profile_and_device_id_fragment_placeholder) as ProfileAndDeviceIdFragment?
  }
}
