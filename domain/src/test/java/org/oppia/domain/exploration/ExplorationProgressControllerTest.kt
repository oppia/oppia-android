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
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.util.data.AsyncResult
import org.oppia.util.threading.BackgroundDispatcher
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
  @field:TestDispatcher
  lateinit var testDispatcher: TestCoroutineDispatcher

  @Mock
  lateinit var mockCurrentStateLiveDataObserver: Observer<AsyncResult<EphemeralState>>

  @Captor
  lateinit var currentStateResultCaptor: ArgumentCaptor<AsyncResult<EphemeralState>>

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
  fun testGetCurrentState_playExploration_returnsPendingResultFromLoadingExploration() = runBlockingTest(
    coroutineContext
  ) {
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)

    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    advanceUntilIdle()

    // The second-to-latest result stays pending since the exploration was loading (the actual result is the fully
    // loaded exploration).
    verify(mockCurrentStateLiveDataObserver, atLeast(2)).onChanged(currentStateResultCaptor.capture())
    val results = currentStateResultCaptor.allValues
    assertThat(results[results.size - 2].isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_playExploration_loaded_returnsInitialStatePending() = runBlockingTest(
    coroutineContext
  ) {
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)

    val currentStateLiveData = explorationProgressController.getCurrentState()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentStateLiveDataObserver, atLeastOnce()).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo(TEST_INIT_STATE_NAME)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentState_playInvalidExploration_thenPlayValidExp_returnsInitialPendingState() = runBlockingTest(
    coroutineContext
  ) {
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
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo(TEST_INIT_STATE_NAME)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testFinishExploration_beforePlaying_fails() = runBlockingTest(coroutineContext) {
    val exception = assertThrows(IllegalStateException::class) { explorationDataController.stopPlayingExploration() }

    assertThat(exception).hasMessageThat().contains("Cannot finish playing an exploration that hasn't yet been started")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testPlayExploration_withoutFinishingPrevious_fails() = runBlockingTest(coroutineContext) {
    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)

    // Try playing another exploration without finishing the previous one.
    val exception = assertThrows(IllegalStateException::class) {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_5)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected to finish previous exploration before starting a new one.")
  }

  // testGetCurrentState_playSecondExploration_afterFinishingPrevious_loaded_returnsInitialState
  // testSubmitAnswer_forContinueButton_returnsAnswerIsCorrect
  // testGetCurrentState_whileSubmittingAnswer_becomesPending
  // testGetCurrentState_afterSubmittingCorrectAnswer_becomesCompletedState
  // testSubmitAnswer_forTextInput_wrongAnswer_returnsAnswerIsWrong
  // testSubmitAnswer_forTextInput_correctAnswer_returnsAnswerIsCorrect
  // testGetCurrentState_afterPreviousState_submitWrongAnswer_updatePendingState
  // testGetCurrentState_afterPreviousState_submitRightAnswer_pendingStateBecomesCompleted
  // testGetCurrentState_thirdState_isTerminalState
  // testSubmitAnswer_beforePlaying_fails
  // testSubmitAnswer_whileLoading_fails
  // testSubmitAnswer_whileSubmittingAnotherAnswer_fails
  // testMoveToPrevious_beforePlaying_fails
  // testMoveToPrevious_whileLoadingExploration_fails
  // testMoveToPrevious_whileSubmittingAnswer_fails
  // testMoveToPrevious_onInitialState_fails
  // testGetCurrentState_afterMoveToPrevious_onSecondState_updatesToCompletedFirstState
  // testGetCurrentState_afterMoveToPrevious_onThirdState_updatesToCompletedSecondState
  // testMoveToNext_beforePlaying_fails
  // testMoveToNext_whileLoadingExploration_fails
  // testMoveToNext_whileSubmittingAnswer_fails
  // testMoveToNext_onFinalState_fails
  // testMoveToNext_forPendingInitialState_fails
  // testGetCurrentState_afterMoveToNext_onCompletedFirstState_updatesToPendingSecondState
  // testGetCurrentState_afterMoveToNext_onCompletedSecondState_updatesToTerminalThirdState
  // testGetCurrentState_afterMovePreviousAndNext_returnsCurrentState
  // testGetCurrentState_afterMoveNextAndPrevious_returnsCurrentState
  // testGetCurrentState_afterMoveToPrevious_onSecondState_newObserver_receivesCompletedFirstState

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

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher {
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
