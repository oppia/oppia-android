package org.oppia.android.domain.topic

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
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
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

/** Tests for [StoryProgressController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StoryProgressControllerTest.TestApplication::class)
class StoryProgressControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var storyProgressController: StoryProgressController

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Mock
  lateinit var mockRecordProgressObserver: Observer<AsyncResult<Any?>>

  @Captor
  lateinit var recordProgressResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock
  lateinit var mockRetrieveChapterPlayStateObserver: Observer<AsyncResult<ChapterPlayState>>

  @Captor
  lateinit var retrieveChapterPlayStateCaptor: ArgumentCaptor<AsyncResult<ChapterPlayState>>

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    profileId = ProfileId.newBuilder().setInternalId(0).build()
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testStoryProgressController_recordCompletedChapter_isSuccessful() {
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
  }

  @Test
  fun testStoryProgressController_recordChapterAsInProgressSaved_isSuccessful() {
    storyProgressController.recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
  }

  @Test
  fun testStoryProgressController_chapterCompleted_markChapterAsSaved_playStateIsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()

    storyProgressController.recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
    verifyChapterPlayStateIsCorrect(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      ChapterPlayState.COMPLETED
    )
  }

  @Test
  fun testStoryProgressController_chapterNotStarted_markChapterAsSaved_playStateIsSaved() {
    storyProgressController.recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
    verifyChapterPlayStateIsCorrect(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      ChapterPlayState.IN_PROGRESS_SAVED
    )
  }

  @Test
  fun testStoryProgressController_markChapterAsNotSaved_markChapterAsSaved_playStateIsSaved() {
    storyProgressController.recordChapterAsInProgressNotSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()

    storyProgressController.recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
    verifyChapterPlayStateIsCorrect(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      ChapterPlayState.IN_PROGRESS_SAVED
    )
  }

  @Test
  fun testStoryProgressController_recordChapterAsNotSaved_isSuccessful() {
    storyProgressController.recordChapterAsInProgressNotSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
  }

  @Test
  fun testStoryProgressController_chapterCompleted_markChapterAsNotSaved_playStateIsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()

    storyProgressController.recordChapterAsInProgressNotSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
    verifyChapterPlayStateIsCorrect(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      ChapterPlayState.COMPLETED
    )
  }

  @Test
  fun testStoryProgressController_chapterNotStarted_markChapterAsSaved_playStateIsNotSaved() {
    storyProgressController.recordChapterAsInProgressNotSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
    verifyChapterPlayStateIsCorrect(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      ChapterPlayState.IN_PROGRESS_NOT_SAVED
    )
  }

  @Test
  fun testStoryProgressController_markChapterAsSaved_markChapterAsNotSaved_playStateIsNotSaved() {
    storyProgressController.recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()

    storyProgressController.recordChapterAsInProgressNotSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
    verifyChapterPlayStateIsCorrect(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      ChapterPlayState.IN_PROGRESS_NOT_SAVED
    )
  }

  private fun verifyChapterPlayStateIsCorrect(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    chapterPlayState: ChapterPlayState
  ) {
    storyProgressController.retrieveChapterPlayStateByExplorationId(
      profileId,
      topicId,
      storyId,
      explorationId
    ).toLiveData().observeForever(mockRetrieveChapterPlayStateObserver)

    testCoroutineDispatchers.runCurrent()

    verify(mockRetrieveChapterPlayStateObserver, atLeastOnce())
      .onChanged(retrieveChapterPlayStateCaptor.capture())

    assertThat(retrieveChapterPlayStateCaptor.value.isSuccess()).isTrue()
    assertThat(retrieveChapterPlayStateCaptor.value.getOrThrow()).isEqualTo(chapterPlayState)
  }

  private fun verifyRecordProgressSucceeded() {
    verify(mockRecordProgressObserver, atLeastOnce())
      .onChanged(recordProgressResultCaptor.capture())
    assertThat(recordProgressResultCaptor.value.isSuccess()).isTrue()
    reset(mockRecordProgressObserver)
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
    //  module in tests to avoid needing to specify these settings for tests.
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(storyProgressControllerTest: StoryProgressControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStoryProgressControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(storyProgressControllerTest: StoryProgressControllerTest) {
      component.inject(storyProgressControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
