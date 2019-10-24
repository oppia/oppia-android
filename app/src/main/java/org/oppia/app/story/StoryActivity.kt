package org.oppia.app.story

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val STORY_ACTIVITY_STORY_ID_ARGUMENT_KEY = "StoryActivity.story_id"

/** Activity for stories. */
class StoryActivity : InjectableAppCompatActivity() {
  @Inject lateinit var storyActivityPresenter: StoryActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    storyActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [StoryActivity] for a specified topic ID. */
    fun createStoryActivityIntent(context: Context, storyId: String): Intent {
      val intent = Intent(context, StoryActivity::class.java)
      intent.putExtra(STORY_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      return intent
    }
  }
}
