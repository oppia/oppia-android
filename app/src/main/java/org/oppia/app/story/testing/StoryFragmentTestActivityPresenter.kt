package org.oppia.app.story.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.story.StoryFragment
import javax.inject.Inject

/** The presenter for [StoryFragmentTestActivity]. */
@ActivityScope
class StoryFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.story_fragment_test_activity)
    if (getAudioFragment() == null) {
      val storyId = checkNotNull(activity.intent.getStringExtra(STORY_ID_TEST_INTENT_EXTRA)) {
        "Expected non-null story ID to be passed in using extra key: $STORY_ID_TEST_INTENT_EXTRA"
      }
      activity.supportFragmentManager.beginTransaction().add(
        R.id.story_fragment_placeholder,
        StoryFragment.newInstance(storyId)
      ).commitNow()
    }
  }

  private fun getAudioFragment(): AudioFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.audio_fragment_placeholder) as AudioFragment?
  }
}
