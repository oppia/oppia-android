package org.oppia.android.domain.hintsandsolution

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
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HintIndex
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ProfileId
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
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.util.toAnswerString
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
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
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** Tests for [HintHandler]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HintHandlerTest.TestApplication::class)
class HintHandlerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var hintHandler: HintHandler

  @field:[Inject DelayShowInitialHintMillis]
  lateinit var delayShowInitialHintMs: Provider<Long>

  @field:[Inject DelayShowAdditionalHintsMillis]
  lateinit var delayShowAdditionalHintsMs: Provider<Long>

  @field:[Inject DelayShowAdditionalHintsFromWrongAnswerMillis]
  lateinit var delayShowAdditionalHintsFromWrongAnswerMs: Provider<Long>

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var explorationProgressController: ExplorationProgressController

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Inject
  lateinit var explorationCheckpointController: ExplorationCheckpointController

  @Mock
  lateinit var mockCurrentStateLiveDataObserver: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<*>>

  @Captor
  lateinit var currentStateResultCaptor: ArgumentCaptor<AsyncResult<EphemeralState>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testMaybeScheduleShowHint_wrongAnswer_returnsHintStateForHint1() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false
    )
    navigateToPrototypeFractionInputState()
    submitWrongAnswerForPrototypeState2()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    val hintState = hintHandler.maybeScheduleShowHint(
      currentState.state,
      currentState.pendingState.wrongAnswerCount
    )
    assertThat(hintState.hintSequenceNumber).isEqualTo(4)
    assertThat(hintState.trackedAnswerCount).isEqualTo(1)
    assertThat(hintState.helpIndex).isEqualTo(HelpIndex.getDefaultInstance())
    assertThat(hintState.delayToShowNextHintAndSolution).isEqualTo(delayShowInitialHintMs.get())
  }

  @Test
  fun testMaybeScheduleShowHint_revealHint_anotherWrongAnswer_returnsHintStateForSolution() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false
    )
    navigateToPrototypeFractionInputState()
    submitWrongAnswerForPrototypeState2()
    explorationProgressController.submitHintIsRevealed(
      hintIsRevealed = true,
      hintIndex = 0
    )
    submitWrongAnswerForPrototypeState2()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    val hintState = hintHandler.maybeScheduleShowHint(
      currentState.state,
      currentState.pendingState.wrongAnswerCount
    )
    assertThat(hintState.hintSequenceNumber).isEqualTo(4)
    assertThat(hintState.trackedAnswerCount).isEqualTo(2)
    assertThat(hintState.helpIndex).isEqualTo(HelpIndex.newBuilder().setShowSolution(true).build())
    assertThat(hintState.delayToShowNextHintAndSolution).isEqualTo(-1)
  }

  @Test
  fun testShowNewHintAndSolution_returnsCorrectHintState() {
    subscribeToCurrentStateToAllowExplorationToLoad()
    playExploration(
      profileId.internalId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false
    )
    navigateToPrototypeFractionInputState()
    submitWrongAnswerForPrototypeState2()

    verify(
      mockCurrentStateLiveDataObserver,
      atLeastOnce()
    ).onChanged(currentStateResultCaptor.capture())
    assertThat(currentStateResultCaptor.value.isSuccess()).isTrue()
    val currentState = currentStateResultCaptor.value.getOrThrow()
    val hintState = hintHandler.showNewHintAndSolution(
      currentState.state,
      currentState.hintState.hintSequenceNumber
    )
    assertThat(hintState.hintSequenceNumber).isEqualTo(3)
    assertThat(hintState.trackedAnswerCount).isEqualTo(1)
    assertThat(hintState.helpIndex).isEqualTo(
      HelpIndex.newBuilder().setHintIndex(
        HintIndex.newBuilder()
          .setIsHintRevealed(false)
          .setIndex(0)
          .build()
      ).build()
    )
    assertThat(hintState.delayToShowNextHintAndSolution).isEqualTo(delayShowInitialHintMs.get())
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
    shouldSavePartialProgress: Boolean
  ) {
    verifyOperationSucceeds(
      explorationDataController.startPlayingExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        shouldSavePartialProgress
      )
    )
  }

  private fun navigateToPrototypeFractionInputState() {
    // Fraction input is the second state of the exploration.
    playThroughPrototypeState1AndMoveToNextState()
  }

  private fun playThroughPrototypeState1AndMoveToNextState() {
    submitPrototypeState1Answer()
    moveToNextState()
  }

  private fun moveToNextState() {
    verifyOperationSucceeds(explorationProgressController.moveToNextState())
  }

  private fun submitPrototypeState1Answer() {
    // First state: Continue interaction.
    submitContinueButtonAnswer()
  }

  private fun submitContinueButtonAnswer() {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createContinueButtonAnswer())
    )
  }

  private fun createContinueButtonAnswer() =
    createTextInputAnswer(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER)

  private fun createTextInputAnswer(textAnswer: String): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        normalizedString = textAnswer
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

  private fun submitFractionAnswer(fraction: Fraction) {
    verifyOperationSucceeds(
      explorationProgressController.submitAnswer(createFractionAnswer(fraction))
    )
  }

  private fun createFractionAnswer(fraction: Fraction): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply {
        this.fraction = fraction
      }.build()
    )
  }

  private fun convertToUserAnswer(answer: InteractionObject): UserAnswer {
    return UserAnswer.newBuilder().setAnswer(answer).setPlainAnswer(answer.toAnswerString()).build()
  }

  /**
   * Verifies that the specified live data provides at least one successful operation. This will
   * change test-wide mock state, and synchronizes background execution.
   */
  private fun <T : Any?> verifyOperationSucceeds(liveData: LiveData<AsyncResult<T>>) {
    Mockito.reset(mockAsyncResultLiveDataObserver)
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
    Mockito.reset(mockAsyncResultLiveDataObserver)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    //  module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

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

    fun inject(hintHandlerTest: HintHandlerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHintHandlerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(hintHandlerTest: HintHandlerTest) {
      component.inject(hintHandlerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
