package org.oppia.util.profile

import org.oppia.app.model.StoryTextSize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryTextSizeConverter @Inject constructor() {

  companion object {
    fun getStoryTextSizeInFloat(storyTextSize: StoryTextSize): Float {
      return when (storyTextSize) {
        StoryTextSize.SMALL_TEXT_SIZE -> 16f
        StoryTextSize.MEDIUM_TEXT_SIZE -> 18f
        StoryTextSize.LARGE_TEXT_SIZE -> 20f
        else -> 22f
      }
    }
  }
}
