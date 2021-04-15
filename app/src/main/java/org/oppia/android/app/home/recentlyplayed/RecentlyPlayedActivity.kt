package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.RecentlyPlayedActivityIntentExtras
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Activity for recent stories. */
class RecentlyPlayedActivity : InjectableAppCompatActivity(), RouteToExplorationListener {

  @Inject
  lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val bundle =
      checkNotNull(intent.extras) { "Expected bundle to be passed to RecentlyPlayedActivity" }
    val recentlyPlayedActivityIntentExtras = bundle.getProto(
      RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS,
      RecentlyPlayedActivityIntentExtras.getDefaultInstance()
    )
    recentlyPlayedActivityPresenter.handleOnCreate(recentlyPlayedActivityIntentExtras)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    internal const val RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS =
      "RecentlyPlayedActivity.intent_extras"

    /** Returns a new [Intent] to route to [RecentlyPlayedActivity]. */
    fun createRecentlyPlayedActivityIntent(
      context: Context,
      recentlyPlayedActivityIntentExtras: RecentlyPlayedActivityIntentExtras
    ): Intent {
      val bundle = Bundle()
      bundle.putProto(RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS, recentlyPlayedActivityIntentExtras)

      val intent = Intent(context, RecentlyPlayedActivity::class.java)
      intent.putExtras(bundle)
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
