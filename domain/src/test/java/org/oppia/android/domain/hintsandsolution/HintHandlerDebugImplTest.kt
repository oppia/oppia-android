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
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionHelper
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

/** Tests for [HintHandlerDebugImpl]. */
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HintHandlerDebugImplTest.TestApplication::class)
class HintHandlerDebugImplTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockHintMonitor: HintHandler.HintMonitor

  @Inject
  lateinit var hintHandlerDebugImplFactory: HintHandlerDebugImpl.FactoryDebugImpl

  @Inject
  lateinit var explorationRetriever: ExplorationRetriever

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var showAllHintsAndSolutionHelper: ShowAllHintsAndSolutionHelper

  private lateinit var hintHandler: HintHandler

  private val expWithNoHintsOrSolution by lazy {
    explorationRetriever.loadExploration("test_single_interactive_state_exp_no_hints_no_solution")
  }

  private val expWithOneHintAndNoSolution by lazy {
    explorationRetriever.loadExploration(
      "test_single_interactive_state_exp_with_one_hint_and_no_solution"
    )
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
    hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)
  }

  /* Tests for startWatchingForHintsInNewState */

  @Test
  fun testStartWatchingForHints_showAllHelpsDisabled_stateWithoutHints_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithNoHintsOrSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithoutHints_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithNoHintsOrSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsDisabled_stateWithoutHints_helpIndexIsEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithNoHintsOrSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithoutHints_helpIndexIsEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithNoHintsOrSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsDisabled_stateWithHints_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithHints_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    // This will be called multiple times as we are showing all helps and thus on every help
    // revealed, this function is called.
    verify(mockHintMonitor, atLeast(1)).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsDisabled_stateWithHints_helpIndexIsEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithHints_allHelpsAreRevealed() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  /* Tests for finishState */

  @Test
  fun testFinishState_showAllHelpsDisabled_defaultState_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testFinishState_showAllHelpsEnabled_defaultState_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testFinishState_showAllHelpsDisabled_defaultState_helpIndexIsEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testFinishState_showAllHelpsEnabled_defaultState_helpIndexIsEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testFinishState_showAllHelpsDisabled_newStateWithHints_helpIndexIsEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

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
  fun testFinishState_showAllHelpsEnabled_newStateWithHints_allHelpsAreRevealed() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    // Note that this is slightly suspect: normally, a state would be sourced from an independent
    // question or from the same exploration. This tactic is taken to simplify the data structure
    // requirements for the test, and because it should be more or less functionally equivalent.
    hintHandler.finishState(expWithOneHintAndNoSolution.getInitialState())

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  /* Tests for handleWrongAnswerSubmission */

  @Test
  fun testWrongAnswerSubmission_showAllHelpsDisabled_stateWithHints_monitorNotCalled() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testWrongAnswerSubmission_showAllHelpsEnabled_stateWithHints_monitorNotCalled() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testWrongAnswerSubmission_showAllHelpsDisabled_stateWithHints_helpIndexStaysEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testWrongAnswerSubmission_showAllHelpsEnabled_stateWithHints_allHelpsAreRevealed() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualTo(
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    )
  }

  @Test
  fun testWrongAnswerSubmission_showAllHelpsDisabled_twice_stateWithHints_monitorCalled() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

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
  fun testWrongAnswerSubmission_showAllHelpsEnabled_twice_stateWithHints_monitorNotCalled() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate two answers being submitted subsequently.
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 2)

    // The monitor here is not called because all helps are already revealed and thus there is no
    // new interaction on submitting wrong answer.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  /* Tests for viewHint */

  @Test
  fun testViewHint_showAllHelpsDisabled_noHintAvailable_throwsException() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewHint(hintIndex = 0)
    }

    // No hint is available to reveal.
    assertThat(exception).hasMessageThat().contains("Cannot reveal hint")
  }

  @Test
  fun testViewHint_showAllHelpsEnabled_noHintAvailable_throwsException() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewHint(hintIndex = 0)
    }

    // No hint is available to reveal.
    assertThat(exception).hasMessageThat().contains("Cannot reveal hint")
  }

  @Test
  fun testViewHint_showAllHelpsDisabled_hintAvailable_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    reset(mockHintMonitor)

    hintHandler.viewHint(hintIndex = 0)

    // Viewing the hint should trigger a change in the help index.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testViewHint_showAllHelpsEnabled_hintAvailable_throwsException() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    triggerFirstHint()
    reset(mockHintMonitor)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewHint(hintIndex = 0)
    }

    // The exception is thrown because all helps are already revealed when we loaded the state and
    // thus calling 'viewHint' on the HelpIndex state EVERYTHING_REVEALED throws exception.
    assertThat(exception).hasMessageThat().contains(
      "Cannot reveal hint for current index: EVERYTHING_REVEALED"
    )
  }

  /* Tests for viewSolution */

  @Test
  fun testViewSolution_showAllHelpsDisabled_nothingAvailable_throwsException() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The solution is not yet available to be seen (no hints have been viewed).
    assertThat(exception).hasMessageThat().contains("Cannot reveal solution")
  }

  @Test
  fun testViewSolution_showAllHelpsEnabled_nothingAvailable_throwsException() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The solution is not yet available to be seen (no hints have been viewed).
    assertThat(exception).hasMessageThat().contains("Cannot reveal solution")
  }

  @Test
  fun testViewSolution_showAllHelpsDisabled_solutionAvailable_callsMonitor() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

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
  fun testViewSolution_showAllHelpsEnabled_solutionAvailable_throwsException() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    val exception = assertThrows(IllegalStateException::class) {
      hintHandler.viewSolution()
    }

    // The exception is thrown because all helps are already revealed when we loaded the state and
    // thus calling 'viewSolution' on the HelpIndex state EVERYTHING_REVEALED throws exception.
    assertThat(exception).hasMessageThat().contains(
      "Cannot reveal solution for current index: EVERYTHING_REVEALED"
    )
  }

  /* Tests for navigateToPreviousState */

  @Test
  fun testNavigateToPreviousState_showAllHelpsDisabled_pendingHint_wait60Sec_monitorNotCalled() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    hintHandler.navigateToPreviousState()
    waitFor60Seconds()

    // The monitor should not be called since the user navigated away from the pending state.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testNavigateToPreviousState_showAllHelpsEnabled_monitorNotCalled() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    hintHandler.navigateToPreviousState()

    // The monitor should not be called since the user navigated away from the pending state and all
    // helps are already revealed and there is nothing to monitor now.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testNavigateToPreviousState_showAllHelpsDisabled_multipleTimes_pendingHint_wait60Sec_monitorNotCalled() { // ktlint-disable max-line-length
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

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

  @Test
  fun testNavigateToPreviousState_showAllHelpsEnabled_multipleTimes_monitorNotCalled() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)

    // Simulate navigating back three states.
    hintHandler.navigateToPreviousState()
    hintHandler.navigateToPreviousState()
    hintHandler.navigateToPreviousState()

    // The monitor should not be called since the pending state isn't visible and all helps are
    // already revealed and there is nothing to monitor now.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  /* Tests for navigateBackToLatestPendingState */

  @Test
  fun testNavigateBackToLatestPendingState_showAllHelpsEnabled_fromPreviousState_monitorNotCalled() { // ktlint-disable max-line-length
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.navigateToPreviousState()
    reset(mockHintMonitor)

    hintHandler.navigateBackToLatestPendingState()

    // The monitor should not be called after returning to the pending state as all helps are
    // already revealed and there is nothing to monitor now.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testNavigateBackToLatestPendingState_showAllHelpsDisabled_fromPreviousState_pendingHint_monitorNotCalled() { // ktlint-disable max-line-length
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.navigateToPreviousState()
    reset(mockHintMonitor)

    hintHandler.navigateBackToLatestPendingState()

    // The monitor should not be called immediately after returning to the pending state.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testNavigateBackToLatestPendingState_showAllHelpsDisabled_fromPreviousState_pendingHint_wait60Sec_monitorCalled() { // ktlint-disable max-line-length
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.navigateToPreviousState()
    hintHandler.navigateBackToLatestPendingState()
    reset(mockHintMonitor)

    waitFor60Seconds()

    // The hint should not be available since the user has waited for the counter to finish.
    verify(mockHintMonitor).onHelpIndexChanged()
  }

  /*
   * Tests for getCurrentHelpIndex (more detailed state machine tests; some may be redundant
   * with earlier tests). It's suggested to reference the state machine diagram laid out in
   * HintHandler's class KDoc when inspecting the following tests.
   */

  @Test
  fun testGetCurrentHelpIndex_showAllHelpsDisabled_initialState_isEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_showAllHelpsDisabled_wait10Sec_isEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor10Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_showAllHelpsDisabled_wait30Sec_isEmpty() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    waitFor30Seconds()

    val helpIndex = hintHandler.getCurrentHelpIndex()

    assertThat(helpIndex).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_showAllHelpsDisabled_wait60Sec_hasAvailableHint() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = false)

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
  fun testGetCurrentHelpIndex_showAllHelpsEnabled_initialState_allHelpsAreRevealed() {
    showAllHintsAndSolutionHelper.setShowAllHintsAndSolution(isEnabled = true)

    val state = expWithHintsAndSolution.getInitialState()

    hintHandler.startWatchingForHintsInNewState(state)

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
      TestModule::class, HintsAndSolutionDebugModule::class, HintsAndSolutionConfigModule::class,
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

    fun inject(hintHandlerDebugImplTest: HintHandlerDebugImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHintHandlerDebugImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(hintHandlerDebugImplTest: HintHandlerDebugImplTest) {
      component.inject(hintHandlerDebugImplTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
