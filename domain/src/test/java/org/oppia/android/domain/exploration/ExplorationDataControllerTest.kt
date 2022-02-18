package org.oppia.android.domain.exploration

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
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
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
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_2
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_3
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ExplorationDataController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationDataControllerTest.TestApplication::class)
class ExplorationDataControllerTest {
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
  lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockCurrentStateLiveDataObserver: Observer<AsyncResult<EphemeralState>>

  @Mock
  lateinit var mockExplorationObserver: Observer<AsyncResult<Exploration>>

  @Captor
  lateinit var explorationResultCaptor: ArgumentCaptor<AsyncResult<Exploration>>

  val internalProfileId: Int = -1

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testController_providesInitialLiveDataForFractions0Exploration() {
    val explorationLiveData =
      explorationDataController.getExplorationById(FRACTIONS_EXPLORATION_ID_0).toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())

    assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
    assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
    val exploration = explorationResultCaptor.value.getOrThrow()
    assertThat(exploration.title).isEqualTo("What is a Fraction?")
    assertThat(exploration.languageCode).isEqualTo("en")
    assertThat(exploration.statesCount).isEqualTo(25)
  }

  @Test
  fun testController_providesInitialLiveDataForFractions1Exploration() {
    val explorationLiveData =
      explorationDataController.getExplorationById(FRACTIONS_EXPLORATION_ID_1).toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
    assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
    val exploration = explorationResultCaptor.value.getOrThrow()
    assertThat(exploration.title).isEqualTo("The Meaning of \"Equal Parts\"")
    assertThat(exploration.languageCode).isEqualTo("en")
    assertThat(exploration.statesCount).isEqualTo(18)
  }

  @Test
  fun testController_providesInitialLiveDataForRatios0Exploration() {
    val explorationLiveData =
      explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_0).toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
    assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
    val exploration = explorationResultCaptor.value.getOrThrow()
    assertThat(exploration.title).isEqualTo("What is a Ratio?")
    assertThat(exploration.languageCode).isEqualTo("en")
    assertThat(exploration.statesCount).isEqualTo(26)
  }

  @Test
  fun testController_providesInitialLiveDataForRatios1Exploration() {
    val explorationLiveData =
      explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_1).toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
    assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
    val exploration = explorationResultCaptor.value.getOrThrow()
    assertThat(exploration.title).isEqualTo("Order is Important")
    assertThat(exploration.languageCode).isEqualTo("en")
    assertThat(exploration.statesCount).isEqualTo(22)
  }

  @Test
  fun testController_providesInitialLiveDataForRatios2Exploration() {
    val explorationLiveData =
      explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_2).toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
    assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
    val exploration = explorationResultCaptor.value.getOrThrow()
    assertThat(exploration.title).isEqualTo("Equivalent Ratios")
    assertThat(exploration.languageCode).isEqualTo("en")
    assertThat(exploration.statesCount).isEqualTo(24)
  }

  @Test
  fun testController_providesInitialLiveDataForRatios3Exploration() {
    val explorationLiveData =
      explorationDataController.getExplorationById(RATIOS_EXPLORATION_ID_3).toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationObserver, atLeastOnce()).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isSuccess()).isTrue()
    assertThat(explorationResultCaptor.value.getOrThrow()).isNotNull()
    val exploration = explorationResultCaptor.value.getOrThrow()
    assertThat(exploration.title).isEqualTo("Writing Ratios in Simplest Form")
    assertThat(exploration.languageCode).isEqualTo("en")
    assertThat(exploration.statesCount).isEqualTo(21)
  }

  @Test
  fun testController_returnsFailedForNonExistentExploration() {
    val explorationLiveData =
      explorationDataController.getExplorationById("NON_EXISTENT_TEST").toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationObserver).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isFailure()).isTrue()
    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: NON_EXISTENT_TEST")
  }

  @Test
  fun testController_returnsFailed_logsException() {
    val explorationLiveData =
      explorationDataController.getExplorationById("NON_EXISTENT_TEST").toLiveData()
    explorationLiveData.observeForever(mockExplorationObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockExplorationObserver).onChanged(explorationResultCaptor.capture())
    assertThat(explorationResultCaptor.value.isFailure()).isTrue()
    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("Asset doesn't exist: NON_EXISTENT_TEST")
  }

  @Test
  fun testStopPlayingExploration_withoutStartingSession_fails() {
    explorationDataController.stopPlayingExploration()
    testCoroutineDispatchers.runCurrent()

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(java.lang.IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Cannot finish playing an exploration that hasn't yet been started")
  }

  @Test
  fun testStartPlayingExploration_withoutStoppingSession_fails() {
    explorationDataController.startPlayingExploration(
      internalProfileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    explorationDataController.startPlayingExploration(
      internalProfileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    )
    testCoroutineDispatchers.runCurrent()

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(java.lang.IllegalStateException::class.java)
    assertThat(exception).hasMessageThat()
      .contains("Expected to finish previous exploration before starting a new one.")
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
     * The size limit is set so that after the two fake checkpoints are saved with
     * [ExplorationCheckpointTestHelper], the size of the exploration checkpoint database will
     * exceed once the exploration with [TEST_EXPLORATION_ID_2] is saved after it has been loaded.
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
      TestExplorationStorageModule::class, HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class, NetworkConnectionUtilDebugModule::class,
      AssetModule::class, LocaleProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationDataControllerTest: ExplorationDataControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationDataControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationDataControllerTest: ExplorationDataControllerTest) {
      component.inject(explorationDataControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
