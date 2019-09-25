package org.oppia.domain.exploration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.InteractionObject
import org.oppia.util.data.AsyncResult

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through an exploration. Note that
 * this controller only supports one active exploration at a time.
 *
 * The current exploration session is started via the exploration data controller.
 */
class ExplorationProgressController {
  // TODO(#180): Add support for hints.
  // TODO(#179): Add support for parameters.
  // TODO(#181): Add support for solutions.
  // TODO(#182): Add support for refresher explorations.

  /**
   * Submits an answer to the current state and returns how the UI should respond to this answer. The returned
   * [LiveData] will only have at most two results posted: a pending result, and then a completed success/failure
   * result. Failures in this case represent a failure of the app (possibly due to networking conditions). The app
   * should report this error in a consumable way to the user so that they may take action on it. No additional values
   * will be reported to the [LiveData]. Each call to this method returns a new, distinct, [LiveData] object that must
   * be observed.
   *
   * If the app undergoes a configuration change, calling code should rely on the [LiveData] from [getCurrentState] to
   * know whether a current answer is pending. That [LiveData] will have its state changed to pending during answer
   * submission and until answer resolution.
   *
   * Submitting an answer may result in the learner staying in the current state, moving to a new state in the
   * exploration, being shown a concept card, or being navigated to another exploration altogether. Note that once a
   * correct answer is processed, the current state reported to [getCurrentState] will change from a pending state to a
   * completed state since the learner completed that card. The learner can then proceed from the current completed
   * state to the next pending state using [moveToNextState].
   */
  fun submitAnswer(answer: InteractionObject): LiveData<AsyncResult<AnswerOutcome>> {
    return MutableLiveData(AsyncResult.pending())
  }

  /**
   * Navigates to the previous state in the stack. If the learner is currently on the initial state, this method will
   * throw an exception. Calling code is responsible to make sure that this method is not called when it's not possible
   * to navigate to a previous card.
   */
  fun moveToPreviousState() {}

  /**
   * Navigates to the next state in the graph. This method is only valid if the current [EphemeralState] reported by
   * [getCurrentState] is a completed state. If the current state is pending or terminal, this function will throw an
   * exception since it's not possible for the learner to actually navigate forward. Calling code is responsible for
   * ensuring this method is only called when it's possible to navigate forward.
   *
   * Note that if the current state is a pending state, the user needs to submit a correct answer that routes to a later
   * state via [submitAnswer] in order for the current state to change to a completed state.
   */
  fun moveToNextState() {}

  /**
   * Returns a [LiveData] monitoring the current [EphemeralState] the learner is currently viewing. If this state
   * corresponds to a a terminal state, then the learner has completed the exploration. Note that [moveToPreviousState]
   * and [moveToNextState] will automatically update observers of this live data when the next state is navigated to.
   *
   * Note that the returned [LiveData] is always the same object no matter when this method is called, except
   * potentially when a new exploration is started.
   *
   * This [LiveData] may initially be pending while the exploration object is loaded. It may also switch from a
   * completed to a pending result during transient operations like submitting an answer via [submitAnswer]. Calling
   * code should be made resilient to this by caching the current state object to display since it may disappear
   * temporarily during answer submission. Calling code should persist this state object across configuration changes if
   * needed since it cannot rely on this [LiveData] for immediate state reconstitution after configuration changes.
   */
  fun getCurrentState(): LiveData<AsyncResult<EphemeralState>> {
    return MutableLiveData(AsyncResult.pending())
  }
}
