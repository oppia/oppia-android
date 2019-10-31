package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

/**
 * Returns a new [StateItemViewModel] corresponding to this interaction with an initial, optional answer filled in,
 * optionally read-only (e.g. if the interaction is no longer accepting new answers), a receiver for answers if this
 * interaction pushes answers, the [Interaction] object corresponding to the interaction view, and the exploration ID.
 */
typealias InteractionViewModelFactory = (
  explorationId: String, interaction: Interaction, interactionAnswerReceiver: InteractionAnswerReceiver,
  existingAnswer: InteractionObject?, isReadOnly: Boolean
) -> StateItemViewModel
