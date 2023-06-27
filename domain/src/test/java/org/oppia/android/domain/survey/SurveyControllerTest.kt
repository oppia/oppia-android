package org.oppia.android.domain.survey

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
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [SurveyController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SurveyControllerTest.TestApplication::class)
class SurveyControllerTest {
  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var surveyController: SurveyController

  @Inject
  lateinit var surveyProgressController: SurveyProgressController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testController_startSurveySession_succeeds() {
    val surveyDataProvider =
      surveyController.startSurveySession()

    monitorFactory.waitForNextSuccessfulResult(surveyDataProvider)
  }

  @Test
  fun testController_startSurveySession_sessionStartsWithInitialQuestion() {
    surveyController.startSurveySession()

    val result = surveyProgressController.getCurrentQuestion()
    val ephemeralQuestion = monitorFactory.waitForNextSuccessfulResult(result)
    assertThat(ephemeralQuestion.question.questionName).isEqualTo(SurveyQuestionName.USER_TYPE)
  }

  @Test
  fun testStopSurveySession_withoutStartingSession_returnsFailure() {
    val stopProvider = surveyController.stopSurveySession()

    // The operation should be failing since the session hasn't started.
    val result = monitorFactory.waitForNextFailureResult(stopProvider)

    assertThat(result).isInstanceOf(IllegalStateException::class.java)
    assertThat(result).hasMessageThat().contains("Session isn't initialized yet.")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  @Module
  class TestModule {
    internal companion object {
      var enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
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

  @Module
  class TestLoggingIdentifierModule {
    companion object {
      const val applicationIdSeed = 1L
    }

    @Provides
    @ApplicationIdSeed
    fun provideApplicationIdSeed(): Long = applicationIdSeed
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ApplicationLifecycleModule::class, TestDispatcherModule::class, LocaleProdModule::class,
      ExplorationProgressModule::class, TestLogReportingModule::class, AssetModule::class,
      NetworkConnectionUtilDebugModule::class, SyncStatusModule::class, LogStorageModule::class,
      TestLoggingIdentifierModule::class, SurveyQuestionModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(surveyControllerTest: SurveyControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSurveyControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(surveyControllerTest: SurveyControllerTest) {
      component.inject(surveyControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
