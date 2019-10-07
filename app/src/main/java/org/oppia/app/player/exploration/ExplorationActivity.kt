package org.oppia.app.player.exploration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The starting point for exploration. */
class ExplorationActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var explorationActivityPresenter: ExplorationActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    explorationActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [ExplorationActivity] for a specified exploration ID. */
    fun createExplorationActivityIntent(context: Context): Intent {
      return Intent(context, ExplorationActivity::class.java)
    }
  }
}
