package org.oppia.app.options

import androidx.databinding.ObservableField

/** StoryText size settings view model for the recycler view in [OptionsFragment]. */
class OptionsStoryTextViewViewModel : OptionsItemViewModel() {
  val storyTextSize = ObservableField<String>("")

  fun setStoryTextSize(storyTextSizeValue: String) {
    storyTextSize.set(storyTextSizeValue)
  }
}

