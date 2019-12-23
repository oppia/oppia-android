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
    val storyId: String = checkNotNull(intent.getStringExtra(STORY_ACTIVITY_INTENT_EXTRA)) {
      "Expected extra story ID to be included for StoryActivity."
    }
    storyActivityPresenter.handleOnCreate(storyId)
  }

  override fun routeToExploration(explorationId: String, topicId: String?) {
    startActivity(ExplorationActivity.createExplorationActivityIntent(this, explorationId, topicId))
  }

  companion object {
    const val STORY_ACTIVITY_INTENT_EXTRA = "StoryActivity.story_id"

    /** Returns a new [Intent] to route to [StoryActivity] for a specified story ID. */
    fun createStoryActivityIntent(context: Context, storyId: String): Intent {
      val intent = Intent(context, StoryActivity::class.java)
      intent.putExtra(STORY_ACTIVITY_INTENT_EXTRA, storyId)
      return intent
    }
  }
}
