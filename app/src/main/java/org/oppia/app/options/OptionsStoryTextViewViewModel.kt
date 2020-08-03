package org.oppia.app.options

import android.util.Log
import androidx.databinding.ObservableField

/** StoryText size settings view model for the recycler view in [OptionsFragment]. */
class OptionsStoryTextViewViewModel(
  private val routeToStoryTextSizeListener: RouteToStoryTextSizeListener
) : OptionsItemViewModel() {
  val storyTextSize = ObservableField<String>("")

  fun setStoryTextSize(storyTextSizeValue: String) {
    storyTextSize.set(storyTextSizeValue)
  }

  fun onStoryTextSizeClicked() {
    if (isMultipaneOptions.get()!!) {
      Log.d("Multipane", "OptionsStoryTextViewViewModel")
      // TODO add this fragment to "multipaneOptionsContainer"
    } else {
      routeToStoryTextSizeListener.routeStoryTextSize(storyTextSize.get())
    }
  }
}
