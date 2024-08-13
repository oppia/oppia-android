package org.oppia.android.app.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.STORY_ACTIVITY
import org.oppia.android.app.model.StoryActivityParams
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for stories. */
class StoryActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToExplorationListener,
  RouteToResumeLessonListener {
  @Inject
  lateinit var storyActivityPresenter: StoryActivityPresenter
  private lateinit var profileId: ProfileId
  private lateinit var classroomId: String
  private lateinit var topicId: String
  private lateinit var storyId: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      STORY_ACTIVITY_PARAMS_KEY,
      StoryActivityParams.getDefaultInstance()
    )
    profileId =
      intent?.extractCurrentUserProfileId() ?: ProfileId.newBuilder().setLoggedOut(true).build()
    classroomId = checkNotNull(args.classroomId) {
      "Expected extra classroom ID to be included for StoryActivity."
    }
    topicId = checkNotNull(args.topicId) {
      "Expected extra topic ID to be included for StoryActivity."
    }
    storyId = checkNotNull(args.storyId) {
      "Expected extra story ID to be included for StoryActivity."
    }

    if (!profileId.loggedOut) {
      storyActivityPresenter.handleOnCreate(
        profileId.loggedInInternalProfileId,
        classroomId,
        topicId,
        storyId
      )
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

  override fun onBackPressed() {
    finish()
  }

  companion object {

    /** Params key for StoryActivity. */
    const val STORY_ACTIVITY_PARAMS_KEY = "StoryActivity.params"

    /** Returns a new [Intent] to route to [StoryActivity] for a specified story. */
    fun createStoryActivityIntent(
      context: Context,
      internalProfileId: Int,
      classroomId: String,
      topicId: String,
      storyId: String
    ): Intent {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      val args = StoryActivityParams.newBuilder().apply {
        this.classroomId = classroomId
        this.topicId = topicId
        this.storyId = storyId
      }.build()
      return Intent(context, StoryActivity::class.java).apply {
        putProtoExtra(STORY_ACTIVITY_PARAMS_KEY, args)
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(STORY_ACTIVITY)
      }
    }
  }
}
