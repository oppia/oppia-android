package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.administratorcontrols.AdministratorControlsFragment
import javax.inject.Inject

/** The presenter for [AdministratorControlsFragmentTestActivity]. */
@ActivityScope
class AdministratorControlsFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  /** Initializes the [AdministratorControlsFragmentTestActivity] and sets the navigation drawer. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.administrator_controls_fragment_test_activity)
    activity.supportFragmentManager.beginTransaction().replace(
      R.id.administrator_controls_fragment_test_activity_fragment_container,
      AdministratorControlsFragment.newInstance(isMultipane = false)
    ).commitNow()
  }

  /** Returns [AdministratorControlsFragment] instance. */
  private fun getAdministratorControlsFragment(): AdministratorControlsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.administrator_controls_fragment_test_activity_fragment_container
      ) as? AdministratorControlsFragment
  }
}
