package org.oppia.android.app.player.exploration

import org.oppia.android.app.model.ReadingTextSize

/**
 * To set the font-size correctly we need to fetch it before loading the `ExplorationFragment` and
 * therefore this listener listens to the default reading text size in `ExplorationManagerFragment` and
 * passes the information to `ExplorationActivity` which eventually loads the `ExplorationFragment`.
 */
interface DefaultFontSizeStateListener {
  fun onDefaultFontSizeLoaded(readingTextSize: ReadingTextSize)
}
