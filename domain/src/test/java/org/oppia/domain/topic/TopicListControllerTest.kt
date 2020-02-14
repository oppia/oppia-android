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
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.DefaultGcsResource
import org.oppia.util.parser.ImageDownloadUrlTemplate
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [TopicListController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TopicListControllerTest {
  @Inject lateinit var topicListController: TopicListController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  // TODO(#15): Add tests for recommended lessons rather than promoted, and tests for the 'continue playing' LiveData
  //  not providing any data for cases when there are no ongoing lessons. Also, add tests for other uncovered cases
  //  (such as having and not having lessons in either of the OngoingStoryList section, or AsyncResult errors).

  @Test
  fun testRetrieveTopicList_isSuccessful() {
    val topicListLiveData = topicListController.getTopicList()

    val topicListResult = topicListLiveData.value
    assertThat(topicListResult).isNotNull()
    assertThat(topicListResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveTopicList_providesListOfMultipleTopics() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    assertThat(topicList.topicSummaryCount).isGreaterThan(1)
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectTopicInfo() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.name).isEqualTo("First Topic")
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectThumbnail() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectLessonCount() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.totalChapterCount).isEqualTo(4)
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectTopicInfo() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val secondTopic = topicList.getTopicSummary(1)
    assertThat(secondTopic.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(secondTopic.name).isEqualTo("Second Topic")
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectThumbnail() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val secondTopic = topicList.getTopicSummary(1)
    assertThat(secondTopic.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectLessonCount() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val secondTopic = topicList.getTopicSummary(1)
    assertThat(secondTopic.totalChapterCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectTopicInfo() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val fractionsTopic = topicList.getTopicSummary(2)
    assertThat(fractionsTopic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.name).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectThumbnail() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val fractionsTopic = topicList.getTopicSummary(2)
    assertThat(fractionsTopic.topicThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectLessonCount() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val fractionsTopic = topicList.getTopicSummary(2)
    assertThat(fractionsTopic.totalChapterCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectTopicInfo() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val ratiosTopic = topicList.getTopicSummary(3)
    assertThat(ratiosTopic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.name).isEqualTo("Ratios and Proportional Reasoning")
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectThumbnail() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val ratiosTopic = topicList.getTopicSummary(3)
    assertThat(ratiosTopic.topicThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectLessonCount() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val ratiosTopic = topicList.getTopicSummary(3)
    assertThat(ratiosTopic.totalChapterCount).isEqualTo(4)
  }

  @Test
  fun testRetrieveTopicList_promotedLesson_hasCorrectLessonInfo() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val promotedStory = topicList.promotedStory
    assertThat(promotedStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(promotedStory.storyName).isEqualTo("Matthew Goes to the Bakery")
  }

  @Test
  fun testRetrieveTopicList_promotedLesson_hasCorrectTopicInfo() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val promotedStory = topicList.promotedStory
    assertThat(promotedStory.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(promotedStory.topicName).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveTopicList_promotedLesson_hasCorrectCompletionStats() {
    val topicListLiveData = topicListController.getTopicList()

    val topicList = topicListLiveData.value!!.getOrThrow()
    val promotedStory = topicList.promotedStory
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveOngoingStoryList_isSuccessful() {
    val ongoingStoryListLiveData = topicListController.getOngoingStoryList()

    val ongoingStoryListResult = ongoingStoryListLiveData.value
    assertThat(ongoingStoryListResult).isNotNull()
    assertThat(ongoingStoryListResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveOngoingStoryList_withinSevenDays_hasOngoingLesson() {
    val ongoingStoryListLiveData = topicListController.getOngoingStoryList()

    val ongoingStoryList = ongoingStoryListLiveData.value!!.getOrThrow()
    assertThat(ongoingStoryList.recentStoryCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveOngoingStoryList_recentLesson_hasCorrectStoryInfo() {
    val ongoingStoryListLiveData = topicListController.getOngoingStoryList()

    val ongoingStoryList = ongoingStoryListLiveData.value!!.getOrThrow()
    val recentLesson = ongoingStoryList.getRecentStory(0)
    assertThat(recentLesson.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(recentLesson.storyName).isEqualTo("Matthew Goes to the Bakery")
  }

  @Test
  fun testRetrieveOngoingStoryList_recentLesson_hasCorrectTopicInfo() {
    val ongoingStoryListLiveData = topicListController.getOngoingStoryList()

    val ongoingStoryList = ongoingStoryListLiveData.value!!.getOrThrow()
    val recentLesson = ongoingStoryList.getRecentStory(0)
    assertThat(recentLesson.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(recentLesson.topicName).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveOngoingStoryList_recentLesson_hasCorrectCompletionStats() {
    val ongoingStoryListLiveData = topicListController.getOngoingStoryList()

    val ongoingStoryList = ongoingStoryListLiveData.value!!.getOrThrow()
    val recentLesson = ongoingStoryList.getRecentStory(0)
    assertThat(recentLesson.completedChapterCount).isEqualTo(1)
    assertThat(recentLesson.totalChapterCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveOngoingStoryList_recentLesson_hasCorrectThumbnail() {
    val ongoingStoryListLiveData = topicListController.getOngoingStoryList()

    val ongoingStoryList = ongoingStoryListLiveData.value!!.getOrThrow()
    val recentLesson = ongoingStoryList.getRecentStory(0)
    assertThat(recentLesson.lessonThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
  }

  @Test
  fun testRetrieveOngoingStoryList_earlierThanSevenDays_doesNotHaveOngoingLesson() {
    val ongoingStoryListLiveData = topicListController.getOngoingStoryList()

    val ongoingStoryList = ongoingStoryListLiveData.value!!.getOrThrow()
    assertThat(ongoingStoryList.olderStoryCount).isEqualTo(0)
  }

  private fun setUpTestApplicationComponent() {
    DaggerTopicListControllerTest_TestApplicationComponent.builder()
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

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @DefaultGcsPrefix
    @Singleton
    fun provideDefaultGcsPrefix(): String {
      return "https://storage.googleapis.com/"
    }

    @Provides
    @DefaultGcsResource
    @Singleton
    fun provideDefaultGcsResource(): String {
      return "oppiaserver-resources/"
    }

    @Provides
    @ImageDownloadUrlTemplate
    @Singleton
    fun provideImageDownloadUrlTemplate(): String {
      return "%s/%s/assets/image/%s"
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

    fun inject(topicListControllerTest: TopicListControllerTest)
  }
}
