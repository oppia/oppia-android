package org.oppia.android.testing.lightweightcheckpointing

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
import org.oppia.android.app.model.ExplorationCheckpoint
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
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
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

/** Tests for [ExplorationCheckpointTestHelper]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationCheckpointTestHelperTest.TestApplication::class)
class ExplorationCheckpointTestHelperTest {
  @Inject lateinit var context: Context
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper
  @Inject lateinit var explorationCheckpointController: ExplorationCheckpointController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testSaveCheckpointForFractionsStory0Exploration0_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = profileId,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint = retrieveCheckpoint(profileId, FRACTIONS_EXPLORATION_ID_0)
    assertThat(checkpoint.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_0_TITLE)
    assertThat(checkpoint.pendingStateName)
      .isEqualTo(FRACTIONS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME)
  }

  @Test
  fun testUpdateCheckpointForFractionsStory0Exploration0_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = profileId,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint = retrieveCheckpoint(profileId, FRACTIONS_EXPLORATION_ID_0)
    assertThat(checkpoint.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_0_TITLE)
    assertThat(checkpoint.pendingStateName)
      .isEqualTo(FRACTIONS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME)

    explorationCheckpointTestHelper.updateCheckpointForFractionsStory0Exploration0(
      profileId = profileId,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint1 = retrieveCheckpoint(profileId, FRACTIONS_EXPLORATION_ID_0)
    assertThat(checkpoint1.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_0_TITLE)
    assertThat(checkpoint1.pendingStateName)
      .isEqualTo(FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME)
  }

  @Test
  fun testSaveCheckpointForFractionsStory0Exploration1_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId,
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint = retrieveCheckpoint(profileId, FRACTIONS_EXPLORATION_ID_1)
    assertThat(checkpoint.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_1_TITLE)
    assertThat(checkpoint.pendingStateName)
      .isEqualTo(FRACTIONS_STORY_0_EXPLORATION_1_FIRST_STATE_NAME)
  }

  @Test
  fun testUpdateCheckpointForFractionsStory0Exploration1_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = profileId,
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint = retrieveCheckpoint(profileId, FRACTIONS_EXPLORATION_ID_1)
    assertThat(checkpoint.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_1_TITLE)
    assertThat(checkpoint.pendingStateName)
      .isEqualTo(
        FRACTIONS_STORY_0_EXPLORATION_1_FIRST_STATE_NAME
      )

    explorationCheckpointTestHelper.updateCheckpointForFractionsStory0Exploration1(
      profileId = profileId,
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION,
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint1 = retrieveCheckpoint(profileId, FRACTIONS_EXPLORATION_ID_1)
    assertThat(checkpoint1.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_1_TITLE)
    assertThat(checkpoint1.pendingStateName)
      .isEqualTo(FRACTIONS_STORY_0_EXPLORATION_1_SECOND_STATE_NAME)
  }

  @Test
  fun testSaveCheckpointForRatiosStory0Exploration0_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = profileId,
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint = retrieveCheckpoint(profileId, RATIOS_EXPLORATION_ID_0)
    assertThat(checkpoint.explorationTitle).isEqualTo(RATIOS_EXPLORATION_0_TITLE)
    assertThat(checkpoint.pendingStateName).isEqualTo(RATIOS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME)
  }

  @Test
  fun testUpdateCheckpointForRatiosStory0Exploration0_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForRatiosStory0Exploration0(
      profileId = profileId,
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint = retrieveCheckpoint(profileId, RATIOS_EXPLORATION_ID_0)
    assertThat(checkpoint.explorationTitle).isEqualTo(RATIOS_EXPLORATION_0_TITLE)
    assertThat(checkpoint.pendingStateName).isEqualTo(RATIOS_STORY_0_EXPLORATION_0_FIRST_STATE_NAME)

    explorationCheckpointTestHelper.updateCheckpointForRatiosStory0Exploration0(
      profileId = profileId,
      version = RATIOS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    // Verify saved checkpoint has correct exploration title and pending state name.
    val checkpoint1 = retrieveCheckpoint(profileId, RATIOS_EXPLORATION_ID_0)
    assertThat(checkpoint1.explorationTitle).isEqualTo(RATIOS_EXPLORATION_0_TITLE)
    assertThat(checkpoint1.pendingStateName)
      .isEqualTo(RATIOS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME)
  }

  private fun retrieveCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): ExplorationCheckpoint {
    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(profileId, explorationId)
    return monitorFactory.waitForNextSuccessfulResult(retrieveCheckpointProvider)
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

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ExplorationStorageModule::class, NetworkConnectionUtilDebugModule::class, AssetModule::class,
      LocaleProdModule::class, LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      RatioInputModule::class, ImageClickInputModule::class, InteractionsModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationCheckpointTestHelperTest: ExplorationCheckpointTestHelperTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationCheckpointTestHelperTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationCheckpointTestHelperTest: ExplorationCheckpointTestHelperTest) {
      component.inject(explorationCheckpointTestHelperTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
