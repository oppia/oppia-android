package org.oppia.app.option

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [StoryTextSizeActivity]. */
@ActivityScope
class StoryTextSizeActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var optionSelectorListener: OptionSelectorListener
  fun handleOnCreate() {
    activity.setContentView(R.layout.story_text_size_activity)
    optionSelectorListener =  activity as OptionSelectorListener
  }
}
