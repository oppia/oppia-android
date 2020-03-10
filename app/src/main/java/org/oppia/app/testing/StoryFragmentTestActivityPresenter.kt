package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.story.StoryFragment
import javax.inject.Inject

/** The presenter for [StoryFragmentTestActivity]. */
@ActivityScope
class StoryFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.story_fragment_test_activity)
    if (getStoryFragment() == null) {
      val internalProfileId = activity.intent.getIntExtra(INTERNAL_PROFILE_ID_TEST_INTENT_EXTRA, -1)
      val topicId = checkNotNull(activity.intent.getStringExtra(TOPIC_ID_TEST_INTENT_EXTRA)) {
        "Expected non-null topic ID to be passed in using extra key: $TOPIC_ID_TEST_INTENT_EXTRA"
      }
      val storyId = checkNotNull(activity.intent.getStringExtra(STORY_ID_TEST_INTENT_EXTRA)) {
        "Expected non-null story ID to be passed in using extra key: $STORY_ID_TEST_INTENT_EXTRA"
      }
      activity.supportFragmentManager.beginTransaction().add(
        R.id.story_fragment_placeholder,
        StoryFragment.newInstance(internalProfileId, topicId, storyId)
      ).commitNow()
    }
  }

  private fun getStoryFragment(): StoryFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.story_fragment_placeholder) as StoryFragment?
  }
}
