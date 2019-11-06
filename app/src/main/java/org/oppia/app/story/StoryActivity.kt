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
    val storyId: String? = intent.getStringExtra(STORY_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    storyActivityPresenter.handleOnCreate(storyId)
  }

  override fun onSupportNavigateUp(): Boolean {
    return storyActivityPresenter.handleOnSupportNavigationUp()
  }

  override fun routeToExploration(explorationId: String) {
    startActivity(ExplorationActivity.createExplorationActivityIntent(this, explorationId))
  }

  companion object {
    const val STORY_ACTIVITY_STORY_ID_ARGUMENT_KEY = "StoryActivity.story_id"

    /** Returns a new [Intent] to route to [StoryActivity] for a specified story ID. */
    fun createStoryActivityIntent(context: Context, storyId: String): Intent {
      val intent = Intent(context, StoryActivity::class.java)
      intent.putExtra(STORY_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      return intent
    }
  }
}
