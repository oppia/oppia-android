package org.oppia.app.home

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject lateinit var homeActivityController: HomeActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    homeActivityController.handleOnCreate()
  }

  override fun routeToExploration() {
    startActivity(ExplorationActivity.createExplorationActivityIntent(this))
  }
}
