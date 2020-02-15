package org.oppia.app.administratorcontrols

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import javax.inject.Inject

class AdministratorControlsActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.administrator_controls_activity)
    if (getAdministratorControlsFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.administrator_controls_fragment_placeholder,
        AdministratorControlsFragment()
      ).commitNow()
    }
  }

  private fun getAdministratorControlsFragment(): AdministratorControlsFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.administrator_controls_fragment_placeholder) as AdministratorControlsFragment?
  }
}
