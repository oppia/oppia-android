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
  // Whether any state in this graph is a checkpoint. This is a graph-level property, so it's computed
  // once and cached: explorations without checkpoints are the common case and are queried on every
  // navigation event, so re-scanning every state each time would be wasteful. Reset with the graph.
  private var graphHasCheckpointState: Boolean? = null

  /** Resets this graph to the new graph represented by the specified [Map]. */
  fun reset(stateGraph: Map<String, State>) {
    this.stateGraph = stateGraph
    remainingCheckpointCountCache.clear()
    graphHasCheckpointState = null
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
   * Returns the minimum number of checkpoints still remaining from [startStateName] to the terminal
   * state, or null if this exploration doesn't support checkpoint progress from that state. The
   * count is the *minimum* because it follows the shortest correct path; straying onto a longer
   * branch can pass through extra checkpoints, so callers should treat this as a lower bound.
   *
   * The start state itself is excluded because callers combine this with the learner's completed
   * checkpoint count at their current deck position. Any future checkpoint states and the terminal
   * state are included, so a learner at the terminal state has zero remaining checkpoints.
   *
   * Caching is a dynamic-programming optimization. Because a suffix of a shortest path is itself a
   * shortest path, finding the path from [startStateName] simultaneously determines the remaining
   * count of every state along it, so the first cache miss caches all of them in a single backward
   * pass. Callers query this once per navigation event as the learner advances along that same
   * path, so every later query is a cache hit and the search never runs again. We deliberately stop
   * there and never seed one path's walk from another path's cached suffix: equal-length shortest
   * paths can pass through different checkpoints, so reusing a cached suffix could make the result
   * depend on the order queries arrive in. Walking the concrete path each time keeps the count tied
   * to the path actually taken.
   */
  fun computeMinimumCheckpointCount(startStateName: String): Int? {
    // Early return on a cache hit. This is the only entry point, so the worker below runs only on a
    // genuine miss: either a direct first query for this state, or the first query for its path.
    if (remainingCheckpointCountCache.containsKey(startStateName)) {
      return remainingCheckpointCountCache[startStateName]
    }
    return computeAndCacheCheckpointCountsFrom(startStateName)
  }

  /**
   * Computes the minimum remaining checkpoint count from [startStateName], caching it and the
   * count for every other state on the discovered path. Caches and returns null when checkpoint
   * progress can't be computed, so the unsupported-exploration checks aren't repeated on later
   * queries for the same start state.
   */
  private fun computeAndCacheCheckpointCountsFrom(startStateName: String): Int? {
    val mainPath = findMainPath(startStateName)
    if (mainPath == null) {
      remainingCheckpointCountCache[startStateName] = null
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
   * Returns the shortest correct-answer path from [startStateName] to the single terminal state, or
   * null when this exploration doesn't support checkpoint progress: it has no checkpoint
   * states (the common, expected case, returned quietly), it doesn't have exactly one terminal
   * state, or no correct-answer path reaches the terminal (the latter two are logged).
   */
  private fun findMainPath(startStateName: String): List<String>? {
    if (!hasAnyCheckpointState()) return null

    // Requiring a single terminal state reduces this to a pathfind between two fixed points.
    val terminalStateNames = stateGraph.values.filter(isTerminalState).map { it.name }
    if (terminalStateNames.size != 1) {
      oppiaLogger.d(
        STATE_GRAPH_LOG_TAG,
        "Cannot compute checkpoint count: expected exactly one terminal state but found" +
          " ${terminalStateNames.size}."
      )
      return null
    }

    val mainPath = findShortestCorrectPath(startStateName, terminalStateNames.single())
    if (mainPath == null) {
      oppiaLogger.d(
        STATE_GRAPH_LOG_TAG,
        "Cannot compute checkpoint count: no correct-answer path from $startStateName to the" +
          " terminal state."
      )
    }
    return mainPath
  }

  /**
   * Returns whether any state in this graph is a checkpoint, computing it once and caching the
   * result because it's a graph-level property that's re-queried on every navigation event.
   */
  private fun hasAnyCheckpointState(): Boolean {
    val cached = graphHasCheckpointState
    if (cached != null) return cached
    val hasCheckpoint = stateGraph.values.any { it.isCheckpoint }
    graphHasCheckpointState = hasCheckpoint
    return hasCheckpoint
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
    // A correct outcome's destStateName is the empty-string proto default when the exploration data
    // doesn't set one; skip those so an empty name can't enter the search and crash the later
    // getValue lookup. Valid lessons always give a correct answer a real destination.
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
