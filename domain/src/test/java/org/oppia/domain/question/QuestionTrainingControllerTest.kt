package org.oppia.domain.question

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.topic.StoryProgressControllerTest.TestFirebaseModule
import org.oppia.domain.topic.TEST_QUESTION_ID_0
import org.oppia.domain.topic.TEST_QUESTION_ID_1
import org.oppia.domain.topic.TEST_QUESTION_ID_3
import org.oppia.domain.topic.TEST_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.domain.topic.TEST_SKILL_ID_2
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config

/** Tests for [QuestionTrainingController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class QuestionTrainingControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject lateinit var questionTrainingController: QuestionTrainingController

  @Inject lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController

  @Mock
  lateinit var mockQuestionListObserver: Observer<AsyncResult<Any>>

  @Mock
  lateinit var mockCurrentQuestionLiveDataObserver: Observer<AsyncResult<EphemeralQuestion>>

  @Captor
  lateinit var questionListResultCaptor: ArgumentCaptor<AsyncResult<Any>>

  @Captor
  lateinit var currentQuestionResultCaptor: ArgumentCaptor<AsyncResult<EphemeralQuestion>>

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    TestQuestionModule.questionSeed = 0
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerQuestionTrainingControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_startTrainingSession_succeeds() = runBlockingTest(coroutineContext) {
    val questionListLiveData = questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    advanceUntilIdle()

    questionListLiveData.observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_startTrainingSession_sessionStartsWithInitialQuestion() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    advanceUntilIdle()

    val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    assertThat(currentQuestionResultCaptor.value.getOrThrow().question.questionId).isEqualTo(TEST_QUESTION_ID_1)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_startTrainingSession_differentSeed_succeeds() = runBlockingTest(coroutineContext) {
    TestQuestionModule.questionSeed = 2
    setUpTestApplicationComponent() // Recreate with the new seed
    val questionListLiveData = questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    advanceUntilIdle()

    questionListLiveData.observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_startTrainingSession_differentSeed_sessionStartsWithInitialQuestion() =
    runBlockingTest(coroutineContext) {
      TestQuestionModule.questionSeed = 2
      setUpTestApplicationComponent() // Recreate with the new seed
      questionTrainingController.startQuestionTrainingSession(
        listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
      )
      advanceUntilIdle()

      val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
      resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      advanceUntilIdle()

      verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      assertThat(currentQuestionResultCaptor.value.getOrThrow().question.questionId).isEqualTo(TEST_QUESTION_ID_0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_startTrainingSession_differentSkills_succeeds() = runBlockingTest(coroutineContext) {
    val questionListLiveData = questionTrainingController.startQuestionTrainingSession(
      listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
    )
    advanceUntilIdle()

    questionListLiveData.observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_startTrainingSession_differentSkills_sessionStartsWithInitialQuestion() =
    runBlockingTest(coroutineContext) {
      questionTrainingController.startQuestionTrainingSession(
        listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
      )
      advanceUntilIdle()

      val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
      resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
      advanceUntilIdle()

      verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
      assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
      assertThat(currentQuestionResultCaptor.value.getOrThrow().question.questionId).isEqualTo(TEST_QUESTION_ID_3)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_startTrainingSession_noSkills_fails() = runBlockingTest(coroutineContext) {
    val questionListLiveData = questionTrainingController.startQuestionTrainingSession(listOf())
    advanceUntilIdle()

    questionListLiveData.observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver, atLeastOnce()).onChanged(questionListResultCaptor.capture())
    assertThat(questionListResultCaptor.value.isFailure()).isTrue()
  }

  @Qualifier
  annotation class TestDispatcher

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
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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
  @Component(modules = [
    TestModule::class, ContinueModule::class, FractionInputModule::class, ItemSelectionInputModule::class,
    MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class, NumericInputRuleModule::class,
    TextInputRuleModule::class, InteractionsModule::class, TestQuestionModule::class, TestFirebaseModule::class
  ])
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
