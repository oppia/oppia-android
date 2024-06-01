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
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.topic.TEST_CLASSROOM_ID_1
import org.oppia.android.domain.topic.TEST_CLASSROOM_ID_2
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
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
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetClassroomList_isSuccessful() {
    val classroomListProvider = classroomController.getClassroomList(profileId0)

    monitorFactory.waitForNextSuccessfulResult(classroomListProvider)
  }

  @Test
  fun testGetClassroomList_providesListOfMultipleClassrooms() {
    val classroomList = getClassroomList()

    assertThat(classroomList.size).isGreaterThan(1)
  }

  @Test
  fun testGetClassroomList_firstClassroom_hasCorrectClassroomInfo() {
    val classroomList = getClassroomList()

    val firstClassroom = classroomList[0]
    assertThat(firstClassroom.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(firstClassroom.classroomTitle.html).isEqualTo("Science")
  }

  @Test
  fun testGetClassroomList_firstClassroom_hasCorrectTopicCount() {
    val classroomList = getClassroomList()

    val firstClassroom = classroomList[0]
    assertThat(firstClassroom.topicSummaryCount).isEqualTo(2)
  }

  @Test
  fun testGetClassroomList_secondClassroom_hasCorrectClassroomInfo() {
    val classroomList = getClassroomList()

    val secondClassroom = classroomList[1]
    assertThat(secondClassroom.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(secondClassroom.classroomTitle.html).isEqualTo("Maths")
  }

  @Test
  fun testGetClassroomList_secondClassroom_hasCorrectTopicCount() {
    val classroomList = getClassroomList()

    val secondClassroom = classroomList[1]
    assertThat(secondClassroom.topicSummaryCount).isEqualTo(2)
  }

  @Test
  fun testGetClassroomList_thirdClassroom_hasCorrectClassroomInfo() {
    val classroomList = getClassroomList()

    val thirdClassroom = classroomList[2]
    assertThat(thirdClassroom.classroomId).isEqualTo(TEST_CLASSROOM_ID_2)
    assertThat(thirdClassroom.classroomTitle.html).isEqualTo("English")
  }

  @Test
  fun testGetClassroomList_thirdClassroom_hasCorrectTopicCount() {
    val classroomList = getClassroomList()

    val thirdClassroom = classroomList[2]
    assertThat(thirdClassroom.topicSummaryCount).isEqualTo(1)
  }

  private fun getClassroomList() =
    monitorFactory.waitForNextSuccessfulResult(classroomController.getClassroomList(profileId0))

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
