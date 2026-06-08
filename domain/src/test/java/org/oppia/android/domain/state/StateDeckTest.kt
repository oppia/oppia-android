package org.oppia.android.domain.state

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.State
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.UserAnswer
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

private const val TERMINAL_INTERACTION_ID = "EndExploration"

/** Tests for [StateDeck], focusing on its completed-checkpoint counting for lesson progress. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class StateDeckTest {
  // Mirrors ExplorationProgress.isTopStateTerminal: a state is terminal when its interaction is the
  // end-of-exploration interaction.
  private val isTerminalState: (State) -> Boolean = {
    it.interaction.id == TERMINAL_INTERACTION_ID
  }

  @Test
  fun testCompletedCheckpointCount_atInitialState_isOne() {
    // The learner hasn't moved yet, but the initial state always counts as the first checkpoint.
    val deck = StateDeck(createState("init"), isTerminalState)

    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(1)
  }

  @Test
  fun testCompletedCheckpointCount_reachedCheckpointState_isCounted() {
    // init -> s1(checkpoint): the initial state plus one more reached checkpoint gives 2.
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1", isCheckpoint = true))

    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(2)
  }

  @Test
  fun testCompletedCheckpointCount_reachedNonCheckpointState_isNotCounted() {
    // init -> s1(not a checkpoint): only the initial state counts, so the count stays at 1.
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))

    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(1)
  }

  @Test
  fun testCompletedCheckpointCount_initialStateFlaggedCheckpoint_isNotDoubleCounted() {
    // The initial state already counts once; an explicit card_is_checkpoint flag must not add to it.
    val deck = StateDeck(createState("init", isCheckpoint = true), isTerminalState)

    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(1)
  }

  @Test
  fun testCompletedCheckpointCount_atTerminalState_countsEveryCheckpoint() {
    // init -> s1(checkpoint) -> s2 -> s3(checkpoint) -> end. The initial and terminal states always
    // count, plus the two flagged states in between, giving 4.
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1", isCheckpoint = true))
    deck.advanceTo(createState("s2"))
    deck.advanceTo(createState("s3", isCheckpoint = true))
    deck.advanceTo(createTerminalState("end"))

    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(4)
  }

  @Test
  fun testCompletedCheckpointCount_terminalStateFlaggedCheckpoint_isNotDoubleCounted() {
    // The terminal state already counts once; an explicit card_is_checkpoint flag must not add to it.
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1", isCheckpoint = true))
    deck.advanceTo(createTerminalState("end", isCheckpoint = true))

    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(3)
  }

  @Test
  fun testCompletedCheckpointCount_navigatedBackward_countsDown() {
    // After reaching s1(checkpoint) the count is 2; navigating back to the initial state drops it
    // to 1, matching the learner's earlier position.
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1", isCheckpoint = true))
    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(2)

    deck.navigateToPreviousState()

    assertThat(deck.computeCompletedCheckpointCount()).isEqualTo(1)
  }

  @Test
  fun testGetCurrentEphemeralState_nullTotalCount_hasNoCheckpointProgress() {
    val deck = StateDeck(createState("init"), isTerminalState)

    val ephemeralState = deck.getCurrentEphemeralStateWithoutProgress()

    assertThat(ephemeralState.hasCheckpointProgress()).isFalse()
  }

  @Test
  fun testGetCurrentEphemeralState_withTotalCount_setsCheckpointProgress() {
    // At the initial state the completed count is 1 out of the provided total of 4.
    val deck = StateDeck(createState("init"), isTerminalState)

    val ephemeralState = deck.getCurrentEphemeralState(
      HelpIndex.getDefaultInstance(),
      timestamp = 0,
      isContinueButtonAnimationSeen = true,
      totalCheckpointCount = 4
    )

    assertThat(ephemeralState.checkpointProgress.completedCheckpointCount).isEqualTo(1)
    assertThat(ephemeralState.checkpointProgress.totalCheckpointCount).isEqualTo(4)
  }

  @Test
  fun testGetCurrentEphemeralState_afterAdvancing_reflectsCompletedCount() {
    // After reaching s1(checkpoint) the completed count carried in the progress updates to 2.
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1", isCheckpoint = true))

    val ephemeralState = deck.getCurrentEphemeralState(
      HelpIndex.getDefaultInstance(),
      timestamp = 0,
      isContinueButtonAnimationSeen = true,
      totalCheckpointCount = 4
    )

    assertThat(ephemeralState.checkpointProgress.completedCheckpointCount).isEqualTo(2)
  }

  @Test
  fun testGetCurrentEphemeralState_atPendingState_isPendingState() {
    val deck = StateDeck(createState("init"), isTerminalState)

    val ephemeralState = deck.getCurrentEphemeralStateWithoutProgress()

    assertThat(ephemeralState.stateTypeCase).isEqualTo(EphemeralState.StateTypeCase.PENDING_STATE)
  }

  @Test
  fun testGetCurrentEphemeralState_atPreviousState_isCompletedState() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))
    deck.navigateToPreviousState()

    val ephemeralState = deck.getCurrentEphemeralStateWithoutProgress()

    assertThat(ephemeralState.stateTypeCase).isEqualTo(EphemeralState.StateTypeCase.COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentEphemeralState_atTerminalState_isTerminalState() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createTerminalState("end"))

    val ephemeralState = deck.getCurrentEphemeralStateWithoutProgress()

    assertThat(ephemeralState.stateTypeCase).isEqualTo(EphemeralState.StateTypeCase.TERMINAL_STATE)
  }

  @Test
  fun testNavigateToNextState_atTopOfDeck_throwsException() {
    val deck = StateDeck(createState("init"), isTerminalState)

    val exception = assertThrows(IllegalStateException::class.java) { deck.navigateToNextState() }

    assertThat(exception).hasMessageThat().contains("Cannot navigate to next state")
  }

  @Test
  fun testNavigateToPreviousState_atInitialState_throwsException() {
    val deck = StateDeck(createState("init"), isTerminalState)

    val exception =
      assertThrows(IllegalStateException::class.java) { deck.navigateToPreviousState() }

    assertThat(exception).hasMessageThat().contains("Cannot navigate to previous state")
  }

  @Test
  fun testSubmitAnswer_atTerminalState_throwsException() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createTerminalState("end"))

    val exception = assertThrows(IllegalStateException::class.java) {
      deck.submitAnswer(
        UserAnswer.getDefaultInstance(),
        SubtitledHtml.getDefaultInstance(),
        isCorrectAnswer = true
      )
    }

    assertThat(exception).hasMessageThat().contains("Cannot submit an answer to a terminal state")
  }

  @Test
  fun testPushState_withoutSubmittingAnswer_throwsException() {
    val deck = StateDeck(createState("init"), isTerminalState)

    val exception = assertThrows(IllegalStateException::class.java) {
      deck.pushState(
        createState("s1"),
        prohibitSameStateName = true,
        timestamp = 0,
        isContinueButtonAnimationSeen = true
      )
    }

    assertThat(exception).hasMessageThat().contains("Cannot push another state without an answer")
  }

  @Test
  fun testGetViewedStateCount_afterAdvancingTwice_isTwo() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))
    deck.advanceTo(createState("s2"))

    assertThat(deck.getViewedStateCount()).isEqualTo(2)
  }

  @Test
  fun testGetTopStateIndex_afterAdvancing_tracksPosition() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))

    assertThat(deck.getTopStateIndex()).isEqualTo(1)
  }

  @Test
  fun testGetPendingTopState_afterAdvancing_isLatestState() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))

    assertThat(deck.getPendingTopState().name).isEqualTo("s1")
  }

  @Test
  fun testGetCurrentState_afterNavigatingBack_isPreviousState() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))
    deck.navigateToPreviousState()

    assertThat(deck.getCurrentState().name).isEqualTo("init")
  }

  @Test
  fun testResetDeck_afterAdvancing_clearsProgressToNewInitialState() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))

    deck.resetDeck(createState("fresh"))

    assertThat(deck.getViewedStateCount()).isEqualTo(0)
    assertThat(deck.getTopStateIndex()).isEqualTo(0)
    assertThat(deck.getPendingTopState().name).isEqualTo("fresh")
  }

  @Test
  fun testResumeDeck_restoresProvidedProgress() {
    val deck = StateDeck(createState("init"), isTerminalState)
    val previousStates = listOf(
      EphemeralState.newBuilder().setState(createState("init")).build()
    )

    deck.resumeDeck(
      pendingTopState = createState("s1"),
      previousStates = previousStates,
      currentDialogInteractions = listOf(),
      stateIndex = 1
    )

    assertThat(deck.getViewedStateCount()).isEqualTo(1)
    assertThat(deck.getTopStateIndex()).isEqualTo(1)
    assertThat(deck.getPendingTopState().name).isEqualTo("s1")
  }

  @Test
  fun testCreateExplorationCheckpoint_capturesDeckState() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))

    val checkpoint = deck.createExplorationCheckpoint(
      explorationVersion = 5,
      explorationTitle = "Fractions",
      timestamp = 123,
      helpIndex = HelpIndex.getDefaultInstance()
    )

    assertThat(checkpoint.pendingStateName).isEqualTo("s1")
    assertThat(checkpoint.stateIndex).isEqualTo(1)
    assertThat(checkpoint.explorationVersion).isEqualTo(5)
    assertThat(checkpoint.explorationTitle).isEqualTo("Fractions")
    assertThat(checkpoint.timestampOfFirstCheckpoint).isEqualTo(123)
    assertThat(checkpoint.completedStatesInCheckpointCount).isEqualTo(1)
  }

  @Test
  fun testAddFlashbackState_marksLastInteractionAsOffered() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.submitAnswer(
      UserAnswer.getDefaultInstance(),
      SubtitledHtml.getDefaultInstance(),
      isCorrectAnswer = false
    )
    assertThat(deck.wasFlashbackPreviouslyOffered()).isFalse()

    deck.addFlashbackState("revisit_state")

    assertThat(deck.wasFlashbackPreviouslyOffered()).isTrue()
  }

  @Test
  fun testSetFlashbackIsViewed_marksLastInteractionAsViewed() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.submitAnswer(
      UserAnswer.getDefaultInstance(),
      SubtitledHtml.getDefaultInstance(),
      isCorrectAnswer = false
    )
    assertThat(deck.isFlashbackViewed()).isFalse()

    deck.setFlashbackIsViewed()

    assertThat(deck.isFlashbackViewed()).isTrue()
  }

  @Test
  fun testHasFlashbackState_withMatchingLinkedSkill_isTrueAndReturnsStateName() {
    val deck = StateDeck(createState("init", linkedSkillId = "skill_1"), isTerminalState)
    deck.advanceTo(createState("s1"))

    assertThat(deck.hasFlashbackState("skill_1")).isTrue()
    assertThat(deck.hasFlashbackState("other_skill")).isFalse()
    assertThat(deck.getFlashbackStateName("skill_1")).isEqualTo("init")
  }

  @Test
  fun testGetFlashbackEphemeralState_whenStateExists_returnsThatState() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))

    val flashbackState = deck.getFlashbackEphemeralState("init")

    assertThat(flashbackState.state.name).isEqualTo("init")
  }

  @Test
  fun testGetFlashbackEphemeralState_whenStateMissing_returnsDefaultInstance() {
    val deck = StateDeck(createState("init"), isTerminalState)
    deck.advanceTo(createState("s1"))

    val flashbackState = deck.getFlashbackEphemeralState("nonexistent")

    assertThat(flashbackState).isEqualTo(EphemeralState.getDefaultInstance())
  }

  /**
   * Submits a correct answer to the current top state, pushes [nextState] onto the deck and
   * navigates to it--mirroring how the controller advances the learner one card forward.
   */
  private fun StateDeck.advanceTo(nextState: State) {
    submitAnswer(
      UserAnswer.getDefaultInstance(),
      SubtitledHtml.getDefaultInstance(),
      isCorrectAnswer = true
    )
    pushState(
      nextState,
      prohibitSameStateName = true,
      timestamp = 0,
      isContinueButtonAnimationSeen = true
    )
    navigateToNextState()
  }

  /** Returns the current [EphemeralState] without any lesson progress attached. */
  private fun StateDeck.getCurrentEphemeralStateWithoutProgress(): EphemeralState {
    return getCurrentEphemeralState(
      HelpIndex.getDefaultInstance(),
      timestamp = 0,
      isContinueButtonAnimationSeen = true,
      totalCheckpointCount = null
    )
  }

  /** Creates a question-style state (e.g. NumericInput) with the given checkpoint flag. */
  private fun createState(
    name: String,
    isCheckpoint: Boolean = false,
    linkedSkillId: String = ""
  ): State {
    return buildState(
      name,
      isCheckpoint,
      linkedSkillId,
      Interaction.newBuilder().setId("NumericInput")
    )
  }

  /** Creates a terminal state (the end of an exploration) with the given checkpoint flag. */
  private fun createTerminalState(name: String, isCheckpoint: Boolean = false): State {
    return buildState(
      name,
      isCheckpoint,
      "",
      Interaction.newBuilder().setId(TERMINAL_INTERACTION_ID)
    )
  }

  private fun buildState(
    name: String,
    isCheckpoint: Boolean,
    linkedSkillId: String,
    interaction: Interaction.Builder
  ): State {
    return State.newBuilder()
      .setName(name)
      .setIsCheckpoint(isCheckpoint)
      .setLinkedSkillId(linkedSkillId)
      .setInteraction(interaction)
      .build()
  }
}
