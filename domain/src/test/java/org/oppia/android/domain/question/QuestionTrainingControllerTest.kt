package org.oppia.android.domain.question

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
import org.oppia.android.app.model.ProfileId
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
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.TEST_QUESTION_ID_0
import org.oppia.android.domain.topic.TEST_QUESTION_ID_1
import org.oppia.android.domain.topic.TEST_QUESTION_ID_3
import org.oppia.android.domain.topic.TEST_SKILL_ID_0
import org.oppia.android.domain.topic.TEST_SKILL_ID_1
import org.oppia.android.domain.topic.TEST_SKILL_ID_2
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
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

/** Tests for [QuestionTrainingController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = QuestionTrainingControllerTest.TestApplication::class)
class QuestionTrainingControllerTest {
  @Inject lateinit var questionTrainingController: QuestionTrainingController
  @Inject lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private lateinit var profileId1: ProfileId

  @Before
  fun setUp() {
    profileId1 = ProfileId.newBuilder().setLoggedInInternalProfileId(1).build()
  }

  @Test
  fun testController_startTrainingSession_succeeds() {
    setUpTestApplicationComponent(questionSeed = 0)

    val questionListDataProvider =
      questionTrainingController.startQuestionTrainingSession(
        profileId1, listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
      )

    monitorFactory.waitForNextSuccessfulResult(questionListDataProvider)
  }

  @Test
  fun testController_startTrainingSession_sessionStartsWithInitialQuestion() {
    setUpTestApplicationComponent(questionSeed = 0)
    questionTrainingController.startQuestionTrainingSession(
      profileId1, listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    testCoroutineDispatchers.runCurrent()

    val result = questionAssessmentProgressController.getCurrentQuestion()

    val ephemeralQuestion = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(ephemeralQuestion.question.questionId).isEqualTo(TEST_QUESTION_ID_1)
  }

  @Test
  fun testController_startTrainingSession_differentSeed_succeeds() {
    setUpTestApplicationComponent(questionSeed = 2)

    val questionListDataProvider =
      questionTrainingController.startQuestionTrainingSession(
        profileId1, listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
      )

    monitorFactory.waitForNextSuccessfulResult(questionListDataProvider)
  }

  @Test
  fun testController_startTrainingSession_differentSeed_sessionStartsWithInitialQuestion() {
    setUpTestApplicationComponent(questionSeed = 2)
    questionTrainingController.startQuestionTrainingSession(
      profileId1, listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    testCoroutineDispatchers.runCurrent()

    val result = questionAssessmentProgressController.getCurrentQuestion()

    val ephemeralQuestion = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(ephemeralQuestion.question.questionId).isEqualTo(TEST_QUESTION_ID_0)
  }

  @Test
  fun testController_startTrainingSession_differentSkills_succeeds() {
    setUpTestApplicationComponent(questionSeed = 0)

    val questionListDataProvider =
      questionTrainingController.startQuestionTrainingSession(
        profileId1, listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
      )

    monitorFactory.waitForNextSuccessfulResult(questionListDataProvider)
  }

  @Test
  fun testController_startTrainingSession_differentSkills_sessionStartsWithInitialQuestion() {
    setUpTestApplicationComponent(questionSeed = 0)
    questionTrainingController.startQuestionTrainingSession(
      profileId1, listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
    )
    testCoroutineDispatchers.runCurrent()

    val result = questionAssessmentProgressController.getCurrentQuestion()

    val ephemeralQuestion = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(ephemeralQuestion.question.questionId).isEqualTo(TEST_QUESTION_ID_3)
  }

  @Test
  fun testController_startTrainingSession_noSkills_fails() {
    setUpTestApplicationComponent(questionSeed = 0)

    val questionListDataProvider =
      questionTrainingController.startQuestionTrainingSession(profileId1, listOf())

    monitorFactory.waitForNextFailureResult(questionListDataProvider)
  }

  @Test
  fun testStopTrainingSession_withoutStartingSession_returnsFailure() {
    setUpTestApplicationComponent(questionSeed = 0)

    val resultProvider = questionTrainingController.stopQuestionTrainingSession()

    val result = monitorFactory.waitForNextFailureResult(resultProvider)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  private fun setUpTestApplicationComponent(questionSeed: Long) {
    TestQuestionModule.questionSeed = questionSeed
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
    // module in tests to avoid needing to specify these settings for tests.
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

  @Module
  class TestQuestionModule {
    companion object {
      var questionSeed = 0L
    }

    @Provides
    @QuestionCountPerTrainingSession
    fun provideQuestionCountPerTrainingSession(): Int = 3

    @Provides
    @QuestionTrainingSeed
    fun provideQuestionTrainingSeed(): Long = questionSeed

    @Provides
    @ViewHintScorePenalty
    fun provideViewHintScorePenalty(): Int = 1

    @Provides
    @WrongAnswerScorePenalty
    fun provideWrongAnswerScorePenalty(): Int = 1

    @Provides
    @MaxScorePerQuestion
    fun provideMaxScorePerQuestion(): Int = 10

    @Provides
    @InternalScoreMultiplyFactor
    fun provideInternalScoreMultiplyFactor(): Int = 10

    @Provides
    @MaxMasteryGainPerQuestion
    fun provideMaxMasteryGainPerQuestion(): Int = 10

    @Provides
    @MaxMasteryLossPerQuestion
    fun provideMaxMasteryLossPerQuestion(): Int = -10

    @Provides
    @ViewHintMasteryPenalty
    fun provideViewHintMasteryPenalty(): Int = 2

    @Provides
    @WrongAnswerMasteryPenalty
    fun provideWrongAnswerMasteryPenalty(): Int = 5

    @Provides
    @InternalMasteryMultiplyFactor
    fun provideInternalMasteryMultiplyFactor(): Int = 100
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      DragDropSortInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class, InteractionsModule::class,
      TestQuestionModule::class, TestLogReportingModule::class, ImageClickInputModule::class,
      LogStorageModule::class, TestDispatcherModule::class, RatioInputModule::class,
      RobolectricModule::class, FakeOppiaClockModule::class, CachingTestModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, AssetModule::class, LocaleProdModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, LoggingIdentifierModule::class,
      ApplicationLifecycleModule::class, SyncStatusModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(questionTrainingControllerTest: QuestionTrainingControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerQuestionTrainingControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(questionTrainingControllerTest: QuestionTrainingControllerTest) {
      component.inject(questionTrainingControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
