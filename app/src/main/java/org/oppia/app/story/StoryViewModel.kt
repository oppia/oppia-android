package org.oppia.app.story

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

@FragmentScope
class StoryViewModel @Inject constructor() : ViewModel() {
  private lateinit var storyId: String

  fun setStoryId(storyId: String) {
    this.storyId = storyId
  }
}