package org.oppia.app.options

import androidx.databinding.ObservableField

/** StoryText size settings view model for the recycler view in [OptionsFragment]. */
class OptionsStoryTextViewViewModel(
  private val routeToStoryTextSizeListener: RouteToStoryTextSizeListener,
  private val loadStoryTextSizeFragmentListener: LoadStoryTextSizeFragmentListener
) : OptionsItemViewModel() {
  val storyTextSize = ObservableField<String>("")

  fun setStoryTextSize(storyTextSizeValue: String) {
    storyTextSize.set(storyTextSizeValue)
  }

  fun loadStoryTextSizeFragment() {
    loadStoryTextSizeFragmentListener.loadStoryTextSizeFragment(storyTextSize.get()!!)
  }

  fun onStoryTextSizeClicked() {
    if (isMultipaneOptions.get()!!) {
      loadStoryTextSizeFragment()
    } else {
      routeToStoryTextSizeListener.routeStoryTextSize(storyTextSize.get())
    }
  }
}
