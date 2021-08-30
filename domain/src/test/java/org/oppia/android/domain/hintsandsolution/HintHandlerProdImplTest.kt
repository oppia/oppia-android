package org.oppia.android.domain.hintsandsolution

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State
import org.oppia.android.domain.exploration.ExplorationRetriever
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [HintHandlerProdImpl]. */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HintHandlerProdImplTest.TestApplication::class)
class HintHandlerProdImplTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockHintMonitor: HintHandler.HintMonitor

  @Inject
  lateinit var hintHandlerProdImplFactory: HintHandlerProdImpl.FactoryProdImpl

  @Inject
  lateinit var explorationRetriever: ExplorationRetriever

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var hintHandler: HintHandler
  private val expWithNoHintsOrSolution by lazy {
    explorationRetriever.loadExploration("test_single_interactive_state_exp_no_hints_no_solution")
  }
  private val expWithOneHintAndNoSolution by lazy {
    explorationRetriever.loadExploration(
      "test_single_interactive_state_exp_with_one_hint_and_no_solution"
    )
  }
  private val expWithOneHintAndSolution by lazy {
    explorationRetriever.loadExploration(
      "test_single_interactive_state_exp_with_one_hint_and_solution"
    )
  }
  private val expWithNoHintsAndOneSolution by lazy {
    explorationRetriever.loadExploration("test_single_interactive_state_exp_with_only_solution")
  }
  private val expWithHintsAndSolution by lazy {
    explorationRetriever.loadExploration(
      "test_single_interactive_state_exp_with_hints_and_solution"
    )
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    // Use the direct HintHandler factory to avoid testing the module setup.
    hintHandler = hintHandlerProdImplFactory.create(mockHintMonitor)
  }

  /* Tests for startWatchingForHintsInNewState */

  @Test
  fun testStartWatchingForHints_stateWithoutHints_callsMonitor() {
    val state = expWithNoHintsOrSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_stateWithoutHints_helpIndexIsEmpty() {
    val state = expWithNoHintsOrSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testStartWatchingForHints_stateWithoutHints_wait60Seconds_monitorNotCalledAgain() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    waitFor60Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testStartWatchingForHints_stateWithoutHints_wait60Seconds_helpIndexIsEmpty() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    waitFor60Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testStartWatchingForHints_stateWithHints_callsMonitor() {
    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_stateWithHints_helpIndexIsEmpty() {
    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testStartWatchingForHints_stateWithHints_wait10Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testStartWatchingForHints_stateWithHints_wait30Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    waitFor30Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testStartWatchingForHints_stateWithHints_wait60Seconds_callsMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    waitFor60Seconds()

    // Verify that the monitor is called again (since there's a hint now available).
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_stateWithHints_wait60Seconds_firstHintIsAvailable() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    waitFor60Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  /* Tests for resumeHintsForSavedState */

  @Test
  fun testResumeHint_stateWithoutHints_noTrackedAnswers_noHintVisible_callsMonitor() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testResumeHint_stateWithoutHints_noTrackedAnswer_noHintVisible_helpIndexIsEmpty() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testResumeHint_stateWithoutHint_twoTrackedAnswer_noHintVisible_helpIndexIsEmpty() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 2,
      HelpIndex.getDefaultInstance(),
      state
    )

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testResumeHint_stateWithoutHints_noTrackedAnswers_wait60Seconds_monitorNotCalledAgain() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )
    reset(mockHintMonitor)

    waitFor60Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHint_stateWithoutHints_twoTrackedAns_noHintVisible_wait60Sec_helpIndexIsEmpty() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 2,
      HelpIndex.getDefaultInstance(),
      state
    )

    waitFor60Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testResumeHint_stateWithHints_noTrackedAnswers_noHintVisible_callsMonitor() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.getDefaultInstance(),
      state = state
    )

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testResumeHints_stateWithHints_noTrackedAnswers_noHintVisible_helpIndexIsEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testResumeHints_stateWithHints_noHintVisible_wait10Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_noHintVisible_wait30Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )
    reset(mockHintMonitor)

    waitFor30Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_noHintVisible_wait60Seconds_callsMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )
    reset(mockHintMonitor)

    waitFor60Seconds()

    // Verify that the monitor is called again (since there's a hint now available).
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testResumeHints_stateWithHints_noHintVisible_wait60Seconds_helpIndexHasNewAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      HelpIndex.getDefaultInstance(),
      state
    )
    reset(mockHintMonitor)

    waitFor60Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testResumeHints_stateWithHints_hintVisible_wait10Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_hintVisible_wait30Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor30Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_hintVisible_wait60Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor60Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_hintRevealed_wait10Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithOneHint_hintRevealed_wait30Seconds_callsMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor30Seconds()

    // Verify that the monitor is called again (since there's a solution now available).
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testResumeHints_stateWithOneHint_hintRevealed_wait30Seconds_helpIndexHasSolution() {
    val state = expWithOneHintAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor30Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testResumeHints_stateWithTwoHints_secondHintRevealed_wait30Seconds_callsMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 1
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor30Seconds()

    // Verify that the monitor is called again (since there's a solution now available).
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testResumeHints_stateWithTwoHints_secondHintRevealed_wait30Seconds_helpIndexHasSolution() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 1
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor30Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testResumeHint_stateWithHints_solutionVisible_callsMonitor() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        showSolution = true
      }.build(),
      state
    )

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testResumeHint_stateWithHints_solutionVisible_helpIndexHasSolution() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        showSolution = true
      }.build(),
      state
    )

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testResumeHints_stateWithHints_solutionVisible_wait10Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        showSolution = true
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_solutionVisible_wait30Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        showSolution = true
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor30Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_solutionVisible_wait60Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        showSolution = true
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor60Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHint_stateWithHints_everythingVisible_callsMonitor() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build(),
      state
    )

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testResumeHint_stateWithHints_everythingVisible_helpIndexShowsEverything() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build(),
      state
    )

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  @Test
  fun testResumeHints_stateWithHints_everythingVisible_wait10Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_everythingVisible_wait30Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testResumeHints_stateWithHints_everythingVisible_wait60Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.resumeHintsForSavedState(
      trackedWrongAnswerCount = 0,
      helpIndex = HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build(),
      state
    )
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  /* Tests for finishState */

  @Test
  fun testFinishState_defaultState_callsMonitor() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testFinishState_defaultState_helpIndexIsEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testFinishState_defaultState_wait60Seconds_monitorNotCalledAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.finishState(State.getDefaultInstance())
    reset(mockHintMonitor)

    waitFor60Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testFinishState_defaultState_wait60Seconds_helpIndexStaysEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.finishState(State.getDefaultInstance())
    reset(mockHintMonitor)

    waitFor60Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testFinishState_newStateWithHints_helpIndexIsEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    // Note that this is slightly suspect: normally, a state would be sourced from an independent
    // question or from the same exploration. This tactic is taken to simplify the data structure
    // requirements for the test, and because it should be more or less functionally equivalent.
    hintHandler.finishState(expWithOneHintAndNoSolution.getInitialState())

    // The help index should be reset.
    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testFinishState_newStateWithHints_wait60Seconds_callsMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.finishState(expWithOneHintAndNoSolution.getInitialState())
    reset(mockHintMonitor)

    waitFor60Seconds()

    // The index should be called again now that there's a new index.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testFinishState_previousStateFullyRevealed_newStateWithHints_wait60Seconds_indexHasNewHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealEverythingInMultiHintState()
    hintHandler.finishState(expWithOneHintAndNoSolution.getInitialState())

    waitFor60Seconds()

    // A new hint index should be revealed despite the entire previous state being completed (since
    // the handler has been reset).
    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testFinishState_newStateWithoutHints_wait60Seconds_doesNotCallMonitorAgain() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealEverythingInMultiHintState()
    hintHandler.finishState(expWithNoHintsOrSolution.getInitialState())
    reset(mockHintMonitor)

    waitFor60Seconds()

    // Since the new state doesn't have any hints, the index will not change.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  /* Tests for handleWrongAnswerSubmission */

  @Test
  fun testWrongAnswerSubmission_stateWithHints_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testWrongAnswerSubmission_stateWithHints_helpIndexStaysEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testWrongAnswerSubmission_stateWithHints_wait10seconds_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testWrongAnswerSubmission_stateWithHints_wait30seconds_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    reset(mockHintMonitor)

    waitFor30Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testWrongAnswerSubmission_stateWithHints_wait60seconds_monitorCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    reset(mockHintMonitor)

    waitFor60Seconds()

    // A hint should now be available, so the monitor will be notified.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testWrongAnswerSubmission_stateWithHints_wait60seconds_helpIndexHasAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    waitFor60Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testWrongAnswerSubmission_twice_stateWithHints_monitorCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate two answers being submitted subsequently.
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    // Submitting two wrong answers subsequently should immediately result in a hint being available.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testWrongAnswerSubmission_twice_stateWithHints_helpIndexHasAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    // Simulate two answers being submitted subsequently.
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testWrongAnswerSubmission_twice_stateWithoutHints_monitorNotCalled() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate two answers being submitted subsequently.
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    // No notification should happen since the state doesn't have any hints.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testWrongAnswerSubmission_twice_stateWithoutHints_helpIndexIsEmpty() {
    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    // Simulate two answers being submitted subsequently.
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    // No hint is available since the state has no hints.
    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  /* Tests for viewHint */

  @Test
  fun testViewHint_noHintAvailable_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewHint(hintIndex = 0)
    }

    // No hint is available to reveal.
    assertThat(exception).hasMessageThat().contains("Cannot reveal hint")
  }

  @Test
  fun testViewHint_hintAvailable_callsMonitor() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    reset(mockHintMonitor)

    hintHandler.viewHint(hintIndex = 0)

    // Viewing the hint should trigger a change in the help index.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testViewHint_hintAvailable_helpIndexUpdatedToShowHintShown() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    reset(mockHintMonitor)

    hintHandler.viewHint(hintIndex = 0)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testViewHint_hintAvailable_multiHintState_wait10Seconds_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    hintHandler.viewHint(hintIndex = 0)
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testViewHint_hintAvailable_multiHintState_wait30Seconds_monitorCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    hintHandler.viewHint(hintIndex = 0)
    reset(mockHintMonitor)

    waitFor30Seconds()

    // 30 seconds is long enough to trigger a second hint to be available.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testViewHint_hintAvailable_multiHintState_wait30Seconds_helpIndexHasNewAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    hintHandler.viewHint(hintIndex = 0)

    waitFor30Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testViewHint_hintAvailable_multiHintState_allHintsRevealed_indexShowsLastRevealedHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testViewHint_multiHintState_allHintsRevealed_triggerSolution_indexShowsSolution() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()

    triggerSolution()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testViewHint_hintAvailable_oneHintState_withSolution_wait10Sec_monitorNotCalled() {
    val state = expWithOneHintAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testViewHint_hintAvailable_oneHintState_withSolution_wait30Sec_monitorCalled() {
    val state = expWithOneHintAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    reset(mockHintMonitor)

    waitFor30Seconds()

    // The solution should now be available.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testViewHint_hintAvailable_oneHintState_withSolution_wait30Sec_indexShowsSolution() {
    val state = expWithOneHintAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()

    waitFor30Seconds()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testViewHint_hintAvailable_oneHintState_noSolution_wait10Sec_monitorNotCalled() {
    val state = expWithOneHintAndNoSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    reset(mockHintMonitor)

    waitFor10Seconds()

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testViewHint_hintAvailable_oneHintState_noSolution_wait30Sec_monitorNotCalled() {
    val state = expWithOneHintAndNoSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    reset(mockHintMonitor)

    waitFor30Seconds()

    // The index is still unchanged since there's nothing left to see.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testViewHint_latestHintViewed_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewHint(hintIndex = 0)
    }

    // No hint is available to reveal since it's already been revealed.
    assertThat(exception).hasMessageThat().contains("Cannot reveal hint")
  }

  @Test
  fun testViewHint_solutionAvailable_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewHint(hintIndex = 0)
    }

    // No hint is available to reveal since all hints have been revealed.
    assertThat(exception).hasMessageThat().contains("Cannot reveal hint")
  }

  @Test
  fun testViewHint_everythingRevealed_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerAndRevealSolution()

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewHint(hintIndex = 0)
    }

    // No hint is available to reveal since everything has been revealed.
    assertThat(exception).hasMessageThat().contains("Cannot reveal hint")
  }

  /* Tests for viewSolution */

  @Test
  fun testViewSolution_nothingAvailable_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The solution is not yet available to be seen (no hints have been viewed).
    assertThat(exception).hasMessageThat().contains("Cannot reveal solution")
  }

  @Test
  fun testViewSolution_hintAvailable_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The solution is not yet available to be seen (one hint is available, but hasn't been viewed).
    assertThat(exception).hasMessageThat().contains("Cannot reveal solution")
  }

  @Test
  fun testViewSolution_hintViewed_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The solution is not yet available to be seen (one hint was viewed, but the solution isn't
    // available yet).
    assertThat(exception).hasMessageThat().contains("Cannot reveal solution")
  }

  @Test
  fun testViewSolution_allHintsViewed_solutionNotTriggered_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The solution is not yet available to be seen since the user hasn't triggered the solution to
    // actually show up.
    assertThat(exception).hasMessageThat().contains("Cannot reveal solution")
  }

  @Test
  fun testViewSolution_solutionAvailable_callsMonitor() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerSolution()
    reset(mockHintMonitor)

    hintHandler.viewSolution()

    // The help index should change when the solution is revealed.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testViewSolution_solutionAvailable_helpIndexUpdatedToShowEverything() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerSolution()

    hintHandler.viewSolution()

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  @Test
  fun testViewSolution_solutionAvailable_wait10Sec_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerSolution()
    hintHandler.viewSolution()
    reset(mockHintMonitor)

    waitFor10Seconds()

    // There's nothing left to be revealed.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testViewSolution_solutionAvailable_wait30Sec_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerSolution()
    hintHandler.viewSolution()
    reset(mockHintMonitor)

    waitFor30Seconds()

    // There's nothing left to be revealed.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testViewSolution_solutionAvailable_wait60Sec_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerSolution()
    hintHandler.viewSolution()
    reset(mockHintMonitor)

    waitFor60Seconds()

    // There's nothing left to be revealed.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testViewSolution_everythingViewed_throwsException() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerAndRevealSolution()

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The solution has already been revealed.
    assertThat(exception).hasMessageThat().contains("Cannot reveal solution")
  }

  /* Tests for navigateToPreviousState */

  @Test
  fun testNavigateToPreviousState_pendingHint_wait60Sec_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    hintHandler.navigateToPreviousState()
    waitFor60Seconds()

    // The monitor should not be called since the user navigated away from the pending state.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testNavigateToPreviousState_multipleTimes_pendingHint_wait60Sec_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate navigating back three states.
    hintHandler.navigateToPreviousState()
    hintHandler.navigateToPreviousState()
    hintHandler.navigateToPreviousState()
    waitFor60Seconds()

    // The monitor should not be called since the pending state isn't visible.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  /* Tests for navigateBackToLatestPendingState */

  @Test
  fun testNavigateBackToLatestPendingState_fromPreviousState_pendingHint_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.navigateToPreviousState()
    reset(mockHintMonitor)

    hintHandler.navigateBackToLatestPendingState()

    // The monitor should not be called immediately after returning to the pending state.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testNavigateBackToLatestPendingState_fromPreviousState_pendingHint_wait60Sec_monitorCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.navigateToPreviousState()
    hintHandler.navigateBackToLatestPendingState()
    reset(mockHintMonitor)

    waitFor60Seconds()

    // The hint should not be available since the user has waited for the counter to finish.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testNavigateBackToLatestPendingState_fromPreviousState_waitRemainingTime_monitorNotCalled() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor30Seconds()
    hintHandler.navigateToPreviousState()
    hintHandler.navigateBackToLatestPendingState()
    reset(mockHintMonitor)

    waitFor30Seconds()

    // Waiting half the necessary time is insufficient to show the hint (since the timer is not
    // resumed, it's reset after returning the pending state).
    verifyNoMoreInteractions(mockHintMonitor)
  }

  /*
   * Tests for getCurrentHelpIndex (more detailed state machine tests; some may be redundant
   * with earlier tests). It's suggested to reference the state machine diagram laid out in
   * HintHandler's class KDoc when inspecting the following tests.
   */

  @Test
  fun testGetCurrentHelpIndex_initialState_isEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_wait10Sec_isEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_wait30Sec_isEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor30Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_wait60Sec_hasAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor60Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_oneWrongAnswer_isEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_oneWrongAnswer_wait10Sec_isEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_oneWrongAnswer_wait30Sec_isEmpty() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor30Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_oneWrongAnswer_wait60Sec_hasAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor60Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_twoWrongAnswers_hasAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_withAvailableHint_anotherWrongAnswer_hasSameAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_viewAvailableHint_hasShownHintIndex() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_viewAvailableHint_wait10Sec_hasShownHintIndex() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_viewAvailableHint_wait30Sec_hasNewAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    waitFor30Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_viewAvailableHint_oneWrongAnswer_hasShownHintIndex() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_viewAvailableHint_oneWrongAnswer_wait10Sec_hasNewAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_viewAvailableHint_twoWrongAnswers_hasShownHintIndex() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    // Multiple wrong answers do not force a hint to be shown except for the first hint.
    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 0
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_viewAvailableHint_twoWrongAnswers_wait10Sec_hasNewAvailableHint() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        nextAvailableHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_noSolution_everythingRevealed() {
    val state = expWithOneHintAndNoSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    triggerAndRevealFirstHint()

    // All hints have been viewed for this state, so nothing remains.
    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_lastIndexViewed() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_wait10Sec_lastIndexViewed() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_wait30Sec_canShowSolution() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    waitFor30Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_wait30Sec_revealSolution_everythingRevealed() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    waitFor30Seconds()
    hintHandler.viewSolution()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_oneWrongAnswer_lastIndexViewed() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_oneWrongAnswer_wait10Sec_canShowSolution() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_twoWrongAnswers_lastIndexViewed() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    // Multiple subsequent wrong answers only affects the first hint.
    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        latestRevealedHintIndex = 1
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_allHintsViewed_twoWrongAnswers_wait10Sec_canShowSolution() {
    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_isEmpty() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_wait10Sec_isEmpty() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_wait30Sec_isEmpty() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor30Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_wait60Sec_canShowSolution() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor60Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_oneWrongAnswer_isEmpty() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_oneWrongAnswer_wait10Sec_isEmpty() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_oneWrongAnswer_wait30Sec_isEmpty() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor30Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_oneWrongAnswer_wait60Sec_canShowSolution() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    waitFor60Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_twoWrongAnswers_canShowSolution() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()
    )
  }

  @Test
  fun testGetCurrentHelpIndex_onlySolution_triggeredAndRevealed_everythingIsRevealed() {
    val state = expWithNoHintsAndOneSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor60Seconds()
    hintHandler.viewSolution()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  private fun Exploration.getInitialState(): State = statesMap.getValue(initStateName)

  private fun triggerFirstHint() = waitFor60Seconds()

  private fun triggerSecondHint() = waitFor30Seconds()

  private fun triggerSolution() = waitFor30Seconds()

  private fun triggerAndRevealFirstHint() {
    triggerFirstHint()
    hintHandler.viewHint(hintIndex = 0)
  }

  private fun triggerAndRevealSecondHint() {
    triggerSecondHint()
    hintHandler.viewHint(hintIndex = 1)
  }

  private fun triggerAndRevealSolution() {
    triggerSolution()
    hintHandler.viewSolution()
  }

  private fun triggerAndRevealEverythingInMultiHintState() {
    triggerAndRevealFirstHint()
    triggerAndRevealSecondHint()
    triggerAndRevealSolution()
  }

  private fun waitFor10Seconds() = waitFor(seconds = 10)

  private fun waitFor30Seconds() = waitFor(seconds = 30)

  private fun waitFor60Seconds() = waitFor(seconds = 60)

  private fun waitFor(seconds: Long) {
    // There's a weird quirk at the moment where the initial coroutine doesn't start without a
    // runCurrent(). This seems a bit like a bug within the dispatchers; it should probably flush
    // current tasks before advancing time.
    // TODO(#3700): Fix this behavior (once fixed, 'runCurrent' should be able to be removed below).
    testCoroutineDispatchers.runCurrent()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(seconds))
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context = application

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, HintsAndSolutionProdModule::class, HintsAndSolutionConfigModule::class,
      TestLogReportingModule::class, TestDispatcherModule::class, RobolectricModule::class,
      LoggerModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(hintHandlerProdImplTest: HintHandlerProdImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHintHandlerProdImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(hintHandlerProdImplTest: HintHandlerProdImplTest) {
      component.inject(hintHandlerProdImplTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
