package org.oppia.app.story

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [StoryActivity]. */
@ActivityScope
class StoryActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate(storyId: String) {
    activity.setContentView(R.layout.story_activity)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    if (getStoryFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.story_fragment_placeholder,
        StoryFragment.newInstance(storyId)
      ).commitNow()
    }
  }

  fun handleOnSupportNavigationUp(): Boolean {
    activity.finish()
    return true
  }

  private fun getStoryFragment(): StoryFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.story_fragment_placeholder) as StoryFragment?
  }
}
