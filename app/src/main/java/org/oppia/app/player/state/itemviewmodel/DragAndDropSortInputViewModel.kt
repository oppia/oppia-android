package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.Interaction
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

class DragAndDropSortInputViewModel(
  explorationId: String,
  interaction: Interaction,
  interactionAnswerReceiver: InteractionAnswerReceiver,
  interactionAnswerErrorReceiver: InteractionAnswerErrorReceiver
) : StateItemViewModel(ViewType.DRAG_DROP_SORT_INTERACTION), InteractionAnswerHandler {
  private val allowMultipleItemsInSamePosition: Boolean by lazy {
    interaction.customizationArgsMap["allowMultipleItemsInSamePosition"]?.boolValue ?: false
  }
  private val choiceStrings: List<String> by lazy {
    interaction.customizationArgsMap["choices"]?.setOfHtmlString?.htmlList ?: listOf()
  }
}
