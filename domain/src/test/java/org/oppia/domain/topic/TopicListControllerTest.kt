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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.ProfileId
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
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
import kotlin.coroutines.EmptyCoroutineContext

/** Tests for [TopicListController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TopicListControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var topicListController: TopicListController

  @Inject
  lateinit var storyProgressController: StoryProgressController

  @Mock
  lateinit var mockOngoingStoryListObserver: Observer<AsyncResult<OngoingStoryList>>
  @Captor
  lateinit var ongoingStoryListResultCaptor: ArgumentCaptor<AsyncResult<OngoingStoryList>>

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  private lateinit var profileId0: ProfileId

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    profileId0 = ProfileId.newBuilder().setInternalId(0).build()
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTopicListControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
  @ExperimentalCoroutinesApi
  fun testRetrieveOngoingStoryList_defaultLesson_hasCorrectInfo() =
    runBlockingTest(coroutineContext) {
      topicListController.getOngoingStoryList(profileId0).observeForever(mockOngoingStoryListObserver)
      advanceUntilIdle()

      verifyGetOngoingStoryListSucceeded()
      verifyDefaultOngoingStoryListSucceeded()
    }

  // Fractions - Exp1 - seen
  // Fractions - Exp1 -finished
  // Fractions - Exp1 -finished and Exp2 Seen
  // Fractions - Exp1 -finished and Exp2 finished

  // Ratios - Exp0 -Seen and Exp2 -Seen
  // Ratios - Exp0 -Finished and Exp2 -Seen

  // Ratios - Exp0 -Finished and Exp2 -Finished and Fractions Exp0 Finished - Recent

  // Ratios - Exp0 -Finished and Exp2 -Finished and Fractions Exp0 Finished - Old

  // Ratios - Exp0 -Finished and Exp2 -Finished and Fractions Exp0 Finished - Recent/old

  private fun verifyGetOngoingStoryListSucceeded() {
    verify(mockOngoingStoryListObserver, atLeastOnce()).onChanged(ongoingStoryListResultCaptor.capture())
    assertThat(ongoingStoryListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyDefaultOngoingStoryListSucceeded() {
    val ongoingTopicList = ongoingStoryListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.recentStoryList.size).isEqualTo(2)
    assertThat(ongoingTopicList.recentStoryList[0].explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(ongoingTopicList.recentStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(ongoingTopicList.recentStoryList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(ongoingTopicList.recentStoryList[0].topicName).isEqualTo("Fractions")
    assertThat(ongoingTopicList.recentStoryList[0].nextChapterName).isEqualTo("What is a Fraction?")
    assertThat(ongoingTopicList.recentStoryList[0].lessonThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    assertThat(ongoingTopicList.recentStoryList[0].completedChapterCount).isEqualTo(0)
    assertThat(ongoingTopicList.recentStoryList[0].totalChapterCount).isEqualTo(2)

    assertThat(ongoingTopicList.recentStoryList[1].explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(ongoingTopicList.recentStoryList[1].storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(ongoingTopicList.recentStoryList[1].topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(ongoingTopicList.recentStoryList[1].nextChapterName).isEqualTo("What is a Ratio?")
    assertThat(ongoingTopicList.recentStoryList[1].topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(ongoingTopicList.recentStoryList[1].lessonThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    assertThat(ongoingTopicList.recentStoryList[1].completedChapterCount).isEqualTo(0)
    assertThat(ongoingTopicList.recentStoryList[1].totalChapterCount).isEqualTo(2)
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
