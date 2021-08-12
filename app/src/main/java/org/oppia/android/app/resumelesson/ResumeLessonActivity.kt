package org.oppia.android.app.resumelesson

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** Activity that allows the user to resume a saved exploration. */
class ResumeLessonActivity :
  InjectableAppCompatActivity(),
  RouteToExplorationListener {

  @Inject
  lateinit var resumeLessonActivityPresenter: ResumeLessonActivityPresenter
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private var backflowScreen: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(RESUME_LESSON_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)
    topicId =
      checkNotNull(intent.getStringExtra(RESUME_LESSON_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)) {
        "Expected topic ID to be included in intent for ResumeLessonActivity."
      }
    storyId =
      checkNotNull(intent.getStringExtra(RESUME_LESSON_ACTIVITY_STORY_ID_ARGUMENT_KEY)) {
        "Expected story ID to be included in intent for ResumeLessonActivity."
      }
    explorationId =
      checkNotNull(intent.getStringExtra(RESUME_LESSON_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY)) {
        "Expected exploration ID to be included in intent for ResumeLessonActivity."
      }
    backflowScreen = intent.getIntExtra(RESUME_LESSON_ACTIVITY_BACKFLOW_SCREEN_KEY, -1)
    resumeLessonActivityPresenter.handleOnCreate(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen,
    )
  }

  // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
  companion object {
    /** Argument key for profile ID in [ResumeLessonActivity] */
    const val RESUME_LESSON_ACTIVITY_PROFILE_ID_ARGUMENT_KEY =
      "ResumeLessonActivity.profile_id"

    /** Argument key for topic ID in [ResumeLessonActivity] */
    const val RESUME_LESSON_ACTIVITY_TOPIC_ID_ARGUMENT_KEY =
      "ResumeLessonActivity.topic_id"

    /** Argument key for story ID in [ResumeLessonActivity] */
    const val RESUME_LESSON_ACTIVITY_STORY_ID_ARGUMENT_KEY =
      "ResumeLessonActivity.story_id"

    /** Argument key for exploration ID in [ResumeLessonActivity] */
    const val RESUME_LESSON_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY =
      "ResumeLessonActivity.exploration_id"

    /** Argument key for backflow screen in [ResumeLessonActivity] */
    const val RESUME_LESSON_ACTIVITY_BACKFLOW_SCREEN_KEY =
      "ResumeLessonActivity.backflow_screen"

    /**
     *  Returns a new [Intent] to route to [ResumeLessonActivity] for a specified exploration.
     */
    fun createResumeLessonActivityIntent(
      context: Context,
      profileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String,
      backflowScreen: Int?
    ): Intent {
      val intent = Intent(context, ResumeLessonActivity::class.java)
      intent.putExtra(RESUME_LESSON_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, profileId)
      intent.putExtra(RESUME_LESSON_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(RESUME_LESSON_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      intent.putExtra(RESUME_LESSON_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, explorationId)
      intent.putExtra(RESUME_LESSON_ACTIVITY_BACKFLOW_SCREEN_KEY, backflowScreen)
      return intent
    }
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen,
        isCheckpointingEnabled
      )
    )
    finish()
  }
}
