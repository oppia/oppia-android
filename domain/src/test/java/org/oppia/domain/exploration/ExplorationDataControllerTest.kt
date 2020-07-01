package org.oppia.domain.exploration

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
import org.oppia.app.model.Exploration
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_1
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_2
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_3
import org.oppia.domain.topic.TEST_EXPLORATION_ID_3
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestLogReportingModule
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

/** Tests for [ExplorationDataController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ExplorationDataControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Mock
  lateinit var mockExplorationObserver: Observer<AsyncResult<Exploration>>

  @Captor
  lateinit var explorationResultCaptor: ArgumentCaptor<AsyncResult<Exploration>>

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
    DaggerExplorationDataControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForTheWelcomeExploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(TEST_EXPLORATION_ID_5)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)
      val expectedExplorationStateSet = listOf(
        "END", "Estimate 100", "Numeric input",
        "Things you can do", "Welcome!", "What language"
      )

      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("Welcome to Oppia!")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(6)
      assertThat(exploration.statesMap.keys).containsExactlyElementsIn(expectedExplorationStateSet)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForTheAboutOppiaExploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(TEST_EXPLORATION_ID_6)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)
      val expectedExplorationStateSet = listOf(
        "About this website", "Contact", "Contribute", "Credits", "END",
        "End Card", "Example1", "Example3", "First State", "Site License", "So what can I tell you"
      )

      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("About Oppia")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(11)
      assertThat(exploration.statesMap.keys).containsExactlyElementsIn(expectedExplorationStateSet)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForFractions0Exploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(FRACTIONS_EXPLORATION_ID_0)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)
      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())

      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("What is a Fraction?")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(25)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForFractions1Exploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(FRACTIONS_EXPLORATION_ID_1)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)

      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("The Meaning of \"Equal Parts\"")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(18)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForRatios0Exploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_0)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)

      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("What is a Ratio?")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(26)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForRatios1Exploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_1)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)

      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("Order is Important")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(22)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForRatios2Exploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_2)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)

      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("Equivalent Ratios")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(24)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveDataForRatios3Exploration() =
    runBlockingTest(coroutineContext) {
      val explorationLiveData =
        explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_3)
      advanceUntilIdle()
      explorationLiveData.observeForever(mockExplorationObserver)

      verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
      assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
      assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
      val exploration = explorationResultCaptor.value.getOrThrow()
      assertThat(exploration.title).isEqualTo("Writing Ratios in Simplest Form")
      assertThat(exploration.languageCode).isEqualTo("en")
      assertThat(exploration.statesCount).isEqualTo(21)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_returnsNullForNonExistentExploration() = runBlockingTest(coroutineContext) {
    val explorationLiveData =
      explorationDataController.getExplorationById("NON_EXISTENT_TEST")
    advanceUntilIdle()
    explorationLiveData.observeForever(mockExplorationObserver)
    verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isFailure()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_returnsNull_logsException() = runBlockingTest(coroutineContext) {
    val explorationLiveData =
      explorationDataController.getExplorationById("NON_EXISTENT_TEST")
    advanceUntilIdle()
    explorationLiveData.observeForever(mockExplorationObserver)
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("Invalid exploration ID: NON_EXISTENT_TEST")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStopPlayingExploration_withoutStartingSession_fails() =
    runBlockingTest(coroutineContext) {
      explorationDataController.stopPlayingExploration()
      advanceUntilIdle()

      val exception = fakeExceptionLogger.getMostRecentException()

      assertThat(exception).isInstanceOf(java.lang.IllegalStateException::class.java)
      assertThat(exception).hasMessageThat()
        .contains("Cannot finish playing an exploration that hasn't yet been started")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStartPlayingExploration_withoutStoppingSession_fails() =
    runBlockingTest(coroutineContext) {
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_3)
      explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_7)
      advanceUntilIdle()

      val exception = fakeExceptionLogger.getMostRecentException()

      assertThat(exception).isInstanceOf(java.lang.IllegalStateException::class.java)
      assertThat(exception).hasMessageThat()
        .contains("Expected to finish previous exploration before starting a new one.")
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
    fun provideBackgroundDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(
      @TestDispatcher testDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class,
      MultipleChoiceInputModule::class, NumberWithUnitsRuleModule::class,
      NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class,
      TestLogReportingModule::class,ImageClickInputModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationDataControllerTest: ExplorationDataControllerTest)
  }
}
