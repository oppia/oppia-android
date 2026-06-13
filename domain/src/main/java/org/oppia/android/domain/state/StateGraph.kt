package org.oppia.android.domain.state

import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.State
import org.oppia.android.domain.oppialogger.OppiaLogger

private const val STATE_GRAPH_LOG_TAG = "StateGraph"

/**
 * Graph that provides lookup access for an exploration's [State]s, processing for the outcome of a
 * submitted learner answer, and the remaining checkpoint count toward the terminal state.
 */
class StateGraph constructor(
  private var stateGraph: Map<String, State>,
  private val isTerminalState: (State) -> Boolean,
  private val oppiaLogger: OppiaLogger
) {
  private val remainingCheckpointCountCache = mutableMapOf<String, Int?>()

  /** Resets this graph to the new graph represented by the specified [Map]. */
  fun reset(stateGraph: Map<String, State>) {
    this.stateGraph = stateGraph
    remainingCheckpointCountCache.clear()
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
   * Returns the number of checkpoints still remaining from [startStateName] to the terminal state
   * along the shortest correct path, or null if this exploration doesn't support checkpoint
   * progress from that state.
   *
   * The start state itself is excluded because callers combine this with the learner's completed
   * checkpoint count at their current deck position. Any future checkpoint states and the terminal
   * state are included, so a learner at the terminal state has zero remaining checkpoints.
   *
   * When a path is found, this also caches the remaining count for the other states on that path.
   * This avoids running the same search again as the learner moves through those states.
   */
  fun computeRemainingCheckpointCount(startStateName: String): Int? {
    if (remainingCheckpointCountCache.containsKey(startStateName)) {
      return remainingCheckpointCountCache[startStateName]
    }
    // On success the path states (including the start) are already cached below; on a null result
    // the start is cached here so the unsupported-exploration checks aren't repeated every query.
    return computeAndCacheCheckpointCountsFrom(startStateName).also { remainingCheckpointCount ->
      remainingCheckpointCountCache[startStateName] = remainingCheckpointCount
    }
  }

  /**
   * Computes the remaining checkpoint count from [startStateName]. It also caches the remaining
   * count for each state on that path. Returns null when checkpoint progress can't be computed.
   */
  private fun computeAndCacheCheckpointCountsFrom(startStateName: String): Int? {
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

    val mainPath = findShortestCorrectPath(startStateName, terminalStateNames.single())
    if (mainPath == null) {
      oppiaLogger.w(
        STATE_GRAPH_LOG_TAG,
        "Cannot compute checkpoint count: no correct-answer path from $startStateName to the" +
          " terminal state."
      )
      return null
    }

    // Walk backward from the terminal so each state gets the number of checkpoints after it. The
    // terminal always counts as the final checkpoint, even though lesson data doesn't mark it.
    var remainingCount = 0
    for (index in mainPath.indices.reversed()) {
      val stateName = mainPath[index]
      remainingCheckpointCountCache[stateName] = remainingCount
      val countsAsCheckpoint =
        index == mainPath.lastIndex || stateGraph.getValue(stateName).isCheckpoint
      if (countsAsCheckpoint) remainingCount++
    }
    return remainingCheckpointCountCache.getValue(startStateName)
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
