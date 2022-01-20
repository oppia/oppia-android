package org.oppia.android.domain.exploration.lightweightcheckpointing

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
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ExplorationCheckpointDetails
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.testing.TestLogReportingModule
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
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationCheckpointControllerTest.TestApplication::class)
class ExplorationCheckpointControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Inject
  lateinit var explorationCheckpointController: ExplorationCheckpointController

  @Inject
  lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper

  @Mock
  lateinit var mockResultObserver: Observer<AsyncResult<Any?>>

  @Captor
  lateinit var resultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock
  lateinit var mockExplorationCheckpointObserver: Observer<AsyncResult<ExplorationCheckpoint>>

  @Captor
  lateinit var explorationCheckpointCaptor: ArgumentCaptor<AsyncResult<ExplorationCheckpoint>>

  @Mock
  lateinit var mockCheckpointDetailsObserver: Observer<AsyncResult<ExplorationCheckpointDetails>>

  @Captor
  lateinit var checkpointDetailsCaptor: ArgumentCaptor<AsyncResult<ExplorationCheckpointDetails>>

  private val firstTestProfile = ProfileId.newBuilder().setInternalId(0).build()
  private val secondTestProfile = ProfileId.newBuilder().setInternalId(1).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  @Test
  fun testController_saveCheckpoint_databaseNotFull_isSuccessfulWithDatabaseInCorrectState() {
    saveCheckpoint(firstTestProfile, index = 0)
    assertThat(resultCaptor.value.getOrThrow()).isEqualTo(
      CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT
    )
  }

  @Test
  fun testController_saveCheckpoint_databaseFull_isSuccessfulWithDatabaseInCorrectState() {
    saveMultipleCheckpoints(firstTestProfile, numberOfCheckpoints = 2)
    saveCheckpoint(firstTestProfile, index = 3)
    assertThat(resultCaptor.value.getOrThrow()).isEqualTo(
      CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT
    )
  }

  @Test
  fun testController_databaseFullForFirstTestProfile_checkDatabaseNotFullForSecondTestProfile() {
    saveMultipleCheckpoints(firstTestProfile, numberOfCheckpoints = 3)
    saveCheckpoint(secondTestProfile, index = 0)
    assertThat(resultCaptor.value.getOrThrow()).isEqualTo(
      CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT
    )
  }

  @Test
  fun testController_saveCheckpoint_retrieveSavedCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    val retrieveCheckpointLiveData = explorationCheckpointController.retrieveExplorationCheckpoint(
      firstTestProfile,
      FRACTIONS_EXPLORATION_ID_0
    ).toLiveData()
    retrieveCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    verifyMockObserverIsSuccessful(mockExplorationCheckpointObserver, explorationCheckpointCaptor)
  }

  @Test
  fun testController_saveCheckpoint_retrieveUnsavedCheckpoint_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    val retrieveCheckpointLiveData = explorationCheckpointController.retrieveExplorationCheckpoint(
      firstTestProfile,
      FRACTIONS_EXPLORATION_ID_1
    ).toLiveData()
    retrieveCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    verifyMockObserverIsFailure(mockExplorationCheckpointObserver, explorationCheckpointCaptor)

    assertThat(explorationCheckpointCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationCheckpointController.ExplorationCheckpointNotFoundException::class.java
    )
  }

  @Test
  fun testController_saveCheckpoint_retrieveCheckpointWithDifferentProfileId_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val retrieveCheckpointLiveData = explorationCheckpointController.retrieveExplorationCheckpoint(
      secondTestProfile,
      FRACTIONS_EXPLORATION_ID_0
    ).toLiveData()
    retrieveCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    verifyMockObserverIsFailure(mockExplorationCheckpointObserver, explorationCheckpointCaptor)

    assertThat(explorationCheckpointCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationCheckpointController.ExplorationCheckpointNotFoundException::class.java
    )
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

    val retrieveCheckpointLiveData = explorationCheckpointController.retrieveExplorationCheckpoint(
      firstTestProfile,
      FRACTIONS_EXPLORATION_ID_0
    ).toLiveData()
    retrieveCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    verifyMockObserverIsSuccessful(mockExplorationCheckpointObserver, explorationCheckpointCaptor)

    val updatedCheckpoint =
      explorationCheckpointCaptor.value.getOrDefault(ExplorationCheckpoint.getDefaultInstance())
    assertThat(updatedCheckpoint.pendingStateName)
      .matches(FRACTIONS_STORY_0_EXPLORATION_0_SECOND_STATE_NAME)
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

    val oldestCheckpointDetailsLiveData =
      explorationCheckpointController
        .retrieveOldestSavedExplorationCheckpointDetails(firstTestProfile).toLiveData()
    oldestCheckpointDetailsLiveData.observeForever(mockCheckpointDetailsObserver)
    verifyMockObserverIsSuccessful(mockCheckpointDetailsObserver, checkpointDetailsCaptor)

    val oldestCheckpointDetails = checkpointDetailsCaptor.value.getOrThrow()
    assertThat(oldestCheckpointDetails.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(oldestCheckpointDetails.explorationTitle).isEqualTo(FRACTIONS_EXPLORATION_0_TITLE)
  }

  @Test
  fun testCheckpointController_databaseEmpty_retrieveOldestCheckpointDetails_isFailure() {
    val oldestCheckpointDetailsLiveData =
      explorationCheckpointController
        .retrieveOldestSavedExplorationCheckpointDetails(firstTestProfile).toLiveData()
    oldestCheckpointDetailsLiveData.observeForever(mockCheckpointDetailsObserver)
    verifyMockObserverIsFailure(mockCheckpointDetailsObserver, checkpointDetailsCaptor)

    assertThat(checkpointDetailsCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationCheckpointController.ExplorationCheckpointNotFoundException::class.java
    )
  }

  @Test
  fun testCheckpointController_saveCheckpoint_deleteSavedCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )

    val deleteCheckpointLiveData =
      explorationCheckpointController.deleteSavedExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      ).toLiveData()
    deleteCheckpointLiveData.observeForever(mockResultObserver)
    verifyMockObserverIsSuccessful(mockResultObserver, resultCaptor)

    // Verify that the checkpoint was deleted.
    val retrieveCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        firstTestProfile,
        FRACTIONS_EXPLORATION_ID_0
      ).toLiveData()
    retrieveCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    verifyMockObserverIsFailure(mockExplorationCheckpointObserver, explorationCheckpointCaptor)

    assertThat(explorationCheckpointCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationCheckpointController.ExplorationCheckpointNotFoundException::class.java
    )
  }

  @Test
  fun testController_saveCheckpoint_deleteUnsavedCheckpoint_isFailure() {
    saveCheckpoint(firstTestProfile, index = 0)

    val deleteCheckpointLiveData = explorationCheckpointController.deleteSavedExplorationCheckpoint(
      firstTestProfile,
      FRACTIONS_EXPLORATION_ID_0
    ).toLiveData()
    deleteCheckpointLiveData.observeForever(mockResultObserver)
    verifyMockObserverIsFailure(mockResultObserver, resultCaptor)

    assertThat(resultCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationCheckpointController.ExplorationCheckpointNotFoundException::class.java
    )
    assertThat(resultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("No saved checkpoint with explorationId")
  }

  @Test
  fun testController_saveCheckpoint_deleteSavedCheckpointFromDifferentProfile_isFailure() {
    saveCheckpoint(firstTestProfile, index = 0)

    val deleteCheckpointLiveData = explorationCheckpointController.deleteSavedExplorationCheckpoint(
      secondTestProfile,
      createExplorationIdForIndex(0)
    ).toLiveData()
    deleteCheckpointLiveData.observeForever(mockResultObserver)
    verifyMockObserverIsFailure(mockResultObserver, resultCaptor)

    assertThat(resultCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationCheckpointController.ExplorationCheckpointNotFoundException::class.java
    )
    assertThat(resultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("No saved checkpoint with explorationId")
  }

  @Test
  fun testController_saveCompatibleCheckpoint_retrieveCheckpoint_isSuccessful() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_CURRENT_VERSION
    )
    explorationCheckpointController.retrieveExplorationCheckpoint(
      firstTestProfile,
      FRACTIONS_EXPLORATION_ID_0
    ).toLiveData().observeForever(mockExplorationCheckpointObserver)
    verifyMockObserverIsSuccessful(mockExplorationCheckpointObserver, explorationCheckpointCaptor)
  }

  @Test
  fun testController_saveInCompatibleCheckpoint_retrieveCheckpoint_isFailure() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId = firstTestProfile,
      version = FRACTIONS_STORY_0_EXPLORATION_0_OLD_VERSION
    )
    explorationCheckpointController.retrieveExplorationCheckpoint(
      firstTestProfile,
      FRACTIONS_EXPLORATION_ID_0
    ).toLiveData().observeForever(mockExplorationCheckpointObserver)
    verifyMockObserverIsFailure(mockExplorationCheckpointObserver, explorationCheckpointCaptor)

    assertThat(explorationCheckpointCaptor.value.getErrorOrNull()).isInstanceOf(
      ExplorationCheckpointController.OutdatedExplorationCheckpointException::class.java
    )
  }

  private fun <T> verifyMockObserverIsSuccessful(
    mockObserver: Observer<AsyncResult<T>>,
    captor: ArgumentCaptor<AsyncResult<T>>
  ) {
    testCoroutineDispatchers.runCurrent()
    verify(mockObserver, atLeastOnce()).onChanged(captor.capture())
    assertThat(captor.value.isSuccess()).isTrue()
  }

  private fun <T> verifyMockObserverIsFailure(
    mockObserver: Observer<AsyncResult<T>>,
    captor: ArgumentCaptor<AsyncResult<T>>
  ) {
    testCoroutineDispatchers.runCurrent()
    verify(mockObserver, atLeastOnce()).onChanged(captor.capture())
    assertThat(captor.value.isFailure()).isTrue()
  }

  private fun saveCheckpoint(
    profileId: ProfileId,
    index: Int
  ) {
    reset(mockResultObserver)
    explorationCheckpointController.recordExplorationCheckpoint(
      profileId = profileId,
      explorationId = createExplorationIdForIndex(index),
      explorationCheckpoint = createCheckpoint(index)
    ).toLiveData().observeForever(mockResultObserver)
    verifyMockObserverIsSuccessful(mockResultObserver, resultCaptor)
  }

  /**
   * Updates the saved checkpoint for the test exploration specified by the [index] supplied.
   *
   * For this function to work as intended, it has to be made sure that a checkpoint for the test
   * exploration specified by the index already exists in the checkpoint database of that profile.
   *
   * This function can update the checkpoint of a particular test exploration only once.
   */
  private fun saveUpdatedCheckpoint(
    profileId: ProfileId,
    index: Int
  ) {
    reset(mockResultObserver)
    explorationCheckpointController.recordExplorationCheckpoint(
      profileId = profileId,
      explorationId = createExplorationIdForIndex(index),
      explorationCheckpoint = createUpdatedCheckpoint(index)
    ).toLiveData().observeForever(mockResultObserver)
    verifyMockObserverIsSuccessful(mockResultObserver, resultCaptor)
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

  private fun createUpdatedCheckpoint(index: Int): ExplorationCheckpoint =
    ExplorationCheckpoint.newBuilder()
      .setExplorationTitle(createExplorationTitleForIndex(index))
      .setPendingStateName("second_state")
      .setStateIndex(1)
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
      LocaleProdModule::class, FakeOppiaClockModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, LoggingIdentifierModule::class
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
