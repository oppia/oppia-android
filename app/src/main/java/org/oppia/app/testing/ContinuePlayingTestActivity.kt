package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.RouteToStoryListener
import javax.inject.Inject

/** Test activity for recent stories. */
class ContinuePlayingTestActivity : InjectableAppCompatActivity(), RouteToStoryListener {
  @Inject lateinit var continuePlayingTestActivityPresenter: ContinuePlayingTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    continuePlayingTestActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [ContinuePlayingTestActivity]. */
    fun createContinuePlayingActivityIntent(context: Context): Intent {
      return Intent(context, ContinuePlayingTestActivity::class.java)
    }
  }

  override fun routeToStory(storyId: String) {
    startActivity(StoryActivity.createStoryActivityIntent(this, storyId))
  }
}
