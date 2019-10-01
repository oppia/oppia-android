package org.oppia.app.player.exploration

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.home.HomeFragment
import org.oppia.app.player.state.StateFragment
import javax.inject.Inject

/** The controller for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.exploration_activity)
    if (getExplorationFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        StateFragment()
      ).commitNow()
    }
  }

  private fun getExplorationFragment(): StateFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.exploration_fragment_placeholder) as StateFragment?
  }
}
