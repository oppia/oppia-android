package org.oppia.domain.question

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
import org.junit.Before
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
import org.oppia.app.model.EphemeralQuestion
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.topic.TEST_QUESTION_ID_0
import org.oppia.domain.topic.TEST_QUESTION_ID_1
import org.oppia.domain.topic.TEST_QUESTION_ID_3
import org.oppia.domain.topic.TEST_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.domain.topic.TEST_SKILL_ID_2
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [QuestionTrainingController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
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

  @Before
  fun setUp() {
    TestQuestionModule.questionSeed = 0
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerQuestionTrainingControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testController_startTrainingSession_succeeds() {
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
    questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    testCoroutineDispatchers.runCurrent()

    val resultLiveData =
      questionAssessmentProgressController.getCurrentQuestion()
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
    TestQuestionModule.questionSeed = 2
    setUpTestApplicationComponent() // Recreate with the new seed
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
    TestQuestionModule.questionSeed = 2
    setUpTestApplicationComponent() // Recreate with the new seed
    questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    testCoroutineDispatchers.runCurrent()

    val resultLiveData =
      questionAssessmentProgressController.getCurrentQuestion()
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
    questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
    )
    testCoroutineDispatchers.runCurrent()

    val resultLiveData =
      questionAssessmentProgressController.getCurrentQuestion()
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
    questionTrainingController.stopQuestionTrainingSession()
    testCoroutineDispatchers.runCurrent()

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot stop a new training session which wasn't started")
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
      LogStorageModule::class, TestDispatcherModule::class, RatioInputModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(questionTrainingControllerTest: QuestionTrainingControllerTest)
  }
}
