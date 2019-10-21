package org.oppia.app.player.state.listener

/**
 * This interface helps to check if the learner has selected some option,
 * or if the learner has started typing in edit-text interactions.
 */
interface InputInteractionTextListener {
  fun hasLearnerStartedAnswering(inputInteractionStarted: Boolean)
}
