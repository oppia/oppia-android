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
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_TIME
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.BEGIN_SURVEY
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.CLOSE_REVISION_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.COMPLETE_APP_ONBOARDING
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.CONSOLE_LOG
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_CONCEPT_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_EXPLORATION_ACTIVITY
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_HOME
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_INFO_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_LESSONS_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_PRACTICE_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_PROFILE_CHOOSER
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_QUESTION_PLAYER
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_REVISION_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_REVISION_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_STORY_ACTIVITY
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RETROFIT_CALL_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RETROFIT_CALL_FAILED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SHOW_SURVEY_POPUP
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OppiaLogger]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class OppiaLoggerTest {
  private companion object {
    private const val TEST_TIMESTAMP = 1234567898765
    private const val TEST_CLASSROOM_ID = "test_classroomId"
    private const val TEST_TOPIC_ID = "test_topicId"
    private const val TEST_STORY_ID = "test_storyId"
    private const val TEST_EXPLORATION_ID = "test_explorationId"
    private const val TEST_QUESTION_ID = "test_questionId"
    private const val TEST_SKILL_ID = "test_skillId"
    private const val TEST_SKILL_LIST_ID = "test_skillListId"
    private const val TEST_SUB_TOPIC_ID = 1

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

    private const val TEST_ERROR_LOG_LEVEL = "test_log_level"
    private const val TEST_ERROR_LOG_TAG = "test_error_log_tag"
    private const val TEST_ERROR_LOG_MSG = "test_error_log_msg"
    private const val TEST_ERROR_LOG_EXCEPTION = "test_error_log_exception"

    private const val TEST_URL = "test_url"
    private const val TEST_HEADERS = "test_headers"
    private const val TEST_BODY = "test_body"
    private const val TEST_RESPONSE_CODE = 200

    private const val TEST_INSTALLATION_ID = "test_installation_id"
    private const val TEST_APP_SESSION_ID = "test_app_session_id"
    private const val TEST_FOREGROUND_TIME = 5000L

    private val TEST_VERBOSE_EXCEPTION = Throwable(TEST_VERBOSE_LOG_EXCEPTION)
    private val TEST_DEBUG_EXCEPTION = Throwable(TEST_DEBUG_LOG_EXCEPTION)
    private val TEST_INFO_EXCEPTION = Throwable(TEST_INFO_LOG_EXCEPTION)
    private val TEST_WARN_EXCEPTION = Throwable(TEST_WARN_LOG_EXCEPTION)
    private val TEST_ERROR_EXCEPTION = Throwable(TEST_ERROR_LOG_EXCEPTION)
  }

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

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
  fun testController_createOpenExplorationActivityContext_returnsCorrectExplorationContext() {
    val eventContext = oppiaLogger.createOpenExplorationActivityContext(
      TEST_CLASSROOM_ID,
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_EXPLORATION_ACTIVITY)
    assertThat(eventContext.openExplorationActivity.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.openExplorationActivity.storyId).matches(TEST_STORY_ID)
    assertThat(eventContext.openExplorationActivity.explorationId).matches(TEST_EXPLORATION_ID)
  }

  @Test
  fun testController_createOpenHomeContext_returnsCorrectExplorationContext() {
    val eventContext = oppiaLogger.createOpenHomeContext()

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_HOME)
  }

  @Test
  fun testController_createOpenProfileChooserContext_returnsCorrectExplorationContext() {
    val eventContext = oppiaLogger.createOpenProfileChooserContext()

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_PROFILE_CHOOSER)
  }

  @Test
  fun testController_createQuestionContext_returnsCorrectQuestionContext() {
    val eventContext = oppiaLogger.createOpenQuestionPlayerContext(
      TEST_QUESTION_ID,
      listOf(TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID)
    )

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_QUESTION_PLAYER)
    assertThat(eventContext.openQuestionPlayer.questionId).matches(TEST_QUESTION_ID)
    assertThat(eventContext.openQuestionPlayer.skillIdList)
      .containsAtLeastElementsIn(arrayOf(TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID))
  }

  @Test
  fun testController_createStoryContext_returnsCorrectStoryContext() {
    val eventContext = oppiaLogger.createOpenStoryActivityContext(
      TEST_TOPIC_ID,
      TEST_STORY_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_STORY_ACTIVITY)
    assertThat(eventContext.openStoryActivity.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.openStoryActivity.storyId).matches(TEST_STORY_ID)
  }

  @Test
  fun testController_createOpenInfoTabContext_returnsCorrectTopicContext() {
    val eventContext = oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_INFO_TAB)
    assertThat(eventContext.openInfoTab.topicId).matches(TEST_TOPIC_ID)
  }

  @Test
  fun testController_createOpenLessonsTabContext_returnsCorrectTopicContext() {
    val eventContext = oppiaLogger.createOpenLessonsTabContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_LESSONS_TAB)
    assertThat(eventContext.openLessonsTab.topicId).matches(TEST_TOPIC_ID)
  }

  @Test
  fun testController_createOpenPracticeTabContext_returnsCorrectTopicContext() {
    val eventContext = oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_PRACTICE_TAB)
    assertThat(eventContext.openPracticeTab.topicId).matches(TEST_TOPIC_ID)
  }

  @Test
  fun testController_createOpenRevisionTabContext_returnsCorrectTopicContext() {
    val eventContext = oppiaLogger.createOpenRevisionTabContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_REVISION_TAB)
    assertThat(eventContext.openRevisionTab.topicId).matches(TEST_TOPIC_ID)
  }

  @Test
  fun testController_createOpenConceptCardContext_returnsCorrectConceptCardContext() {
    val eventContext = oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_CONCEPT_CARD)
    assertThat(eventContext.openConceptCard.skillId).matches(TEST_SKILL_ID)
  }

  @Test
  fun testController_createOpenRevisionCardContext_returnsCorrectRevisionCardContext() {
    val eventContext = oppiaLogger.createOpenRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(OPEN_REVISION_CARD)
    assertThat(eventContext.openRevisionCard.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.openRevisionCard.subTopicId).isEqualTo(TEST_SUB_TOPIC_ID)
  }

  @Test
  fun testController_createCloseRevisionCardContext_returnsCorrectRevisionCardContext() {
    val eventContext = oppiaLogger.createCloseRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(CLOSE_REVISION_CARD)
    assertThat(eventContext.closeRevisionCard.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.closeRevisionCard.subTopicId).isEqualTo(TEST_SUB_TOPIC_ID)
  }

  @Test
  fun testController_createShowSurveyPopupContext_returnsCorrectShowSurveyPopupContext() {
    val eventContext = oppiaLogger.createShowSurveyPopupContext(TEST_EXPLORATION_ID, TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(SHOW_SURVEY_POPUP)
    assertThat(eventContext.showSurveyPopup.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.showSurveyPopup.explorationId).isEqualTo(TEST_EXPLORATION_ID)
  }

  @Test
  fun testController_createBeginSurveyContext_returnsCorrectBeginSurveyContext() {
    val eventContext = oppiaLogger.createBeginSurveyContext(TEST_EXPLORATION_ID, TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(BEGIN_SURVEY)
    assertThat(eventContext.beginSurvey.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.beginSurvey.explorationId).isEqualTo(TEST_EXPLORATION_ID)
  }

  @Test
  fun testController_createAppOnBoardingContext_returnsCorrectAppOnBoardingContextContext() {
    val eventContext = oppiaLogger.createAppOnBoardingContext()

    assertThat(eventContext.activityContextCase).isEqualTo(COMPLETE_APP_ONBOARDING)
    assertThat(eventContext.completeAppOnboarding.completeAppOnboarding).isEqualTo(true)
  }

  @Test
  fun testController_createConsoleLogContext_returnsCorrectConsoleLogContext() {
    val eventContext = oppiaLogger.createConsoleLogContext(
      logLevel = TEST_ERROR_LOG_LEVEL,
      logTag = TEST_ERROR_LOG_TAG,
      errorLog = TEST_ERROR_LOG_MSG
    )

    assertThat(eventContext.activityContextCase).isEqualTo(CONSOLE_LOG)
    assertThat(eventContext.consoleLog.logLevel).matches(TEST_ERROR_LOG_LEVEL)
    assertThat(eventContext.consoleLog.logTag).matches(TEST_ERROR_LOG_TAG)
    assertThat(eventContext.consoleLog.fullErrorLog).matches(TEST_ERROR_LOG_MSG)
  }

  @Test
  fun testController_createRetrofitCallContext_returnsCorrectRetrofitCallContext() {
    val eventContext = oppiaLogger.createRetrofitCallContext(
      url = TEST_URL,
      headers = TEST_HEADERS,
      body = TEST_BODY,
      responseCode = TEST_RESPONSE_CODE,
    )

    assertThat(eventContext.activityContextCase).isEqualTo(RETROFIT_CALL_CONTEXT)
    assertThat(eventContext.retrofitCallContext.requestUrl).matches(TEST_URL)
    assertThat(eventContext.retrofitCallContext.headers).matches(TEST_HEADERS)
    assertThat(eventContext.retrofitCallContext.body).matches(TEST_BODY)
    assertThat(eventContext.retrofitCallContext.responseStatusCode).isEqualTo(TEST_RESPONSE_CODE)
  }

  @Test
  fun testController_createRetrofitCallFailedContext_returnsCorrectRetrofitCallFailedContext() {
    val eventContext = oppiaLogger.createRetrofitCallFailedContext(
      url = TEST_URL,
      headers = TEST_HEADERS,
      body = TEST_BODY,
      responseCode = TEST_RESPONSE_CODE,
      errorMessage = TEST_ERROR_LOG_MSG,
    )

    assertThat(eventContext.activityContextCase).isEqualTo(RETROFIT_CALL_FAILED_CONTEXT)
    assertThat(eventContext.retrofitCallFailedContext.requestUrl).matches(TEST_URL)
    assertThat(eventContext.retrofitCallFailedContext.headers).matches(TEST_HEADERS)
    assertThat(eventContext.retrofitCallFailedContext.body).matches(TEST_BODY)
    assertThat(eventContext.retrofitCallFailedContext.responseStatusCode)
      .isEqualTo(TEST_RESPONSE_CODE)
    assertThat(eventContext.retrofitCallFailedContext.errorMessage).matches(TEST_ERROR_LOG_MSG)
  }

  @Test
  fun testController_createAppInForegroundTimeContext_returnsCorrectAppInForegroundTimeContext() {
    val eventContext = oppiaLogger.createAppInForegroundTimeContext(
      installationId = TEST_INSTALLATION_ID,
      appSessionId = TEST_APP_SESSION_ID,
      foregroundTime = TEST_FOREGROUND_TIME
    )

    assertThat(eventContext.activityContextCase).isEqualTo(APP_IN_FOREGROUND_TIME)
    assertThat(eventContext.appInForegroundTime.installationId).matches(TEST_INSTALLATION_ID)
    assertThat(eventContext.appInForegroundTime.appSessionId).matches(TEST_APP_SESSION_ID)
    assertThat(eventContext.appInForegroundTime.foregroundTime)
      .isEqualTo(TEST_FOREGROUND_TIME.toFloat())
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, TestLogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      PlatformParameterSingletonModule::class, LoggingIdentifierModule::class,
      SyncStatusModule::class, ApplicationLifecycleModule::class
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
