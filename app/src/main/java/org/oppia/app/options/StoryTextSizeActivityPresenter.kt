package org.oppia.app.options

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [StoryTextSizeActivity]. */
@ActivityScope
class StoryTextSizeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var fontSize: String

  fun handleOnCreate(prefSummaryValue: String) {
    activity.setContentView(R.layout.story_text_size_activity)
    fontSize = prefSummaryValue
    if (getStoryTextSizeFragment() == null) {
      val storyTextSizeFragment = StoryTextSizeFragment.newInstance(prefSummaryValue)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.story_text_size_container, storyTextSizeFragment).commitNow()
    }
  }

  fun setSelectedStoryTextSize(fontSize: String) {
    this.fontSize = fontSize
  }

  fun getSelectedStoryTextSize(): String {
    return fontSize
  }

  private fun getStoryTextSizeFragment(): StoryTextSizeFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.story_text_size_container)
      as StoryTextSizeFragment?
  }
}
