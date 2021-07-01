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
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Topic
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ModifyLessonProgressController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ModifyLessonProgressControllerTest.TestApplication::class)
class ModifyLessonProgressControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var modifyLessonProgressController: ModifyLessonProgressController

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Mock
  lateinit var mockAllTopicsObserver: Observer<AsyncResult<List<Topic>>>

  @Captor
  lateinit var allTopicsResultCaptor: ArgumentCaptor<AsyncResult<List<Topic>>>

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    profileId = ProfileId.newBuilder().setInternalId(1).build()
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  @Test
  fun testRetrieveAllTopics_isSuccessful() {
    val allTopicsLiveData = modifyLessonProgressController.getAllTopics(profileId).toLiveData()
    allTopicsLiveData.observeForever(mockAllTopicsObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockAllTopicsObserver).onChanged(allTopicsResultCaptor.capture())
    val allTopicsResult = allTopicsResultCaptor.value
    assertThat(allTopicsResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveAllTopics_providesListOfMultipleTopics() {
    val allTopics = retrieveAllTopics()
    assertThat(allTopics.size).isGreaterThan(1)
  }

  @Test
  fun testRetrieveAllTopics_firstTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()
    val firstTopic = allTopics[0]
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.name).isEqualTo("First Test Topic")
  }

  @Test
  fun testRetrieveAllTopics_secondTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()
    val secondTopic = allTopics[1]
    assertThat(secondTopic.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(secondTopic.name).isEqualTo("Second Test Topic")
  }

  @Test
  fun testRetrieveAllTopics_fractionsTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()
    val fractionsTopic = allTopics[2]
    assertThat(fractionsTopic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.name).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveAllTopics_ratiosTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()
    val ratiosTopic = allTopics[3]
    assertThat(ratiosTopic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.name).isEqualTo("Ratios and Proportional Reasoning")
  }

  @Test
  fun testRetrieveAllTopics_doesNotContainUnavailableTopic() {
    val allTopics = retrieveAllTopics()
    val topicIds = allTopics.map(Topic::getTopicId)
    assertThat(topicIds).doesNotContain(TEST_TOPIC_ID_2)
  }

  @Test
  fun testRetrieveAllTopics_firstTopic_withoutAnyProgress_correctProgressFound() {
    val allTopics = retrieveAllTopics()
    val firstTopic = allTopics[0]
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(firstTopic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    assertThat(firstTopic.storyList[0].chapterList[1].missingPrerequisiteChapter.name)
      .isEqualTo(firstTopic.storyList[0].chapterList[0].name)
  }

  @Test
  fun testRetrieveAllTopics_firstTopic_withoutTopicCompleted_correctProgressFound() {
    markFirstTestTopicCompleted()
    val allTopics = retrieveAllTopics()
    val firstTopic = allTopics[0]
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(firstTopic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun markFirstTestTopicCompleted() {
    storyProgressTestHelper.markCompletedTestTopic0(
      profileId,
      timestampOlderThanOneWeek = false
    )
  }

  private fun retrieveAllTopics(): List<Topic> {
    val allTopicsLiveData = modifyLessonProgressController.getAllTopics(profileId).toLiveData()
    allTopicsLiveData.observeForever(mockAllTopicsObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockAllTopicsObserver).onChanged(allTopicsResultCaptor.capture())
    return allTopicsResultCaptor.value.getOrThrow()
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
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(modifyLessonProgressControllerTest: ModifyLessonProgressControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerModifyLessonProgressControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(modifyLessonProgressControllerTest: ModifyLessonProgressControllerTest) {
      component.inject(modifyLessonProgressControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
