package org.oppia.android.domain.state

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AnswerGroup
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.State
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TERMINAL_INTERACTION_ID = "EndExploration"

/** Tests for [StateGraph], focusing on its checkpoint-counting behavior. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class StateGraphTest {
  @Inject
  lateinit var oppiaLogger: OppiaLogger

  // Mirrors ExplorationProgress.isTopStateTerminal: a state is terminal when its interaction is the
  // end-of-exploration interaction.
  private val isTerminalState: (State) -> Boolean = {
    it.interaction.id == TERMINAL_INTERACTION_ID
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testMinimumCheckpointCount_fromInitial_countsIntermediatesPlusTerminal() {
    // init -> s1(checkpoint) -> s2(checkpoint) -> s3 -> end. From init the remaining count is the
    // two future checkpoints plus the terminal (init is excluded as completed): 2 + 1 = 3.
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "s1"),
      createContinueState(name = "s1", nextStateName = "s2", isCheckpoint = true),
      createQuestionState(name = "s2", correctDestStateName = "s3", isCheckpoint = true),
      createContinueState(name = "s3", nextStateName = "end"),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isEqualTo(3)
  }

  @Test
  fun testMinimumCheckpointCount_continueCardInPath_isTraversedAndCounted() {
    // A Continue card has no answer groups and only moves forward through its default outcome. The
    // path init -> continueCard(checkpoint) -> end must still be found; from init the remaining
    // count is the future checkpoint plus the terminal: 1 + 1 = 2.
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "continueCard"),
      createContinueState(name = "continueCard", nextStateName = "end", isCheckpoint = true),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isEqualTo(2)
  }

  @Test
  fun testMinimumCheckpointCount_noCheckpointStates_returnsNull() {
    // No state is marked as a checkpoint, so this exploration doesn't support the progress indicator.
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "middle"),
      createContinueState(name = "middle", nextStateName = "end"),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isNull()
  }

  @Test
  fun testMinimumCheckpointCount_checkpointOnInitialAndTerminal_doesNotDoubleCount() {
    // The start is excluded (already completed) and the terminal is always counted once, so marking
    // them as checkpoints must not change the count. Only "middle" is a future checkpoint, so the
    // remaining count from init is 1 + 1 = 2 (not 3).
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "middle", isCheckpoint = true),
      createContinueState(name = "middle", nextStateName = "end", isCheckpoint = true),
      createTerminalState(name = "end", isCheckpoint = true)
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isEqualTo(2)
  }

  @Test
  fun testMinimumCheckpointCount_branchesReconverge_usesShortestPath() {
    // "init" has two correct answers: a direct one to "join" and a longer detour through "detour".
    // The shortest path init -> join -> end is used, so from init only "join" is a future
    // checkpoint: 1 + 1 = 2.
    val graph = createStateGraph(
      createBranchingQuestionState(
        name = "init",
        correctDestStateNames = listOf("detour", "join"),
        isCheckpoint = true
      ),
      createQuestionState(name = "detour", correctDestStateName = "join", isCheckpoint = true),
      createContinueState(name = "join", nextStateName = "end", isCheckpoint = true),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isEqualTo(2)
  }

  @Test
  fun testMinimumCheckpointCount_fromInitialState_excludesAlreadyCompletedInitialState() {
    // init -> s1(checkpoint) -> s2(checkpoint) -> end. The initial state is already counted by
    // StateDeck, so the remaining count only includes the two future checkpoints and the terminal.
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "s1", isCheckpoint = true),
      createContinueState(name = "s1", nextStateName = "s2", isCheckpoint = true),
      createContinueState(name = "s2", nextStateName = "end", isCheckpoint = true),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isEqualTo(3)
  }

  @Test
  fun testMinimumCheckpointCount_fromBranchWithExtraCheckpoints_countsBackToMainPath() {
    // The main path is init -> join(checkpoint) -> end, so the remaining count from init is 2
    // (join + terminal). If the learner has instead reached detour(checkpoint), the remaining path
    // detour -> review(checkpoint) -> join(checkpoint) -> end gives 3. This lets callers compute
    // completed-so-far + remaining instead of showing progress like 4/3 on branch paths.
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "join", isCheckpoint = true),
      createContinueState(name = "detour", nextStateName = "review", isCheckpoint = true),
      createContinueState(name = "review", nextStateName = "join", isCheckpoint = true),
      createContinueState(name = "join", nextStateName = "end", isCheckpoint = true),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isEqualTo(2)
    assertThat(graph.computeMinimumCheckpointCount("detour")).isEqualTo(3)
  }

  @Test
  fun testMinimumCheckpointCount_atTerminalState_isZero() {
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "end", isCheckpoint = true),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("end")).isEqualTo(0)
  }

  @Test
  fun testMinimumCheckpointCount_multipleTerminalStates_returnsNull() {
    // More than one terminal state means the exploration can't be reduced to a single start-to-end
    // path, so the count is null (and a warning is logged).
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "end1", isCheckpoint = true),
      createTerminalState(name = "end1"),
      createTerminalState(name = "end2")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isNull()
  }

  @Test
  fun testMinimumCheckpointCount_noCorrectPathToTerminal_returnsNull() {
    // The only correct answer from "init" loops to a dead-end that never reaches the terminal state,
    // so there's no main path and the count is null (and a warning is logged).
    val graph = createStateGraph(
      createQuestionState(name = "init", correctDestStateName = "stuck", isCheckpoint = true),
      createQuestionState(name = "stuck", correctDestStateName = "stuck"),
      createTerminalState(name = "end")
    )

    assertThat(graph.computeMinimumCheckpointCount("init")).isNull()
  }

  @Test
  fun testComputeAnswerOutcome_withRefresherExplorationId_setsRefresher() {
    val graph = createStateGraph(createTerminalState("current"))
    val currentState = createQuestionState(name = "current", correctDestStateName = "next")
    val outcome = Outcome.newBuilder()
      .setDestStateName("next")
      .setRefresherExplorationId("refresher_id")
      .build()

    val answerOutcome = graph.computeAnswerOutcomeForResult(currentState, outcome)

    assertThat(answerOutcome.refresherExplorationId).isEqualTo("refresher_id")
  }

  @Test
  fun testComputeAnswerOutcome_withMissingPrereqSkillId_setsMissingPrereq() {
    val graph = createStateGraph(createTerminalState("current"))
    val currentState = createQuestionState(name = "current", correctDestStateName = "next")
    val outcome = Outcome.newBuilder()
      .setDestStateName("next")
      .setMissingPrerequisiteSkillId("skill_id")
      .build()

    val answerOutcome = graph.computeAnswerOutcomeForResult(currentState, outcome)

    assertThat(answerOutcome.missingPrerequisiteSkillId).isEqualTo("skill_id")
  }

  @Test
  fun testComputeAnswerOutcome_destinationIsCurrentState_marksSameState() {
    val graph = createStateGraph(createTerminalState("current"))
    val currentState = createQuestionState(name = "current", correctDestStateName = "current")
    val outcome = Outcome.newBuilder().setDestStateName("current").build()

    val answerOutcome = graph.computeAnswerOutcomeForResult(currentState, outcome)

    assertThat(answerOutcome.sameState).isTrue()
  }

  @Test
  fun testComputeAnswerOutcome_destinationIsOtherState_setsStateName() {
    val graph = createStateGraph(createTerminalState("current"))
    val currentState = createQuestionState(name = "current", correctDestStateName = "next")
    val outcome = Outcome.newBuilder().setDestStateName("next").build()

    val answerOutcome = graph.computeAnswerOutcomeForResult(currentState, outcome)

    assertThat(answerOutcome.stateName).isEqualTo("next")
  }

  /** Builds a [StateGraph] from the given [states], keyed by state name. */
  private fun createStateGraph(vararg states: State): StateGraph {
    return StateGraph(
      states.associateBy { it.name },
      isTerminalState,
      oppiaLogger
    )
  }

  /**
   * Creates a question-style state (e.g. NumericInput) whose correct answer leads to
   * [correctDestStateName] and whose default ("wrong answer") outcome loops back to itself.
   */
  private fun createQuestionState(
    name: String,
    correctDestStateName: String,
    isCheckpoint: Boolean = false
  ): State = createBranchingQuestionState(name, listOf(correctDestStateName), isCheckpoint)

  /**
   * Creates a question-style state with one correct answer group per entry in
   * [correctDestStateNames], so a single state can branch into multiple correct destinations.
   */
  private fun createBranchingQuestionState(
    name: String,
    correctDestStateNames: List<String>,
    isCheckpoint: Boolean = false
  ): State {
    val interaction = Interaction.newBuilder().setId("NumericInput")
    correctDestStateNames.forEach { destStateName ->
      interaction.addAnswerGroups(
        AnswerGroup.newBuilder().setOutcome(createOutcome(destStateName, labelledAsCorrect = true))
      )
    }
    // The default outcome represents a wrong answer, which keeps the learner on the same state.
    interaction.setDefaultOutcome(createOutcome(name, labelledAsCorrect = false))
    return createState(name, isCheckpoint, interaction)
  }

  /**
   * Creates a Continue state, which has no answer groups and only moves forward through its default
   * outcome (to [nextStateName]).
   */
  private fun createContinueState(
    name: String,
    nextStateName: String,
    isCheckpoint: Boolean = false
  ): State {
    val interaction = Interaction.newBuilder()
      .setId("Continue")
      .setDefaultOutcome(createOutcome(nextStateName, labelledAsCorrect = false))
    return createState(name, isCheckpoint, interaction)
  }

  /** Creates a terminal state (the end of an exploration), which has no outgoing outcomes. */
  private fun createTerminalState(name: String, isCheckpoint: Boolean = false): State {
    return createState(name, isCheckpoint, Interaction.newBuilder().setId(TERMINAL_INTERACTION_ID))
  }

  private fun createState(
    name: String,
    isCheckpoint: Boolean,
    interaction: Interaction.Builder
  ): State {
    return State.newBuilder()
      .setName(name)
      .setIsCheckpoint(isCheckpoint)
      .setInteraction(interaction)
      .build()
  }

  private fun createOutcome(destStateName: String, labelledAsCorrect: Boolean): Outcome {
    return Outcome.newBuilder()
      .setDestStateName(destStateName)
      .setLabelledAsCorrect(labelledAsCorrect)
      .build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerStateGraphTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(stateGraphTest: StateGraphTest)
  }
}
