package org.oppia.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import javax.inject.Inject

private const val KEY_INTERNAL_PROFILE_ID_ARGUMENT = "INTERNAL_PROFILE_ID"

/** Activity for recent stories. */
class RecentlyPlayedActivity : InjectableAppCompatActivity(), RouteToExplorationListener {

  @Inject lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId = intent.getIntExtra(KEY_INTERNAL_PROFILE_ID_ARGUMENT, -1)
    recentlyPlayedActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    /** Returns a new [Intent] to route to [RecentlyPlayedActivity]. */
    fun createRecentlyPlayedActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, RecentlyPlayedActivity::class.java)
      intent.putExtra(KEY_INTERNAL_PROFILE_ID_ARGUMENT, internalProfileId)
      return intent
    }
  }

  override fun routeToExploration(internalProfileId: Int, topicId: String, storyId: String, explorationId: String) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId
      )
    )
  }
}
