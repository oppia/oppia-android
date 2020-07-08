package org.oppia.app.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

/** Activity for stories. */
class StoryActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject
  lateinit var storyActivityPresenter: StoryActivityPresenter
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
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

  override fun onBackPressed() {
    startActivity(
      TopicActivity.createTopicPlayStoryActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId
      )
    )
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
