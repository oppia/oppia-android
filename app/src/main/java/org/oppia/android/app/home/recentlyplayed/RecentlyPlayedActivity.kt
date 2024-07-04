package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for recent stories. */
class RecentlyPlayedActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToExplorationListener,
  RouteToResumeLessonListener {

  @Inject
  lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val recentlyPlayedActivityParams = intent.getProtoExtra(
      RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS_KEY,
      RecentlyPlayedActivityParams.getDefaultInstance()
    )
    recentlyPlayedActivityPresenter.handleOnCreate(recentlyPlayedActivityParams)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS_KEY =
      "RecentlyPlayedActivity.intent_extras"

    /** Returns a new [Intent] to route to [RecentlyPlayedActivity]. */
    fun createRecentlyPlayedActivityIntent(
      context: Context,
      recentlyPlayedActivityParams: RecentlyPlayedActivityParams
    ): Intent {
      return Intent(context, RecentlyPlayedActivity::class.java).apply {
        putProtoExtra(
          RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS_KEY,
          recentlyPlayedActivityParams
        )
        decorateWithScreenName(ScreenName.RECENTLY_PLAYED_ACTIVITY)
      }
    }
  }

  override fun routeToExploration(
    profileId: ProfileId,
    classroomId: String,
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
        classroomId,
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
    classroomId: String,
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
        classroomId,
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
    override fun createIntent(
      recentlyPlayedActivityParams: RecentlyPlayedActivityParams
    ): Intent =
      createRecentlyPlayedActivityIntent(activity, recentlyPlayedActivityParams)
  }
}
