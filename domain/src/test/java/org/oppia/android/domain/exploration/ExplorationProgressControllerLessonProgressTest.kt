package org.oppia.android.domain.exploration

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ItemSelectionAnswerState
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.domain.util.toAnswerString
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/**
 * Tests for [ExplorationProgressController]'s lesson progress wiring, run with the
 * EnableLessonProgressVisualization feature flag enabled.
 *
 * These are kept in a separate test suite because [ExplorationProgressControllerTest] currently
 * does not support feature flag toggling.
 *
 * test_exp_id_2 has five checkpoints along its main path: the initial Continue state, the
 * MultipleChoice, NumberInput and RatioInput states (all marked with is_checkpoint), and the
 * terminal End state. The completed count progresses 1 -> 2 (MultipleChoice) -> 3 (NumberInput)
 * -> 4 (RatioInput) as the learner advances, and counts back down when navigating backward.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationProgressControllerLessonProgressTest.TestApplication::class)
class ExplorationProgressControllerLessonProgressTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var explorationDataController: ExplorationDataController
  @Inject lateinit var explorationProgressController: ExplorationProgressController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val profileId = LegacyProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableLessonProgressVisualization(true)
    TestPlatformParameterModule.forceEnableFlashbackSupport(true)
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testGetCurrentState_initialState_progressIsFirstOfFiveCheckpoints() {
    startPlayingNewExploration(TEST_EXPLORATION_ID_2)

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // The initial state always counts as the first of the exploration's checkpoints.
    assertThat(ephemeralState.hasCheckpointProgress()).isTrue()
    assertThat(ephemeralState.checkpointProgress.completedCheckpointCount).isEqualTo(1)
    assertThat(ephemeralState.checkpointProgress.totalCheckpointCount).isEqualTo(5)
  }

  @Test
  fun testGetCurrentState_passFirstCheckpoint_completedCheckpointCountIncrements() {
    startPlayingNewExploration(TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // The multiple choice state is the exploration's first marked checkpoint.
    val ephemeralState = navigateToMultipleChoiceState()

    assertThat(ephemeralState.checkpointProgress.completedCheckpointCount).isEqualTo(2)
    assertThat(ephemeralState.checkpointProgress.totalCheckpointCount).isEqualTo(5)
  }

  @Test
  fun testGetCurrentState_passSecondCheckpoint_completedCheckpointCountIncrements() {
    startPlayingNewExploration(TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // The numeric input state is the exploration's second marked checkpoint.
    val ephemeralState = navigateToNumericInputState()

    assertThat(ephemeralState.checkpointProgress.completedCheckpointCount).isEqualTo(3)
    assertThat(ephemeralState.checkpointProgress.totalCheckpointCount).isEqualTo(5)
  }

  @Test
  fun testGetCurrentState_passThirdCheckpoint_completedCheckpointCountIncrements() {
    startPlayingNewExploration(TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // The ratio input state is the exploration's third marked checkpoint.
    val ephemeralState = navigateToRatioInputState()

    assertThat(ephemeralState.checkpointProgress.completedCheckpointCount).isEqualTo(4)
    assertThat(ephemeralState.checkpointProgress.totalCheckpointCount).isEqualTo(5)
  }

  @Test
  fun testGetCurrentState_reachTerminalState_allCheckpointsCompleted() {
    startPlayingNewExploration(TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // The terminal state is the exploration's last checkpoint, so reaching it completes every
    // checkpoint and the completed count matches the total.
    val ephemeralState = navigateToTerminalState()

    assertThat(ephemeralState.hasCheckpointProgress()).isTrue()
    assertThat(ephemeralState.checkpointProgress.completedCheckpointCount).isEqualTo(5)
    assertThat(ephemeralState.checkpointProgress.totalCheckpointCount).isEqualTo(5)
  }

  @Test
  fun testGetCurrentState_navigateBackward_completedCountDecreases() {
    startPlayingNewExploration(TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()

    // Advancing to the first marked checkpoint takes the completed count up to 2.
    val forwardState = navigateToMultipleChoiceState()
    assertThat(forwardState.checkpointProgress.completedCheckpointCount).isEqualTo(2)

    // Navigating back to the earlier (non-checkpoint) fraction state drops the completed count back
    // down to just the initial checkpoint.
    val backwardState = moveToPreviousState()
    assertThat(backwardState.checkpointProgress.completedCheckpointCount).isEqualTo(1)
    assertThat(backwardState.checkpointProgress.totalCheckpointCount).isEqualTo(5)
  }

  @Test
  fun testGetCurrentState_noExplorationCheckpoint_hasNoCheckpointProgress() {
    startPlayingNewExploration(
      TEST_EXPLORATION_ID_4, TEST_TOPIC_ID_1, TEST_STORY_ID_2
    )

    val ephemeralState = waitForGetCurrentStateSuccessfulLoad()

    // Explorations without any checkpoint states don't support the indicator, so it stays unset even
    // when the feature is enabled.
    assertThat(ephemeralState.hasCheckpointProgress()).isFalse()
  }

  @Test
  fun testGetCurrentState_duringFlashback_progressIsUnchanged_thenRetainedOnReturn() {
    startPlayingNewExploration(TEST_EXPLORATION_ID_2)
    waitForGetCurrentStateSuccessfulLoad()
    navigateToRatioInputState()

    // Submit a wrong answer so a flashback to the earlier same-skill card is offered, then open it.
    val pendingState = submitRatioInputAnswer(
      RatioExpression.newBuilder().apply { addAllRatioComponent(listOf(4, 7)) }.build()
    )
    val flashbackState = moveToFlashbackState(
      pendingState.pendingState.wrongAnswerList[0].stateNameToRevisit
    )

    // A flashback doesn't move the learner's position in the deck, so the indicator stays visible
    // with the unchanged pre-flashback count while the earlier card is reviewed.
    assertThat(flashbackState.flashbackState).isTrue()
    assertThat(flashbackState.hasCheckpointProgress()).isTrue()
    assertThat(flashbackState.checkpointProgress.completedCheckpointCount).isEqualTo(4)
    assertThat(flashbackState.checkpointProgress.totalCheckpointCount).isEqualTo(5)

    // Returning to the lesson keeps the indicator at the learner's unchanged position.
    val resumedState = moveBackToLatest()
    assertThat(resumedState.flashbackState).isFalse()
    assertThat(resumedState.hasCheckpointProgress()).isTrue()
    assertThat(resumedState.checkpointProgress.completedCheckpointCount).isEqualTo(4)
    assertThat(resumedState.checkpointProgress.totalCheckpointCount).isEqualTo(5)
  }

  private fun startPlayingNewExploration(
    explorationId: String,
    topicId: String = TEST_TOPIC_ID_0,
    storyId: String = TEST_STORY_ID_0
  ) {
    val startPlayingProvider = explorationDataController.startPlayingNewExploration(
      profileId.internalId, TEST_CLASSROOM_ID_0, topicId, storyId, explorationId
    )
    monitorFactory.waitForNextSuccessfulResult(startPlayingProvider)
  }

  private fun waitForGetCurrentStateSuccessfulLoad(): EphemeralState {
    return monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.getCurrentState()
    )
  }

  /** Plays the Continue and fraction states and stops on the (first checkpoint) multiple choice. */
  private fun navigateToMultipleChoiceState(): EphemeralState {
    submitContinueButtonAnswer()
    moveToNextState()
    submitFractionAnswer(fraction = 1, denominator = 2)
    return moveToNextState()
  }

  /** Plays up to and stops on the numeric input state, the exploration's second checkpoint. */
  private fun navigateToNumericInputState(): EphemeralState {
    navigateToMultipleChoiceState()
    submitMultipleChoiceAnswer(choiceIndex = 2)
    moveToNextState()
    submitItemSelectionAnswer(0)
    moveToNextState()
    submitItemSelectionAnswer(0, 3, 2)
    return moveToNextState()
  }

  /** Plays up to and stops on the ratio input state, the exploration's third checkpoint. */
  private fun navigateToRatioInputState(): EphemeralState {
    navigateToNumericInputState()
    submitNumericInputAnswer(121.0)
    return moveToNextState()
  }

  /** Plays through the remaining (non-checkpoint) states and stops on the terminal state. */
  private fun navigateToTerminalState(): EphemeralState {
    navigateToRatioInputState()
    submitRatioInputAnswer(
      RatioExpression.newBuilder().apply { addAllRatioComponent(listOf(4, 5)) }.build()
    )
    moveToNextState()
    submitAnswer(createTextInputAnswer("finnish"))
    moveToNextState()
    submitDragAndDropAnswer(
      listOf("ca_choices_1"),
      listOf("ca_choices_2"),
      listOf("ca_choices_3"),
      listOf("ca_choices_0")
    )
    moveToNextState()
    submitDragAndDropAnswer(
      listOf("ca_choices_0", "ca_choices_1"),
      listOf("ca_choices_3"),
      listOf("ca_choices_2")
    )
    return moveToNextState()
  }

  private fun submitContinueButtonAnswer() {
    submitAnswer(createTextInputAnswer(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER))
  }

  private fun submitFractionAnswer(fraction: Int, denominator: Int) {
    submitAnswer(
      convertToUserAnswer(
        InteractionObject.newBuilder().apply {
          this.fraction = Fraction.newBuilder().apply {
            numerator = fraction
            this.denominator = denominator
          }.build()
        }.build()
      )
    )
  }

  private fun submitNumericInputAnswer(numericAnswer: Double) {
    submitAnswer(
      convertToUserAnswer(
        InteractionObject.newBuilder().apply { real = numericAnswer }.build()
      )
    )
  }

  private fun submitRatioInputAnswer(ratioExpression: RatioExpression): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitAnswer(
        convertToUserAnswer(
          InteractionObject.newBuilder().apply { this.ratioExpression = ratioExpression }.build()
        )
      )
    )
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun submitMultipleChoiceAnswer(choiceIndex: Int) {
    val answer = InteractionObject.newBuilder().apply { nonNegativeInt = choiceIndex }.build()
    submitAnswer(
      UserAnswer.newBuilder()
        .setAnswer(answer)
        .setItemSelectionAnswer(
          ItemSelectionAnswerState.newBuilder().addAllSelectedIndexes(listOf(choiceIndex)).build()
        )
        .build()
    )
  }

  private fun submitItemSelectionAnswer(vararg positions: Int) {
    val contentIds = positions.map { pos -> "ca_choices_$pos" }
    val answer = InteractionObject.newBuilder().apply {
      setOfTranslatableHtmlContentIds = SetOfTranslatableHtmlContentIds.newBuilder().apply {
        addAllContentIds(
          contentIds.map { choice ->
            TranslatableHtmlContentId.newBuilder().apply { contentId = choice }.build()
          }
        )
      }.build()
    }.build()
    submitAnswer(
      UserAnswer.newBuilder()
        .setAnswer(answer)
        .setItemSelectionAnswer(
          ItemSelectionAnswerState.newBuilder().addAllSelectedIndexes(positions.toList()).build()
        )
        .build()
    )
  }

  private fun submitDragAndDropAnswer(vararg selectedChoicesLists: List<String>) {
    submitAnswer(createDragAndDropAnswer(selectedChoicesLists.toList()))
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

  private fun createTextInputAnswer(textAnswer: String): UserAnswer {
    return convertToUserAnswer(
      InteractionObject.newBuilder().apply { normalizedString = textAnswer }.build()
    )
  }

  private fun convertToUserAnswer(answer: InteractionObject): UserAnswer {
    return UserAnswer.newBuilder().setAnswer(answer).setPlainAnswer(answer.toAnswerString()).build()
  }

  private fun submitAnswer(userAnswer: UserAnswer) {
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.submitAnswer(userAnswer)
    )
  }

  private fun moveToNextState(): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(explorationProgressController.moveToNextState())
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun moveToPreviousState(): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(explorationProgressController.moveToPreviousState())
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun moveToFlashbackState(stateName: String): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(
      explorationProgressController.moveToFlashback(stateName)
    )
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun moveBackToLatest(): EphemeralState {
    monitorFactory.waitForNextSuccessfulResult(explorationProgressController.moveBackToLatest())
    return waitForGetCurrentStateSuccessfulLoad()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

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

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(): Boolean = true
  }

  @Singleton
  @Component(
    modules = [
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      AssetModule::class,
      ContinueModule::class,
      DragDropSortInputModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageTestModule::class,
      FakeOppiaClockModule::class,
      FractionInputModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      ImageClickInputModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogStorageModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MultipleChoiceInputModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PlatformParameterSingletonModule::class,
      RatioInputModule::class,
      RobolectricModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class,
      TestPlatformParameterModule::class,
      TextInputRuleModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: ExplorationProgressControllerLessonProgressTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationProgressControllerLessonProgressTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: ExplorationProgressControllerLessonProgressTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
