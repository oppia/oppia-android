package org.oppia.app.customview.inputInteractionView

import org.oppia.app.model.InteractionObject

/** This interface helps to get PendingAnswer of [NumberInputInteractionView]. */
interface InteractionAnswerRetriever {
  fun getPendingAnswer(): InteractionObject
}
