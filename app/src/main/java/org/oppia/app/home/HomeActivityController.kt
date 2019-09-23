package org.oppia.app.home

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.state.StateFragment
import javax.inject.Inject

/** The controller for [HomeActivity]. */
@ActivityScope
class HomeActivityController @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.home_activity)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.home_fragment_placeholder,
      StateFragment()
    ).commitNow()
  }
}
