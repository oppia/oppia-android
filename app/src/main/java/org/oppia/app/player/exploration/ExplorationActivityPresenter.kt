package org.oppia.app.player.exploration

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.home.HomeFragment
import javax.inject.Inject

/** The controller for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.exploration_activity)
    if (getExplorationFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        ExplorationFragment()
      ).commitNow()
    }
  }

  private fun getExplorationFragment(): ExplorationFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.exploration_fragment_placeholder) as ExplorationFragment?
  }
}
