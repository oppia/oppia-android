package org.oppia.android.domain.survey

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
import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.FakeFirestoreEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.logging.EventLogSubject
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [SurveyProgressController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SurveyProgressControllerTest.TestApplication::class)
class SurveyProgressControllerTest {
  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var surveyController: SurveyController

  @Inject
  lateinit var surveyProgressController: SurveyProgressController

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var fakeFirestoreEventLogger: FakeFirestoreEventLogger

  private val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(1).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testStartSurveySession_succeeds() {
    val surveyDataProvider =
      surveyController.startSurveySession(questions, profileId = profileId)

    monitorFactory.waitForNextSuccessfulResult(surveyDataProvider)
  }

  @Test
  fun testStartSurveySession_sessionStartsWithInitialQuestion() {
    startSuccessfulSurveySession()

    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()
    assertThat(ephemeralQuestion.question.questionName).isEqualTo(SurveyQuestionName.USER_TYPE)
  }

  @Test
  fun testGetCurrentQuestion_sessionLoaded_returnsInitialQuestionPending() {
    startSuccessfulSurveySession()

    val ephemeralQuestion = waitForGetCurrentQuestionSuccessfulLoad()

    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isGreaterThan(0)
    assertThat(ephemeralQuestion.question.questionName).isEqualTo(SurveyQuestionName.USER_TYPE)
    assertThat(ephemeralQuestion.hasPreviousQuestion).isEqualTo(false)
    assertThat(ephemeralQuestion.hasNextQuestion).isEqualTo(true)
    assertThat(ephemeralQuestion.questionTypeCase)
      .isEqualTo(EphemeralSurveyQuestion.QuestionTypeCase.PENDING_QUESTION)
  }

  @Test
  fun testGetCurrentQuestion_fourthQuestion_isTerminalQuestion() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)

    val currentQuestion = submitNpsAnswer(7)

    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(3)
    assertThat(currentQuestion.totalQuestionCount).isEqualTo(4)
    assertThat(currentQuestion.questionTypeCase)
      .isEqualTo(EphemeralSurveyQuestion.QuestionTypeCase.TERMINAL_QUESTION)
  }

  @Test
  fun testGetCurrentQuestion_noSessionStarted_throwsException() {
    // Can't retrieve the current question until the survey session is started.
    val getCurrentQuestionProvider = surveyProgressController.getCurrentQuestion()

    val result = monitorFactory.waitForNextFailureResult(getCurrentQuestionProvider)
    assertThat(result).hasCauseThat().hasMessageThat().contains("Survey is not yet initialized.")
  }

  @Test
  fun testSubmitAnswer_beforeStartingSurvey_isFailure() {
    val submitAnswerProvider =
      surveyProgressController.submitAnswer(createUserTypeAnswer(UserTypeAnswer.LEARNER))

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(submitAnswerProvider)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testSubmitAnswer_forUserTypeQuestion_succeeds() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()

    val result = surveyProgressController.submitAnswer(createUserTypeAnswer(UserTypeAnswer.PARENT))

    // Verify that the answer submission was successful.
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forMarketFitQuestion_succeeds() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)

    val result =
      surveyProgressController.submitAnswer(
        createMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)
      )
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forNpsScoreQuestion_succeeds() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)

    val result =
      surveyProgressController.submitAnswer(createNpsAnswer(9))
    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testSubmitAnswer_forTextInput_succeeds() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)
    submitNpsAnswer(7)

    val result =
      surveyProgressController.submitAnswer(
        createTextInputAnswer(
          SurveyQuestionName.PASSIVE_FEEDBACK,
          TEXT_ANSWER
        )
      )

    monitorFactory.waitForNextSuccessfulResult(result)
  }

  @Test
  fun testMoveToNext_beforePlaying_isFailure() {
    val moveToNextProvider = surveyProgressController.moveToNextQuestion()

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(moveToNextProvider)
    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testMoveToNext_onTerminalQuestion_failsWithError() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)
    submitNpsAnswer(7)
    submitTextInputAnswer(SurveyQuestionName.PASSIVE_FEEDBACK, TEXT_ANSWER)

    val moveToNextProvider = surveyProgressController.moveToNextQuestion()

    val error = monitorFactory.waitForNextFailureResult(moveToNextProvider)

    assertThat(error)
      .hasMessageThat()
      .contains("Cannot navigate to next question; at terminal question.")
  }

  @Test
  fun testSubmitAnswer_submitNpsScore0f3_loadsDetractorFeedbackQuestion() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.NOT_DISAPPOINTED)

    val ephemeralQuestion = submitNpsAnswer(3)

    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(3)
    assertThat(ephemeralQuestion.question.questionName)
      .isEqualTo(SurveyQuestionName.DETRACTOR_FEEDBACK)
  }

  @Test
  fun testSubmitAnswer_submitNpsScore0f7_loadsPassiveFeedbackQuestion() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.DISAPPOINTED)

    val ephemeralQuestion = submitNpsAnswer(7)

    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(3)
    assertThat(ephemeralQuestion.question.questionName)
      .isEqualTo(SurveyQuestionName.PASSIVE_FEEDBACK)
  }

  @Test
  fun testSubmitAnswer_submitNpsScore0f10_loadsPromoterFeedbackQuestion() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)

    val ephemeralQuestion = submitNpsAnswer(10)

    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(3)
    assertThat(ephemeralQuestion.question.questionName)
      .isEqualTo(SurveyQuestionName.PROMOTER_FEEDBACK)
  }

  @Test
  fun testMoveToPreviousQuestion_atInitialQuestion_isFailure() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()

    val moveTolPreviousProvider = surveyProgressController.moveToPreviousQuestion()
    val result = monitorFactory.waitForNextFailureResult(moveTolPreviousProvider)

    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat()
      .contains("Cannot navigate to previous question; at initial question.")
  }

  @Test
  fun testMoveToPreviousQuestion_afterMovingToNextQuestion_isSuccess() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.LEARNER)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)

    val currentQuestion = moveToPreviousQuestion()

    assertThat(currentQuestion.currentQuestionIndex).isEqualTo(1)
    assertThat(currentQuestion.question.questionName)
      .isEqualTo(SurveyQuestionName.MARKET_FIT)
  }

  @Test
  fun testSubmitAnswer_afterMovingToPreviousQuestion_isSuccess() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.LEARNER)
    // Submit answer and move to next
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)

    moveToPreviousQuestion()

    // Submit a different answer to the navigated question
    val submitAnswerProvider =
      surveyProgressController.submitAnswer(createMarketFitAnswer(MarketFitAnswer.NOT_DISAPPOINTED))

    // New answer is submitted successfully
    monitorFactory.waitForNextSuccessfulResult(submitAnswerProvider)
  }

  @Test
  fun testStopSurveySession_withoutStartingSession_returnsFailure() {
    val stopProvider = surveyController.stopSurveySession(surveyCompleted = true)

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(stopProvider)

    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  @Test
  fun testStopSurveySession_afterStartingPreviousSession_succeeds() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    val stopProvider = surveyController.stopSurveySession(surveyCompleted = false)
    monitorFactory.waitForNextSuccessfulResult(stopProvider)
  }

  @Test
  fun testEndSurvey_beforeCompletingMandatoryQuestions_logsAbandonSurveyEvent() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    // Submit and navigate to NPS question
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)
    stopSurveySession(surveyCompleted = false)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    EventLogSubject.assertThat(eventLog).hasAbandonSurveyContextThat {
      hasSurveyDetailsThat {
        hasSurveyIdThat().isNotEmpty()
        hasInternalProfileIdThat().isEqualTo("1")
      }
      hasQuestionNameThat().isEqualTo(SurveyQuestionName.NPS)
    }
  }

  @Test
  fun testEndSurvey_afterCompletingMandatoryQuestions_logsMandatorySurveyResponseEvent() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)
    // Submit and navigate to FEEDBACK question
    submitNpsAnswer(10)
    stopSurveySession(surveyCompleted = false)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    EventLogSubject.assertThat(eventLog).hasMandatorySurveyResponseContextThat {
      hasSurveyDetailsThat {
        hasSurveyIdThat().isNotEmpty()
        hasInternalProfileIdThat().isEqualTo("1")
      }
      hasUserTypeAnswerThat().isEqualTo(UserTypeAnswer.PARENT)
      hasMarketFitAnswerThat().isEqualTo(MarketFitAnswer.VERY_DISAPPOINTED)
      hasNpsScoreAnswerThat().isEqualTo(10)
    }
  }

  @Test
  fun testEndSurvey_afterCompletingAllQuestions_logsMandatorySurveyResponseEvent() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)
    submitNpsAnswer(10)
    submitTextInputAnswer(SurveyQuestionName.PROMOTER_FEEDBACK, TEXT_ANSWER)
    stopSurveySession(surveyCompleted = true)

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    EventLogSubject.assertThat(eventLog).hasMandatorySurveyResponseContextThat {
      hasSurveyDetailsThat {
        hasSurveyIdThat().isNotEmpty()
        hasInternalProfileIdThat().isEqualTo("1")
      }
      hasUserTypeAnswerThat().isEqualTo(UserTypeAnswer.PARENT)
      hasMarketFitAnswerThat().isEqualTo(MarketFitAnswer.VERY_DISAPPOINTED)
      hasNpsScoreAnswerThat().isEqualTo(10)
    }
  }

  @Test
  fun testEndSurvey_afterCompletingAllQuestions_logsOptionalSurveyResponseEvent() {
    startSuccessfulSurveySession()
    waitForGetCurrentQuestionSuccessfulLoad()
    submitUserTypeAnswer(UserTypeAnswer.PARENT)
    submitMarketFitAnswer(MarketFitAnswer.VERY_DISAPPOINTED)
    submitNpsAnswer(10)
    submitTextInputAnswer(SurveyQuestionName.PROMOTER_FEEDBACK, TEXT_ANSWER)
    stopSurveySession(surveyCompleted = true)

    val eventLog = fakeFirestoreEventLogger.getMostRecentEvent()

    EventLogSubject.assertThat(eventLog).hasOptionalSurveyResponseContextThat {
      hasSurveyDetailsThat {
        hasSurveyIdThat().isNotEmpty()
        hasInternalProfileIdThat().isEqualTo("1")
      }
      hasFeedbackAnswerThat().isEqualTo(TEXT_ANSWER)
    }
  }

  private fun stopSurveySession(surveyCompleted: Boolean) {
    val stopProvider = surveyController.stopSurveySession(surveyCompleted)
    monitorFactory.waitForNextSuccessfulResult(stopProvider)
  }

  private fun startSuccessfulSurveySession() {
    monitorFactory.waitForNextSuccessfulResult(
      surveyController.startSurveySession(questions, profileId = profileId)
    )
  }

  private fun waitForGetCurrentQuestionSuccessfulLoad(): EphemeralSurveyQuestion {
    return monitorFactory.waitForNextSuccessfulResult(
      surveyProgressController.getCurrentQuestion()
    )
  }

  private fun moveToPreviousQuestion(): EphemeralSurveyQuestion {
    // This operation might fail for some tests.
    monitorFactory.ensureDataProviderExecutes(
      surveyProgressController.moveToPreviousQuestion()
    )
    return waitForGetCurrentQuestionSuccessfulLoad()
  }

  private fun submitAnswer(answer: SurveySelectedAnswer): EphemeralSurveyQuestion {
    monitorFactory.waitForNextSuccessfulResult(
      surveyProgressController.submitAnswer(answer)
    )
    return waitForGetCurrentQuestionSuccessfulLoad()
  }

  private fun submitUserTypeAnswer(answer: UserTypeAnswer): EphemeralSurveyQuestion {
    return submitAnswer(createUserTypeAnswer(answer))
  }

  private fun createUserTypeAnswer(
    answer: UserTypeAnswer
  ): SurveySelectedAnswer {
    return SurveySelectedAnswer.newBuilder()
      .setQuestionName(SurveyQuestionName.USER_TYPE)
      .setUserType(answer)
      .build()
  }

  private fun submitMarketFitAnswer(answer: MarketFitAnswer): EphemeralSurveyQuestion {
    return submitAnswer(createMarketFitAnswer(answer))
  }

  private fun createMarketFitAnswer(
    answer: MarketFitAnswer
  ): SurveySelectedAnswer {
    return SurveySelectedAnswer.newBuilder()
      .setQuestionName(SurveyQuestionName.MARKET_FIT)
      .setMarketFit(answer)
      .build()
  }

  private fun submitNpsAnswer(answer: Int): EphemeralSurveyQuestion {
    return submitAnswer(createNpsAnswer(answer))
  }

  private fun createNpsAnswer(
    answer: Int
  ): SurveySelectedAnswer {
    return SurveySelectedAnswer.newBuilder()
      .setQuestionName(SurveyQuestionName.NPS)
      .setNpsScore(answer)
      .build()
  }

  private fun submitTextInputAnswer(
    questionName: SurveyQuestionName,
    textAnswer: String
  ): EphemeralSurveyQuestion = submitAnswer(createTextInputAnswer(questionName, textAnswer))

  private fun createTextInputAnswer(
    questionName: SurveyQuestionName,
    textAnswer: String
  ): SurveySelectedAnswer {
    return SurveySelectedAnswer.newBuilder()
      .setQuestionName(questionName)
      .setFreeFormAnswer(textAnswer)
      .build()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  @Module
  class TestModule {
    internal companion object {
      var enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
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
    @EnableLearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      // Enable the study by default in tests.
      return PlatformParameterValue.createDefaultParameter(defaultValue = true)
    }
  }

  @Module
  class TestLoggingIdentifierModule {
    companion object {
      const val applicationIdSeed = 1L
    }

    @Provides
    @ApplicationIdSeed
    fun provideApplicationIdSeed(): Long = applicationIdSeed
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ApplicationLifecycleModule::class, TestDispatcherModule::class, LocaleProdModule::class,
      ExplorationProgressModule::class, TestLogReportingModule::class, AssetModule::class,
      NetworkConnectionUtilDebugModule::class, SyncStatusModule::class, LogStorageModule::class,
      TestLoggingIdentifierModule::class, TestAuthenticationModule::class,
    ]
  )

  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(surveyProgressControllerTest: SurveyProgressControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSurveyProgressControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(surveyProgressControllerTest: SurveyProgressControllerTest) {
      component.inject(surveyProgressControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  companion object {
    private const val TEXT_ANSWER = "Some text answer"
    private val questions = listOf(
      SurveyQuestionName.USER_TYPE,
      SurveyQuestionName.MARKET_FIT,
      SurveyQuestionName.NPS
    )
  }
}
