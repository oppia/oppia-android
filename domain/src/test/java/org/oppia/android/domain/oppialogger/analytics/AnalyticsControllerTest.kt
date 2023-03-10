package org.oppia.android.domain.oppialogger.analytics

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
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.SWAHILI
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.FakeSyncStatusManager
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADED
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.DATA_UPLOADING
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NO_CONNECTIVITY
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
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

/** Tests for [AnalyticsController]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AnalyticsControllerTest.TestApplication::class)
class AnalyticsControllerTest {
  @Inject lateinit var analyticsController: AnalyticsController
  @Inject lateinit var oppiaLogger: OppiaLogger
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var fakeSyncStatusManager: FakeSyncStatusManager
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var translationController: TranslationController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testController_logImportantEvent_withQuestionContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logImportantEvent_withExplorationContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenExplorationActivityContext(
        TEST_TOPIC_ID,
        TEST_STORY_ID,
        TEST_EXPLORATION_ID
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenExplorationActivityContext()
  }

  @Test
  fun testController_logImportantEvent_withOpenInfoTabContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenInfoTabContext()
  }

  @Test
  fun testController_logImportantEvent_withOpenPracticeTabContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenPracticeTabContext()
  }

  @Test
  fun testController_logImportantEvent_withOpenLessonsTabContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenLessonsTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenLessonsTabContext()
  }

  @Test
  fun testController_logImportantEvent_withOpenRevisionTabContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenRevisionTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenRevisionTabContext()
  }

  @Test
  fun testController_logImportantEvent_withStoryContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenStoryActivityContext(TEST_TOPIC_ID, TEST_STORY_ID),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenStoryActivityContext()
  }

  @Test
  fun testController_logImportantEvent_withRevisionContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenRevisionCardContext()
  }

  @Test
  fun testController_logImportantEvent_withConceptCardContext_checkLogsEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenConceptCardContext()
  }

  @Test
  fun testLogImportantEvent_forOpenHomeEvent_logsEssentialEventWithCurrentTime() {
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()

    analyticsController.logImportantEvent(openHomeEventContext, profileId = null, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
  }

  @Test
  fun testController_logImportantEvent_nullProfileId_hasDefaultLanguageSettings() {
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()
    // Create a new profile & set its language settings, but don't use it when logging an event.
    val profileId = addNewProfileAndLogIn()
    ensureAppLanguageIsUpdatedTo(profileId, ENGLISH)
    ensureWrittenTranslationsLanguageIsUpdatedTo(profileId, SWAHILI)
    ensureAudioTranslationsLanguageIsUpdatedTo(profileId, HINGLISH)

    analyticsController.logImportantEvent(openHomeEventContext, profileId = null, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    // There are no language settings for an event logged that doesn't correspond to a profile.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAppLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasWrittenTranslationLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasAudioTranslationLanguageSelectionThat().isEqualToDefaultInstance()
  }

  @Test
  fun testController_logImportantEvent_profileWithNoLangSettings_hasDefaultLanguageSettings() {
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()
    // Create a profile without any language settings.
    val profileId = addNewProfileAndLogIn()

    analyticsController.logImportantEvent(openHomeEventContext, profileId, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    // If a profile corresponding to an event has no language settings, then the event shouldn't,
    // either.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAppLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasWrittenTranslationLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasAudioTranslationLanguageSelectionThat().isEqualToDefaultInstance()
  }

  @Test
  fun testController_logImportantEvent_profileWithLangSettings_hasCorrectLanguageSettings() {
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()
    val profileId = addNewProfileAndLogIn()
    ensureAppLanguageIsUpdatedTo(profileId, ENGLISH)
    ensureWrittenTranslationsLanguageIsUpdatedTo(profileId, SWAHILI)
    ensureAudioTranslationsLanguageIsUpdatedTo(profileId, HINGLISH)

    analyticsController.logImportantEvent(openHomeEventContext, profileId, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    // A profile's language settings should reflect in corresponding logged events.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAppLanguageSelectionThat().isSelectedLanguageThat().isEqualTo(ENGLISH)
    assertThat(eventLog)
      .hasWrittenTranslationLanguageSelectionThat()
      .isSelectedLanguageThat()
      .isEqualTo(SWAHILI)
    assertThat(eventLog)
      .hasAudioTranslationLanguageSelectionThat()
      .isSelectedLanguageThat()
      .isEqualTo(HINGLISH)
  }

  @Test
  fun testController_logLowPriorityEvent_withQuestionContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withExplorationContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenExplorationActivityContext(
        TEST_TOPIC_ID,
        TEST_STORY_ID,
        TEST_EXPLORATION_ID
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenExplorationActivityContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withOpenInfoTabContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenInfoTabContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withOpenPracticeTabContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenPracticeTabContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withOpenLessonsTabContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenLessonsTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenLessonsTabContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withOpenRevisionTabContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenRevisionTabContext(TEST_TOPIC_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenRevisionTabContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withStoryContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenStoryActivityContext(TEST_TOPIC_ID, TEST_STORY_ID),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenStoryActivityContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withRevisionContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenRevisionCardContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withConceptCardContext_checkLogsEvent() {
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenConceptCardContext()
  }

  // TODO(#3621): Addition of tests tracking behaviour of the controller after uploading of logs to
  //  the remote service.

  @Test
  fun testController_logImportantEvent_withNoNetwork_checkLogsEventToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLogsProvider = analyticsController.getEventLogStore()

    val eventLog = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider).getEventLog(0)
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withNoNetwork_checkLogsEventToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLogsProvider = analyticsController.getEventLogStore()

    val eventLog = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider).getEventLog(0)
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logLowPriorityEvent_nullProfileId_hasDefaultLanguageSettings() {
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()
    // Create a new profile & set its language settings, but don't use it when logging an event.
    val profileId = addNewProfileAndLogIn()
    ensureAppLanguageIsUpdatedTo(profileId, ENGLISH)
    ensureWrittenTranslationsLanguageIsUpdatedTo(profileId, SWAHILI)
    ensureAudioTranslationsLanguageIsUpdatedTo(profileId, HINGLISH)

    analyticsController.logLowPriorityEvent(openHomeEventContext, profileId = null, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    // There are no language settings for an event logged that doesn't correspond to a profile.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAppLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasWrittenTranslationLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasAudioTranslationLanguageSelectionThat().isEqualToDefaultInstance()
  }

  @Test
  fun testController_logLowPriorityEvent_profileWithNoLangSettings_hasDefaultLanguageSettings() {
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()
    // Create a profile without any language settings.
    val profileId = addNewProfileAndLogIn()

    analyticsController.logLowPriorityEvent(openHomeEventContext, profileId, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    // If a profile corresponding to an event has no language settings, then the event shouldn't,
    // either.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAppLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasWrittenTranslationLanguageSelectionThat().isEqualToDefaultInstance()
    assertThat(eventLog).hasAudioTranslationLanguageSelectionThat().isEqualToDefaultInstance()
  }

  @Test
  fun testController_logLowPriorityEvent_profileWithLangSettings_hasCorrectLanguageSettings() {
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()
    val profileId = addNewProfileAndLogIn()
    ensureAppLanguageIsUpdatedTo(profileId, ENGLISH)
    ensureWrittenTranslationsLanguageIsUpdatedTo(profileId, SWAHILI)
    ensureAudioTranslationsLanguageIsUpdatedTo(profileId, HINGLISH)

    analyticsController.logLowPriorityEvent(openHomeEventContext, profileId, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    // A profile's language settings should reflect in corresponding logged events.
    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).hasAppLanguageSelectionThat().isSelectedLanguageThat().isEqualTo(ENGLISH)
    assertThat(eventLog)
      .hasWrittenTranslationLanguageSelectionThat()
      .isSelectedLanguageThat()
      .isEqualTo(SWAHILI)
    assertThat(eventLog)
      .hasAudioTranslationLanguageSelectionThat()
      .isSelectedLanguageThat()
      .isEqualTo(HINGLISH)
  }

  @Test
  fun testController_logImportantEvent_withNoNetwork_exceedLimit_checkEventLogStoreSize() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultipleEvents()

    val eventLogsProvider = analyticsController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider)
    assertThat(eventLogs.eventLogList).hasSize(2)
  }

  @Test
  fun testController_logImportantEvent_logLowPriorityEvent_withNoNetwork_checkOrderinCache() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLogsProvider = analyticsController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider)
    val firstEventLog = eventLogs.getEventLog(0)
    val secondEventLog = eventLogs.getEventLog(1)

    assertThat(firstEventLog).isOptionalPriority()
    assertThat(secondEventLog).isEssentialPriority()
  }

  @Test
  fun testController_logImportantEvent_switchToNoNetwork_logLowPriorityEvent_checkManagement() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val logsProvider = analyticsController.getEventLogStore()

    val uploadedEventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    val cachedEventLog = monitorFactory.waitForNextSuccessfulResult(logsProvider).getEventLog(0)

    assertThat(uploadedEventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(uploadedEventLog).isEssentialPriority()
    assertThat(uploadedEventLog).hasOpenQuestionPlayerContext()

    assertThat(cachedEventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(cachedEventLog).isOptionalPriority()
    assertThat(cachedEventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logEvents_exceedLimit_withNoNetwork_checkCorrectEventIsEvicted() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultipleEvents()

    val logsProvider = analyticsController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(logsProvider)
    val firstEventLog = eventLogs.getEventLog(0)
    val secondEventLog = eventLogs.getEventLog(1)
    assertThat(eventLogs.eventLogList).hasSize(2)
    // In this case, 3 ESSENTIAL and 1 OPTIONAL event was logged. So while pruning, none of the
    // retained logs should have OPTIONAL priority.
    assertThat(firstEventLog).isEssentialPriority()
    assertThat(secondEventLog).isEssentialPriority()
    // If we analyse the implementation of logMultipleEvents(), we can see that record pruning will
    // begin from the logging of the third record. At first, the second event log will be removed as
    // it has OPTIONAL priority and the event logged at the third place will become the event record
    // at the second place in the store. When the forth event gets logged then the pruning will be
    // purely based on timestamp of the event as both event logs have ESSENTIAL priority. As the
    // third event's timestamp was lesser than that of the first event, it will be pruned from the
    // store and the forth event will become the second event in the store.
    assertThat(firstEventLog).hasTimestampThat().isEqualTo(1556094120000)
    assertThat(secondEventLog).hasTimestampThat().isEqualTo(1556094100000)
  }

  @Test
  fun testController_logEvent_withoutNetwork_verifySyncStatusIsUnchanged() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      1556094120000
    )
    testCoroutineDispatchers.runCurrent()
    assertThat(fakeSyncStatusManager.getSyncStatuses()).containsExactly(NO_CONNECTIVITY)
  }

  @Test
  fun testController_logEvent_verifySyncStatusChangesToRepresentLoggedEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      1556094120000
    )
    testCoroutineDispatchers.runCurrent()

    val syncStatuses = fakeSyncStatusManager.getSyncStatuses()
    assertThat(syncStatuses).containsExactly(DATA_UPLOADING, DATA_UPLOADED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun logMultipleEvents() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      1556094120000
    )
    testCoroutineDispatchers.runCurrent()

    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      1556094110000
    )
    testCoroutineDispatchers.runCurrent()

    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      1556093100000
    )
    testCoroutineDispatchers.runCurrent()

    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      ),
      profileId = null,
      1556094100000
    )
    testCoroutineDispatchers.runCurrent()
  }

  private fun addNewProfileAndLogIn(): ProfileId {
    val addProfileProvider = profileManagementController.addProfile(
      name = "Test Profile",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = 0,
      isAdmin = false
    )
    monitorFactory.ensureDataProviderExecutes(addProfileProvider)

    return ProfileId.newBuilder().apply { internalId = 0 }.build().also { expectedProfileId ->
      val logInProvider = profileManagementController.loginToProfile(expectedProfileId)
      monitorFactory.waitForNextSuccessfulResult(logInProvider) // Ensure that the login succeeds.
    }
  }

  private fun ensureAppLanguageIsUpdatedTo(profileId: ProfileId, language: OppiaLanguage) {
    val resultProvider =
      translationController.updateAppLanguage(
        profileId, AppLanguageSelection.newBuilder().apply { selectedLanguage = language }.build()
      )
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun ensureWrittenTranslationsLanguageIsUpdatedTo(
    profileId: ProfileId,
    language: OppiaLanguage
  ) {
    val resultProvider =
      translationController.updateWrittenTranslationContentLanguage(
        profileId,
        WrittenTranslationLanguageSelection.newBuilder().apply {
          selectedLanguage = language
        }.build()
      )
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun ensureAudioTranslationsLanguageIsUpdatedTo(
    profileId: ProfileId,
    language: OppiaLanguage
  ) {
    val resultProvider =
      translationController.updateAudioTranslationContentLanguage(
        profileId,
        AudioTranslationLanguageSelection.newBuilder().apply {
          selectedLanguage = language
        }.build()
      )
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
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
    @PerformanceMetricsLogStorageCacheSize
    fun provideMetricLogStorageCacheSize(): Int = 10
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class, SyncStatusTestModule::class, AssetModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(analyticsControllerTest: AnalyticsControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAnalyticsControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(analyticsControllerTest: AnalyticsControllerTest) {
      component.inject(analyticsControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
