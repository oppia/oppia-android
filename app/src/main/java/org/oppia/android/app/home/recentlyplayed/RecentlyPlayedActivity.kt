package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.RecentlyPlayedActivityIntentExtras
import org.oppia.android.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** Activity for recent stories. */
class RecentlyPlayedActivity : InjectableAppCompatActivity(), RouteToExplorationListener {

  @Inject
  lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val recentlyPlayedActivityIntentExtras = RecentlyPlayedActivityIntentExtras.parseFrom(
      intent.getByteArrayExtra(RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS)
    )

    recentlyPlayedActivityPresenter.handleOnCreate(recentlyPlayedActivityIntentExtras)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS = "RecentlyPlayedActivity.intent_extras"

    /** Returns a new [Intent] to route to [RecentlyPlayedActivity]. */
    fun createRecentlyPlayedActivityIntent(
      context: Context,
      recentlyPlayedActivityIntentExtras: RecentlyPlayedActivityIntentExtras
    ): Intent {
      val intent = Intent(context, RecentlyPlayedActivity::class.java)
      intent.putExtra(
        RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS,
        recentlyPlayedActivityIntentExtras.toByteArray()
      )
      return intent
    }
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen
      )
    )
  }
}
