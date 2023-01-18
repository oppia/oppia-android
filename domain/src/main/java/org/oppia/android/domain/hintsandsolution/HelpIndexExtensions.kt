package org.oppia.android.domain.hintsandsolution

import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.EVERYTHING_REVEALED
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.SHOW_SOLUTION
import org.oppia.android.app.model.Hint

/**
 * Returns whether the specified [hintIndex] relative to the provided list of [Hint]s has been seen
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
 * Returns, based on the specified [HelpIndex], an updated version of [hintList] that contains
 * exactly the hints currently revealed or available to be revealed.
 */
fun HelpIndex.dropLastUnavailable(hintList: List<Hint>): List<Hint> {
  val nextAvailableHintIndex = when (indexTypeCase) {
    NEXT_AVAILABLE_HINT_INDEX -> nextAvailableHintIndex
    LATEST_REVEALED_HINT_INDEX -> latestRevealedHintIndex
    SHOW_SOLUTION, EVERYTHING_REVEALED -> hintList.lastIndex // All hints are available.
    INDEXTYPE_NOT_SET, null -> -1 // Something failed, so assume no hints are visible.
  }
  return hintList.take(nextAvailableHintIndex + 1)
}

/**
 * Returns whether, based on the specified [HelpIndex], the solution is available to be viewed
 * (either as a new reveal or if it's already available, per [isSolutionRevealed]).
 */
fun HelpIndex.isSolutionAvailable(): Boolean =
  indexTypeCase == SHOW_SOLUTION || isSolutionRevealed()

/**
 * Returns whether, based on a specified [HelpIndex], the solution has been viewed by the learner.
 */
fun HelpIndex.isSolutionRevealed(): Boolean = indexTypeCase == EVERYTHING_REVEALED
