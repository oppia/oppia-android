package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.Interaction
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

/**
 * Returns a new [StateItemViewModel] corresponding to this interaction with the exploration's ID, the [Interaction]
 * object corresponding to the interaction view, a receiver for answers if this interaction pushes answers, and whether
 * there's a previous button enabled (only relevant for navigation-based interactions).
 */
typealias InteractionViewModelFactory = (
  explorationId: String,
  interaction: Interaction,
  interactionAnswerReceiver: InteractionAnswerReceiver,
  interactionAnswerErrorReceiver: InteractionAnswerErrorReceiver,
  hasPreviousButton: Boolean
) -> StateItemViewModel
