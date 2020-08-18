package org.oppia.app.options

import androidx.databinding.ObservableField

/** StoryText size settings view model for the recycler view in [OptionsFragment]. */
class OptionsStoryTextSizeViewModel(
  private val routeToStoryTextSizeListener: RouteToStoryTextSizeListener,
  private val loadStoryTextSizeListener: LoadStoryTextSizeListener
) : OptionsItemViewModel() {
  val storyTextSize = ObservableField<String>("")

  fun setStoryTextSize(storyTextSizeValue: String) {
    storyTextSize.set(storyTextSizeValue)
  }

  fun loadStoryTextSizeFragment() {
    loadStoryTextSizeListener.loadStoryTextSizeFragment(storyTextSize.get()!!)
  }

  fun onStoryTextSizeClicked() {
    if (isMultipane.get()!!) {
      loadStoryTextSizeListener.loadStoryTextSizeFragment(storyTextSize.get()!!)
    } else {
      routeToStoryTextSizeListener.routeStoryTextSize(storyTextSize.get())
    }
  }
}
