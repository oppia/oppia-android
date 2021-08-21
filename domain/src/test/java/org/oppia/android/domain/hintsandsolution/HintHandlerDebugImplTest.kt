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
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionController
import org.oppia.android.domain.exploration.ExplorationRetriever
import org.oppia.android.testing.TestLogReportingModule
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
  lateinit var showAllHintsAndSolutionController: ShowAllHintsAndSolutionController

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
  }

  @Test
  fun testFactory_showAllHelpsDisabled_constructNewHandler_returnsProdImplHandler() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = false)

    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    assertThat(hintHandler).isInstanceOf(HintHandlerProdImpl::class.java)
  }

  @Test
  fun testFactory_showAllHelpsEnabled_constructNewHandler_returnsDebugImplHandler() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)

    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    assertThat(hintHandler).isInstanceOf(HintHandlerDebugImpl::class.java)
  }

  /* Tests for startWatchingForHintsInNewState */

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithoutHints_callsMonitor() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithoutHints_helpIndexIsEmpty() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithHints_callsMonitor() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testStartWatchingForHints_showAllHelpsEnabled_stateWithHints_everythingIsRevealed() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

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
  fun testFinishState_showAllHelpsEnabled_defaultState_callsMonitor() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)
    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    verify(mockHintMonitor).onHelpIndexChanged()
  }

  @Test
  fun testFinishState_showAllHelpsEnabled_defaultState_helpIndexIsEmpty() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    // Simulate the default instance case (which can occur specifically for questions).
    hintHandler.finishState(State.getDefaultInstance())

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testFinishState_showAllHelpsEnabled_newStateWithHints_everythingIsRevealed() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

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
  fun testWrongAnswerSubmission_showAllHelpsEnabled_stateWithHints_monitorNotCalled() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)
    hintHandler.handleWrongAnswerSubmission(wrongAnswerCount = 1)

    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testWrongAnswerSubmission_showAllHelpsEnabled_stateWithHints_everythingIsRevealed() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

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
  fun testWrongAnswerSubmission_showAllHelpsEnabled_twice_stateWithHints_monitorNotCalled() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

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

  /* Tests for navigateToPreviousState */

  @Test
  fun testNavigateToPreviousState_showAllHelpsEnabled_monitorNotCalled() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    reset(mockHintMonitor)
    hintHandler.navigateToPreviousState()

    // The monitor should not be called since the user navigated away from the pending state and all
    // helps are already revealed and there is nothing to monitor now.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  @Test
  fun testNavigateToPreviousState_showAllHelpsEnabled_multipleTimes_monitorNotCalled() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = false)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

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
  fun testNavigateBackToLatestPendingState_showAllHelpsEnabled_fromPrevState_monitorNotCalled() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = false)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithHintsAndSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)
    hintHandler.navigateToPreviousState()
    reset(mockHintMonitor)
    hintHandler.navigateBackToLatestPendingState()

    // The monitor should not be called after returning to the pending state as all helps are
    // already revealed and there is nothing to monitor now.
    verifyNoMoreInteractions(mockHintMonitor)
  }

  /*
   * Tests for getCurrentHelpIndex (more detailed state machine tests; some may be redundant
   * with earlier tests). It's suggested to reference the state machine diagram laid out in
   * HintHandler's class KDoc when inspecting the following tests.
   */

  @Test
  fun testGetCurrentHelpIndex_showAllHelpsEnabled_stateWithoutHints_helpIndexIsEmpty() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

    val state = expWithNoHintsOrSolution.getInitialState()
    hintHandler.startWatchingForHintsInNewState(state)

    assertThat(hintHandler.getCurrentHelpIndex()).isEqualToDefaultInstance()
  }

  @Test
  fun testGetCurrentHelpIndex_showAllHelpsEnabled_stateWithHints_everythingIsRevealed() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    // Use the direct HintHandler factory to avoid testing the module setup.
    val hintHandler = hintHandlerDebugImplFactory.create(mockHintMonitor)

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
