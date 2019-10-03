package org.oppia.domain.exploration

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.app.model.Exploration
import org.oppia.app.model.InteractionObject
import org.oppia.util.data.AsyncResult
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

/** Tests for [ExplorationProgressController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ExplorationProgressControllerTest {
  // TODO(#114): Add much more thorough tests for the integration pathway.

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var explorationProgressController: ExplorationProgressController

  @Inject
  lateinit var explorationRetriever: ExplorationRetriever

  @ExperimentalCoroutinesApi
  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: TestCoroutineDispatcher

  @Mock
  lateinit var mockCurrentStateLiveDataObserver: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<Any?>>

  @Mock
  lateinit var mockAsyncAnswerOutcomeObserver: Observer<AsyncResult<AnswerOutcome>>

  @Captor
  lateinit var currentStateResultCaptor: ArgumentCaptor<AsyncResult<EphemeralState>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Captor
  lateinit var asyncAnswerOutcomeCaptor: ArgumentCaptor<AsyncResult<AnswerOutcome>>

  @ExperimentalCoroutinesApi
  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()

    // Require coroutines to be flushed to avoid synchronous execution that could interfere with testing ordered async
    // logic that behaves differently in prod.
    testDispatcher.pauseDispatcher()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_noExploration_isPending() = runBlockingTest(coroutineContext) {
    val currentStateLiveData = explorationProgressController.getCurrentState()

    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentStateLiveDataObserver, atLeastOnce()).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPlayExploration_invalid_returnsSuccess() = runBlockingTest(coroutineContext) {
    val resultLiveData = explorationDataController.startPlayingExploration("invalid_exp_id")
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    // An invalid exploration is not known until it's fully loaded, and that's observed via getCurrentState.
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_playInvalidExploration_returnsFailure() = runBlockingTest(coroutineContext) {
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    explorationDataController.startPlayingExploration("invalid_exp_id")
    advanceUntilIdle()

    verify(mockCurrentStateLiveDataObserver, atLeastOnce()).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isFailure()).isTrue()
    assertThat(currentStateResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Invalid exploration ID: invalid_exp_id")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPlayExploration_valid_returnsSuccess() = runBlockingTest(coroutineContext) {
    val resultLiveData = explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_playExploration_returnsPendingResultFromLoadingExploration() = runBlockingTest(
    coroutineContext
  ) {
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    advanceUntilIdle()

    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    advanceUntilIdle()

    // The second-to-latest result stays pending since the exploration was loading (the actual result is the fully
    // loaded exploration). This is only true if the observer begins before starting to load the exploration.
    verify(mockCurrentStateLiveDataObserver, atLeast(2)).onChanged(currentStateResultCaptor.capture())
    val results = currentStateResultCaptor.allValues
    assertThat(results[results.size - 2].isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_playExploration_loaded_returnsInitialStatePending() = runBlockingTest(
    coroutineContext
  ) {
    val exploration = getTestExploration5()
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)

    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentStateLiveDataObserver, atLeastOnce()).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo(exploration.initStateName)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_playInvalidExploration_thenPlayValidExp_returnsInitialPendingState() = runBlockingTest(
    coroutineContext
  ) {
    val exploration = getTestExploration5()
    // Start with playing an invalid exploration.
    explorationDataController.startPlayingExploration("invalid_exp_id")
    explorationDataController.stopPlayingExploration()

    // Then a valid one.
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    advanceUntilIdle()

    // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce()).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo(exploration.initStateName)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testFinishExploration_beforePlaying_failWithError() = runBlockingTest(coroutineContext) {
    val resultLiveData = explorationDataController.stopPlayingExploration()
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot finish playing an exploration that hasn't yet been started")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPlayExploration_withoutFinishingPrevious_failsWithError() = runBlockingTest(coroutineContext) {
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)

    // Try playing another exploration without finishing the previous one.
    val resultLiveData = explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Expected to finish previous exploration before starting a new one.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_playSecondExploration_afterFinishingPrevious_loaded_returnsInitialState() = runBlockingTest(
    coroutineContext
  ) {
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    // Start with playing a valid exploration, then stop.
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    explorationDataController.stopPlayingExploration()
    advanceUntilIdle()

    // Then another valid one.
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_6)
    advanceUntilIdle()

    // The latest result should correspond to the valid ID, and the progress controller should gracefully recover.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce()).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo(getTestExploration6().initStateName)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() = runBlockingTest(
    coroutineContext
  ) {
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    advanceUntilIdle()

    // The current interaction is multiple choice.
    val result = explorationProgressController.submitAnswer(InteractionObject.newBuilder().setNonNegativeInt(0).build())
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() = runBlockingTest(
    coroutineContext
  ) {
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    advanceUntilIdle()

    // The current interaction is multiple choice.
    val result = explorationProgressController.submitAnswer(InteractionObject.newBuilder().setNonNegativeInt(0).build())
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).isEqualTo("Yes!")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() = runBlockingTest(
    coroutineContext
  ) {
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    advanceUntilIdle()

    // The current interaction is multiple choice.
    val result = explorationProgressController.submitAnswer(InteractionObject.newBuilder().setNonNegativeInt(0).build())
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_providesDefaultFeedbackAndNewStateTransition() = runBlockingTest(
    coroutineContext
  ) {
    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    advanceUntilIdle()

    // The current interaction is multiple choice.
    val result = explorationProgressController.submitAnswer(InteractionObject.newBuilder().setNonNegativeInt(1).build())
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    advanceUntilIdle()

    // Verify that the answer submission was successful.
    verify(mockAsyncAnswerOutcomeObserver, atLeastOnce()).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Hm, it certainly looks like it")
  }

  // testGetCurrentState_whileSubmittingAnswer_becomesPending
  // testGetCurrentState_afterSubmittingWrongAnswer_updatesPendingState
  // testGetCurrentState_afterSubmittingCorrectAnswer_becomesCompletedState
  // testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition
  // testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome
  // testGetCurrentState_secondState_submitWrongAnswer_updatePendingState
  // testGetCurrentState_secondState_submitRightAnswer_pendingStateBecomesCompleted
  // testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition
  // testSubmitAnswer_forNumericInput_wrongAnswer_returnsDefaultOutcome
  // testSubmitAnswer_forContinue_returnsOutcomeWithTransition
  // testGetCurrentState_fifthState_isTerminalState
  // testSubmitAnswer_beforePlaying_failsWithError
  // testSubmitAnswer_whileLoading_failsWithError
  // testSubmitAnswer_whileSubmittingAnotherAnswer_failsWithError
  // testMoveToPrevious_beforePlaying_failsWithError
  // testMoveToPrevious_whileLoadingExploration_failsWithError
  // testMoveToPrevious_whileSubmittingAnswer_failsWithError
  // testMoveToPrevious_onInitialState_failsWithError
  // testMoveToPrevious_forStateWithCompletedPreviousState_succeeds
  // testGetCurrentState_afterMoveToPrevious_onSecondState_updatesToCompletedFirstState
  // testGetCurrentState_afterMoveToPrevious_onThirdState_updatesToCompletedSecondState
  // testMoveToNext_beforePlaying_failsWithError
  // testMoveToNext_whileLoadingExploration_failsWithError
  // testMoveToNext_whileSubmittingAnswer_failsWithError
  // testMoveToNext_onFinalState_failsWithError
  // testMoveToNext_forPendingInitialState_failsWithError
  // testMoveToNext_forCompletedState_succeeds
  // testGetCurrentState_afterMoveToNext_onCompletedFirstState_updatesToPendingSecondState
  // testGetCurrentState_afterMoveToNext_onCompletedSecondState_updatesToTerminalThirdState
  // testGetCurrentState_afterMovePreviousAndNext_returnsCurrentState
  // testGetCurrentState_afterMoveNextAndPrevious_returnsCurrentState
  // testGetCurrentState_afterMoveToPrevious_onSecondState_newObserver_receivesCompletedFirstState

  private suspend fun getTestExploration5(): Exploration {
    return explorationRetriever.loadExploration(TEST_EXPLORATION_ID_5)
  }

  private suspend fun getTestExploration6(): Exploration {
    return explorationRetriever.loadExploration(TEST_EXPLORATION_ID_6)
  }

  private fun setUpTestApplicationComponent() {
    DaggerExplorationProgressControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move to a common test library.
  /** A replacement to JUnit5's assertThrows(). */
  private fun <T: Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
      throw IllegalStateException("JUnit failed to interrupt execution flow.")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
  }

  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): TestCoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationProgressControllerTest: ExplorationProgressControllerTest)
  }
}
