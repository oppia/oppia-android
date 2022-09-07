package org.oppia.android.domain.spotlight

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.Spotlight.FeatureCase.FIRST_CHAPTER
import org.oppia.android.app.model.SpotlightViewState
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
import org.oppia.android.domain.exploration.ExplorationProgressControllerTest
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@Suppress("SameParameterValue", "FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SpotlightStateControllerTest.TestApplication::class)
class SpotlightStateControllerTest {

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @Inject
  lateinit var dataProviderTestMonitor: DataProviderTestMonitor.Factory

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()
  private val profileId1 = ProfileId.newBuilder().setInternalId(1).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
  }

  @Test
  fun retrieveSpotlightViewState_spotlightStatesNeverMarked_returnsSpotlightViewStateUnspecified() {
    val spotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId, FIRST_CHAPTER)
    val state = dataProviderTestMonitor.waitForNextSuccessfulResult(spotlightStateProvider)
    assertEquals(state, SpotlightViewState.SPOTLIGHT_VIEW_STATE_UNSPECIFIED)
  }

  @Test
  fun testMarkSpotlightState_validFeature_notYetMarked_returnsSuccess() {
    val markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId, FIRST_CHAPTER)
    dataProviderTestMonitor.waitForNextSuccessfulResult(markSpotlightProvider)
  }

  @Test
  fun testMarkSpotlightState_validFeature_alreadyMarked_returnsSuccess() {
    var markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId, FIRST_CHAPTER)
    dataProviderTestMonitor.waitForNextSuccessfulResult(markSpotlightProvider)

    markSpotlightProvider = spotlightStateController.markSpotlightViewed(profileId, FIRST_CHAPTER)
    dataProviderTestMonitor.waitForNextSuccessfulResult(markSpotlightProvider)
  }

  @Test
  fun testMarkSpotlightView_invalidFeature_returnsFailure() {
    val invalidFeature = Spotlight.FeatureCase.FEATURE_NOT_SET
    val markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId, invalidFeature)
    val result = dataProviderTestMonitor.waitForNextFailureResult(markSpotlightProvider)
    assertThrows(SpotlightStateController.SpotlightFeatureNotFoundException::class, )
  }

  @Test
  fun testMarkSpotlightState_validFeature_differentProfile_returnsSuccess() {
    val markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId1, FIRST_CHAPTER)
    dataProviderTestMonitor.waitForNextSuccessfulResult(markSpotlightProvider)
  }

  @Test
  fun testRetrieveSpotlightViewState_onboardingNext_notMarked_returnsViewNotSeen() {
    val retrieveSpotlightStateProvider = spotlightStateController.retrieveSpotlightViewState(profileId, FIRST_CHAPTER)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertEquals(result, SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
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

    @Provides
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      // Enable the study by default in tests.
      return PlatformParameterValue.createDefaultParameter(defaultValue = true)
    }
  }
  @Singleton
  @Component(
    modules = [
      TestModule::class ,ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, TestLogReportingModule::class,
      ImageClickInputModule::class, LogStorageModule::class, TestDispatcherModule::class,
      RatioInputModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ExplorationProgressControllerTest.TestExplorationStorageModule::class, HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class, NetworkConnectionUtilDebugModule::class,
      AssetModule::class, LocaleProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): SpotlightStateControllerTest.TestApplicationComponent
    }

    fun inject(spotlightStateControllerTest: SpotlightStateControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: SpotlightStateControllerTest.TestApplicationComponent by lazy {
      DaggerSpotlightStateControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(spotlightStateControllerTest: SpotlightStateControllerTest) {
      component.inject(spotlightStateControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
