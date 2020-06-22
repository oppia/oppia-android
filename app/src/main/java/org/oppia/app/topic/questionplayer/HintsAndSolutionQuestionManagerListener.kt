package org.oppia.app.topic.questionplayer

import org.oppia.app.model.State

interface HintsAndSolutionQuestionManagerListener {

  fun onQuestionStateLoaded(
    state: State,
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  )
}
