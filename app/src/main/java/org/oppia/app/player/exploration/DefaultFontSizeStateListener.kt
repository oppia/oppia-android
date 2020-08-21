package org.oppia.app.player.exploration

import org.oppia.app.model.ReadingTextSize

/**
 * To set the font-size correctly we need to fetch it before loading the `ExplorationFragment` and
 * therefore this listener listens to the default story size in `ExplorationManagerFragment` and
 * passes the information to `ExplorationActivity` which eventually loads the `ExplorationFragment`.
 */
interface DefaultFontSizeStateListener {
  fun onDefaultFontSizeLoaded(readingTextSize: ReadingTextSize)
}
