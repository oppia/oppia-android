package org.oppia.app.story

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.domain.topic.TEST_STORY_ID_1
import javax.inject.Inject

/** The controller for [StoryActivity]. */
@ActivityScope
class StoryActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.story_activity)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.story_fragment_placeholder,
      StoryFragment.newInstace(TEST_STORY_ID_1)
    ).commitNow()
  }

  fun handleOnSupportNavigationUp(): Boolean {
    activity.finish()
    return true
  }
}
