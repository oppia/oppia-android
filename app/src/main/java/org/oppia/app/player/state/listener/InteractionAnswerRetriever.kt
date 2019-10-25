package org.oppia.app.player.state.listener

import org.oppia.app.model.InteractionObject

/** This interface helps to get pending answer of any  learner interaction. */
interface InteractionAnswerRetriever {
  fun getPendingAnswer(): InteractionObject
}
