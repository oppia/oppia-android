package org.oppia.app.player.exploration

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The starting point for exploration*/
class ExplorationActivity : InjectableAppCompatActivity() {
  @Inject lateinit var explorationActivityController: ExplorationActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    explorationActivityController.handleOnCreate()
  }
}
