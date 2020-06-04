package org.oppia.app.player.exploration

import org.oppia.app.model.StoryTextSize

/** Listener for default font size fetch. */
interface DefaultFontSizeStateListener {
  fun onDefaultFontSizeLoaded(storyTextSize: StoryTextSize)
}
