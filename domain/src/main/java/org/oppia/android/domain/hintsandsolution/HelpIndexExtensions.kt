package org.oppia.android.domain.hintsandsolution

import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.EVERYTHING_REVEALED
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.SHOW_SOLUTION
import org.oppia.android.app.model.Hint

/**
 * Returns whether the specified [hintIndex] relative to the proivded list of [Hint]s has been seen
 * by the user based on this [HelpIndex].
 */
fun HelpIndex.isHintRevealed(hintIndex: Int, hintList: List<Hint>): Boolean {
  val lastShownHintIndex = when (indexTypeCase) {
    SHOW_SOLUTION, EVERYTHING_REVEALED -> hintList.lastIndex
    NEXT_AVAILABLE_HINT_INDEX -> nextAvailableHintIndex - 1
    LATEST_REVEALED_HINT_INDEX -> latestRevealedHintIndex
    INDEXTYPE_NOT_SET, null -> -1
  }
  return hintIndex <= lastShownHintIndex
}

/**
 * Returns whether, based on a specified [HelpIndex], the solution has been viewed by the learner.
 */
fun HelpIndex.isSolutionRevealed(): Boolean = indexTypeCase == EVERYTHING_REVEALED
