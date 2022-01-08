package org.oppia.android.domain.oppialogger

import android.app.Application
import android.content.Context
import android.util.Log
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
import org.oppia.android.app.model.EventLog
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.platformparameter.SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.SplashScreenWelcomeMsg
import org.oppia.android.util.platformparameter.SyncUpWorkerTimePeriodHours
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_TIMESTAMP = 1556094120000
private const val TEST_TOPIC_ID = "test_topicId"
private const val TEST_STORY_ID = "test_storyId"
private const val TEST_EXPLORATION_ID = "test_explorationId"
private const val TEST_QUESTION_ID = "test_questionId"
private const val TEST_SKILL_ID = "test_skillId"
private const val TEST_SKILL_LIST_ID = "test_skillListId"
private const val TEST_SUB_TOPIC_ID = 1
private const val TEST_LEARNER_ID = "test_learnerId"
private const val TEST_DEVICE_ID = "test_deviceId"
private const val TEST_SESSION_ID = "test_sessionId"
private const val TEST_EXPLORATION_VERSION = "test_exploration_version"
private const val TEST_STATE_NAME = "test_state_name"
private const val TEST_HINT_INDEX = "test_hint_index"
private const val TEST_IS_ANSWER_CORRECT = true
private const val TEST_CONTENT_ID = "test_contentId"

private const val TEST_VERBOSE_LOG_TAG = "test_verbose_log_tag"
private const val TEST_VERBOSE_LOG_MSG = "test_verbose_log_msg"
private const val TEST_VERBOSE_LOG_EXCEPTION = "test_verbose_log_exception"

private const val TEST_DEBUG_LOG_TAG = "test_debug_log_tag"
private const val TEST_DEBUG_LOG_MSG = "test_debug_log_msg"
private const val TEST_DEBUG_LOG_EXCEPTION = "test_debug_log_exception"

private const val TEST_INFO_LOG_TAG = "test_info_log_tag"
private const val TEST_INFO_LOG_MSG = "test_info_log_msg"
private const val TEST_INFO_LOG_EXCEPTION = "test_info_log_exception"

private const val TEST_WARN_LOG_TAG = "test_warn_log_tag"
private const val TEST_WARN_LOG_MSG = "test_warn_log_msg"
private const val TEST_WARN_LOG_EXCEPTION = "test_warn_log_exception"

private const val TEST_ERROR_LOG_TAG = "test_error_log_tag"
private const val TEST_ERROR_LOG_MSG = "test_error_log_msg"
private const val TEST_ERROR_LOG_EXCEPTION = "test_error_log_exception"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class OppiaLoggerTest {

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  private val TEST_VERBOSE_EXCEPTION = Throwable(TEST_VERBOSE_LOG_EXCEPTION)
  private val TEST_DEBUG_EXCEPTION = Throwable(TEST_DEBUG_LOG_EXCEPTION)
  private val TEST_INFO_EXCEPTION = Throwable(TEST_INFO_LOG_EXCEPTION)
  private val TEST_WARN_EXCEPTION = Throwable(TEST_WARN_LOG_EXCEPTION)
  private val TEST_ERROR_EXCEPTION = Throwable(TEST_ERROR_LOG_EXCEPTION)

  private val GENERIC_DATA = EventLog.GenericData.newBuilder()
    .setDeviceId(TEST_DEVICE_ID)
    .setLearnerId(TEST_LEARNER_ID)
    .build()

  private val EXPLORATION_DATA = EventLog.ExplorationData.newBuilder()
    .setSessionId(TEST_SESSION_ID)
    .setExplorationId(TEST_EXPLORATION_ID)
    .setExplorationVersion(TEST_EXPLORATION_VERSION)
    .setStateName(TEST_STATE_NAME)
    .build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    ShadowLog.reset()
  }

  @Test
  fun testConsoleLogger_logVerboseMessage_checkLoggedMessageIsCorrect() {
    oppiaLogger.v(TEST_VERBOSE_LOG_TAG, TEST_VERBOSE_LOG_MSG)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_VERBOSE_LOG_TAG)
    assertThat(log.msg).isEqualTo(TEST_VERBOSE_LOG_MSG)
    assertThat(log.type).isEqualTo(Log.VERBOSE)
  }

  @Test
  fun testConsoleLogger_logVerboseMessageWithException_checkLoggedMessageIsCorrect() {
    oppiaLogger.v(TEST_VERBOSE_LOG_TAG, TEST_VERBOSE_LOG_MSG, TEST_VERBOSE_EXCEPTION)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_VERBOSE_LOG_TAG)
    assertThat(log.msg).contains(TEST_VERBOSE_LOG_MSG)
    assertThat(log.msg).contains(Log.getStackTraceString(TEST_VERBOSE_EXCEPTION))
    assertThat(log.type).isEqualTo(Log.VERBOSE)
  }

  @Test
  fun testConsoleLogger_logDebugMessage_checkLoggedMessageIsCorrect() {
    oppiaLogger.d(TEST_DEBUG_LOG_TAG, TEST_DEBUG_LOG_MSG)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_DEBUG_LOG_TAG)
    assertThat(log.msg).isEqualTo(TEST_DEBUG_LOG_MSG)
    assertThat(log.type).isEqualTo(Log.DEBUG)
  }

  @Test
  fun testConsoleLogger_logDebugMessageWithException_checkLoggedMessageIsCorrect() {
    oppiaLogger.d(TEST_DEBUG_LOG_TAG, TEST_DEBUG_LOG_MSG, TEST_DEBUG_EXCEPTION)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_DEBUG_LOG_TAG)
    assertThat(log.msg).contains(TEST_DEBUG_LOG_MSG)
    assertThat(log.msg).contains(Log.getStackTraceString(TEST_DEBUG_EXCEPTION))
    assertThat(log.type).isEqualTo(Log.DEBUG)
  }

  @Test
  fun testConsoleLogger_logInfoMessage_checkLoggedMessageIsCorrect() {
    oppiaLogger.i(TEST_INFO_LOG_TAG, TEST_INFO_LOG_MSG)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_INFO_LOG_TAG)
    assertThat(log.msg).isEqualTo(TEST_INFO_LOG_MSG)
    assertThat(log.type).isEqualTo(Log.INFO)
  }

  @Test
  fun testConsoleLogger_logInfoMessageWithException_checkLoggedMessageIsCorrect() {
    oppiaLogger.i(TEST_INFO_LOG_TAG, TEST_INFO_LOG_MSG, TEST_INFO_EXCEPTION)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_INFO_LOG_TAG)
    assertThat(log.msg).contains(TEST_INFO_LOG_MSG)
    assertThat(log.msg).contains(Log.getStackTraceString(TEST_INFO_EXCEPTION))
    assertThat(log.type).isEqualTo(Log.INFO)
  }

  @Test
  fun testConsoleLogger_logWarnMessage_checkLoggedMessageIsCorrect() {
    oppiaLogger.w(TEST_WARN_LOG_TAG, TEST_WARN_LOG_MSG)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_WARN_LOG_TAG)
    assertThat(log.msg).isEqualTo(TEST_WARN_LOG_MSG)
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testConsoleLogger_logWarnMessageWithException_checkLoggedMessageIsCorrect() {
    oppiaLogger.w(TEST_WARN_LOG_TAG, TEST_WARN_LOG_MSG, TEST_WARN_EXCEPTION)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_WARN_LOG_TAG)
    assertThat(log.msg).contains(TEST_WARN_LOG_MSG)
    assertThat(log.msg).contains(Log.getStackTraceString(TEST_WARN_EXCEPTION))
    assertThat(log.type).isEqualTo(Log.WARN)
  }

  @Test
  fun testConsoleLogger_logErrorMessage_checkLoggedMessageIsCorrect() {
    oppiaLogger.e(TEST_ERROR_LOG_TAG, TEST_ERROR_LOG_MSG)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_ERROR_LOG_TAG)
    assertThat(log.msg).isEqualTo(TEST_ERROR_LOG_MSG)
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  fun testConsoleLogger_logErrorMessageWithException_checkLoggedMessageIsCorrect() {
    oppiaLogger.e(TEST_ERROR_LOG_TAG, TEST_ERROR_LOG_MSG, TEST_ERROR_EXCEPTION)
    val log = ShadowLog.getLogs().last()
    assertThat(log.tag).isEqualTo(TEST_ERROR_LOG_TAG)
    assertThat(log.msg).contains(TEST_ERROR_LOG_MSG)
    assertThat(log.msg).contains(Log.getStackTraceString(TEST_ERROR_EXCEPTION))
    assertThat(log.type).isEqualTo(Log.ERROR)
  }

  @Test
  fun testController_createExplorationContext_returnsCorrectExplorationContext() {
    val eventContext = oppiaLogger.createExplorationContext(
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
    )
    assertThat(eventContext.explorationContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.explorationContext.storyId).matches(TEST_STORY_ID)
    assertThat(eventContext.explorationContext.explorationId).matches(TEST_EXPLORATION_ID)
  }

  @Test
  fun testController_createQuestionContext_returnsCorrectQuestionContext() {
    val eventContext = oppiaLogger.createQuestionContext(
      TEST_QUESTION_ID,
      listOf(TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID)
    )

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
    )
    assertThat(eventContext.questionContext.questionId).matches(TEST_QUESTION_ID)
    assertThat(eventContext.questionContext.skillIdList)
      .containsAtLeastElementsIn(arrayOf(TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID))
  }

  @Test
  fun testController_createStoryContext_returnsCorrectStoryContext() {
    val eventContext = oppiaLogger.createStoryContext(
      TEST_TOPIC_ID,
      TEST_STORY_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.STORY_CONTEXT
    )
    assertThat(eventContext.storyContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.storyContext.storyId).matches(TEST_STORY_ID)
  }

  @Test
  fun testController_createTopicContext_returnsCorrectTopicContext() {
    val eventContext = oppiaLogger.createTopicContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.TOPIC_CONTEXT
    )
    assertThat(eventContext.topicContext.topicId).matches(TEST_TOPIC_ID)
  }

  @Test
  fun testController_createConceptCardContext_returnsCorrectConceptCardContext() {
    val eventContext = oppiaLogger.createConceptCardContext(TEST_SKILL_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.CONCEPT_CARD_CONTEXT
    )
    assertThat(eventContext.conceptCardContext.skillId).matches(TEST_SKILL_ID)
  }

  @Test
  fun testController_createRevisionCardContext_returnsCorrectRevisionCardContext() {
    val eventContext =
      oppiaLogger.createRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.REVISION_CARD_CONTEXT
    )
    assertThat(eventContext.revisionCardContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.revisionCardContext.subTopicId).isEqualTo(TEST_SUB_TOPIC_ID)
  }

  @Test
  fun testController_createGenericData_returnsGenericDataWithCorrectValues() {
    val genericData = oppiaLogger.createGenericData(TEST_DEVICE_ID, TEST_LEARNER_ID)

    assertThat(genericData).isInstanceOf(EventLog.GenericData::class.java)
    assertThat(genericData.deviceId).isEqualTo(TEST_DEVICE_ID)
    assertThat(genericData.learnerId).isEqualTo(TEST_LEARNER_ID)
  }

  @Test
  fun testController_createExplorationData_returnsExplorationDataWithCorrectValues() {
    val explorationData = oppiaLogger.createExplorationData(
      TEST_SESSION_ID,
      TEST_EXPLORATION_ID,
      TEST_EXPLORATION_VERSION,
      TEST_STATE_NAME
    )

    assertThat(explorationData).isInstanceOf(EventLog.ExplorationData::class.java)
    assertThat(explorationData.sessionId).isEqualTo(TEST_SESSION_ID)
    assertThat(explorationData.explorationId).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(explorationData.explorationVersion).isEqualTo(TEST_EXPLORATION_VERSION)
    assertThat(explorationData.stateName).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testController_createStartCardContext_returnsCorrectStartCardContext() {
    val eventContext =
      oppiaLogger.createStartCardContext(TEST_SKILL_ID, GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.START_CARD_CONTEXT
    )
    assertThat(eventContext.startCardContext.skillId).matches(TEST_SKILL_ID)
    assertThat(eventContext.startCardContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.startCardContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createEndCardContext_returnsCorrectEndCardContext() {
    val eventContext =
      oppiaLogger.createEndCardContext(TEST_SKILL_ID, GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.END_CARD_CONTEXT
    )
    assertThat(eventContext.endCardContext.skillId).matches(TEST_SKILL_ID)
    assertThat(eventContext.endCardContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.endCardContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createHintOfferedContext_returnsCorrectHintOfferedContext() {
    val eventContext =
      oppiaLogger.createHintOfferedContext(TEST_HINT_INDEX, GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.HINT_OFFERED_CONTEXT
    )
    assertThat(eventContext.hintOfferedContext.hintIndex).matches(TEST_HINT_INDEX)
    assertThat(eventContext.hintOfferedContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.hintOfferedContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createAccessHintContext_returnsCorrectAccessHintContext() {
    val eventContext =
      oppiaLogger.createAccessHintContext(TEST_HINT_INDEX, GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.ACCESS_HINT_CONTEXT
    )
    assertThat(eventContext.accessHintContext.hintIndex).matches(TEST_HINT_INDEX)
    assertThat(eventContext.accessHintContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.accessHintContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createSolutionOfferedContext_returnsCorrectSolutionOfferedContext() {
    val eventContext =
      oppiaLogger.createSolutionOfferedContext(GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.SOLUTION_OFFERED_CONTEXT
    )
    assertThat(eventContext.solutionOfferedContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.solutionOfferedContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createAccessSolutionContext_returnsCorrectAccessSolutionContext() {
    val eventContext =
      oppiaLogger.createAccessSolutionContext(GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.ACCESS_SOLUTION_CONTEXT
    )
    assertThat(eventContext.accessSolutionContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.accessSolutionContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createSubmitAnswerContext_returnsCorrectSubmitAnswerContext() {
    val eventContext =
      oppiaLogger.createSubmitAnswerContext(TEST_IS_ANSWER_CORRECT, GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.SUBMIT_ANSWER_CONTEXT
    )
    assertThat(eventContext.submitAnswerContext.isAnswerCorrect).isEqualTo(TEST_IS_ANSWER_CORRECT)
    assertThat(eventContext.submitAnswerContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.submitAnswerContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createPlayVoiceOverContext_returnsCorrectPlayVoiceOverContext() {
    val eventContext =
      oppiaLogger.createPlayVoiceOverContext(TEST_CONTENT_ID, GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.PLAY_VOICE_OVER_CONTEXT
    )
    assertThat(eventContext.playVoiceOverContext.contentId).isEqualTo(TEST_CONTENT_ID)
    assertThat(eventContext.playVoiceOverContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.playVoiceOverContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createAppInBackgroundContext_returnsCorrectAppInBackgroundContext() {
    val eventContext =
      oppiaLogger.createAppInBackgroundContext(GENERIC_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.APP_IN_BACKGROUND_CONTEXT
    )
    assertThat(eventContext.appInBackgroundContext.genericData).isEqualTo(GENERIC_DATA)
  }

  @Test
  fun testController_createAppInForegroundContext_returnsCorrectAppInForegroundContext() {
    val eventContext =
      oppiaLogger.createAppInForegroundContext(GENERIC_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_CONTEXT
    )
    assertThat(eventContext.appInForegroundContext.genericData).isEqualTo(GENERIC_DATA)
  }

  @Test
  fun testController_createExitExplorationContext_returnsCorrectExitExplorationContext() {
    val eventContext =
      oppiaLogger.createExitExplorationContext(GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.EXIT_EXPLORATION_CONTEXT
    )
    assertThat(eventContext.exitExplorationContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.exitExplorationContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createFinishExplorationContext_returnsCorrectFinishExplorationContext() {
    val eventContext =
      oppiaLogger.createFinishExplorationContext(GENERIC_DATA, EXPLORATION_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.FINISH_EXPLORATION_CONTEXT
    )
    assertThat(eventContext.finishExplorationContext.genericData).isEqualTo(GENERIC_DATA)
    assertThat(eventContext.finishExplorationContext.explorationData).isEqualTo(EXPLORATION_DATA)
  }

  @Test
  fun testController_createResumeExplorationContext_returnsCorrectResumeExplorationContext() {
    val eventContext =
      oppiaLogger.createResumeExplorationContext(GENERIC_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.RESUME_EXPLORATION_CONTEXT
    )
    assertThat(eventContext.resumeExplorationContext.genericData).isEqualTo(GENERIC_DATA)
  }

  @Test
  fun testController_createStartOverExplorationContext_returnsCorrectStartOverExplorationContext() {
    val eventContext =
      oppiaLogger.createStartOverExplorationContext(GENERIC_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.START_OVER_EXPLORATION_CONTEXT
    )
    assertThat(eventContext.startOverExplorationContext.genericData).isEqualTo(GENERIC_DATA)
  }

  @Test
  fun testController_createDeleteProfileContext_returnsCorrectDeleteProfileContext() {
    val eventContext =
      oppiaLogger.createDeleteProfileContext(GENERIC_DATA)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.DELETE_PROFILE_CONTEXT
    )
    assertThat(eventContext.deleteProfileContext.genericData).isEqualTo(GENERIC_DATA)
  }

  @Test
  fun testController_featureDisabled_logLearnerAnalyticsEvent_verifyEventNotLogged() {
    TestPlatformParameterModule.forceLearnerAnalyticsStudy = false
    oppiaLogger.logLearnerAnalyticsTransitionEvent(
      TEST_TIMESTAMP,
      EventLog.EventAction.EVENT_ACTION_UNSPECIFIED,
      null
    )

    assertThat(fakeEventLogger.noEventsPresent()).isTrue()
  }

  @Test
  fun testController_featureEnabled_logLearnerAnalyticsEvent_verifyEventLogged() {
    TestPlatformParameterModule.forceLearnerAnalyticsStudy = true
    setUpTestApplicationComponent()

    oppiaLogger.logLearnerAnalyticsTransitionEvent(
      TEST_TIMESTAMP,
      EventLog.EventAction.EVENT_ACTION_UNSPECIFIED,
      null
    )

    assertThat(fakeEventLogger.noEventsPresent()).isFalse()
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaLoggerTest_TestApplicationComponent.builder()
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
  }

  @Module
  class TestPlatformParameterModule {

    companion object {
      var forceLearnerAnalyticsStudy: Boolean = false
    }

    @Provides
    @SplashScreenWelcomeMsg
    fun provideSplashScreenWelcomeMsgParam(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE)
    }

    @Provides
    @SyncUpWorkerTimePeriodHours
    fun provideSyncUpWorkerTimePeriod(): PlatformParameterValue<Int> {
      return PlatformParameterValue.createDefaultParameter(
        SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE
      )
    }

    @Provides
    @EnableLanguageSelectionUi
    fun provideEnableLanguageSelectionUi(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(
        ENABLE_LANGUAGE_SELECTION_UI_DEFAULT_VALUE
      )
    }

    @Provides
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      return PlatformParameterValue.createDefaultParameter(forceLearnerAnalyticsStudy)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, TestLogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(oppiaLoggerTest: OppiaLoggerTest)
  }
}
