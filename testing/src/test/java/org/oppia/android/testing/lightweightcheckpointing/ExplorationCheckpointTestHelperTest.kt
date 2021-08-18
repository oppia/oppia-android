package org.oppia.android.testing.lightweightcheckpointing

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
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ExplorationCheckpointTestHelper]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationCheckpointTestHelperTest.TestApplication::class)
class ExplorationCheckpointTestHelperTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var explorationCheckpointTestHelper: ExplorationCheckpointTestHelper

  @Inject
  lateinit var explorationCheckpointController: ExplorationCheckpointController

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockExplorationCheckpointObserver: Observer<AsyncResult<ExplorationCheckpoint>>

  @Captor
  lateinit var explorationCheckpointCaptor: ArgumentCaptor<AsyncResult<ExplorationCheckpoint>>

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testSaveFakeExplorationCheckpoint_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveFakeExplorationCheckpoint(
      profileId.internalId,
      version = 0,
      timestamp = 0L
    )

    val retrieveFakeCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        FAKE_EXPLORATION_ID_1
      ).toLiveData()
    retrieveFakeCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify exploration checkpoint was saved with correct exploration title.
    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()
    assertThat(explorationCheckpointCaptor.value.getOrThrow().explorationTitle)
      .isEqualTo(FAKE_EXPLORATION_TITLE_1)
  }

  @Test
  fun testSaveTwoFakeCheckpoints_checkCheckpointsAreSaved() {
    explorationCheckpointTestHelper.saveTwoFakeExplorationCheckpoint(
      profileId.internalId,
      versionOfFirstCheckpoint = 0,
      versionOfSecondCheckpoint = 0,
      timestampOfFirstCheckpoint = 0L,
      timestampOfSecondCheckpoint = 0L
    )

    var retrieveFakeCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        FAKE_EXPLORATION_ID_1
      ).toLiveData()
    retrieveFakeCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify the first exploration checkpoint was saved with correct exploration title.
    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()
    assertThat(explorationCheckpointCaptor.value.getOrThrow().explorationTitle)
      .isEqualTo(FAKE_EXPLORATION_TITLE_1)

    reset(mockExplorationCheckpointObserver)
    retrieveFakeCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        FAKE_EXPLORATION_ID_2
      ).toLiveData()
    retrieveFakeCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify the second exploration checkpoint was saved with correct exploration title.
    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()
    assertThat(explorationCheckpointCaptor.value.getOrThrow().explorationTitle)
      .isEqualTo(FAKE_EXPLORATION_TITLE_2)
  }

  @Test
  fun testSaveCheckpointForFractionsStory0Exploration0_checkCheckpointIsSaved() {
    explorationCheckpointTestHelper.saveCheckpointForFractionsStory0Exploration0(
      profileId.internalId,
      version = 0,
      timestamp = 0L
    )

    val retrieveFakeCheckpointLiveData =
      explorationCheckpointController.retrieveExplorationCheckpoint(
        profileId,
        FRACTIONS_EXPLORATION_ID_0
      ).toLiveData()
    retrieveFakeCheckpointLiveData.observeForever(mockExplorationCheckpointObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify exploration checkpoint was saved with correct exploration title.
    verify(mockExplorationCheckpointObserver, atLeastOnce())
      .onChanged(explorationCheckpointCaptor.capture())
    assertThat(explorationCheckpointCaptor.value.isSuccess()).isTrue()
    assertThat(explorationCheckpointCaptor.value.getOrThrow().explorationTitle)
      .isEqualTo(FRACTIONS_EXPLORATION_0_TITLE)
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ExplorationStorageModule::class, NetworkConnectionUtilDebugModule::class,
      NetworkConnectionDebugUtilModule::class,
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
