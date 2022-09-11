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
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.topic.RouteToResumeLessonListener
import javax.inject.Inject

/** Activity for stories. */
class StoryActivity :
  InjectableAppCompatActivity(),
  RouteToExplorationListener,
  RouteToResumeLessonListener {
  @Inject
  lateinit var storyActivityPresenter: StoryActivityPresenter
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    internalProfileId = intent.getIntExtra(STORY_ACTIVITY_INTENT_EXTRA_INTERNAL_PROFILE_ID, -1)
    topicId = checkNotNull(intent.getStringExtra(STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID)) {
      "Expected extra topic ID to be included for StoryActivity."
    }
    storyId = checkNotNull(intent.getStringExtra(STORY_ACTIVITY_INTENT_EXTRA_STORY_ID)) {
      "Expected extra story ID to be included for StoryActivity."
    }
    storyActivityPresenter.handleOnCreate(internalProfileId, topicId, storyId)
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
    const val STORY_ACTIVITY_INTENT_EXTRA_INTERNAL_PROFILE_ID = "StoryActivity.internal_profile_id"
    const val STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID = "StoryActivity.topic_id"
    const val STORY_ACTIVITY_INTENT_EXTRA_STORY_ID = "StoryActivity.story_id"

    /** Returns a new [Intent] to route to [StoryActivity] for a specified story. */
    fun createStoryActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String,
      storyId: String
    ): Intent {
      val intent = Intent(context, StoryActivity::class.java)
      intent.putExtra(STORY_ACTIVITY_INTENT_EXTRA_INTERNAL_PROFILE_ID, internalProfileId)
      intent.putExtra(STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID, topicId)
      intent.putExtra(STORY_ACTIVITY_INTENT_EXTRA_STORY_ID, storyId)
      return intent
    }
  }
}
