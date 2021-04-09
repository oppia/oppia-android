package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** Activity for recent stories. */
class RecentlyPlayedActivity : InjectableAppCompatActivity(), RouteToExplorationListener {

  @Inject
  lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId = intent.getIntExtra(
      RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY,
      -1
    )
    val recentlyPlayedTitleEnum =
      intent.getSerializableExtra(RECENTLY_PLAYED_ACTIVITY_TITLE_ENUM_KEY)
        as RecentlyPlayedTitleEnum
    recentlyPlayedActivityPresenter.handleOnCreate(internalProfileId, recentlyPlayedTitleEnum)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY =
      "RecentlyPlayedActivity.internal_profile_id"

    internal const val RECENTLY_PLAYED_ACTIVITY_TITLE_ENUM_KEY =
      "RecentlyPlayedActivity.title_enum_key"

    /** Returns a new [Intent] to route to [RecentlyPlayedActivity]. */
    fun createRecentlyPlayedActivityIntent(
      context: Context,
      internalProfileId: Int,
      recentlyPlayedTitleEnum: RecentlyPlayedTitleEnum
    ): Intent {
      val intent = Intent(context, RecentlyPlayedActivity::class.java)
      intent.putExtra(RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY, internalProfileId)
      intent.putExtra(RECENTLY_PLAYED_ACTIVITY_TITLE_ENUM_KEY, recentlyPlayedTitleEnum)
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
