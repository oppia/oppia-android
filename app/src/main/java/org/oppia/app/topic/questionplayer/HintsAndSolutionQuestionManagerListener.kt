package org.oppia.app.topic.questionplayer

import org.oppia.app.model.State

/** Listener for fetching current Question state data. */
interface HintsAndSolutionQuestionManagerListener {

  fun onQuestionStateLoaded(state: State)
}
