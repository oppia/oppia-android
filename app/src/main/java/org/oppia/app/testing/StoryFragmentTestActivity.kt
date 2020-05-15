package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import javax.inject.Inject

const val INTERNAL_PROFILE_ID_TEST_INTENT_EXTRA = "StoryFragmentTestActivity.internalProfileId"
const val TOPIC_ID_TEST_INTENT_EXTRA = "StoryFragmentTestActivity.topic_id"
const val STORY_ID_TEST_INTENT_EXTRA = "StoryFragmentTestActivity.story_id"

/** Test activity used for story fragment. */
class StoryFragmentTestActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject lateinit var storyFragmentTestActivityPresenter: StoryFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    storyFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun routeToExploration(internalProfileId: Int, topicId: String, storyId: String, explorationId: String,backflowId: String?) {
    // Do nothing since routing should be tested at the StoryActivity level.
  }

  companion object {
    /** Returns an [Intent] to create new [StoryFragmentTestActivity]s. */
    fun createTestActivityIntent(context: Context, internalProfileId: Int, topicId: String, storyId: String): Intent {
      val intent = Intent(context, StoryFragmentTestActivity::class.java)
      intent.putExtra(INTERNAL_PROFILE_ID_TEST_INTENT_EXTRA, internalProfileId)
      intent.putExtra(TOPIC_ID_TEST_INTENT_EXTRA, topicId)
      intent.putExtra(STORY_ID_TEST_INTENT_EXTRA, storyId)
      return intent
    }
  }
}
