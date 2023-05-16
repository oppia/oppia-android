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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.ExplorationActiveTimeController
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.oppialogger.ApplicationIdSeed
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_LENGTH_SHORT = 120000L
private const val SESSION_LENGTH_LONG = 360000L
private const val SURVEY_GRACE_PERIOD_DAYS = 30

/** Tests for [SurveyGatingController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = SurveyGatingControllerTest.TestApplication::class)
class SurveyGatingControllerTest {
  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Inject
  lateinit var surveyGatingController: SurveyGatingController

  @Inject
  lateinit var explorationActiveTimeController: ExplorationActiveTimeController

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
  }

  @Test
  fun testGating_lateNight_stillWithinGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(LATE_NIGHT_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_lateNight_isPastGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(LATE_NIGHT_UTC_TIMESTAMP_MILLIS)
    // not setting the surveyLastShownTimestamp because the default is set to the beginning of epoch
    // which will always be more than the grace period days in the past.
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_lateNight_isPastGracePeriod_minimumAggregateTimeMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(LATE_NIGHT_UTC_TIMESTAMP_MILLIS)
    // not setting the surveyLastShownTimestamp because the default is set to the beginning of epoch
    // which will always be more than the grace period days in the past.
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_earlyMorning_stillWithinGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EARLY_MORNING_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_earlyMorning_isPastGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EARLY_MORNING_UTC_TIMESTAMP_MILLIS)
    // not setting the surveyLastShownTimestamp because the default is set to the beginning of epoch
    // which will always be more than the grace period days in the past.
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_earlyMorning_isPastGracePeriod_minimumAggregateTimeMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EARLY_MORNING_UTC_TIMESTAMP_MILLIS)
    // not setting the surveyLastShownTimestamp because the default is set to the beginning of epoch
    // which will always be more than the grace period days in the past.
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_midMorning_stillWithinGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(MID_MORNING_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_midMorning_stillWithinGracePeriod_minimumAggregateTimeMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(MID_MORNING_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_midMorning_isPastGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(MID_MORNING_UTC_TIMESTAMP_MILLIS)
    // not setting the surveyLastShownTimestamp because the default is set to the beginning of epoch
    // which will always be more than the grace period days in the past.
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_midMorning_isPastGracePeriod_minimumAggregateTimeMet_returnsTrue() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    testCoroutineDispatchers.advanceTimeBy(
      TimeUnit.DAYS.toMillis(SURVEY_GRACE_PERIOD_DAYS.toLong())
    )
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(MID_MORNING_UTC_TIMESTAMP_MILLIS)
    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isTrue()
  }

  @Test
  fun testGating_afternoon_stillWithinGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(AFTERNOON_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_afternoon_stillWithinGracePeriod__minimumAggregateTimeMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(AFTERNOON_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_afternoon_isPastGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(AFTERNOON_UTC_TIMESTAMP_MILLIS)
    // not setting the surveyLastShownTimestamp because the default is set to the beginning of epoch
    // which will always be more than the grace period days in the past.
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_afternoon_isPastGracePeriod_minimumAggregateTimeMet_returnsTrue() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    testCoroutineDispatchers.advanceTimeBy(
      TimeUnit.DAYS.toMillis(SURVEY_GRACE_PERIOD_DAYS.toLong())
    )
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(AFTERNOON_UTC_TIMESTAMP_MILLIS)
    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isTrue()
  }

  @Test
  fun testGating_evening_stillWithinGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EVENING_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_evening_stillWithinGracePeriod_minimumAggregateTimeMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EVENING_UTC_TIMESTAMP_MILLIS)
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_evening_isPastGracePeriod_minimumAggregateTimeNotMet_returnsFalse() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EVENING_UTC_TIMESTAMP_MILLIS)
    // not setting the surveyLastShownTimestamp because the default is set to the beginning of epoch
    // which will always be more than the grace period days in the past.
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  @Test
  fun testGating_evening_isPastGracePeriod_minimumAggregateTimeMet_returnsTrue() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    testCoroutineDispatchers.advanceTimeBy(
      TimeUnit.DAYS.toMillis(SURVEY_GRACE_PERIOD_DAYS.toLong())
    )
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EVENING_UTC_TIMESTAMP_MILLIS)
    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_0, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isTrue()
  }

  @Test
  fun testGating_criteriaMetOnOneProfile_doesNotTriggerSurveyOnOtherProfiles() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    oppiaClock.setCurrentTimeMs(EVENING_UTC_TIMESTAMP_MILLIS)

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_0)
    )
    startAndEndExplorationSession(SESSION_LENGTH_LONG)

    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.loginToProfile(PROFILE_ID_1)
    )
    monitorFactory.ensureDataProviderExecutes(
      profileManagementController.updateSurveyLastShownTimestamp(PROFILE_ID_1)
    )
    startAndEndExplorationSession(SESSION_LENGTH_SHORT)

    val gatingProvider = surveyGatingController.maybeShowSurvey(PROFILE_ID_1, TEST_TOPIC_ID_0)

    val result = monitorFactory.waitForNextSuccessfulResult(gatingProvider)

    assertThat(result).isFalse()
  }

  private fun startAndEndExplorationSession(
    sessionLengthMs: Long,
  ) {
    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(sessionLengthMs)
    explorationActiveTimeController.setExplorationSessionStopped(PROFILE_ID_0, TEST_TOPIC_ID_0)
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
/*

    @Provides
    @Singleton
    @EnableLearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      // Snapshot the value so that it doesn't change between injection and use.
      val enableFeature = enableLearnerStudyAnalytics
      return object : PlatformParameterValue<Boolean> {
        override val value: Boolean = enableFeature
      }
    }
*/

    /*  @Provides
      @NpsSurveyGracePeriodInDays
      fun provideNpsSurveyGracePeriodInDays(): PlatformParameterValue<Int> {
        return PlatformParameterValue.createDefaultParameter(
          NPS_SURVEY_GRACE_PERIOD_IN_DAYS_DEFAULT_VALUE
        )
      }

      @Provides
      @NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
      fun provideNpsSurveyMinimumAggregateLearningTimeInATopicInMinutes():
        PlatformParameterValue<Int> {
        return PlatformParameterValue.createDefaultParameter(
          NPS_SURVEY_MINIMUM_AGGREGATE_LEARNING_TIME_IN_A_TOPIC_IN_MINUTES_DEFAULT_VAL
        )
      }*/
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
      TestLoggingIdentifierModule::class, TestPlatformParameterModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(surveyGatingControllerTest: SurveyGatingControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerSurveyGatingControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(surveyGatingControllerTest: SurveyGatingControllerTest) {
      component.inject(surveyGatingControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  private companion object {
    // Date & time: Wed Apr 24 2019 08:22:03 GMT.
    private const val EARLY_MORNING_UTC_TIMESTAMP_MILLIS = 1556094123000

    // Date & time: Wed Apr 24 2019 10:30:12 GMT.
    private const val MID_MORNING_UTC_TIMESTAMP_MILLIS = 1556101812000

    // Date & time: Tue Apr 23 2019 14:22:00 GMT.
    private const val AFTERNOON_UTC_TIMESTAMP_MILLIS = 1556029320000

    // Date & time: Tue Apr 23 2019 21:26:12 GMT.
    private const val EVENING_UTC_TIMESTAMP_MILLIS = 1556054772000

    // Date & time: Tue Apr 23 2019 23:22:00 GMT.
    private const val LATE_NIGHT_UTC_TIMESTAMP_MILLIS = 1556061720000

    private val PROFILE_ID_0 = ProfileId.newBuilder().setInternalId(0).build()
    private val PROFILE_ID_1 = ProfileId.newBuilder().setInternalId(1).build()
  }
}
