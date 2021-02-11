package org.oppia.android.domain.question

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.EphemeralQuestion
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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.topic.TEST_QUESTION_ID_0
import org.oppia.android.domain.topic.TEST_QUESTION_ID_1
import org.oppia.android.domain.topic.TEST_QUESTION_ID_3
import org.oppia.android.domain.topic.TEST_SKILL_ID_0
import org.oppia.android.domain.topic.TEST_SKILL_ID_1
import org.oppia.android.domain.topic.TEST_SKILL_ID_2
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.time.FakeOppiaClockModule
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
import javax.inject.Singleton

/** Tests for [QuestionTrainingController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = QuestionTrainingControllerTest.TestApplication::class)
class QuestionTrainingControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var questionTrainingController: QuestionTrainingController

  @Inject
  lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockQuestionListObserver: Observer<AsyncResult<Any>>

  @Mock
  lateinit var mockCurrentQuestionLiveDataObserver: Observer<AsyncResult<EphemeralQuestion>>

  @Captor
  lateinit var questionListResultCaptor: ArgumentCaptor<AsyncResult<Any>>

  @Captor
  lateinit var currentQuestionResultCaptor: ArgumentCaptor<AsyncResult<EphemeralQuestion>>

  @Test
  fun testController_startTrainingSession_succeeds() {
    setUpTestApplicationComponent(questionSeed = 0)
    val questionListLiveData =
      questionTrainingController.startQuestionTrainingSession(
        listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
      )
    testCoroutineDispatchers.runCurrent()

    questionListLiveData.observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testController_startTrainingSession_sessionStartsWithInitialQuestion() {
    setUpTestApplicationComponent(questionSeed = 0)
    questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    testCoroutineDispatchers.runCurrent()

    val resultLiveData =
      questionAssessmentProgressController.getCurrentQuestion().toLiveData()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentQuestionResultCaptor.value.getOrThrow().question.questionId).isEqualTo(
      TEST_QUESTION_ID_1
    )
  }

  @Test
  fun testController_startTrainingSession_differentSeed_succeeds() {
    setUpTestApplicationComponent(questionSeed = 2)
    val questionListLiveData =
      questionTrainingController.startQuestionTrainingSession(
        listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
      )
    testCoroutineDispatchers.runCurrent()

    questionListLiveData.observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testController_startTrainingSession_differentSeed_sessionStartsWithInitialQuestion() {
    setUpTestApplicationComponent(questionSeed = 2)
    questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    testCoroutineDispatchers.runCurrent()

    val resultLiveData =
      questionAssessmentProgressController.getCurrentQuestion().toLiveData()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentQuestionResultCaptor.value.getOrThrow().question.questionId).isEqualTo(
      TEST_QUESTION_ID_0
    )
  }

  @Test
  fun testController_startTrainingSession_differentSkills_succeeds() {
    setUpTestApplicationComponent(questionSeed = 0)
    val questionListLiveData =
      questionTrainingController.startQuestionTrainingSession(
        listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
      )
    testCoroutineDispatchers.runCurrent()

    questionListLiveData.observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  fun testController_startTrainingSession_differentSkills_sessionStartsWithInitialQuestion() {
    setUpTestApplicationComponent(questionSeed = 0)
    questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
    )
    testCoroutineDispatchers.runCurrent()

    val resultLiveData =
      questionAssessmentProgressController.getCurrentQuestion().toLiveData()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentQuestionResultCaptor.value.getOrThrow().question.questionId).isEqualTo(
      TEST_QUESTION_ID_3
    )
  }

  @Test
  fun testController_startTrainingSession_noSkills_fails() {
    setUpTestApplicationComponent(questionSeed = 0)
    val questionListLiveData =
      questionTrainingController.startQuestionTrainingSession(listOf())
    testCoroutineDispatchers.runCurrent()

    questionListLiveData.observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isFailure()).isTrue()
  }

  @Test
  fun testController_startTrainingSession_noSkills_fails_logsException() {
    setUpTestApplicationComponent(questionSeed = 0)
    questionTrainingController.startQuestionTrainingSession(listOf())
    questionTrainingController.startQuestionTrainingSession(listOf())
    testCoroutineDispatchers.runCurrent()

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot start a new training session until the previous one is completed.")
  }

  @Test
  fun testStopTrainingSession_withoutStartingSession_fails_logsException() {
    setUpTestApplicationComponent(questionSeed = 0)
    questionTrainingController.stopQuestionTrainingSession()
    testCoroutineDispatchers.runCurrent()

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot stop a new training session which wasn't started")
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
      RobolectricModule::class, FakeOppiaClockModule::class
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
