package org.oppia.app.customview.inputInteractionView

import org.oppia.app.model.InteractionObject

/** This interface that helps to get answer from input interaction views. */
interface InteractionAnswerRetriever {
  fun getPendingAnswer(): InteractionObject
}
