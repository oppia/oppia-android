package org.oppia.android.app.resumelesson

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ResumeLessonActivityParams
import org.oppia.android.app.model.ScreenName.RESUME_LESSON_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that allows the user to resume a saved exploration. */
class ResumeLessonActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject lateinit var resumeLessonActivityPresenter: ResumeLessonActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)

    val params = intent.getProtoExtra(PARAMS_KEY, ResumeLessonActivityParams.getDefaultInstance())
    resumeLessonActivityPresenter.handleOnCreate(
      params.profileId,
      params.topicId,
      params.storyId,
      params.explorationId,
      params.parentScreen,
      params.checkpoint
    )
  }

  // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
  companion object {
    private const val PARAMS_KEY = "ResumeLessonActivity.params"

    /**
     * A convenience function for creating a new [ResumeLessonActivity] intent by prefilling common
     * params needed by the activity.
     */
    fun createIntent(
      context: Context,
      profileId: ProfileId,
      topicId: String,
      storyId: String,
      explorationId: String,
      parentScreen: ExplorationActivityParams.ParentScreen,
      checkpoint: ExplorationCheckpoint
    ): Intent {
      val params = ResumeLessonActivityParams.newBuilder().apply {
        this.profileId = profileId
        this.topicId = topicId
        this.storyId = storyId
        this.explorationId = explorationId
        this.parentScreen = parentScreen
        this.checkpoint = checkpoint
      }.build()
      return createIntent(context, params)
    }

    /** Returns a new [Intent] open an [ResumeLessonActivity] with the specified [params]. */
    fun createIntent(context: Context, params: ResumeLessonActivityParams): Intent {
      return Intent(context, ResumeLessonActivity::class.java).apply {
        putProtoExtra(PARAMS_KEY, params)
        decorateWithScreenName(RESUME_LESSON_ACTIVITY)
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

  interface Injector {
    fun inject(activity: ResumeLessonActivity)
  }
}
