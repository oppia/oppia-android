package org.oppia.app.customview.inputInteractionView

import org.oppia.app.model.InteractionObject

interface InteractionAnswerRetriever {
  fun getPendingAnswer(): InteractionObject
}