package org.oppia.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** Activity for recent stories. */
class RecentlyPlayedActivity : InjectableAppCompatActivity(), RouteToExplorationListener {

  @Inject lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    recentlyPlayedActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [RecentlyPlayedActivity]. */
    fun createRecentlyPlayedActivityIntent(context: Context): Intent {
      return Intent(context, RecentlyPlayedActivity::class.java)
    }
  }

  override fun routeToExploration(explorationId: String, topicId: String?) {
    startActivity(ExplorationActivity.createExplorationActivityIntent(this, explorationId, topicId))
  }
}
