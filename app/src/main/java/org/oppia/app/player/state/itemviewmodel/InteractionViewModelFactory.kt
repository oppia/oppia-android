package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.Interaction
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

/**
 * Returns a new [StateItemViewModel] corresponding to this interaction with a receiver for answers if this interaction
 * pushes answers, the [Interaction] object corresponding to the interaction view, and the exploration ID.
 */
typealias InteractionViewModelFactory = (
  explorationId: String, interaction: Interaction, interactionAnswerReceiver: InteractionAnswerReceiver, interactionAnswerHandler: InteractionAnswerErrorReceiver
) -> StateItemViewModel
