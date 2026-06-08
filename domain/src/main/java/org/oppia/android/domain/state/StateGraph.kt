package org.oppia.android.domain.state

import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.State
import org.oppia.android.domain.oppialogger.OppiaLogger

private const val STATE_GRAPH_LOG_TAG = "StateGraph"

/**
 * Graph that provides lookup access for an exploration's [State]s, processing for the outcome of a
 * submitted learner answer, and the count of checkpoints along the exploration's main path.
 */
class StateGraph constructor(
  private var stateGraph: Map<String, State>,
  private val initialStateName: String,
  private val isTerminalState: (State) -> Boolean,
  private val oppiaLogger: OppiaLogger
) {
  /**
   * The total number of checkpoints in the current exploration, or null if it doesn't support the
   * lesson progress indicator (see [computeCheckpointCount] for when that's the case). The count
   * always includes the initial and terminal states.
   *
   * This is lazily computed and memoized since it's needed throughout a play session but never
   * changes for a given exploration.
   */
  val checkpointCount: Int? by lazy { computeCheckpointCount() }

  /** Resets this graph to the new graph represented by the specified [Map]. */
  fun reset(stateGraph: Map<String, State>) {
    this.stateGraph = stateGraph
  }

  /** Returns the [State] corresponding to the specified name. */
  fun getState(stateName: String): State {
    return stateGraph.getValue(stateName)
  }

  /** Returns an [AnswerOutcome] based on the current state and resulting [Outcome] from the learner's answer. */
  fun computeAnswerOutcomeForResult(currentState: State, outcome: Outcome): AnswerOutcome {
    val answerOutcomeBuilder = AnswerOutcome.newBuilder()
      .setFeedback(outcome.feedback)
      .setLabelledAsCorrectAnswer(outcome.labelledAsCorrect)
      .setState(currentState)
      .setIsDefaultOutcome(outcome == currentState.interaction.defaultOutcome)
    when {
      outcome.refresherExplorationId.isNotEmpty() ->
        answerOutcomeBuilder.refresherExplorationId = outcome.refresherExplorationId
      outcome.missingPrerequisiteSkillId.isNotEmpty() ->
        answerOutcomeBuilder.missingPrerequisiteSkillId = outcome.missingPrerequisiteSkillId
      outcome.destStateName == currentState.name -> answerOutcomeBuilder.sameState = true
      else -> answerOutcomeBuilder.stateName = outcome.destStateName
    }
    return answerOutcomeBuilder.build()
  }

  /**
   * Returns the number of checkpoints along the exploration's main path, or null if it doesn't
   * support checkpoint-based progress: it has no checkpoint states, doesn't have exactly one
   * terminal state, or has no correct-answer path to that terminal state.
   */
  private fun computeCheckpointCount(): Int? {
    // No checkpoint states means the indicator isn't supported. This is the common, expected case,
    // so it returns quietly instead of logging a warning.
    if (stateGraph.values.none { it.isCheckpoint }) return null

    // Requiring a single terminal state reduces this to a pathfind between two fixed points.
    val terminalStateNames = stateGraph.values.filter(isTerminalState).map { it.name }
    if (terminalStateNames.size != 1) {
      oppiaLogger.w(
        STATE_GRAPH_LOG_TAG,
        "Cannot compute checkpoint count: expected exactly one terminal state but found" +
          " ${terminalStateNames.size}."
      )
      return null
    }

    val mainPath = findShortestCorrectPath(initialStateName, terminalStateNames.single())
    if (mainPath == null) {
      oppiaLogger.w(
        STATE_GRAPH_LOG_TAG,
        "Cannot compute checkpoint count: no correct-answer path from the initial state to the" +
          " terminal state."
      )
      return null
    }

    // Count the checkpoints between the endpoints, then add 2 for the initial and terminal states
    // (always checkpoints). Excluding the endpoints first avoids double-counting a marked one.
    val intermediateCheckpointCount = mainPath
      .drop(1)
      .dropLast(1)
      .count { stateName -> stateGraph.getValue(stateName).isCheckpoint }
    return intermediateCheckpointCount + 2
  }

  /**
   * Returns the shortest path of state names (inclusive of both endpoints) from [startStateName] to
   * [destinationStateName] along the main forward route, or null if no such path exists. This is a
   * breadth-first search, which finds a shortest path on the unweighted state graph.
   */
  private fun findShortestCorrectPath(
    startStateName: String,
    destinationStateName: String
  ): List<String>? {
    // Maps each visited state to the one it was first reached from (start maps to null). This is
    // also the visited set, and is used to rebuild the path at the end.
    val predecessors = mutableMapOf<String, String?>(startStateName to null)
    val statesToProcess = ArrayDeque<String>()
    statesToProcess.add(startStateName)
    while (statesToProcess.isNotEmpty()) {
      val currentStateName = statesToProcess.removeAt(0)
      if (currentStateName == destinationStateName) {
        return reconstructPath(predecessors, destinationStateName)
      }
      for (nextStateName in getForwardDestinations(currentStateName)) {
        if (!predecessors.containsKey(nextStateName)) {
          predecessors[nextStateName] = currentStateName
          statesToProcess.add(nextStateName)
        }
      }
    }
    return null
  }

  /**
   * Returns the next state names reachable from [stateName] along the main path: the destinations of
   * its correct answer groups, or its default outcome's destination for cards that have no correct
   * answer group (such as Continue).
   */
  private fun getForwardDestinations(stateName: String): List<String> {
    val interaction = stateGraph.getValue(stateName).interaction
    val correctAnswerDestinations = interaction.answerGroupsList
      .map { it.outcome }
      .filter { it.labelledAsCorrect && it.destStateName.isNotEmpty() }
      .map { it.destStateName }
    // Only fall back to the default outcome when no answer group is marked correct, so a question
    // card's "wrong answer" default (which usually loops back to itself) isn't mistaken for the path.
    return correctAnswerDestinations.ifEmpty {
      val defaultDestination = interaction.defaultOutcome.destStateName
      if (defaultDestination.isNotEmpty() && defaultDestination != stateName) {
        listOf(defaultDestination)
      } else {
        emptyList()
      }
    }.distinct()
  }

  /**
   * Rebuilds the path to [destinationStateName] by walking [predecessors] backwards from it,
   * returning the state names ordered from start to destination.
   */
  private fun reconstructPath(
    predecessors: Map<String, String?>,
    destinationStateName: String
  ): List<String> {
    val path = mutableListOf<String>()
    var currentStateName: String? = destinationStateName
    while (currentStateName != null) {
      // Prepend so the backward walk still yields a start-to-destination ordering.
      path.add(0, currentStateName)
      currentStateName = predecessors.getValue(currentStateName)
    }
    return path
  }
}
