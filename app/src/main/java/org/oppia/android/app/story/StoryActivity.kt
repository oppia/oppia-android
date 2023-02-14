package org.oppia.android.app.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.STORY_ACTIVITY
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for stories. */
class StoryActivity :
  InjectableAppCompatActivity(),
  RouteToExplorationListener,
  RouteToResumeLessonListener {
  @Inject
  lateinit var storyActivityPresenter: StoryActivityPresenter
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent.extractCurrentUserProfileId()
    topicId = checkNotNull(intent.getStringExtra(STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID)) {
      "Expected extra topic ID to be included for StoryActivity."
    }
    storyId = checkNotNull(intent.getStringExtra(STORY_ACTIVITY_INTENT_EXTRA_STORY_ID)) {
      "Expected extra story ID to be included for StoryActivity."
    }
    storyActivityPresenter.handleOnCreate(profileId.internalId, topicId, storyId)
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

  override fun onBackPressed() {
    finish()
  }

  companion object {
    const val STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID = "StoryActivity.topic_id"
    const val STORY_ACTIVITY_INTENT_EXTRA_STORY_ID = "StoryActivity.story_id"

    /** Returns a new [Intent] to route to [StoryActivity] for a specified story. */
    fun createStoryActivityIntent(
      context: Context,
      profileId: ProfileId,
      topicId: String,
      storyId: String
    ): Intent {
      return Intent(context, StoryActivity::class.java).apply {
        putExtra(STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID, topicId)
        putExtra(STORY_ACTIVITY_INTENT_EXTRA_STORY_ID, storyId)
        decorateWithScreenName(STORY_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }
}
