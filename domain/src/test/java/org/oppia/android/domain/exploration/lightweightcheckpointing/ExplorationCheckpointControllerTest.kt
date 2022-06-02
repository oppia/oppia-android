package org.oppia.android.domain.exploration.lightweightcheckpointing

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
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController.ExplorationCheckpointNotFoundException
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController.OutdatedExplorationCheckpointException
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.lightweightcheckpointing.ExplorationCheckpointTestHelper
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_EXPLORATION_0_TITLE
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_0_OLD_VERSION
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME
import org.oppia.android.testing.lightweightcheckpointing.FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The base exploration id for every exploration used for testing [ExplorationCheckpointController].
 * The exploration id of every test exploration will start with
 * the string [BASE_TEST_EXPLORATION_ID].
 */
private const val BASE_TEST_EXPLORATION_ID = "test_exploration_"

/**
 * The base exploration title for every exploration used for testing
 * [ExplorationCheckpointController]. The exploration title of every test exploration will start
 * with the string [BASE_TEST_EXPLORATION_TITLE].
 */
private const val BASE_TEST_EXPLORATION_TITLE = "Test Exploration "

/**
 * Tests for [ExplorationCheckpointController].
 *
 * For testing this controller, checkpoints of hypothetical explorations are saved, updated,
 * retrieved and deleted. These hypothetical explorations are referred to as "test explorations".
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationCheckpointControllerTest.TestApplication::class)
class ExplorationCheckpointControllerTest {
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var explorationCheckpointController: ExplorationCheckpointController
  @Inject lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private val firstTestProfile = ProfileId.newBuilder().setInternalId(0).build()
  private val secondTestProfile = ProfileId.newBuilder().setInternalId(1).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  @Test
  fun testController_saveCheckpoint_databaseNotFull_isSuccessfulWithDatabaseInCorrectState() {
    val result = saveCheckpoint(firstTestProfile, index = 0)

    assertThat(result).isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT)
  }

  @Test
  fun testController_saveCheckpoint_databaseFull_isSuccessfulWithDatabaseInCorrectState() {
    saveMultipleCheckpoints(firstTestProfile, numberOfCheckpoints = 2)

    val result = saveCheckpoint(firstTestProfile, index = 3)

    assertThat(result).isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT)
  }

  @Test
  fun testController_databaseFullForFirstTestProfile_checkDatabaseNotFullForSecondTestProfile() {
    saveMultipleCheckpoints(firstTestProfile, numberOfCheckpoints = 3)

    val result = saveCheckpoint(secondTestProfile, index = 0)

    assertThat(result).isEqualTo(CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT)
  }

  @Test
  fun testController_saveCheckpoint_retrieveSavedCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    monitorFactory.waitForNextSuccessfulResult(retrieveCheckpointProvider)
  }

  @Test
  fun testController_saveCheckpoint_retrieveUnsavedCheckpoint_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_1
      )

    val error = monitorFactory.waitForNextFailureResult(retrieveCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
  }

  @Test
  fun testController_saveCheckpoint_retrieveCheckpointWithDifferentProfileId_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        secondTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(retrieveCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
  }

  @Test
  fun testController_saveCheckpoint_updateSavedCheckpoint_checkUpdatedCheckpointIsRetrieved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.updateCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val updatedCheckpoint = monitorFactory.waitForNextSuccessfulResult(retrieveCheckpointProvider)
    assertThat(updatedCheckpoint.pendingStateName)
      .isEqualTo(FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME)
  }

  @Test
  fun testController_saveCheckpoints_retrieveOldestCheckpointDetails_correctCheckpointRetrieved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration1(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_1_CURRENT_VERSION
    )

    val checkpointProvider =
      explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(
        firstTestProfile
      )

    val oldestCheckpointDetails = monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
    assertThat(oldestCheckpointDetails.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(oldestCheckpointDetails.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_0_TITLE)
  }

  @Test
  fun testCheckpointController_databaseEmpty_retrieveOldestCheckpointDetails_isFailure() {
    val checkpointProvider =
      explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(
        firstTestProfile
      )

    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
  }

  @Test
  fun testCheckpointController_saveCheckpoint_deleteSavedCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    monitorFactory.waitForNextSuccessfulResult(deleteCheckpointProvider)
  }

  @Test
  fun testCheckpointController_saveCheckpoint_deleteSavedCheckpoint_checkpointWasDeleted() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )
    monitorFactory.ensureDataProviderExecutes(deleteCheckpointProvider)

    // Verify that the checkpoint was deleted.
    val retrieveCheckpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(retrieveCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
  }

  @Test
  fun testController_saveCheckpoint_deleteUnsavedCheckpoint_isFailure() {
    saveCheckpoint(firstTestProfile, index = 0)

    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(deleteCheckpointProvider)
    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
    assertThat(error).hasMessageThat().contains("No saved checkpoint with explorationId")
  }

  @Test
  fun testController_saveCheckpoint_deleteSavedCheckpointFromDifferentProfile_isFailure() {
    saveCheckpoint(firstTestProfile, index = 0)

    val deleteCheckpointProvider =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        secondTestProfile,
        createExplorationIdForIndex(0)
      )

    val error = monitorFactory.waitForNextFailureResult(deleteCheckpointProvider)

    assertThat(error).isInstanceOf(ExplorationCheckpointNotFoundException::class.java)
    assertThat(error).hasMessageThat().contains("No saved checkpoint with explorationId")
  }

  @Test
  fun testController_saveCompatibleCheckpoint_retrieveCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val checkpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    monitorFactory.waitForNextSuccessfulResult(checkpointProvider)
  }

  @Test
  fun testController_saveInCompatibleCheckpoint_retrieveCheckpoint_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_OLD_VERSION
    )

    val checkpointProvider =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      )

    val error = monitorFactory.waitForNextFailureResult(checkpointProvider)
    assertThat(error).isInstanceOf(OutdatedExplorationCheckpointException::class.java)
  }

  private fun saveCheckpoint(profileId: ProfileId, index: Int): Any? {
    val recordProvider = explorationCheckpointController.recordExplorationCheckpoint(
      profileId = profileId,
      explorationId = createExplorationIdForIndex(index),
      explorationCheckpoint = createCheckpoint(index)
    )
    return monitorFactory.waitForNextSuccessfulResult(recordProvider)
  }

  private fun saveMultipleCheckpoints(profileId: ProfileId, numberOfCheckpoints: Int) {
    for (index in 0 until numberOfCheckpoints) {
      saveCheckpoint(profileId, index)
    }
  }

  /**
   * Every test exploration has a unique index associated with it. The explorationId for any
   * test exploration is created by concatenating the string [BASE_TEST_EXPLORATION_ID] with the
   * unique index of that exploration.
   *
   * Test explorations are indexed from 0. The explorationId for the exploration with index 0 will
   * be formed by concatenating the string [BASE_TEST_EXPLORATION_ID] with its index, i.e. 0.
   * Therefore the exploration id of the exploration with index 0 will be the string
   * "test_exploration_0".
   *
   * @return a unique explorationId for every test exploration. The explorationId of every
   *         test exploration will be of the form "test_exploration_#", where the symbol "#"
   *         represents an non-negative integer.
   */
  private fun createExplorationIdForIndex(index: Int): String =
    BASE_TEST_EXPLORATION_ID + index

  /**
   * Similar to [createExplorationIdForIndex], exploration title for any test exploration  are
   * created by concatenating the string [BASE_TEST_EXPLORATION_TITLE] with the the unique index
   * of that exploration.
   *
   * For example the exploration title of the exploration indexed at 0 will be "Test Exploration 0".
   *
   * @return a unique explorationTitle for every test exploration. The explorationTitle for any
   *         test exploration is of the form "Test Exploration #".
   */
  private fun createExplorationTitleForIndex(index: Int): String =
    BASE_TEST_EXPLORATION_TITLE + index

  private fun createCheckpoint(index: Int): ExplorationCheckpoint =
    ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(createExplorationTitleForIndex(index))
      .setPendingStateName("first_state")
      .setStateIndex(0)
      .build()

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
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
     * Each test checkpoint is estimated to be about 60 bytes in size, it is expected that the
     * database will exceed this limit when the third test checkpoint is saved.
     */
    @Provides
    @ExplorationStorageDatabaseSize
    fun provideExplorationStorageDatabaseSize(): Int = 150
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class,
      TestExplorationStorageModule::class, TestDispatcherModule::class, RobolectricModule::class,
      LogStorageModule::class, NetworkConnectionUtilDebugModule::class, AssetModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(explorationCheckpointControllerTest: ExplorationCheckpointControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationCheckpointControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationCheckpointControllerTest: ExplorationCheckpointControllerTest) {
      component.inject(explorationCheckpointControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
