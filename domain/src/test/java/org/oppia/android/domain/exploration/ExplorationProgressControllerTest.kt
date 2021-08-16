package org.oppia.android.domain.exploration

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.ClickOnImage
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.EphemeralState.StateTypeCase.COMPLETED_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.PENDING_STATE
import org.oppia.android.app.model.EphemeralState.StateTypeCase.TERMINAL_STATE
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.domain.util.toAnswerString
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.HelpIndex

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."
private const val INVALID_TOPIC_ID = "invalid_topic_id"
private const val INVALID_STORY_ID = "invalid_story_id"
private const val INVALID_EXPLORATION_ID = "invalid_exp_id"

/** Tests for [ExplorationProgressController]. */
@Suppress("SameParameterValue") // To avoid ignorable warnings for test helper methods.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationProgressControllerTest.TestApplication::class)
class ExplorationProgressControllerTest {
  // TODO(#3646): Add much more thorough tests for the integration pathway.

  // TODO(#59): Once AsyncDataSubscriptionManager can be replaced with a fake, add the following
  //  tests once careful testing timing can be controlled:
  //  - testMoveToNext_whileSubmittingAnswer_failsWithError
  //  - testGetCurrentState_whileSubmittingCorrectMultiChoiceAnswer_updatesToPending
  //  - testSubmitAnswer_whileSubmittingAnotherAnswer_failsWithError
  //  - testMoveToPrevious_whileSubmittingAnswer_failsWithError

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var explorationProgressController: ExplorationProgressController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Inject
  lateinit var explorationCheckpointController: ExplorationCheckpointController

  @Mock
  lateinit var mockCurrentStateLiveDataObserver: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockCurrentStateLiveDataObserver2: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<*>>

  @Mock
  lateinit var mockAsyncAnswerOutcomeObserver: Observer<AsyncResult<AnswerOutcome>>

  @Mock
  lateinit var mockAsyncHintObserver: Observer<AsyncResult<Hint>>

  @Mock
  lateinit var mockAsyncSolutionObserver: Observer<AsyncResult<Solution>>

  @Captor
  lateinit var currentStateResultCaptor: ArgumentCaptor<AsyncResult<EphemeralState>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Captor
  lateinit var asyncAnswerOutcomeCaptor: ArgumentCaptor<AsyncResult<AnswerOutcome>>

  @Mock
  lateinit var mockExplorationCheckpointObserver: Observer<AsyncResult<ExplorationCheckpoint>>

  @Captor
  lateinit var explorationCheckpointCaptor: ArgumentCaptor<AsyncResult<ExplorationCheckpoint>>

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetCurrentState_noExploration_isPending() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()

    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun testPlayExploration_invalid_returnsSuccess() {
    val resultLiveData =
      explorationDataController.startPlayingExploration(
        profileId.internalId,
        INVALID_TOPIC_ID,
        INVALID_STORY_ID,
        INVALID_EXPLORATION_ID,
        shouldSavePartialProgress = false,
        explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
      )
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // An invalid exploration is not known until it's fully loaded, and that's observed via
    // getCurrentState.
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(
      profileId.internalId,
      INVALID_TOPIC_ID,
      INVALID_STORY_ID,
      INVALID_EXPLORATION_ID,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isFailure()).isTrue()
    assertThat(currentStateResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("invalid_exp_id")
  }

  @Test
  fun testPlayExploration_valid_returnsSuccess() {
    val resultLiveData =
      explorationDataController.startPlayingExploration(
        profileId.internalId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false,
        explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
      )
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testGetCurrentState_playExploration_returnsPendingResultFromLoadingExploration() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    // The second-to-latest result stays pending since the exploration was loading (the actual
    // result is the fully loaded exploration). This is only true if the observer begins before
    // starting to load the exploration.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeast(2)
    ).onChanged(currentStateResultCaptor.capture())
    val results = currentStateResultCaptor.allValues
    assertThat(results[results.size - 2].isPending()).isTrue()
  }

  @Test
  fun testGetCurrentState_playExploration_loaded_returnsInitialStatePending() {
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo("Continue")
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_thenPlayValidExp_returnsInitialPendingState() {
    // Start with playing an invalid exploration.
    playExploration(
      profileId.internalId,
      INVALID_TOPIC_ID,
      INVALID_STORY_ID,
      INVALID_EXPLORATION_ID,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    endExploration()

    // Then a valid one.
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name).isEqualTo("Continue")
  }

  @Test
  fun testFinishExploration_beforePlaying_failWithError() {
    val resultLiveData = explorationDataController.stopPlayingExploration()
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot finish playing an exploration that hasn't yet been started")
  }

  @Test
  fun testPlayExploration_withoutFinishingPrevious_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    // Try playing another exploration without finishing the previous one.
    val resultLiveData =
      explorationDataController.startPlayingExploration(
        profileId.internalId,
        TEST_TOPIC_ID_0,
        TEST_STORY_ID_0,
        TEST_EXPLORATION_ID_2,
        shouldSavePartialProgress = false,
        explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
      )
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Expected to finish previous exploration before starting a new one.")
  }

  @Test
  fun testGetCurrentState_playSecondExploration_afterFinishingPrev_loaded_returnsInitialState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    // Start with playing a valid exploration, then stop.
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    endExploration()

    // Then another valid one.
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    // The latest result should correspond to the valid ID, and the progress controller should
    // gracefully recover.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentStateResultCaptor.value.getOrThrow().hasPreviousState).isFalse()
    assertThat(currentStateResultCaptor.value.getOrThrow().state.name)
      .isEqualTo("DragDropSortInput")
  }

  @Test
  fun testSubmitAnswer_beforePlaying_failsWithError() {
    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission failed.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isFailure()).isTrue()
    assertThat(asyncAnswerOutcomeCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot submit an answer if an exploration is not being played.")
  }

  @Test
  fun testSubmitAnswer_whileLoading_failsWithError() {
    // Start playing an exploration, but don't wait for it to complete.
    subscribeToCurrentStateToAllowExplorationToLoad()
    explorationDataController.startPlayingExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission failed.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isFailure()).isTrue()
    assertThat(asyncAnswerOutcomeCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot submit an answer while the exploration is being loaded.")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeMultipleChoiceState()

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(2))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_correctAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeMultipleChoiceState()

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(2))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeMultipleChoiceState()

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    assertThat(asyncAnswerOutcomeCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testSubmitAnswer_forMultipleChoice_wrongAnswer_providesDefFeedbackAndSameStateTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeMultipleChoiceState()

    val result =
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("Try again.")
  }

  @Test
  fun testGetCurrentState_afterSubmittingCorrectMultiChoiceAnswer_becomesCompletedState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeMultipleChoiceState()

    submitMultipleChoiceAnswer(2)

    // Verify that the current state updates. It should now be completed with the correct answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(1)
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(0).feedback.html).contains("Correct!")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongMultiChoiceAnswer_updatesPendingState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeMultipleChoiceState()

    submitMultipleChoiceAnswer(0)

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)
    assertThat(currentState.pendingState.getWrongAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(currentState.pendingState.getWrongAnswer(0).feedback.html).contains("Try again.")
  }

  @Test
  fun testGetCurrentState_afterSubmittingWrongThenRightAnswer_updatesToStateWithBothAnswers() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeMultipleChoiceState()
    submitMultipleChoiceAnswer(0)

    submitMultipleChoiceAnswer(2)

    // Verify that the current state updates. It should now be completed with both the wrong and
    // correct answers.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.nonNegativeInt)
      .isEqualTo(0)
    assertThat(currentState.completedState.getAnswer(0).feedback.html).contains("Try again.")
    assertThat(currentState.completedState.getAnswer(1).userAnswer.answer.nonNegativeInt)
      .isEqualTo(2)
    assertThat(currentState.completedState.getAnswer(1).feedback.html).contains("Correct!")
  }

  @Test
  fun testMoveToNext_beforePlaying_failsWithError() {
    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next state if an exploration is not being played.")
  }

  @Test
  fun testMoveToNext_whileLoadingExploration_failsWithError() {
    // Start playing an exploration, but don't wait for it to complete.
    subscribeToCurrentStateToAllowExplorationToLoad()
    explorationDataController.startPlayingExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a next state if an exploration is being loaded.")
  }

  @Test
  fun testMoveToNext_forPendingInitialState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that we can't move ahead since the current state isn't yet completed.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testMoveToNext_forCompletedState_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    submitPrototypeState1Answer()

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testMoveToNext_forCompletedState_movesToNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    submitPrototypeState1Answer()

    moveToNextState()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testMoveToNext_afterMovingFromCompletedState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    submitPrototypeState1Answer()
    moveToNextState()

    // Try skipping past the current state.
    val moveToStateResult =
      explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify we can't move ahead since the new state isn't yet completed.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testMoveToPrevious_beforePlaying_failsWithError() {
    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a previous state if an exploration is not being played.")
  }

  @Test
  fun testMoveToPrevious_whileLoadingExploration_failsWithError() {
    // Start playing an exploration, but don't wait for it to complete.
    subscribeToCurrentStateToAllowExplorationToLoad()
    explorationDataController.startPlayingExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to a previous state if an exploration is being loaded.")
  }

  @Test
  fun testMoveToPrevious_onPendingInitialState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify we can't move behind since the current state is the initial exploration state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testMoveToPrevious_onCompletedInitialState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    submitPrototypeState1Answer()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Still can't navigate behind for a completed initial state since there's no previous state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testMoveToPrevious_forStateWithCompletedPreviousState_succeeds() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that we can navigate to the previous state since the current state is complete and not
    // initial.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testMoveToPrevious_forCompletedState_movesToPreviousState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()

    // Since the answer submission and forward navigation should work (see earlier tests), verify
    // that the move to the previous state does return us back to the initial exploration state
    // (which is now completed).
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Continue")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // The first previous navigation should succeed (see above), but the second will fail since
    // we're back at the initial state.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testSubmitAnswer_forTextInput_correctAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Finnish"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_returnsDefaultOutcome() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer was wrong, and that there's no handler for it so the default outcome
    // is returned.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("Not quite.")
  }

  @Test
  fun testSubmitAnswer_forFractionInput_wrongAnswer_returnsDefaultOutcome_hasHint() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeFractionInputState()

    submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)
    val answerAndFeedback = currentState.pendingState.getWrongAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.fraction.numerator).isEqualTo(1)
    assertThat(answerAndFeedback.userAnswer.answer.fraction.denominator).isEqualTo(3)
    assertThat(answerAndFeedback.feedback.html).contains("Try again.")
    val hintAndSolution = currentState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Remember that two halves")
  }

  @Test
  fun testRevealHint_forWrongAnswer_showHint_returnHintIsRevealed() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeFractionInputState()
    submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, on submission of wrong answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()

    val result = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)

    val hintAndSolution = currentState.state.interaction.getHint(0)
    assertThat(hintAndSolution.hintContent.html).contains("Remember that two halves")

    // Verify that the current state updates. Hint revealed is true.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val updatedState = currentStateResultCaptor.value.getOrThrow()

    assertThat(updatedState.state.interaction.getHint(0).hintIsRevealed).isTrue()
  }

  @Test
  fun testRevealSolution_forWrongAnswer_showSolution_returnSolutionIsRevealed() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeFractionInputState()
    submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, on submission of wrong answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()

    val result = explorationProgressController.submitSolutionIsRevealed()
    result.observeForever(mockAsyncSolutionObserver)
    testCoroutineDispatchers.runCurrent()

    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)

    // Verify that the current state updates. Solution revealed is true.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val updatedState = currentStateResultCaptor.value.getOrThrow()

    assertThat(updatedState.state.interaction.solution.solutionIsRevealed).isTrue()
  }

  @Test
  fun testHintsAndSolution_noHintVisible_checkHelpIndexIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true
    )
    playThroughPrototypeState1AndMoveToNextState()

    // Verify that the helpIndex.IndexTypeCase is equal to INDEX_TYPE_NOT_SET because no hint
    // is visible yet.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET)
  }

  @Test
  fun testHintsAndSolution_wait60Seconds_unrevealedHintIsVisible_checkHelpIndexIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true
    )
    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(60))
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX)
    assertThat(currentState.helpIndex.availableNextHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_submitTwoWrongAnswers_unrevealedHintIsVisible_checkHelpIndexIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true
    )
    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX)
    assertThat(currentState.helpIndex.availableNextHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_revealedHintIsVisible_checkHelpIndexIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val result = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isTrue()
    assertThat(currentState.state.interaction.solution.solutionIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX)
    assertThat(currentState.helpIndex.latestRevealedHintIndex).isEqualTo(0)
  }

  @Test
  fun testHintsAndSolution_allHintsVisible_wait30Seconds_solutionVisible_checkHelpIndexIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val result = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    // The solution should be visible after 30 seconds of the last hint being reveled.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal SHOW_SOLUTION because unrevealed solution is
    // visible.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isTrue()
    assertThat(currentState.state.interaction.solution.solutionIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.SHOW_SOLUTION)
  }

  @Test
  fun testHintAndSol_hintsVisible_submitWrongAns_wait10Second_solVisible_checkHelpIndexIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val result = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    submitWrongAnswerForPrototypeState2()
    // The solution should be visible after 10 seconds becuase one wrong answer was submitted.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(10))
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal SHOW_SOLUTION because unrevealed solution is
    // visible.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isTrue()
    assertThat(currentState.state.interaction.solution.solutionIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.SHOW_SOLUTION)
  }

  @Test
  fun testHintsAndSolution_revealedSolutionIsVisible_checkHelpIndexIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val hintResult = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    hintResult.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    // The solution should be visible after 30 seconds of the last hint being reveled.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()

    val solutionResult = explorationProgressController.submitSolutionIsRevealed()
    solutionResult.observeForever(mockAsyncSolutionObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the helpIndex.IndexTypeCase is equal EVERYTHING_IS_REVEALED because a new the
    // solution has been revealed.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isTrue()
    assertThat(currentState.state.interaction.solution.solutionIsRevealed).isTrue()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.EVERYTHING_REVEALED)
  }

  @Test
  fun testSubmitAnswer_forTextInput_wrongAnswer_afterAllHintsAreExhausted_showSolution() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeFractionInputState()

    submitWrongAnswerForPrototypeState2()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)

    val hint = currentState.state.interaction.getHint(0)
    assertThat(hint.hintContent.html).contains("Remember that two halves")

    val solution = currentState.state.interaction.solution
    assertThat(solution.correctAnswer.numerator).isEqualTo(1)
    assertThat(solution.correctAnswer.denominator).isEqualTo(2)
    assertThat(solution.explanation.html)
      .contains("Half of something has one part in the numerator for every two parts")
  }

  @Test
  fun testGetCurrentState_secondState_submitRightAnswer_pendingStateBecomesCompleted() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Finnish"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should now be completed with the correct answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(1)
    val answerAndFeedback = currentState.completedState.getAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.normalizedString).isEqualTo("Finnish")
    assertThat(answerAndFeedback.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forTextInput_withSpaces_updatesStateWithVerbatimAnswer() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Finnish  "))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. The submitted answer should have a textual version
    // that is a verbatim version of the user-submitted answer.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.completedState.answerCount).isEqualTo(1)
    val answerAndFeedback = currentState.completedState.getAnswer(0)
    assertThat(answerAndFeedback.userAnswer.textualAnswerCase)
      .isEqualTo(UserAnswer.TextualAnswerCase.PLAIN_ANSWER)
    assertThat(answerAndFeedback.userAnswer.plainAnswer).isEqualTo("Finnish  ")
  }

  @Test
  fun testGetCurrentState_eighthState_submitWrongAnswer_updatePendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeTextInputState()

    val result =
      explorationProgressController.submitAnswer(createTextInputAnswer("Klingon"))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the current state updates. It should stay pending, and the wrong answer should be
    // appended.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(1)
    val answerAndFeedback = currentState.pendingState.getWrongAnswer(0)
    assertThat(answerAndFeedback.userAnswer.answer.normalizedString).isEqualTo("Klingon")
    assertThat(answerAndFeedback.feedback.html).contains("Not quite.")
  }

  @Test
  fun testGetCurrentState_afterMovePreviousAndNext_returnsCurrentState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()
    moveToNextState()

    // The current state should stay the same.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveNextAndPrevious_returnsCurrentState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitPrototypeState2Answer() // Submit the answer but do not proceed to the next state.

    moveToNextState()
    moveToPreviousState()

    // The current state should stay the same.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrev_onThirdState_newObserver_receivesCompletedSecondState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    // Move to the previous state and register a new observer.
    moveToPreviousState() // Third state -> second
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver2)
    testCoroutineDispatchers.runCurrent()

    // The new observer should observe the completed second state
    // since it's the current pending state.
    verify(
      mockCurrentStateLiveDataObserver2,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
  }

  @Test
  fun testGetCurrentState_forFirstState_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    // The initial state should not have a next state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forFirstState_afterAnswerSubmission_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    submitPrototypeState1Answer()

    // Simply completing the current state should not result in there being a next state since the
    // user hasn't proceeded to the following state, yet.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    playThroughPrototypeState1AndMoveToNextState()

    // The current state should have a previous state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackward_hasNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()

    // The previous state should have a next state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isTrue()
  }

  @Test
  fun testGetCurrentState_forSecondState_navigateBackwardThenForward_doesNotHaveNextState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()

    moveToPreviousState()
    moveToNextState()

    // Iterating back to the current state should result in no longer having a next state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentStateResultCaptor.value.getOrThrow().hasNextState).isFalse()
  }

  @Test
  fun testSubmitAnswer_forNumericInput_correctAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeNumericInputState()

    val result = explorationProgressController.submitAnswer(createNumericInputAnswer(121.0))
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission was successful.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).contains("Correct!")
  }

  @Test
  fun testSubmitAnswer_forNumericInput_wrongAnswer_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    navigateToPrototypeNumericInputState()

    val result = explorationProgressController.submitAnswer(
      createNumericInputAnswer(122.0)
    )
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the answer submission failed as expected.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.SAME_STATE)
    assertThat(answerOutcome.feedback.html).contains("It's less than that.")
  }

  @Test
  fun testSubmitAnswer_forContinue_returnsOutcomeWithTransition() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    // The first state of the exploration is the Continue interaction.

    val result = explorationProgressController.submitAnswer(createContinueButtonAnswer())
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the continue button succeeds by default.
    verify(
      mockAsyncAnswerOutcomeObserver,
      atLeastOnce()
    ).onChanged(asyncAnswerOutcomeCaptor.capture())
    val answerOutcome = asyncAnswerOutcomeCaptor.value.getOrThrow()
    assertThat(answerOutcome.destinationCase).isEqualTo(AnswerOutcome.DestinationCase.STATE_NAME)
    assertThat(answerOutcome.feedback.html).isEmpty()
  }

  @Test
  fun testGetCurrentState_eleventhState_isTerminalState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )

    playThroughPrototypeExploration()

    // Verify that the last state is terminal.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterMoveToPrevious_onThirdState_updatesToCompletedSecondState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    moveToPreviousState()

    // Verify that the current state is the second state, and is completed. It should also have the
    // previously submitted answer, allowing learners to potentially view past answers.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.completedState.getAnswer(0).userAnswer.answer.fraction)
      .isEqualTo(
        Fraction.newBuilder().apply {
          numerator = 1
          denominator = 2
        }.build()
      )
  }

  @Test
  fun testMoveToNext_onFinalState_failsWithError() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeExploration()

    val moveToStateResult = explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify we can't navigate past the last state of the exploration.
    verify(mockAsyncResultLiveDataObserver, atLeastOnce()).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isFailure()).isTrue()
    assertThat(asyncResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot navigate to next state; at most recent state.")
  }

  @Test
  fun testGetCurrentState_afterPlayingFullSecondExploration_returnsTerminalState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    submitImageRegionAnswer(clickX = 0.5f, clickY = 0.5f, clickedRegion = "Saturn")
    moveToNextState()

    // Verify that we're now on the final state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterPlayingFullSecondExploration_diffPath_returnsTerminalState() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    // Click on Jupiter before Saturn to take a slightly different (valid) path through the
    // exploration. (Note that this does not include actual branching).
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    submitImageRegionAnswer(clickX = 0.2f, clickY = 0.5f, clickedRegion = "Jupiter")
    submitImageRegionAnswer(clickX = 0.5f, clickY = 0.5f, clickedRegion = "Saturn")
    moveToNextState()

    // Verify that a different path can also result in reaching the end state.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(TERMINAL_STATE)
  }

  @Test
  fun testGetCurrentState_afterPlayingThroughPreviousExplorations_returnsStateFromSecondExp() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeExploration()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    submitImageRegionAnswer(clickX = 0.2f, clickY = 0.5f, clickedRegion = "Jupiter")

    // Verify that we're on the second-to-last state of the second exploration.
    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    // This state is not in the other test exp.
    assertThat(currentState.state.name).isEqualTo("ImageClickInput")
  }

  @Test
  fun testMoveToNext_beforePlaying_failsWithError_logsException() {
    val moveToStateResult =
      explorationProgressController.moveToNextState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot navigate to a next state if an exploration is not being played.")
  }

  @Test
  fun testMoveToPrevious_navigatedForwardThenBackToInitial_failsWithError_logsException() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()

    val moveToStateResult =
      explorationProgressController.moveToPreviousState()
    moveToStateResult.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot navigate to previous state; at initial state.")
  }

  @Test
  fun testSubmitAnswer_beforePlaying_failsWithError_logsException() {
    val result = explorationProgressController.submitAnswer(
      createMultipleChoiceAnswer(0)
    )
    result.observeForever(mockAsyncAnswerOutcomeObserver)
    testCoroutineDispatchers.runCurrent()
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot submit an answer if an exploration is not being played.")
  }

  @Test
  fun testGetCurrentState_playInvalidExploration_returnsFailure_logsException() {
    val currentStateLiveData =
      explorationProgressController.getCurrentState().toLiveData()
    currentStateLiveData.observeForever(mockCurrentStateLiveDataObserver)

    playExploration(
      profileId.internalId,
      INVALID_TOPIC_ID,
      INVALID_STORY_ID,
      INVALID_EXPLORATION_ID,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(FileNotFoundException::class.java)
    assertThat(exception).hasMessageThat().contains(INVALID_EXPLORATION_ID)
  }

  @Test
  fun testCheckpointing_loadExploration_checkCheckpointIsSaved() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    testCoroutineDispatchers.runCurrent()

    val retrieveCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        TEST_EXPLORATION_ID_2
      ).toLiveData()

    verifyOperationSucceeds(retrieveCheckpointLiveData)
  }

  @Test
  fun testCheckpointing_playThroughMultipleStates_verifyCheckpointHasCorrectPendingStateName() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "Continue"
    )

    playThroughPrototypeState1AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "Fractions",
    )

    playThroughPrototypeState2AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "MultipleChoice",
    )

    playThroughPrototypeState3AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
  }

  @Test
  fun testCheckpointing_advToFourthState_backToPrevState_verifyCheckpointHasCorrectPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
    moveToPreviousState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
  }

  @Test
  fun testCheckpointing_backTwoStates_nextState_verifyCheckpointHasCorrectPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    moveToPreviousState()
    moveToPreviousState()
    moveToNextState()
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "ItemSelectionMinOne",
    )
  }

  @Test
  fun testCheckpointing_advanceToThirdState_submitMultipleAns_checkCheckpointIsSavedAfterEachAns() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 0)
    testCoroutineDispatchers.runCurrent()

    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 1
    )

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 2
    )

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 2)
    testCoroutineDispatchers.runCurrent()

    // count should be equal to zero because on submission of the correct answer, the
    // pendingTopState changes.
    verifyCheckpointHasCorrectCountOfAnswers(
      profileId,
      TEST_EXPLORATION_ID_2,
      countOfAnswers = 0
    )
  }

  @Test
  fun testCheckpointing_advToThirdState_submitAns_prevState_checkCheckpointIsSavedAfterEachAns() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()
    // option 2 is the correct answer to the third state.
    submitMultipleChoiceAnswer(choiceIndex = 1)
    testCoroutineDispatchers.runCurrent()

    moveToPreviousState()
  }

  @Test
  fun testCheckpointing_advToThirdState_moveToPrevState_checkCheckpointHasStateIndexOfThirdState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    moveToPreviousState()

    verifyCheckpointHasCorrectStateIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      stateIndex = 2
    )
  }

  @Test
  fun testCheckpointing_advToThirdState_prevStates_nextState_checkCheckpointHasCorrectStateIndex() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    moveToPreviousState()
    moveToPreviousState()
    moveToNextState()

    verifyCheckpointHasCorrectStateIndex(
      profileId,
      TEST_EXPLORATION_ID_2,
      stateIndex = 2
    )
  }

  @Test
  fun testCheckpointing_onStateWithContinueInteraction_pressContinue_correctCheckpointIsSaved() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    // Verify that checkpoint is saved when the exploration moves to the new pendingTopState.
    verifyCheckpointHasCorrectPendingStateName(
      profileId,
      TEST_EXPLORATION_ID_2,
      pendingStateName = "Fractions"
    )
  }

  @Test
  fun testCheckpointing_noCheckpointSaved_checkCheckpointStateIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    testCoroutineDispatchers.runCurrent()

    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.checkpointState)
      .isEqualTo(CheckpointState.CHECKPOINT_UNSAVED)
  }

  @Test
  fun testCheckpointing_saveCheckpoint_checkCheckpointStateIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    testCoroutineDispatchers.runCurrent()

    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.checkpointState)
      .isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT)
  }

  @Test
  fun testCheckpointing_saveCheckpoint_databaseFull_checkCheckpointStateIsCorrect() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    testCoroutineDispatchers.runCurrent()

    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()

    testCoroutineDispatchers.runCurrent()

    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.checkpointState)
      .isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT)
  }

  @Test
  fun testCheckpointing_OnSecondState_resumeExploration_expResumedFromCorrectPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that we're on the second state of the second exploration.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_OnSecondState_navigateBack_resumeExploration_checkResumedFromSecondState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    moveToPreviousState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that we're on the second state of the second exploration.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_OnSecondState_submitWrongAns_resumeExploration_checkWrongAnswersVisible() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that three wrong answers are visible to the user.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.pendingState.wrongAnswerCount).isEqualTo(3)
  }

  @Test
  fun testCheckpointing_OnSecondState_submitRightAns_resumeExploration_expResumedFromCompState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    submitPrototypeState2Answer()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
  }

  @Test
  fun testCheckpointing_submitAns_moveToNextState_resumeExploration_answersVisibleOnPrevState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    playThroughPrototypeState2AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    moveToPreviousState()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.completedState.answerCount).isEqualTo(3)
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkPendingStateDoesNotHaveANextState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.hasNextState).isFalse()
  }

  @Test
  fun testCheckpointing_onFirstState_resumeExploration_checkStateDoesNotHaveAPrevState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )

    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.state.name).isEqualTo("Continue")
    assertThat(currentState.hasPreviousState).isFalse()
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkFirstStateHasANextState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    moveToPreviousState()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name).isEqualTo("Continue")
    assertThat(currentState.hasNextState).isTrue()
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkFirstStateDoesNotHaveAPrevState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )
    moveToPreviousState()

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name).isEqualTo("Continue")
    assertThat(currentState.hasPreviousState).isFalse()
  }

  @Test
  fun testCheckpointing_onSecondState_resumeExploration_checkSecondStateHasAPrevState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that we're on the second state of the second exploration because the continue button
    // was not pressed after submitting the correct answer.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.hasPreviousState).isTrue()
  }

  @Test
  fun testCheckpointing_submitAns_doNotPressContinueBtn_resumeExp_pendingStateHasNoNextState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitPrototypeState2Answer()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that the current state is a completed state but has no next state because we have
    // not navigated to the latest pending state yet.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.stateTypeCase).isEqualTo(COMPLETED_STATE)
    assertThat(currentState.state.name).isEqualTo("Fractions")
    assertThat(currentState.hasNextState).isFalse()
  }

  @Test
  fun testCheckpointing_noHintVisible_resumeExp_notHintVisibleOnPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that the helpIndex.IndexTypeCase is equal to INDEX_TYPE_NOT_SET because no hint
    // is visible yet.
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET)
  }

  @Test
  fun testCheckpointing_unrevealedHintIsVisible_resumeExp_unrevealedHintIsVisibleOnPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    // Make the first hint visible by submitting two wrong answers.
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()
    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that the helpIndex.IndexTypeCase is equal AVAILABLE_NEXT_HINT_HINT_INDEX because a new
    // unrevealed hint is visible
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX)
    assertThat(currentState.helpIndex.availableNextHintIndex).isEqualTo(0)
  }

  @Test
  fun testCheckpointing_revealedHintIsVisible_resumeExp_revealedHintIsVisibleOnPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val result = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isTrue()
    assertThat(currentState.state.interaction.solution.solutionIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX)
    assertThat(currentState.helpIndex.latestRevealedHintIndex).isEqualTo(0)
  }

  @Test
  fun testCheckpointing_unrevealedSolIsVisible_resumeExp_unrevealedSolIsVisibleOnPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val result = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    result.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    // The solution should be visible after 30 seconds of the last hint being reveled.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()

    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isTrue()
    assertThat(currentState.state.interaction.solution.solutionIsRevealed).isFalse()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.SHOW_SOLUTION)
  }

  @Test
  fun testCheckpointing_revealedSolutionIsVisible_resumeExp_revealedSolIsVisibleOnPendingState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      ExplorationCheckpoint.getDefaultInstance()
    )
    playThroughPrototypeState1AndMoveToNextState()
    submitWrongAnswerForPrototypeState2()
    submitWrongAnswerForPrototypeState2()

    val hintResult = explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0,
    )
    hintResult.observeForever(mockAsyncHintObserver)
    testCoroutineDispatchers.runCurrent()

    // The solution should be visible after 30 seconds of the last hint being reveled.
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.SECONDS.toMillis(30))
    testCoroutineDispatchers.runCurrent()

    val solutionResult = explorationProgressController.submitSolutionIsRevealed()
    solutionResult.observeForever(mockAsyncSolutionObserver)
    testCoroutineDispatchers.runCurrent()

    endExploration()

    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = true,
      retrieveExplorationCheckpoint(profileId, TEST_EXPLORATION_ID_2)
    )

    // Verify that the helpIndex.IndexTypeCase is equal LATEST_REVEALED_HINT_INDEX because a new
    // revealed hint is visible
    verify(mockCurrentStateLiveDataObserver, atLeastOnce())
      .onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    assertThat(currentState.state.interaction.hintList[0].hintIsRevealed).isTrue()
    assertThat(currentState.state.interaction.solution.solutionIsRevealed).isTrue()
    assertThat(currentState.helpIndex.indexTypeCase)
      .isEqualTo(HelpIndex.IndexTypeCase.EVERYTHING_REVEALED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun retrieveExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): ExplorationCheckpoint {
    testCoroutineDispatchers.runCurrent()
    reset(mockExplorationCheckpointObserver)
    val explorationCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        explorationId
      ).toLiveData()
    explorationCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()

    return explorationCheckpointCaptor.value.getOrThrow()
  }

  /**
   * Creates a blank subscription to the current state to ensure that requests to load the
   * exploration complete, otherwise post-load operations may fail. An observer is required since
   * the current mediator live data implementation will only lazily load data based on whether
   * there's an active subscription.
   */
  private fun subscribeToCurrentStateToAllowExplorationToLoad() {
    explorationProgressController.getCurrentState()
      .toLiveData()
      .observeForever(mockCurrentStateLiveDataObserver)
  }

  private fun playExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    verifyOperationSucceeds(
      explorationDataController.startPlayingExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        shouldSavePartialProgress,
        explorationCheckpoint
      )
    )
  }

  private fun submitContinueButtonAnswer() {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createContinueButtonAnswer())
    )
  }

  private fun submitFractionAnswer(fraction: Fraction) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createFractionAnswer(fraction))
    )
  }

  private fun submitMultipleChoiceAnswer(choiceIndex: Int) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createMultipleChoiceAnswer(choiceIndex))
    )
  }

  private fun submitItemSelectionAnswer(vararg contentIds: String) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createItemSelectionAnswer(contentIds.toList()))
    )
  }

  private fun submitNumericInputAnswer(numericAnswer: Double) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createNumericInputAnswer(numericAnswer))
    )
  }

  private fun submitRatioInputAnswer(ratioExpression: RatioExpression) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createRatioInputAnswer(ratioExpression))
    )
  }

  private fun submitTextInputAnswer(@Suppress("SameParameterValue") textAnswer: String) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createTextInputAnswer(textAnswer))
    )
  }

  private fun submitDragAndDropAnswer(vararg selectedChoicesLists: List<String>) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(
        createDragAndDropAnswer(selectedChoicesLists.toList())
      )
    )
  }

  private fun submitImageRegionAnswer(clickX: Float, clickY: Float, clickedRegion: String) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(
        createImageRegionAnswer(clickX, clickY, clickedRegion)
      )
    )
  }

  private fun playThroughPrototypeExploration() {
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    playThroughPrototypeState6AndMoveToNextState()
    playThroughPrototypeState7AndMoveToNextState()
    playThroughPrototypeState8AndMoveToNextState()
    playThroughPrototypeState9AndMoveToNextState()
    playThroughPrototypeState10AndMoveToNextState()
  }

  private fun navigateToPrototypeFractionInputState() {
    // Fraction input is the second state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
  }

  private fun navigateToPrototypeMultipleChoiceState() {
    // Multiple choice is the third state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
  }

  private fun navigateToPrototypeNumericInputState() {
    // Numeric input is the sixth state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
  }

  private fun navigateToPrototypeTextInputState() {
    // Text input is the eighth state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
    playThroughPrototypeState2AndMoveToNextState()
    playThroughPrototypeState3AndMoveToNextState()
    playThroughPrototypeState4AndMoveToNextState()
    playThroughPrototypeState5AndMoveToNextState()
    playThroughPrototypeState6AndMoveToNextState()
    playThroughPrototypeState7AndMoveToNextState()
  }

  private fun submitPrototypeState1Answer() {
    // First state: Continue interaction.
    submitContinueButtonAnswer()
  }

  private fun submitPrototypeState2Answer() {
    // Second state: Fraction input. Correct answer: 1/2.
    submitFractionAnswer(
      Fraction.newBuilder().apply {
        numerator = 1
        denominator = 2
      }.build()
    )
  }

  private fun submitWrongAnswerForPrototypeState2() {
    submitFractionAnswer(
      Fraction.newBuilder().apply {
        numerator = 1
        denominator = 3
      }.build()
    )
  }

  private fun submitPrototypeState3Answer() {
    // Third state: Multiple choice. Correct answer: Eagle (second third choice).
    submitMultipleChoiceAnswer(choiceIndex = 2)
  }

  private fun submitPrototypeState4Answer() {
    // Fourth state: Item selection (radio buttons). Correct answer: Green (first choice).
    submitItemSelectionAnswer("ca_choices_0")
  }

  private fun submitPrototypeState5Answer() {
    // Fifth state: Item selection (checkboxes). Correct answer: {Red, Green, Blue}.
    submitItemSelectionAnswer("ca_choices_0", "ca_choices_3", "ca_choices_2")
  }

  private fun submitPrototypeState6Answer() {
    // Sixth state: Numeric input. Correct answer: 121.
    submitNumericInputAnswer(121.0)
  }

  private fun submitPrototypeState7Answer() {
    // Seventh state: Ratio input. Correct answer: 4:5.
    submitRatioInputAnswer(
      RatioExpression.newBuilder().apply {
        addAllRatioComponent(listOf(4, 5))
      }.build()
    )
  }

  private fun submitPrototypeState8Answer() {
    // Eighth state: Text input. Correct answer: finnish.
    submitTextInputAnswer("finnish")
  }

  private fun submitPrototypeState9Answer() {
    // Ninth state: Drag Drop Sort. Initial configuration: ca_choices_0, ca_choices_1, ca_choices_2,
    // ca_choices_3. Correct answer: Move 1st item to 4th position.
    submitDragAndDropAnswer(
      listOf("ca_choices_1"),
      listOf("ca_choices_2"),
      listOf("ca_choices_3"),
      listOf("ca_choices_0"),
    )
  }

  private fun submitPrototypeState10Answer() {
    // Tenth state: Drag Drop Sort. Initial configuration: ca_choices_0, ca_choices_1, ca_choices_2,
    // ca_choices_3. Correct answer: Move 1st item to 4th position. Correct answer: Merge first two
    // then move 2nd item to 3rd position.
    submitDragAndDropAnswer(
      listOf("ca_choices_0", "ca_choices_1"),
      listOf("ca_choices_3"),
      listOf("ca_choices_2"),
    )
  }

  private fun playThroughPrototypeState1AndMoveToNextState() {
    submitPrototypeState1Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState2AndMoveToNextState() {
    submitPrototypeState2Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState3AndMoveToNextState() {
    submitPrototypeState3Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState4AndMoveToNextState() {
    submitPrototypeState4Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState5AndMoveToNextState() {
    submitPrototypeState5Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState6AndMoveToNextState() {
    submitPrototypeState6Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState7AndMoveToNextState() {
    submitPrototypeState7Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState8AndMoveToNextState() {
    submitPrototypeState8Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState9AndMoveToNextState() {
    submitPrototypeState9Answer()
    moveToNextState()
  }

  private fun playThroughPrototypeState10AndMoveToNextState() {
    submitPrototypeState10Answer()
    moveToNextState()
  }

  private fun moveToNextState() {
    verifyOperationSucceeds(explorationProgressController.moveToNextState())
  }

  private fun moveToPreviousState() {
    verifyOperationSucceeds(explorationProgressController.moveToPreviousState())
  }

  private fun endExploration() {
    verifyOperationSucceeds(explorationDataController.stopPlayingExploration())
  }

  private fun createContinueButtonAnswer() =
    createTextInputAnswer(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER)

  private fun createFractionAnswer(fraction: Fraction): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        this.fraction = fraction
      }.build()
    )
  }

  private fun createMultipleChoiceAnswer(choiceIndex: Int): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        nonNegativeInt = choiceIndex
      }.build()
    )
  }

  private fun createItemSelectionAnswer(contentIds: List<String>): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        setOfTranslatableHtmlContentIds = SetOfTranslatableHtmlContentIds.newBuilder().apply {
          addAllContentIds(
            contentIds.map { choice ->
              TranslatableHtmlContentId.newBuilder().apply { contentId = choice }.build()
            }
          )
        }.build()
      }.build()
    )
  }

  private fun createNumericInputAnswer(numericAnswer: Double): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        real = numericAnswer
      }.build()
    )
  }

  private fun createRatioInputAnswer(ratioExpression: RatioExpression): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        this.ratioExpression = ratioExpression
      }.build()
    )
  }

  private fun createTextInputAnswer(textAnswer: String): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        normalizedString = textAnswer
      }.build()
    )
  }

  private fun createDragAndDropAnswer(selectedChoicesLists: List<List<String>>): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        listOfSetsOfTranslatableHtmlContentIds =
          ListOfSetsOfTranslatableHtmlContentIds.newBuilder().apply {
            addAllContentIdLists(
              selectedChoicesLists.map { choices ->
                SetOfTranslatableHtmlContentIds.newBuilder().apply {
                  addAllContentIds(
                    choices.map { choice ->
                      TranslatableHtmlContentId.newBuilder().apply { contentId = choice }.build()
                    }
                  )
                }.build()
              }
            )
          }.build()
      }.build()
    )
  }

  private fun createImageRegionAnswer(
    clickX: Float,
    clickY: Float,
    clickedRegion: String
  ): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        clickOnImage = ClickOnImage.newBuilder().apply {
          clickPosition = Point2d.newBuilder().apply {
            x = clickX
            y = clickY
          }.build()
          addClickedRegions(clickedRegion)
        }.build()
      }.build()
    )
  }

  private fun convertToUserAnswer(answer: InteractionObject): UserAnswer {
    return UserAnswer.newBuilder().setAnswer(answer).setPlainAnswer(answer.toAnswerString()).build()
  }

  private fun createCheckpointForFirstHint(
    isHintRevealed: Boolean,
    index: Int
  ): ExplorationCheckpoint {
    return if (isHintRevealed) {
      ExplorationCheckpoint.newBuilder()
        // The state Practice 10 of Fractions, story 0, exp 0 has 3 hints and a solution.
        .setPendingStateName("Practice 10")
        .setHelpIndex(HelpIndex.newBuilder().setAvailableNextHintIndex(index).build())
        .build()
    } else {
      ExplorationCheckpoint.newBuilder()
        // The state Practice 10 of Fractions, story 0, exp 0 has 3 hints and a solution.
        .setPendingStateName("Practice 10")
        .setHelpIndex(HelpIndex.newBuilder().setLatestRevealedHintIndex(index).build())
        .build()
    }
  }

  private fun createCheckpointForSolution(isSolutionRevealed: Boolean): ExplorationCheckpoint {
    return if (isSolutionRevealed) {
      ExplorationCheckpoint.newBuilder()
        // The state Practice 10 of Fractions, story 0, exp 0 has 3 hints and a solution.
        .setPendingStateName("Practice 10")
        .setHelpIndex(HelpIndex.newBuilder().setShowSolution(true).build())
        .build()
    } else {
      ExplorationCheckpoint.newBuilder()
        // The state Practice 10 of Fractions, story 0, exp 0 has 3 hints and a solution.
        .setPendingStateName("Practice 10")
        .setHelpIndex(HelpIndex.newBuilder().setEverythingRevealed(true).build())
        .build()
    }
  }

  private fun verifyCheckpointHasCorrectPendingStateName(
    profileId: ProfileId,
    explorationId: String,
    pendingStateName: String
  ) {
    testCoroutineDispatchers.runCurrent()
    reset(mockExplorationCheckpointObserver)
    val explorationCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        explorationId
      ).toLiveData()
    explorationCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()

    assertThat(explorationCheckpointCaptor.value.getOrThrow().pendingStateName)
      .isEqualTo(pendingStateName)
  }

  private fun verifyCheckpointHasCorrectCountOfAnswers(
    profileId: ProfileId,
    explorationId: String,
    countOfAnswers: Int
  ) {
    testCoroutineDispatchers.runCurrent()
    reset(mockExplorationCheckpointObserver)
    val explorationCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        explorationId
      ).toLiveData()
    explorationCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()

    assertThat(explorationCheckpointCaptor.value.getOrThrow().pendingUserAnswersCount)
      .isEqualTo(countOfAnswers)
  }

  private fun verifyCheckpointHasCorrectStateIndex(
    profileId: ProfileId,
    explorationId: String,
    stateIndex: Int
  ) {
    testCoroutineDispatchers.runCurrent()
    reset(mockExplorationCheckpointObserver)
    val explorationCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        explorationId
      ).toLiveData()
    explorationCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()

    assertThat(explorationCheckpointCaptor.value.getOrThrow().stateIndex)
      .isEqualTo(stateIndex)
  }

  /**
   * Verifies that the specified live data provides at least one successful operation. This will
   * change test-wide mock state, and synchronizes background execution.
   */
  private fun <T : Any?> verifyOperationSucceeds(liveData: LiveData<AsyncResult<T>>) {
    reset(mockAsyncResultLiveDataObserver)
    liveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    asyncResultCaptor.value.apply {
      // This bit of conditional logic is used to add better error reporting when failures occur.
      if (isFailure()) {
        throw AssertionError("Operation failed", getErrorOrNull())
      }
      assertThat(isSuccess()).isTrue()
    }
    reset(mockAsyncResultLiveDataObserver)
  }

  /**
   * Verifies that the specified live data provides a failure result. This will change test-wide
   * mock state, and synchronizes background execution.
   */
  private fun <T : Any?> verifyOperationFails(liveData: LiveData<AsyncResult<T>>) {
    reset(mockAsyncResultLiveDataObserver)
    liveData.observeForever(mockAsyncResultLiveDataObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    asyncResultCaptor.value.apply {
      // This bit of conditional logic is used to add better error reporting when failures occur.
      assertThat(isFailure()).isTrue()
    }
    reset(mockAsyncResultLiveDataObserver)
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

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @TopicListToCache
    fun provideTopicListToCache(): List<String> = listOf()

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  @Module
  class TestExplorationStorageModule {

    /**
     * Provides the size allocated to exploration checkpoint database.
     *
     * For testing, the current [ExplorationStorageDatabaseSize] is set to be 150 Bytes.
     *
     * The size of checkpoint for the the first state in [TEST_EXPLORATION_ID_2] is equal to
     * 150 Bytes, therefore the database will exceeded the allocated limit when the second
     * checkpoint is stored for [TEST_EXPLORATION_ID_2]
     */
    @Provides
    @ExplorationStorageDatabaseSize
    fun provideExplorationStorageDatabaseSize(): Int = 150
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, TestLogReportingModule::class,
      ImageClickInputModule::class, LogStorageModule::class, TestDispatcherModule::class,
      RatioInputModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      TestExplorationStorageModule::class, HintsAndSolutionConfigModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationProgressControllerTest: ExplorationProgressControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationProgressControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationProgressControllerTest: ExplorationProgressControllerTest) {
      component.inject(explorationProgressControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
