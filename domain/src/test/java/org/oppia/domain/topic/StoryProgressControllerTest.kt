package org.oppia.domain.topic

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.ChapterPlayState.COMPLETED
import org.oppia.app.model.ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
import org.oppia.app.model.ChapterPlayState.NOT_STARTED
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

/** Tests for [StoryProgressController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StoryProgressControllerTest {
  @Inject lateinit var storyProgressController: StoryProgressController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetStoryProgress_validStory_isSuccessful() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(TEST_STORY_ID_0)

    val storyProgressResult = storyProgressLiveData.value
    assertThat(storyProgressResult).isNotNull()
    assertThat(storyProgressResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetStoryProgress_validStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(TEST_STORY_ID_0)

    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(1)
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(TEST_EXPLORATION_ID_0)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(COMPLETED)
  }

  @Test
  fun testGetStoryProgress_validSecondStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(TEST_STORY_ID_1)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(3)
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(TEST_EXPLORATION_ID_1)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(COMPLETED)
    assertThat(storyProgress.getChapterProgress(1).explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(storyProgress.getChapterProgress(1).playState).isEqualTo(NOT_STARTED)
    assertThat(storyProgress.getChapterProgress(2).explorationId).isEqualTo(TEST_EXPLORATION_ID_3)
    assertThat(storyProgress.getChapterProgress(2).playState).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetStoryProgress_validFractionsStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(FRACTIONS_STORY_ID_0)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(2)
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(COMPLETED)
    assertThat(storyProgress.getChapterProgress(1).explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_1)
    assertThat(storyProgress.getChapterProgress(1).playState).isEqualTo(NOT_STARTED)
  }

  @Test
  fun testGetStoryProgress_validFirstRatiosStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(RATIOS_STORY_ID_0)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(2)
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(NOT_STARTED)
    assertThat(storyProgress.getChapterProgress(1).explorationId).isEqualTo(RATIOS_EXPLORATION_ID_1)
    assertThat(storyProgress.getChapterProgress(1).playState).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetStoryProgress_validSecondRatiosStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(RATIOS_STORY_ID_1)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(2)
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(RATIOS_EXPLORATION_ID_2)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(NOT_STARTED)
    assertThat(storyProgress.getChapterProgress(1).explorationId).isEqualTo(RATIOS_EXPLORATION_ID_3)
    assertThat(storyProgress.getChapterProgress(1).playState).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetStoryProgress_validThirdStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(TEST_STORY_ID_2)

    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(1)
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(TEST_EXPLORATION_ID_4)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(NOT_STARTED)
  }

  @Test
  fun testGetStoryProgress_invalidStory_providesError() {
    val storyProgressLiveData = storyProgressController.getStoryProgress("invalid_story_id")

    val storyProgressResult = storyProgressLiveData.value
    assertThat(storyProgressResult).isNotNull()
    assertThat(storyProgressResult!!.isFailure()).isTrue()
    assertThat(storyProgressResult.getErrorOrNull())
      .hasMessageThat()
      .contains("No story found with ID: invalid_story_id")
  }

  @Test
  fun testRecordCompletedChapter_validStory_validChapter_alreadyCompleted_succeeds() {
    val recordProgressLiveData = storyProgressController.recordCompletedChapter(TEST_STORY_ID_1, TEST_EXPLORATION_ID_1)

    val recordProgressResult = recordProgressLiveData.value
    assertThat(recordProgressResult).isNotNull()
    assertThat(recordProgressResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRecordCompletedChapter_validStory_validChapter_alreadyCompleted_keepsChapterAsCompleted() {
    storyProgressController.recordCompletedChapter(TEST_STORY_ID_1, TEST_EXPLORATION_ID_1)

    val storyProgress = storyProgressController.getStoryProgress(TEST_STORY_ID_1).value!!.getOrThrow()
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(TEST_EXPLORATION_ID_1)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(COMPLETED)
  }

  @Test
  fun testRecordCompletedChapter_validStory_validChapter_notYetCompleted_succeeds() {
    val recordProgressLiveData = storyProgressController.recordCompletedChapter(TEST_STORY_ID_1, TEST_EXPLORATION_ID_2)

    val recordProgressResult = recordProgressLiveData.value
    assertThat(recordProgressResult).isNotNull()
    assertThat(recordProgressResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRecordCompletedChapter_validStory_validChapter_notYetCompleted_marksChapterAsCompleted() {
    storyProgressController.recordCompletedChapter(TEST_STORY_ID_1, TEST_EXPLORATION_ID_2)

    val storyProgress = storyProgressController.getStoryProgress(TEST_STORY_ID_1).value!!.getOrThrow()
    assertThat(storyProgress.getChapterProgress(1).explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(storyProgress.getChapterProgress(1).playState).isEqualTo(COMPLETED)
  }

  @Test
  fun testRecordCompletedChapter_validStory_validChapter_missingPrereqs_fails() {
    val recordProgressLiveData = storyProgressController.recordCompletedChapter(TEST_STORY_ID_1, TEST_EXPLORATION_ID_3)

    val recordProgressResult = recordProgressLiveData.value
    assertThat(recordProgressResult).isNotNull()
    assertThat(recordProgressResult!!.isFailure()).isTrue()
    assertThat(recordProgressResult.getErrorOrNull())
      .hasMessageThat()
      .contains("Cannot mark chapter as completed, missing prerequisites: $TEST_EXPLORATION_ID_3")
  }

  @Test
  fun testRecordCompletedChapter_validStory_validChapter_missingPrereqs_keepsChapterMissingPrereqs() {
    storyProgressController.recordCompletedChapter(TEST_STORY_ID_1, TEST_EXPLORATION_ID_3)

    val storyProgress = storyProgressController.getStoryProgress(TEST_STORY_ID_1).value!!.getOrThrow()
    assertThat(storyProgress.getChapterProgress(2).explorationId).isEqualTo(TEST_EXPLORATION_ID_3)
    assertThat(storyProgress.getChapterProgress(2).playState).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testRecordCompletedChapter_validStory_invalidChapter_fails() {
    val recordProgressLiveData = storyProgressController.recordCompletedChapter(TEST_STORY_ID_1, "invalid_exp_id")

    val recordProgressResult = recordProgressLiveData.value
    assertThat(recordProgressResult).isNotNull()
    assertThat(recordProgressResult!!.isFailure()).isTrue()
    assertThat(recordProgressResult.getErrorOrNull())
      .hasMessageThat()
      .contains("Chapter not found in story: invalid_exp_id")
  }

  @Test
  fun testRecordCompletedChapter_validSecondStory_validChapter_notYetCompleted_succeeds() {
    val recordProgressLiveData = storyProgressController.recordCompletedChapter(TEST_STORY_ID_2, TEST_EXPLORATION_ID_4)

    val recordProgressResult = recordProgressLiveData.value
    assertThat(recordProgressResult).isNotNull()
    assertThat(recordProgressResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRecordCompletedChapter_validSecondStory_validChapter_notYetCompleted_marksChapterAsCompleted() {
    storyProgressController.recordCompletedChapter(TEST_STORY_ID_2, TEST_EXPLORATION_ID_4)

    val storyProgress = storyProgressController.getStoryProgress(TEST_STORY_ID_2).value!!.getOrThrow()
    assertThat(storyProgress.getChapterProgress(0).explorationId).isEqualTo(TEST_EXPLORATION_ID_4)
    assertThat(storyProgress.getChapterProgress(0).playState).isEqualTo(COMPLETED)
  }

  @Test
  fun testRecordCompletedChapter_validSecondStory_validChapterInOtherStory_fails() {
    val recordProgressLiveData = storyProgressController.recordCompletedChapter(TEST_STORY_ID_2, TEST_EXPLORATION_ID_3)

    val recordProgressResult = recordProgressLiveData.value
    assertThat(recordProgressResult).isNotNull()
    assertThat(recordProgressResult!!.isFailure()).isTrue()
    assertThat(recordProgressResult.getErrorOrNull())
      .hasMessageThat()
      .contains("Chapter not found in story: $TEST_EXPLORATION_ID_3")
  }

  @Test
  fun testRecordCompletedChapter_invalidStory_fails() {
    val recordProgressLiveData =
      storyProgressController.recordCompletedChapter("invalid_story_id", TEST_EXPLORATION_ID_0)

    val recordProgressResult = recordProgressLiveData.value
    assertThat(recordProgressResult).isNotNull()
    assertThat(recordProgressResult!!.isFailure()).isTrue()
    assertThat(recordProgressResult.getErrorOrNull())
      .hasMessageThat()
      .contains("No story found with ID: invalid_story_id")
  }

  private fun setUpTestApplicationComponent() {
    DaggerStoryProgressControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(storyProgressControllerTest: StoryProgressControllerTest)
  }
}
