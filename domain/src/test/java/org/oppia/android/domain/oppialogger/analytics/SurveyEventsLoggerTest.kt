package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.FirestoreLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.survey.SurveyEventsLogger
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.FakeFirestoreEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
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
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [SurveyEventsLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SurveyEventsLoggerTest.TestApplication::class)
class SurveyEventsLoggerTest {
  private companion object {
    private const val TEST_SURVEY_ID = "test_survey_id"
    private const val TEST_ANSWER = "Some text response"
  }

  @Inject
  lateinit var surveyEventsLogger: SurveyEventsLogger

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var fakeFirestoreEventLogger: FakeFirestoreEventLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val profileId by lazy {
    ProfileId.newBuilder().apply { loggedInInternalProfileId = 0 }.build()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLogAbandonSurvey_logsEventWithCorrectValues() {
    surveyEventsLogger.logAbandonSurvey(TEST_SURVEY_ID, profileId, SurveyQuestionName.MARKET_FIT)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(eventLog).hasAbandonSurveyContextThat {
      hasSurveyDetailsThat {
        hasSurveyIdThat().isEqualTo(TEST_SURVEY_ID)
        hasInternalProfileIdThat().isEqualTo("0")
      }
      hasQuestionNameThat().isEqualTo(SurveyQuestionName.MARKET_FIT)
    }
  }

  @Test
  fun testLogMandatoryResponses_logsEventWithCorrectValues() {
    surveyEventsLogger.logMandatoryResponses(
      TEST_SURVEY_ID,
      profileId,
      UserTypeAnswer.LEARNER,
      MarketFitAnswer.DISAPPOINTED,
      npsScore = 8
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()

    assertThat(eventLog).hasMandatorySurveyResponseContextThat {
      hasSurveyDetailsThat {
        hasSurveyIdThat().isNotEmpty()
        hasInternalProfileIdThat().isEqualTo("0")
      }
      hasUserTypeAnswerThat().isEqualTo(UserTypeAnswer.LEARNER)
      hasMarketFitAnswerThat().isEqualTo(MarketFitAnswer.DISAPPOINTED)
      hasNpsScoreAnswerThat().isEqualTo(8)
    }
  }

  @Test
  fun testLogOptionalResponse_logsEventWithCorrectValues() {
    surveyEventsLogger.logOptionalResponse(
      TEST_SURVEY_ID,
      profileId,
      TEST_ANSWER
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeFirestoreEventLogger.getMostRecentEvent()

    assertThat(eventLog).hasOptionalSurveyResponseContextThat {
      hasSurveyDetailsThat {
        hasSurveyIdThat().isNotEmpty()
        hasInternalProfileIdThat().isEqualTo("0")
      }
      hasFeedbackAnswerThat().isEqualTo(TEST_ANSWER)
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerSurveyEventsLoggerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
    // module in tests to avoid needing to specify these settings for tests.
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
  class TestLogStorageModule {
    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2

    @Provides
    @ExceptionLogStorageCacheSize
    fun provideExceptionLogStorageCacheSize(): Int = 2

    @Provides
    @FirestoreLogStorageCacheSize
    fun provideFirestoreLogStorageCacheSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class,
      ApplicationLifecycleModule::class, AssetModule::class, TestAuthenticationModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: SurveyEventsLoggerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSurveyEventsLoggerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: SurveyEventsLoggerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
