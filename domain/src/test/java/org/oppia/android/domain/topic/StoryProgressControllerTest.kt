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
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
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
  fun testStoryProgressController_recordRecentlyPlayedChapter_isSuccessful() {
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      fakeOppiaClock.getCurrentTimeMs()
    ).toLiveData().observeForever(mockRecordProgressObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
  }

  private fun verifyRecordProgressSucceeded() {
    verify(
      mockRecordProgressObserver,
      atLeastOnce()
    ).onChanged(recordProgressResultCaptor.capture())
    assertThat(recordProgressResultCaptor.value.isSuccess()).isTrue()
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
