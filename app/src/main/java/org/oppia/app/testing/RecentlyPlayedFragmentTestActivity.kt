package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** Test activity for recent stories. */
class RecentlyPlayedFragmentTestActivity : InjectableAppCompatActivity(),
  RouteToExplorationListener {
  @Inject lateinit var recentlyPlayedFragmentTestActivityPresenter: RecentlyPlayedFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    recentlyPlayedFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun routeToExploration(explorationId: String, topicId: String?) {
    startActivity(ExplorationActivity.createExplorationActivityIntent(this, explorationId, topicId))
  }
}
