package org.oppia.android.domain.classroom

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.domain.topic.TEST_TOPIC_ID_2
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.testing.FakeAssetRepository
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.image.DefaultGcsPrefix
import org.oppia.android.util.parser.image.ImageDownloadUrlTemplate
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ClassroomController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ClassroomControllerTest.TestApplication::class)
class ClassroomControllerTest {
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var classroomController: ClassroomController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private lateinit var profileId0: ProfileId

  @Before
  fun setUp() {
    profileId0 = ProfileId.newBuilder().setInternalId(0).build()
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetClassroomList_isSuccessful() {
    val classroomListProvider = classroomController.getClassroomList(profileId0)

    monitorFactory.waitForNextSuccessfulResult(classroomListProvider)
  }

  @Test
  fun testGetClassroomList_providesListOfMultipleClassrooms() {
    val classroomList = getClassroomList(profileId0)

    assertThat(classroomList.classroomSummaryList.size).isGreaterThan(1)
  }

  @Test
  fun testGetClassroomList_firstClassroom_hasCorrectClassroomInfo() {
    val classroomList = getClassroomList(profileId0)

    val firstClassroom = classroomList.classroomSummaryList[0]
    assertThat(firstClassroom.classroomSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(firstClassroom.classroomSummary.classroomTitle.html).isEqualTo("Science")
  }

  @Test
  fun testGetClassroomList_firstClassroom_hasCorrectTopicCount() {
    val classroomList = getClassroomList(profileId0)

    val firstClassroom = classroomList.classroomSummaryList[0]
    assertThat(firstClassroom.classroomSummary.topicSummaryCount).isEqualTo(2)
  }

  @Test
  fun testGetClassroomList_secondClassroom_hasCorrectClassroomInfo() {
    val classroomList = getClassroomList(profileId0)

    val secondClassroom = classroomList.classroomSummaryList[1]
    assertThat(secondClassroom.classroomSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(secondClassroom.classroomSummary.classroomTitle.html).isEqualTo("Maths")
  }

  @Test
  fun testGetClassroomList_secondClassroom_hasCorrectTopicCount() {
    val classroomList = getClassroomList(profileId0)

    val secondClassroom = classroomList.classroomSummaryList[1]
    assertThat(secondClassroom.classroomSummary.topicSummaryCount).isEqualTo(2)
  }

  @Test
  fun testGetClassroomList_noPublishedTopicsInThirdClassroom_checkListExcludesThirdClassroom() {
    val classroomList = getClassroomList(profileId0)

    assertThat(classroomList.classroomSummaryList.size).isEqualTo(2)
  }

  @Test
  fun testGetClassrooms_returnsAllClassrooms() {
    val classrooms = classroomController.getClassrooms()

    assertThat(classrooms[0].id).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(classrooms[1].id).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(classrooms[2].id).isEqualTo(TEST_CLASSROOM_ID_2)
  }

  @Test
  fun testGetClassroomById_hasCorrectClassroomInfo() {
    val classroom = classroomController.getClassroomById(TEST_CLASSROOM_ID_0)

    assertThat(classroom.id).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(classroom.translatableTitle.html).isEqualTo("Science")
  }

  @Test
  fun testRetrieveTopicList_isSuccessful() {
    val topicListProvider = classroomController.getTopicList(profileId0, TEST_CLASSROOM_ID_0)

    monitorFactory.waitForNextSuccessfulResult(topicListProvider)
  }

  @Test
  fun testRetrieveTopicList_testTopic0_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_0)

    val firstTopic = topicList.getTopicSummary(0).topicSummary
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.title.html).isEqualTo("First Test Topic")
  }

  @Test
  fun testRetrieveTopicList_testTopic0_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_0)

    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Science")
  }

  @Test
  fun testRetrieveTopicList_testTopic0_hasCorrectLessonCount() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_0)

    val firstTopic = topicList.getTopicSummary(0).topicSummary
    assertThat(firstTopic.totalChapterCount).isEqualTo(3)
  }

  @Test
  fun testRetrieveTopicList_testTopic1_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_0)

    val secondTopic = topicList.getTopicSummary(1).topicSummary
    assertThat(secondTopic.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(secondTopic.title.html).isEqualTo("Second Test Topic")
  }

  @Test
  fun testRetrieveTopicList_testTopic1_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_0)

    val firstTopic = topicList.getTopicSummary(1)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Science")
  }

  @Test
  fun testRetrieveTopicList_testTopic1_hasCorrectLessonCount() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_0)

    val secondTopic = topicList.getTopicSummary(1).topicSummary
    assertThat(secondTopic.totalChapterCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_1)

    val fractionsTopic = topicList.getTopicSummary(0).topicSummary
    assertThat(fractionsTopic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.title.html).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_1)

    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Maths")
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_1)

    val fractionsTopic = topicList.getTopicSummary(0).topicSummary
    assertThat(fractionsTopic.totalChapterCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_1)

    val ratiosTopic = topicList.getTopicSummary(1).topicSummary
    assertThat(ratiosTopic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.title.html).isEqualTo("Ratios and Proportional Reasoning")
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_1)

    val firstTopic = topicList.getTopicSummary(1)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Maths")
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_1)

    val ratiosTopic = topicList.getTopicSummary(1).topicSummary
    assertThat(ratiosTopic.totalChapterCount).isEqualTo(4)
  }

  @Test
  fun testRetrieveTopicList_doesNotContainUnavailableTopic() {
    val topicList = retrieveTopicList(TEST_CLASSROOM_ID_2)

    // Verify that the topic list does not contain a not-yet published topic (since it can't be
    // played by the user).
    val topicIds = topicList.topicSummaryList.map { it.topicSummary }.map { it.topicId }
    assertThat(topicIds).doesNotContain(TEST_TOPIC_ID_2)
  }

  @Test
  fun testGetClassroomIdByTopicId_testTopic0_returnsCorrectClassroomId() {
    val classroomId = classroomController.getClassroomIdByTopicId(TEST_TOPIC_ID_0)

    assertThat(classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
  }

  @Test
  fun testGetClassroomIdByTopicId_testTopic1_returnsCorrectClassroomId() {
    val classroomId = classroomController.getClassroomIdByTopicId(TEST_TOPIC_ID_1)

    assertThat(classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
  }

  @Test
  fun testGetClassroomIdByTopicId_fractionsTopic_returnsCorrectClassroomId() {
    val classroomId = classroomController.getClassroomIdByTopicId(FRACTIONS_TOPIC_ID)

    assertThat(classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
  }

  @Test
  fun testGetClassroomIdByTopicId_ratiosTopic_returnsCorrectClassroomId() {
    val classroomId = classroomController.getClassroomIdByTopicId(RATIOS_TOPIC_ID)

    assertThat(classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
  }

  @Test
  fun testGetClassroomIdByTopicId_testTopic2_returnsCorrectClassroomId() {
    val classroomId = classroomController.getClassroomIdByTopicId(TEST_TOPIC_ID_2)

    assertThat(classroomId).isEqualTo(TEST_CLASSROOM_ID_2)
  }

  @Test
  fun testGetClassroomIdByTopicId_nonExistentTopic_returnsEmptyId() {
    val classroomId = classroomController.getClassroomIdByTopicId("invalid_topic_id")

    assertThat(classroomId).isEmpty()
  }

  private fun getClassroomList(profileId: ProfileId) =
    monitorFactory.waitForNextSuccessfulResult(classroomController.getClassroomList(profileId))

  private fun retrieveTopicList(classroomId: String) = monitorFactory.waitForNextSuccessfulResult(
    classroomController.getTopicList(profileId0, classroomId)
  )

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @Provides
    @DefaultGcsPrefix
    @Singleton
    fun provideDefaultGcsPrefix(): String {
      return "https://storage.googleapis.com/"
    }

    @Provides
    @DefaultResourceBucketName
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

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()

    @Provides
    fun provideFakeAssetRepository(fakeImpl: FakeAssetRepository): AssetRepository = fakeImpl
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, TestPlatformParameterModule::class,
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

    fun inject(classroomControllerTest: ClassroomControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerClassroomControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(classroomControllerTest: ClassroomControllerTest) {
      component.inject(classroomControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
