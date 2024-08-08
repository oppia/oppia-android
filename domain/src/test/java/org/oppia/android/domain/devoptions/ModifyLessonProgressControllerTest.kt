package org.oppia.android.domain.devoptions

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
import org.oppia.android.app.model.EphemeralStorySummary
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
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

/** Tests for [ModifyLessonProgressController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ModifyLessonProgressControllerTest.TestApplication::class)
class ModifyLessonProgressControllerTest {

  companion object {
    private const val TEST_TOPIC_ID_0 = "test_topic_id_0"
    private const val TEST_TOPIC_ID_1 = "test_topic_id_1"
    private const val TEST_TOPIC_ID_2 = "test_topic_id_2"
    private const val FRACTIONS_TOPIC_ID = "GJ2rLXRKD5hw"
    private const val RATIOS_TOPIC_ID = "omzF4oqgeTXd"

    private const val TEST_STORY_ID_0 = "test_story_id_0"
    private const val TEST_STORY_ID_2 = "test_story_id_2"
    private const val FRACTIONS_STORY_ID_0 = "wANbh4oOClga"
    private const val RATIOS_STORY_ID_0 = "wAMdg4oOClga"
    private const val RATIOS_STORY_ID_1 = "xBSdg4oOClga"

    private const val TEST_EXPLORATION_ID_2 = "test_exp_id_2"
    private const val FRACTIONS_EXPLORATION_ID_0 = "umPkwp0L1M0-"
    private const val FRACTIONS_EXPLORATION_ID_1 = "MjZzEVOG47_1"
  }

  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject lateinit var modifyLessonProgressController: ModifyLessonProgressController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(1).build()
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  @Test
  fun testRetrieveAllTopics_isSuccessful() {
    val topicsProvider = modifyLessonProgressController.getAllTopicsWithProgress(profileId)

    monitorFactory.waitForNextSuccessfulResult(topicsProvider)
  }

  @Test
  fun testRetrieveAllTopics_providesListOfMultipleTopics() {
    val allTopics = retrieveAllTopics()

    assertThat(allTopics.size).isGreaterThan(1)
  }

  @Test
  fun testRetrieveAllTopics_firstTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()

    val firstTopic = allTopics[0].topic
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.title.html).isEqualTo("First Test Topic")
  }

  @Test
  fun testRetrieveAllTopics_secondTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()

    val secondTopic = allTopics[1].topic
    assertThat(secondTopic.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(secondTopic.title.html).isEqualTo("Second Test Topic")
  }

  @Test
  fun testRetrieveAllTopics_fractionsTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()

    val fractionsTopic = allTopics[2].topic
    assertThat(fractionsTopic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.title.html).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveAllTopics_ratiosTopic_hasCorrectTopicInfo() {
    val allTopics = retrieveAllTopics()

    val ratiosTopic = allTopics[3].topic
    assertThat(ratiosTopic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.title.html).isEqualTo("Ratios and Proportional Reasoning")
  }

  @Test
  fun testRetrieveAllTopics_doesNotContainUnavailableTopic() {
    val allTopics = retrieveAllTopics()
    val topicIds = allTopics.map { it.topic.topicId }
    assertThat(topicIds).doesNotContain(TEST_TOPIC_ID_2)
  }

  @Test
  fun testRetrieveAllTopics_firstTopic_withoutAnyProgress_correctProgressFound() {
    val allTopics = retrieveAllTopics()

    val firstTopic = allTopics[0].topic
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(firstTopic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    assertThat(firstTopic.storyList[0].chapterList[1].missingPrerequisiteChapter.title.html)
      .isEqualTo(firstTopic.storyList[0].chapterList[0].title.html)
  }

  @Test
  fun testRetrieveAllTopics_firstTopic_withTopicCompleted_correctProgressFound() {
    markFirstTestTopicCompleted()

    val allTopics = retrieveAllTopics()

    val firstTopic = allTopics[0].topic
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(firstTopic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testRetrieveAllTopics_withoutAnyProgress_noTopicIsCompleted() {
    val allTopics = retrieveAllTopics()

    val topicsProgress = allTopics.map(modifyLessonProgressController::checkIfTopicIsCompleted)

    // None of the topics have progress.
    assertThat(topicsProgress.all { !it }).isTrue()
  }

  @Test
  fun markFirstTestTopicCompleted_testRetrieveAllTopics_onlyFirstTestTopicIsCompleted() {
    markFirstTestTopicCompleted()
    val allTopics = retrieveAllTopics()

    val topicsProgress =
      allTopics.associateBy { it.topic.topicId }.mapValues { (_, topic) ->
        modifyLessonProgressController.checkIfTopicIsCompleted(topic)
      }

    // All topics except the test topic 0 should not have progress.
    val nonTestTopics = topicsProgress.filterNot { (id, _) -> id == TEST_TOPIC_ID_0 }
    assertThat(nonTestTopics.values.count { !it }).isEqualTo(allTopics.size - 1)
    assertThat(topicsProgress[TEST_TOPIC_ID_0]).isTrue()
  }

  @Test
  fun testRetrieveAllStories_isSuccessful() {
    val storyProgressProvider = modifyLessonProgressController.getStoryMapWithProgress(profileId)

    monitorFactory.waitForNextSuccessfulResult(storyProgressProvider)
  }

  @Test
  fun testRetrieveAllStories_providesListOfMultipleStories() {
    val allStories = retrieveAllStories()

    assertThat(allStories.size).isGreaterThan(1)
  }

  @Test
  fun testRetrieveAllStories_firstStory_hasCorrectStoryInfo() {
    val allStories = retrieveAllStories()

    val firstStory = allStories[0].storySummary
    assertThat(firstStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(firstStory.storyTitle.html).isEqualTo("First Story")
  }

  @Test
  fun testRetrieveAllStories_otherStory_hasCorrectStoryInfo() {
    val allStories = retrieveAllStories()

    val secondStory = allStories[1].storySummary
    assertThat(secondStory.storyId).isEqualTo(TEST_STORY_ID_2)
    assertThat(secondStory.storyTitle.html).isEqualTo("Other Interesting Story")
  }

  @Test
  fun testRetrieveAllStories_fractionsStory_hasCorrectStoryInfo() {
    val allStories = retrieveAllStories()

    val fractionsStory = allStories[2].storySummary
    assertThat(fractionsStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(fractionsStory.storyTitle.html).isEqualTo("Matthew Goes to the Bakery")
  }

  @Test
  fun testRetrieveAllStories_ratiosStory1_hasCorrectStoryInfo() {
    val allStories = retrieveAllStories()

    val ratiosStory1 = allStories[3].storySummary
    assertThat(ratiosStory1.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(ratiosStory1.storyTitle.html).isEqualTo("Ratios: Part 1")
  }

  @Test
  fun testRetrieveAllStories_ratiosStory2_hasCorrectStoryInfo() {
    val allStories = retrieveAllStories()

    val ratiosStory2 = allStories[4].storySummary
    assertThat(ratiosStory2.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(ratiosStory2.storyTitle.html).isEqualTo("Ratios: Part 2")
  }

  @Test
  fun testRetrieveAllStories_firstStory_withoutAnyProgress_correctProgressFound() {
    val allStories = retrieveAllStories()

    val firstStory = allStories[0].storySummary
    assertThat(firstStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(firstStory.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(firstStory.chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    assertThat(firstStory.chapterList[1].missingPrerequisiteChapter.title.html)
      .isEqualTo(firstStory.chapterList[0].title.html)
  }

  @Test
  fun testRetrieveAllStories_firstStory_withStoryCompleted_correctProgressFound() {
    markFirstStoryCompleted()

    val allStories = retrieveAllStories()

    val firstStory = allStories[0].storySummary
    assertThat(firstStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(firstStory.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(firstStory.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testRetrieveAllStories_withoutAnyProgress_noStoryIsCompleted() {
    val allStories = retrieveAllStories()

    val storiesProgress = allStories.map(modifyLessonProgressController::checkIfStoryIsCompleted)

    // None of the stories have progress.
    assertThat(storiesProgress.all { !it }).isTrue()
  }

  @Test
  fun markFirstStoryCompleted_testRetrieveAllStories_onlyFirstStoryIsCompleted() {
    markFirstStoryCompleted()
    val allStories = retrieveAllStories()

    val storiesProgress =
      allStories.associateBy { it.storySummary.storyId }.mapValues { (_, storySummary) ->
        modifyLessonProgressController.checkIfStoryIsCompleted(storySummary)
      }

    // All stories except the test story 0 should not have progress.
    val nonTestStories = storiesProgress.filterNot { (id, _) -> id == TEST_STORY_ID_0 }
    assertThat(nonTestStories.values.count { !it }).isEqualTo(allStories.size - 1)
    assertThat(storiesProgress[TEST_STORY_ID_0]).isTrue()
  }

  @Test
  fun markFirstAndFractionsTopicsCompleted_bothTopicsAreCompleted() {
    modifyLessonProgressController.markMultipleTopicsCompleted(
      profileId,
      listOf(TEST_TOPIC_ID_0, FRACTIONS_TOPIC_ID)
    )

    val allTopics = retrieveAllTopics()
    val firstTopic = allTopics[0].topic
    val fractionsTopic = allTopics[2].topic
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(firstTopic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(fractionsTopic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(fractionsTopic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun markFirstAndRatios2StoriesCompleted_bothStoriesAreCompleted() {
    modifyLessonProgressController.markMultipleStoriesCompleted(
      profileId,
      mapOf(TEST_STORY_ID_0 to TEST_TOPIC_ID_0, RATIOS_STORY_ID_1 to RATIOS_TOPIC_ID)
    )

    val allStories = retrieveAllStories()
    val firstStory = allStories[0].storySummary
    val ratios2Story = allStories[4].storySummary
    assertThat(firstStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(firstStory.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(firstStory.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(ratios2Story.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(ratios2Story.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(ratios2Story.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun markPrototypeAndBothFractionsExplorationsCompleted_allThreeExplorationsAreCompleted() {
    modifyLessonProgressController.markMultipleChaptersCompleted(
      profileId,
      mapOf(
        TEST_EXPLORATION_ID_2 to Pair(TEST_STORY_ID_0, TEST_TOPIC_ID_0),
        FRACTIONS_EXPLORATION_ID_0 to Pair(FRACTIONS_STORY_ID_0, FRACTIONS_TOPIC_ID),
        FRACTIONS_EXPLORATION_ID_1 to Pair(FRACTIONS_STORY_ID_0, FRACTIONS_TOPIC_ID)
      )
    )

    val allStories = retrieveAllStories()
    val firstStory = allStories[0].storySummary
    val fractionsStory = allStories[2].storySummary
    assertThat(firstStory.chapterList[0].explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(firstStory.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(fractionsStory.chapterList[0].explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(fractionsStory.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(fractionsStory.chapterList[1].explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_1)
    assertThat(fractionsStory.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
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

  private fun markFirstStoryCompleted() {
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId,
      timestampOlderThanOneWeek = false
    )
  }

  private fun retrieveAllTopics(): List<EphemeralTopic> {
    val topicsProvider = modifyLessonProgressController.getAllTopicsWithProgress(profileId)
    return monitorFactory.waitForNextSuccessfulResult(topicsProvider)
  }

  private fun retrieveAllStories(): List<EphemeralStorySummary> {
    val storyProgressProvider = modifyLessonProgressController.getStoryMapWithProgress(profileId)
    return monitorFactory.waitForNextSuccessfulResult(storyProgressProvider).values.flatten()
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
      NetworkConnectionUtilDebugModule::class, AssetModule::class, LocaleProdModule::class,
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
