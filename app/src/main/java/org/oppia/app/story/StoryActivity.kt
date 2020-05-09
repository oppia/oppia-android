package org.oppia.app.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** Activity for stories. */
class StoryActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject lateinit var storyActivityPresenter: StoryActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId = intent.getIntExtra(STORY_ACTIVITY_INTENT_EXTRA_INTERNAL_PROFILE_ID, -1)
    val topicId = checkNotNull(intent.getStringExtra(STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID)) {
      "Expected extra topic ID to be included for StoryActivity."
    }
    val storyId: String = checkNotNull(intent.getStringExtra(STORY_ACTIVITY_INTENT_EXTRA_STORY_ID)) {
      "Expected extra story ID to be included for StoryActivity."
    }
    storyActivityPresenter.handleOnCreate(internalProfileId, topicId, storyId)
  }

  override fun routeToExploration(internalProfileId: Int, topicId: String, storyId: String, explorationId: String) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId
      )
    )
  }

  companion object {
    const val STORY_ACTIVITY_INTENT_EXTRA_INTERNAL_PROFILE_ID = "StoryActivity.internal_profile_id"
    const val STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID = "StoryActivity.topic_id"
    const val STORY_ACTIVITY_INTENT_EXTRA_STORY_ID = "StoryActivity.story_id"

    /** Returns a new [Intent] to route to [StoryActivity] for a specified story. */
    fun createStoryActivityIntent(context: Context, internalProfileId: Int, topicId: String, storyId: String): Intent {
      val intent = Intent(context, StoryActivity::class.java)
      intent.putExtra(STORY_ACTIVITY_INTENT_EXTRA_INTERNAL_PROFILE_ID, internalProfileId)
      intent.putExtra(STORY_ACTIVITY_INTENT_EXTRA_TOPIC_ID, topicId)
      intent.putExtra(STORY_ACTIVITY_INTENT_EXTRA_STORY_ID, storyId)
      return intent
    }
  }
}
