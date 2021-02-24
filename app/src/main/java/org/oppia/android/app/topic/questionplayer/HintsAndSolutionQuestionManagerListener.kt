package org.oppia.android.app.topic.questionplayer

import org.oppia.android.app.model.State

/** Listener for fetching current Question state data. */
interface HintsAndSolutionQuestionManagerListener {

  fun onQuestionStateLoaded(state: State)
}
