package org.oppia.app.player.state

import org.oppia.app.model.InteractionObject

/** This interface helps to get PendingAnswer of [NumericInputInteractionView]. */
interface InteractionAnswerRetriever {
  fun getPendingAnswer(): InteractionObject
}