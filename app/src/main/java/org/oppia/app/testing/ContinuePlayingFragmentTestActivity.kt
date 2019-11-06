package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.RouteToStoryListener
import javax.inject.Inject

/** Test activity for recent stories. */
class ContinuePlayingFragmentTestActivity : InjectableAppCompatActivity(), RouteToStoryListener {
  @Inject lateinit var continuePlayingFragmentTestActivityPresenter: ContinuePlayingFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    continuePlayingFragmentTestActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [ContinuePlayingFragmentTestActivity]. */
    fun createContinuePlayingActivityIntent(context: Context): Intent {
      return Intent(context, ContinuePlayingFragmentTestActivity::class.java)
    }
  }

  override fun routeToStory(storyId: String) {
    startActivity(StoryActivity.createStoryActivityIntent(this, storyId))
  }
}
