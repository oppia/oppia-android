package org.oppia.app.customview.interaction

import org.oppia.app.model.InteractionObject

/** This interface helps to get answer from input interaction views as [InteractionObject]. */
interface InteractionAnswerRetriever {
  fun getPendingAnswer(): InteractionObject
}
