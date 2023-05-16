package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.app.model.ResumeLessonActivityParams
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for recent stories. */
class RecentlyPlayedActivity :
  InjectableAppCompatActivity(),
  RouteToExplorationListener,
  RouteToResumeLessonListener {
  @Inject lateinit var recentlyPlayedActivityPresenter: RecentlyPlayedActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
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
    fun createIntent(
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
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  ) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        explorationActivityParams = ExplorationActivityParams.newBuilder().apply {
          this.profileId = profileId
          this.topicId = topicId
          this.storyId = storyId
          this.explorationId = explorationId
          this.parentScreen = parentScreen
          this.isCheckpointingEnabled = isCheckpointingEnabled
        }.build()
      }.build()
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
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        resumeLessonActivityParams = ResumeLessonActivityParams.newBuilder().apply {
          this.profileId = profileId
          this.topicId = topicId
          this.storyId = storyId
          this.explorationId = explorationId
          this.parentScreen = parentScreen
          this.checkpoint = explorationCheckpoint
        }.build()
      }.build()
    )
  }

  interface Injector {
    fun inject(activity: RecentlyPlayedActivity)
  }

  class RecentlyPlayedActivityIntentFactoryImpl @Inject constructor(
    private val activity: AppCompatActivity
  ) : ActivityIntentFactories.RecentlyPlayedActivityIntentFactory {
    override fun createIntent(
      recentlyPlayedActivityParams: RecentlyPlayedActivityParams
    ): Intent = Companion.createIntent(activity, recentlyPlayedActivityParams)
  }
}
