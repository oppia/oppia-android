package org.oppia.domain.topic

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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
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
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterPlayState.COMPLETED
import org.oppia.app.model.ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
import org.oppia.app.model.ChapterPlayState.NOT_STARTED
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryProgress
import org.oppia.app.model.TopicProgress
import org.oppia.app.model.TopicProgressDatabase
import org.oppia.domain.profile.ProfileTestHelper
import org.oppia.util.data.AsyncResult
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
import kotlin.coroutines.EmptyCoroutineContext

private const val EXPLORATION_ID_1 = "DUMMY_EXPLORATION_ID_1"
private const val EXPLORATION_ID_2 = "DUMMY_EXPLORATION_ID_2"
private const val STORY_ID_1 = "DUMMY_STORY_ID_1"
private const val STORY_ID_2 = "DUMMY_STORY_ID_2"
private const val TOPIC_ID_1 = "DUMMY_TOPIC_ID_1"
private const val TOPIC_ID_2 = "DUMMY_TOPIC_ID_2"

/** Tests for [StoryProgressController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StoryProgressControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  @Inject lateinit var context: Context

  @Inject lateinit var storyProgressController: StoryProgressController

  @Inject lateinit var profileTestHelper: ProfileTestHelper

  @Mock lateinit var mockRecordProgressObserver: Observer<AsyncResult<Any?>>
  @Captor lateinit var recordProgressResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock lateinit var mockProfileProgressObserver: Observer<AsyncResult<TopicProgressDatabase>>
  @Captor lateinit var profileProgressResultCaptor: ArgumentCaptor<AsyncResult<TopicProgressDatabase>>

  @Mock lateinit var mockTopicProgressObserver: Observer<AsyncResult<TopicProgress>>
  @Captor lateinit var topicProgressResultCaptor: ArgumentCaptor<AsyncResult<TopicProgress>>

  @Mock lateinit var mockStoryProgressObserver: Observer<AsyncResult<StoryProgress>>
  @Captor lateinit var storyProgressResultCaptor: ArgumentCaptor<AsyncResult<StoryProgress>>

  @Mock lateinit var mockChapterProgressObserver: Observer<AsyncResult<ChapterPlayState>>
  @Captor lateinit var chapterProgressResultCaptor: ArgumentCaptor<AsyncResult<ChapterPlayState>>

  private lateinit var profileId1: ProfileId
  private lateinit var profileId2: ProfileId

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Before
  fun setUp() {
    profileId1 = ProfileId.newBuilder().setInternalId(1).build()
    profileId2 = ProfileId.newBuilder().setInternalId(2).build()
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
    assertThat(storyProgress.chapterProgressMap[TEST_EXPLORATION_ID_0]).isEqualTo(NOT_STARTED)
  }

  @Test
  fun testGetStoryProgress_validSecondStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(TEST_STORY_ID_1)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(3)
    assertThat(storyProgress.chapterProgressMap[TEST_EXPLORATION_ID_1]).isEqualTo(NOT_STARTED)
    assertThat(storyProgress.chapterProgressMap[TEST_EXPLORATION_ID_2]).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
    assertThat(storyProgress.chapterProgressMap[TEST_EXPLORATION_ID_3]).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetStoryProgress_validFractionsStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(FRACTIONS_STORY_ID_0)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(2)
    assertThat(storyProgress.chapterProgressMap[FRACTIONS_EXPLORATION_ID_0]).isEqualTo(NOT_STARTED)
    assertThat(storyProgress.chapterProgressMap[FRACTIONS_EXPLORATION_ID_1]).isEqualTo(
      NOT_PLAYABLE_MISSING_PREREQUISITES
    )
  }

  @Test
  fun testGetStoryProgress_validFirstRatiosStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(RATIOS_STORY_ID_0)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(2)
    assertThat(storyProgress.chapterProgressMap[RATIOS_EXPLORATION_ID_0]).isEqualTo(NOT_STARTED)
    assertThat(storyProgress.chapterProgressMap[RATIOS_EXPLORATION_ID_1]).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetStoryProgress_validSecondRatiosStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(RATIOS_STORY_ID_1)

    // The third chapter should be missing prerequisites since chapter prior to it has yet to be completed.
    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(2)
    assertThat(storyProgress.chapterProgressMap[RATIOS_EXPLORATION_ID_2]).isEqualTo(NOT_STARTED)
    assertThat(storyProgress.chapterProgressMap[RATIOS_EXPLORATION_ID_3]).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetStoryProgress_validThirdStory_providesCorrectChapterProgress() {
    val storyProgressLiveData = storyProgressController.getStoryProgress(TEST_STORY_ID_2)

    val storyProgress = storyProgressLiveData.value!!.getOrThrow()
    assertThat(storyProgress.chapterProgressCount).isEqualTo(1)
    assertThat(storyProgress.chapterProgressMap[TEST_EXPLORATION_ID_4]).isEqualTo(NOT_STARTED)
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
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_succeeds() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getTopicProgress(profileId1, TOPIC_ID_1).observeForever(mockTopicProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyTopicProgressSucceeded()

      val topicProgress = topicProgressResultCaptor.value.getOrThrow()
      assertThat(topicProgress).isNotNull()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getProfileProgress_profileProgressIsCorrect() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getProfileProgress(profileId1).observeForever(mockProfileProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyProfileProgressSucceeded()

      val profileProgress = profileProgressResultCaptor.value.getOrThrow()
      assertThat(profileProgress).isNotNull()
      assertThat(profileProgress.topicProgressMap[TOPIC_ID_1]).isNotNull()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getTopicProgress_topicProgressIsCorrect() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getTopicProgress(profileId1, TOPIC_ID_1).observeForever(mockTopicProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyTopicProgressSucceeded()

      val topicProgress = topicProgressResultCaptor.value.getOrThrow()
      assertThat(topicProgress).isNotNull()
      assertThat(topicProgress.storyProgressMap[STORY_ID_1]).isNotNull()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getProfileProgressForInvalidProfileId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getProfileProgress(profileId2).observeForever(mockProfileProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyProfileProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getTopicProgressForInvalidProfileId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getTopicProgress(profileId2, TOPIC_ID_1).observeForever(mockTopicProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyTopicProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getTopicProgressForInvalidTopicId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getTopicProgress(profileId1, TOPIC_ID_2).observeForever(mockTopicProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyTopicProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getStoryProgress_storyProgressIsCorrect() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getStoryProgress(profileId1, TOPIC_ID_1, STORY_ID_1)
        .observeForever(mockStoryProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyStoryProgressSucceeded()

      val storyProgress = storyProgressResultCaptor.value.getOrThrow()
      assertThat(storyProgress).isNotNull()
      assertThat(storyProgress.chapterProgressMap[EXPLORATION_ID_1]).isNotNull()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getStoryProgressForInvalidProfileId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getStoryProgress(profileId2, TOPIC_ID_1, STORY_ID_1)
        .observeForever(mockStoryProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()

      verifyStoryProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getStoryProgressForInvalidTopicId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getStoryProgress(profileId1, TOPIC_ID_2, STORY_ID_1)
        .observeForever(mockStoryProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()

      verifyStoryProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getStoryProgressForInvalidStoryId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getStoryProgress(profileId1, TOPIC_ID_1, STORY_ID_2)
        .observeForever(mockStoryProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()

      verifyStoryProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getChapterProgress_chapterProgressIsCorrect() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getChapterProgress(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockChapterProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyChapterProgressSucceeded()

      val chapterProgress = chapterProgressResultCaptor.value.getOrThrow()
      assertThat(chapterProgress).isNotNull()
      assertThat(chapterProgress).isEqualTo(COMPLETED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getChapterProgressForInvalidProfileId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getChapterProgress(profileId2, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockChapterProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyChapterProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getChapterProgressForInvalidTopicId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getChapterProgress(profileId1, TOPIC_ID_2, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockChapterProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyChapterProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getChapterProgressForInvalidStoryID_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getChapterProgress(profileId1, TOPIC_ID_1, STORY_ID_2, EXPLORATION_ID_1)
        .observeForever(mockChapterProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyChapterProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecordCompletedChapter_validData_recordProgress_getChapterProgressForInvalidExplorationId_noResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()

      storyProgressController.getChapterProgress(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_2)
        .observeForever(mockChapterProgressObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyChapterProgressFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgress_validData_recordProgressForMultipleChaptersInSameStory_getChapterProgress_twoResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)

      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_2)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()
      verifyRecordProgressSucceeded()

      storyProgressController.getStoryProgress(profileId1, TOPIC_ID_1, STORY_ID_1)
        .observeForever(mockStoryProgressObserver)
      advanceUntilIdle()
      verifyStoryProgressSucceeded()

      val storyProgress = storyProgressResultCaptor.value.getOrThrow()
      assertThat(storyProgress.chapterProgressCount).isEqualTo(2)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgress_validData_recordProgressForSameChapterInMultipleStories_getTopicProgress_twoResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)

      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_2, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()
      verifyRecordProgressSucceeded()

      storyProgressController.getTopicProgress(profileId1, TOPIC_ID_1)
        .observeForever(mockTopicProgressObserver)
      advanceUntilIdle()
      verifyTopicProgressSucceeded()

      val topicProgress = topicProgressResultCaptor.value.getOrThrow()
      assertThat(topicProgress.storyProgressCount).isEqualTo(2)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgress_validData_recordProgressForMultipleTopics_getProfileProgress_twoResultFound() =
    runBlockingTest(coroutineContext) {
      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_1, STORY_ID_1, EXPLORATION_ID_1)
        .observeForever(mockRecordProgressObserver)

      storyProgressController.recordCompletedChapter(profileId1, TOPIC_ID_2, STORY_ID_2, EXPLORATION_ID_2)
        .observeForever(mockRecordProgressObserver)
      advanceUntilIdle()
      verifyRecordProgressSucceeded()

      storyProgressController.getProfileProgress(profileId1)
        .observeForever(mockProfileProgressObserver)
      advanceUntilIdle()
      verifyProfileProgressSucceeded()

      val profileProgress = profileProgressResultCaptor.value.getOrThrow()
      assertThat(profileProgress.topicProgressCount).isEqualTo(2)
    }

  private fun verifyRecordProgressSucceeded() {
    verify(mockRecordProgressObserver, atLeastOnce()).onChanged(recordProgressResultCaptor.capture())
    assertThat(recordProgressResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyProfileProgressFailed() {
    verify(mockProfileProgressObserver, atLeastOnce()).onChanged(profileProgressResultCaptor.capture())
    val topicProgressDatabase = profileProgressResultCaptor.value.getOrThrow()
    assertThat(topicProgressDatabase.topicProgressMap.size).isEqualTo(0)
  }

  private fun verifyProfileProgressSucceeded() {
    verify(mockProfileProgressObserver, atLeastOnce()).onChanged(profileProgressResultCaptor.capture())
    assertThat(profileProgressResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyTopicProgressFailed() {
    verify(mockTopicProgressObserver, atLeastOnce()).onChanged(topicProgressResultCaptor.capture())
    val topicProgress = topicProgressResultCaptor.value.getOrThrow()
    assertThat(topicProgress.storyProgressMap.size).isEqualTo(0)
  }

  private fun verifyTopicProgressSucceeded() {
    verify(mockTopicProgressObserver, atLeastOnce()).onChanged(topicProgressResultCaptor.capture())
    assertThat(topicProgressResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyStoryProgressFailed(){
    verify(mockStoryProgressObserver, atLeastOnce()).onChanged(storyProgressResultCaptor.capture())
    val storyProgress = storyProgressResultCaptor.value.getOrThrow()
    assertThat(storyProgress.chapterProgressMap.size).isEqualTo(0)
  }

  private fun verifyStoryProgressSucceeded() {
    verify(mockStoryProgressObserver, atLeastOnce()).onChanged(storyProgressResultCaptor.capture())
    assertThat(storyProgressResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyChapterProgressFailed() {
    verify(mockChapterProgressObserver, atLeastOnce()).onChanged(chapterProgressResultCaptor.capture())
    val chapterPlayState = chapterProgressResultCaptor.value.getOrThrow()
    assertThat(chapterPlayState).isEqualTo(NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  private fun verifyChapterProgressSucceeded() {
    verify(mockChapterProgressObserver, atLeastOnce()).onChanged(chapterProgressResultCaptor.capture())
    assertThat(chapterProgressResultCaptor.value.isSuccess()).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerStoryProgressControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Qualifier annotation class TestDispatcher

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
