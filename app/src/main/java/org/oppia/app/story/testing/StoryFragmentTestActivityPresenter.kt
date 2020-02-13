package org.oppia.app.story.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.story.StoryFragment
import org.oppia.domain.topic.TEST_STORY_ID_1
import javax.inject.Inject

/** The presenter for [StoryFragmentTestActivity]. */
@ActivityScope
class StoryFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.story_fragment_test_activity)
    if (getStoryFragment() == null) {
      /*val storyId = checkNotNull(activity.intent.getStringExtra(STORY_ID_TEST_INTENT_EXTRA)) {
        "Expected non-null story ID to be passed in using extra key: $STORY_ID_TEST_INTENT_EXTRA"
      }*/
      activity.supportFragmentManager.beginTransaction().add(
        R.id.story_fragment_placeholder,
        StoryFragment.newInstance(TEST_STORY_ID_1)
      ).commitNow()
    }
  }

  private fun getStoryFragment(): StoryFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.story_fragment_placeholder) as StoryFragment?
  }
}