package org.oppia.app.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for stories. */
class StoryActivity : InjectableAppCompatActivity() {
  @Inject lateinit var storyActivityPresenter: StoryActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    storyActivityPresenter.handleOnCreate()
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
