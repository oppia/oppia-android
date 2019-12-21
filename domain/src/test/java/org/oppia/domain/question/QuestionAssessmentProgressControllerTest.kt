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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
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
import org.oppia.app.model.EphemeralState.StateTypeCase.PENDING_STATE
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
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

/** Tests for [QuestionAssessmentProgressController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class QuestionAssessmentProgressControllerTest {
  private val TEST_SKILL_ID_LIST_012 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, TEST_SKILL_ID_2)
  private val TEST_SKILL_ID_LIST_02 = listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_2)

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var questionTrainingController: QuestionTrainingController

  @Inject lateinit var questionAssessmentProgressController: QuestionAssessmentProgressController

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  @Mock
  lateinit var mockCurrentQuestionLiveDataObserver: Observer<AsyncResult<EphemeralQuestion>>

  @Mock
  lateinit var mockAsyncResultLiveDataObserver: Observer<AsyncResult<Any>>

  @Captor
  lateinit var currentQuestionResultCaptor: ArgumentCaptor<AsyncResult<EphemeralQuestion>>

  @Captor
  lateinit var asyncResultCaptor: ArgumentCaptor<AsyncResult<Any>>

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  // TODO(#111): Add tests for the controller once there's a real controller to test.

  @Test
  @ExperimentalCoroutinesApi
  fun testStartTrainingSession_succeeds() = runBlockingTest(coroutineContext) {
    val resultLiveData = questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)
    resultLiveData.observeForever(mockAsyncResultLiveDataObserver)
    advanceUntilIdle()

    verify(mockAsyncResultLiveDataObserver).onChanged(asyncResultCaptor.capture())
    assertThat(asyncResultCaptor.value.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStopTrainingSession_afterStartingPreviousSession_succeeds() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)

    val resultLiveData = questionTrainingController.stopQuestionTrainingSession()
    advanceUntilIdle()

    assertThat(resultLiveData.value).isNotNull()
    assertThat(resultLiveData.value!!.isSuccess()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStartTrainingSession_afterStartingPreviousSession_fails() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)

    val resultLiveData = questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_02)
    advanceUntilIdle()

    assertThat(resultLiveData.value).isNotNull()
    assertThat(resultLiveData.value!!.isFailure()).isTrue()
    assertThat(resultLiveData.value!!.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot start a new training session until the previous one is completed")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStopTrainingSession_withoutStartingSession_fails() = runBlockingTest(coroutineContext) {
    val resultLiveData = questionTrainingController.stopQuestionTrainingSession()
    advanceUntilIdle()

    assertThat(resultLiveData.value).isNotNull()
    assertThat(resultLiveData.value!!.isFailure()).isTrue()
    assertThat(resultLiveData.value!!.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot stop a new training session which wasn't started")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_noSessionStarted_returnsPendingResult() = runBlockingTest(coroutineContext) {
    val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentQuestionLiveDataObserver).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_sessionStarted_withEmptyQuestionList_fails() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(listOf())

    val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isFailure()).isTrue()
    assertThat(currentQuestionResultCaptor.value.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot start a training session with zero questions.")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetCurrentQuestion_sessionStarted_returnsInitialQuestion() = runBlockingTest(coroutineContext) {
    questionTrainingController.startQuestionTrainingSession(TEST_SKILL_ID_LIST_012)

    val resultLiveData = questionAssessmentProgressController.getCurrentQuestion()
    resultLiveData.observeForever(mockCurrentQuestionLiveDataObserver)
    advanceUntilIdle()

    verify(mockCurrentQuestionLiveDataObserver, atLeastOnce()).onChanged(currentQuestionResultCaptor.capture())
    assertThat(currentQuestionResultCaptor.value.isSuccess()).isTrue()
    val ephemeralQuestion = currentQuestionResultCaptor.value.getOrThrow()
    assertThat(ephemeralQuestion.currentQuestionIndex).isEqualTo(0)
    assertThat(ephemeralQuestion.totalQuestionCount).isEqualTo(3)
    assertThat(ephemeralQuestion.ephemeralState.stateTypeCase).isEqualTo(PENDING_STATE)
    assertThat(ephemeralQuestion.ephemeralState.state.content.html).contains("What is the numerator")
  }

  private fun setUpTestApplicationComponent() {
    DaggerQuestionAssessmentProgressControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
    fun provideQuestionTrainingSeed(): Long = questionSeed++
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class,TestQuestionModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(questionAssessmentProgressControllerTest: QuestionAssessmentProgressControllerTest)
  }
}
