package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.RECENTLY_PLAYED_ACTIVITY
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for recent stories. */
class RecentlyPlayedActivity :
  InjectableAppCompatActivity(),
  RouteToExplorationListener,
  RouteToResumeLessonListener {

  @Inject
  lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val internalProfileId = intent.getIntExtra(
      RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY,
      -1
    )
    recentlyPlayedActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY =
      "RecentlyPlayedActivity.internal_profile_id"

    /** Returns a new [Intent] to route to [RecentlyPlayedActivity]. */
    fun createRecentlyPlayedActivityIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, RecentlyPlayedActivity::class.java).apply {
        putExtra(RECENTLY_PLAYED_ACTIVITY_INTERNAL_PROFILE_ID_KEY, internalProfileId)
        decorateWithScreenName(RECENTLY_PLAYED_ACTIVITY)
      }
    }
  }

  override fun routeToExploration(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        profileId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        isCheckpointingEnabled
      )
    )
  }

  override fun routeToResumeLesson(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    startActivity(
      ResumeLessonActivity.createResumeLessonActivityIntent(
        this,
        profileId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        explorationCheckpoint
      )
    )
  }

  class RecentlyPlayedActivityIntentFactoryImpl @Inject constructor(
    private val activity: AppCompatActivity
  ) : ActivityIntentFactories.RecentlyPlayedActivityIntentFactory {
    override fun createIntent(profileId: ProfileId): Intent =
      createRecentlyPlayedActivityIntent(activity, profileId.internalId)
  }
}
