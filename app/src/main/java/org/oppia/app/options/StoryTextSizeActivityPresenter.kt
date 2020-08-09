package org.oppia.app.options

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.StoryTextSize
import javax.inject.Inject

/** The presenter for [StoryTextSizeActivity]. */
@ActivityScope
class StoryTextSizeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private var fontSize: String = getStoryTextSize(StoryTextSize.SMALL_TEXT_SIZE)

  fun handleOnCreate(prefSummaryValue: String) {
    activity.setContentView(R.layout.story_text_size_activity)
    val storyTextSizeFragment = StoryTextSizeFragment.newInstance(prefSummaryValue)
    activity.supportFragmentManager.beginTransaction()
      .add(R.id.story_text_size_container, storyTextSizeFragment).commitNow()
  }

  fun setSelectedStoryTextSize(fontSize: String) {
    this.fontSize = fontSize
  }

  fun getSelectedStoryTextSize(): String {
    return fontSize
  }

  private fun getStoryTextSize(storyTextSize: StoryTextSize): String {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> "Small"
      StoryTextSize.MEDIUM_TEXT_SIZE -> "Medium"
      StoryTextSize.LARGE_TEXT_SIZE -> "Large"
      else -> "Extra Large"
    }
  }
}
