package org.oppia.android.domain.spotlight

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
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.Spotlight.FeatureCase.FIRST_CHAPTER
import org.oppia.android.app.model.Spotlight.FeatureCase.LESSONS_BACK_BUTTON
import org.oppia.android.app.model.Spotlight.FeatureCase.PROMOTED_STORIES
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_LESSON_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_REVISION_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.VOICEOVER_LANGUAGE_ICON
import org.oppia.android.app.model.Spotlight.FeatureCase.VOICEOVER_PLAY_ICON
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

@Suppress("SameParameterValue", "FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SpotlightStateControllerTest.TestApplication::class)
class SpotlightStateControllerTest {

  @Inject
  lateinit var spotlightStateController: SpotlightStateController

  @Inject
  lateinit var dataProviderTestMonitor: DataProviderTestMonitor.Factory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val profileId0 = ProfileId.newBuilder().setLoggedInInternalProfileId(0).build()
  private val profileId1 = ProfileId.newBuilder().setLoggedInInternalProfileId(1).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testMarkSpotlightState_validFeature_notYetMarked_returnsSuccess() {
    val markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId0, FIRST_CHAPTER)
    val monitor = dataProviderTestMonitor.createMonitor(markSpotlightProvider)
    testCoroutineDispatchers.runCurrent()
    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testMarkSpotlightState_validFeature_alreadyMarked_returnsSuccess() {
    markSpotlightSeen(FIRST_CHAPTER)
    val markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId0, FIRST_CHAPTER)
    val monitor = dataProviderTestMonitor.createMonitor(markSpotlightProvider)
    testCoroutineDispatchers.runCurrent()
    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testMarkSpotlightView_invalidFeature_returnsFailure() {
    val invalidFeature = Spotlight.FeatureCase.FEATURE_NOT_SET
    val markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId0, invalidFeature)
    val monitor = dataProviderTestMonitor.createMonitor(markSpotlightProvider)
    testCoroutineDispatchers.runCurrent()
    val exception = monitor.ensureNextResultIsFailing()
    assertThat(exception).hasMessageThat().contains("Spotlight feature was not found")
  }

  @Test
  fun testMarkSpotlightState_validFeature_differentProfile_returnsSuccess() {
    val markSpotlightProvider =
      spotlightStateController.markSpotlightViewed(profileId1, FIRST_CHAPTER)
    val monitor = dataProviderTestMonitor.createMonitor(markSpotlightProvider)
    testCoroutineDispatchers.runCurrent()
    monitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testRetrieveSpotlightViewState_firstChapter_notMarked_returnsSpotlightStateNotSeen() {
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, FIRST_CHAPTER)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_firstChapter_marked_returnsSpotlightStateSeen() {
    markSpotlightSeen(FIRST_CHAPTER)
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, FIRST_CHAPTER)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_topicLessonTab_notMarked_returnsSpotlightStateNotSeen() {
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, TOPIC_LESSON_TAB)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_topicLessonTab_marked_returnsSpotlightStateSeen() {
    markSpotlightSeen(TOPIC_LESSON_TAB)
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, TOPIC_LESSON_TAB)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_topicRevisionTab_notMarked_returnsSpotlightStateNotSeen() {
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, TOPIC_REVISION_TAB)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_topicRevisionTab_marked_returnsSpotlightStateSeen() {
    markSpotlightSeen(TOPIC_REVISION_TAB)
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, TOPIC_REVISION_TAB)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_promotedStories_notMarked_returnsSpotlightStateNotSeen() {
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, PROMOTED_STORIES)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_promotedStories_marked_returnsSpotlightStateSeen() {
    markSpotlightSeen(PROMOTED_STORIES)
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, PROMOTED_STORIES)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_lessonsBackButton_notMarked_returnsSpotlightStateNotSeen() {
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, LESSONS_BACK_BUTTON)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_lessonsBackButton_marked_returnsSpotlightStateSeen() {
    markSpotlightSeen(LESSONS_BACK_BUTTON)
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, LESSONS_BACK_BUTTON)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_voiceoverPlayIcon_notMarked_returnsSpotlightStateNotSeen() {
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, VOICEOVER_PLAY_ICON)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_voiceoverPlayIcon_marked_returnsSpotlightStateSeen() {
    markSpotlightSeen(VOICEOVER_PLAY_ICON)
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, VOICEOVER_PLAY_ICON)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_voiceoverLangIcon_notMarked_returnsSpotlightStateNotSeen() {
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, VOICEOVER_LANGUAGE_ICON)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_voiceoverLanguageIcon_marked_returnsSpotlightStateSeen() {
    markSpotlightSeen(VOICEOVER_LANGUAGE_ICON)
    val retrieveSpotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, VOICEOVER_LANGUAGE_ICON)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(retrieveSpotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_invalidFeature_returnsFailure() {
    val invalidFeature = Spotlight.FeatureCase.FEATURE_NOT_SET
    val spotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, invalidFeature)
    val monitor = dataProviderTestMonitor.createMonitor(spotlightStateProvider)
    testCoroutineDispatchers.runCurrent()
    val exception = monitor.ensureNextResultIsFailing()
    assertThat(exception).hasMessageThat().contains("Spotlight feature requested was not found")
  }

  @Test
  fun testRetrieveSpotlightViewState_validFeature_marked_differentProfile_returnsSpotlightSeen() {
    dataProviderTestMonitor.waitForNextSuccessfulResult(
      spotlightStateController.markSpotlightViewed(
        profileId1,
        FIRST_CHAPTER
      )
    )

    val spotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId1, FIRST_CHAPTER)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(spotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_SEEN)
  }

  @Test
  fun testRetrieveSpotlightViewState_validFeature_notMarked_diffProfile_returnsSpotlightNotSeen() {
    val spotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId1, FIRST_CHAPTER)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(spotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testMarkSpotlight_markFirstChapterSeen_checkPromotedStoriesNotSeen() {
    markSpotlightSeen(FIRST_CHAPTER)
    val spotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId0, PROMOTED_STORIES)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(spotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  @Test
  fun testMarkSpotlight_markFirstChapterSeenInProfile0_checkNotSeenInProfile1() {
    markSpotlightSeen(FIRST_CHAPTER)
    val spotlightStateProvider =
      spotlightStateController.retrieveSpotlightViewState(profileId1, FIRST_CHAPTER)
    val result = dataProviderTestMonitor.waitForNextSuccessfulResult(spotlightStateProvider)
    assertThat(result).isEqualTo(SpotlightViewState.SPOTLIGHT_NOT_SEEN)
  }

  private fun markSpotlightSeen(spotlightFeature: Spotlight.FeatureCase) {
    dataProviderTestMonitor.waitForNextSuccessfulResult(
      spotlightStateController.markSpotlightViewed(
        profileId0,
        spotlightFeature
      )
    )
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
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, TestLogReportingModule::class,
      ImageClickInputModule::class, LogStorageModule::class, TestDispatcherModule::class,
      RatioInputModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      HintsAndSolutionConfigModule::class,
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

      fun build(): TestApplicationComponent
    }

    fun inject(spotlightStateControllerTest: SpotlightStateControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
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
