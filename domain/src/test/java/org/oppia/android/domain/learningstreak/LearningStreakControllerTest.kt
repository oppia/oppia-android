package org.oppia.android.domain.learningstreak

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LearningStreakController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LearningStreakControllerTest.TestApplication::class)
class LearningStreakControllerTest {
  @Inject
  lateinit var learningStreakController: LearningStreakController
  
  @Inject
  lateinit var profileManagementController: ProfileManagementController
  
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  
  private val testDispatcher = TestCoroutineDispatcher()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @Test
  fun testGetLearningStreak_initialState_returnsZeroStreak() = testDispatcher.runBlockingTest {
    val profileId = 1
    createProfile(profileId)
    
    val learningStreakProvider = learningStreakController.getLearningStreak(profileId)
    val streak = learningStreakProvider.value.getOrThrow()
    
    assertThat(streak.currentStreak).isEqualTo(0)
    assertThat(streak.longestStreak).isEqualTo(0)
  }

  @Test
  fun testRecordLearningSession_firstSession_startsStreak() = testDispatcher.runBlockingTest {
    val profileId = 1
    createProfile(profileId)
    
    learningStreakController.recordLearningSession(profileId)
    testCoroutineDispatchers.runCurrent()
    
    val learningStreakProvider = learningStreakController.getLearningStreak(profileId)
    val streak = learningStreakProvider.value.getOrThrow()
    
    assertThat(streak.currentStreak).isEqualTo(1)
    assertThat(streak.longestStreak).isEqualTo(1)
    assertThat(streak.totalSessionsLoggedToday).isEqualTo(1)
  }

  private fun createProfile(profileId: Int) {
    profileManagementController.addProfile(
      name = "Test User",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = 0,
      isAdmin = false
    )
  }

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
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(
      @BlockingDispatcher blockingDispatcher: CoroutineDispatcher
    ): CoroutineDispatcher {
      return blockingDispatcher
    }

    @EnableLearnerStudyAnalytics
    @Provides
    fun provideEnableLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(false)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(learningStreakControllerTest: LearningStreakControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLearningStreakControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
        .also {
          it.getApplicationStartupListeners().forEach(ApplicationStartupListener::onCreate)
        }
    }

    fun inject(learningStreakControllerTest: LearningStreakControllerTest) {
      component.inject(learningStreakControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
