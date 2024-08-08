package org.oppia.android.domain.topic

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
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
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

/** Tests for [StoryProgressController]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StoryProgressControllerTest.TestApplication::class)
class StoryProgressControllerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var storyProgressController: StoryProgressController
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(0).build()
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testStoryProgressController_recordCompletedChapter_isSuccessful() {
    val recordProvider =
      storyProgressController.recordCompletedChapter(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(recordProvider)
  }

  @Test
  fun testStoryProgressController_recordChapterAsInProgressSaved_isSuccessful() {
    val recordProvider =
      storyProgressController.recordChapterAsInProgressSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(recordProvider)
  }

  @Test
  fun testStoryProgressController_chapterCompleted_markChapterAsSaved_playStateIsCompleted() {
    monitorFactory.ensureDataProviderExecutes(
      storyProgressController.recordCompletedChapter(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )
    )

    val recordInProgressProvider = storyProgressController.recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    )

    monitorFactory.waitForNextSuccessfulResult(recordInProgressProvider)
    val playState =
      retrieveChapterPlayState(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    assertThat(playState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testStoryProgressController_chapterNotStarted_markChapterAsSaved_playStateIsSaved() {
    val recordCompletionProvider =
      storyProgressController.recordChapterAsInProgressSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(recordCompletionProvider)
    val playState =
      retrieveChapterPlayState(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    assertThat(playState).isEqualTo(ChapterPlayState.IN_PROGRESS_SAVED)
  }

  @Test
  fun testStoryProgressController_markChapterAsNotSaved_markChapterAsSaved_playStateIsSaved() {
    monitorFactory.ensureDataProviderExecutes(
      storyProgressController.recordChapterAsInProgressNotSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )
    )

    val progressSavedProvider =
      storyProgressController.recordChapterAsInProgressSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(progressSavedProvider)
    val playState =
      retrieveChapterPlayState(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    assertThat(playState).isEqualTo(ChapterPlayState.IN_PROGRESS_SAVED)
  }

  @Test
  fun testStoryProgressController_recordChapterAsNotSaved_isSuccessful() {
    val recordNotSavedProvider =
      storyProgressController.recordChapterAsInProgressNotSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(recordNotSavedProvider)
  }

  @Test
  fun testStoryProgressController_chapterCompleted_markChapterAsNotSaved_playStateIsCompleted() {
    monitorFactory.ensureDataProviderExecutes(
      storyProgressController.recordCompletedChapter(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )
    )

    val recordNotSavedProvider =
      storyProgressController.recordChapterAsInProgressNotSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(recordNotSavedProvider)
    val playState =
      retrieveChapterPlayState(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    assertThat(playState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testStoryProgressController_chapterNotStarted_markChapterAsSaved_playStateIsNotSaved() {
    val progressNotSavedProvider =
      storyProgressController.recordChapterAsInProgressNotSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(progressNotSavedProvider)
    val playState =
      retrieveChapterPlayState(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    assertThat(playState).isEqualTo(ChapterPlayState.IN_PROGRESS_NOT_SAVED)
  }

  @Test
  fun testStoryProgressController_markChapterAsSaved_markChapterAsNotSaved_playStateIsNotSaved() {
    monitorFactory.ensureDataProviderExecutes(
      storyProgressController.recordChapterAsInProgressSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )
    )

    val progressNotSavedProvider =
      storyProgressController.recordChapterAsInProgressNotSaved(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0,
        fakeOppiaClock.getCurrentTimeMs()
      )

    monitorFactory.waitForNextSuccessfulResult(progressNotSavedProvider)
    val playState =
      retrieveChapterPlayState(
        profileId,
        FRACTIONS_TOPIC_ID,
        FRACTIONS_STORY_ID_0,
        FRACTIONS_EXPLORATION_ID_0
      )
    assertThat(playState).isEqualTo(ChapterPlayState.IN_PROGRESS_NOT_SAVED)
  }

  private fun retrieveChapterPlayState(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String
  ): ChapterPlayState {
    val playStateProvider =
      storyProgressController.retrieveChapterPlayStateByExplorationId(
        profileId, topicId, storyId, explorationId
      )
    return monitorFactory.waitForNextSuccessfulResult(playStateProvider)
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, AssetModule::class
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
