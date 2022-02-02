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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog
import javax.inject.Inject
import javax.inject.Singleton

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

private const val TEST_ERROR_LOG_TAG = "test_error_log_tag"
private const val TEST_ERROR_LOG_MSG = "test_error_log_msg"
private const val TEST_ERROR_LOG_EXCEPTION = "test_error_log_exception"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class OppiaLoggerTest {
  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    ShadowLog.reset()
  }

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  private val TEST_VERBOSE_EXCEPTION = Throwable(TEST_VERBOSE_LOG_EXCEPTION)
  private val TEST_DEBUG_EXCEPTION = Throwable(TEST_DEBUG_LOG_EXCEPTION)
  private val TEST_INFO_EXCEPTION = Throwable(TEST_INFO_LOG_EXCEPTION)
  private val TEST_WARN_EXCEPTION = Throwable(TEST_WARN_LOG_EXCEPTION)
  private val TEST_ERROR_EXCEPTION = Throwable(TEST_ERROR_LOG_EXCEPTION)

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
    val eventContext = oppiaLogger.createOpenExplorationActivityContext(
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
    val eventContext = oppiaLogger.createOpenQuestionPlayerContext(
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
    val eventContext = oppiaLogger.createOpenStoryActivityContext(
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
    val eventContext = oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.CONCEPT_CARD_CONTEXT
    )
    assertThat(eventContext.conceptCardContext.skillId).matches(TEST_SKILL_ID)
  }

  @Test
  fun testController_createRevisionCardContext_returnsCorrectRevisionCardContext() {
    val eventContext =
      oppiaLogger.createOpenRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(
      EventLog.Context.ActivityContextCase.REVISION_CARD_CONTEXT
    )
    assertThat(eventContext.revisionCardContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.revisionCardContext.subTopicId).isEqualTo(TEST_SUB_TOPIC_ID)
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
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class
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
