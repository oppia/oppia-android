package org.oppia.app.player.exploration

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The controller for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityController @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.exploration_activity)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.exploration_fragment_placeholder,
      ExplorationFragment()
    ).commitNow()
  }
}
