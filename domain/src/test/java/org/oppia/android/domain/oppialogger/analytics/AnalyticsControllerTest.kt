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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.SWAHILI
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.ExceptionLogStorageCacheSize
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.logging.TestSyncStatusManager
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.AsyncResult
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
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.INITIAL_UNKNOWN
import org.oppia.android.util.logging.SyncStatusManager.SyncStatus.NO_CONNECTIVITY
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.system.OppiaClock
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val TEST_TIMESTAMP = 1556094120000
private const val TEST_CLASSROOM_ID = "test_classroomId"
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
  @Inject lateinit var analyticsControllerProvider: Provider<AnalyticsController>
  @Inject lateinit var oppiaLogger: OppiaLogger
  @Inject lateinit var networkConnectionUtil: NetworkConnectionDebugUtil
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var testSyncStatusManager: TestSyncStatusManager
  @Inject lateinit var profileManagementControllerProvider: Provider<ProfileManagementController>
  @Inject lateinit var translationController: TranslationController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var oppiaClock: OppiaClock
  @Inject lateinit var persistentCacheStoryFactory: PersistentCacheStore.Factory
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher

  // See testController_cachedEventsFromLastAppInstance_logNewEvent_thenForceSync_everythingUploads
  // for an explanation of why these are provided via indirect injection.
  private val profileManagementController by lazy { profileManagementControllerProvider.get() }
  private val analyticsController by lazy { analyticsControllerProvider.get() }

  @Test
  fun testController_logImportantEvent_withQuestionContext_checkLogsEvent() {
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenExplorationActivityContext(
        TEST_CLASSROOM_ID,
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
    val openHomeEventContext = oppiaLogger.createOpenHomeContext()

    analyticsController.logImportantEvent(openHomeEventContext, profileId = null, TEST_TIMESTAMP)
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
  }

  @Test
  fun testController_logImportantEvent_nullProfileId_hasDefaultLanguageSettings() {
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
  fun testController_logImportantEvent_noProfile_hasNoProfileId() {
    setUpTestApplicationComponent()
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isFalse()
  }

  @Test
  fun testController_logImportantEvent_withProfile_includesProfileId() {
    setUpTestApplicationComponent()
    val profileId = addNewProfileAndLogIn()
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID), profileId, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isTrue()
    assertThat(eventLog).hasProfileIdThat().isEqualTo(profileId)
  }

  @Test
  fun testController_logLowPriorityEvent_withQuestionContext_checkLogsEvent() {
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenExplorationActivityContext(
        TEST_CLASSROOM_ID,
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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

    val eventLog =
      monitorFactory.waitForNextSuccessfulResult(eventLogsProvider).getEventLogsToUpload(0)
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isEssentialPriority()
    assertThat(eventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logLowPriorityEvent_withNoNetwork_checkLogsEventToStore() {
    setUpTestApplicationComponent()
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

    val eventLog =
      monitorFactory.waitForNextSuccessfulResult(eventLogsProvider).getEventLogsToUpload(0)
    assertThat(eventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog).isOptionalPriority()
    assertThat(eventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logLowPriorityEvent_nullProfileId_hasDefaultLanguageSettings() {
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
    setUpTestApplicationComponent()
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
  fun testController_logPriorityEvent_noProfile_hasNoProfileId() {
    setUpTestApplicationComponent()
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID), profileId = null, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isFalse()
  }

  @Test
  fun testController_logPriorityEvent_withProfile_includesProfileId() {
    setUpTestApplicationComponent()
    val profileId = addNewProfileAndLogIn()
    analyticsController.logLowPriorityEvent(
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID), profileId, TEST_TIMESTAMP
    )
    testCoroutineDispatchers.runCurrent()

    val eventLog = fakeAnalyticsEventLogger.getMostRecentEvent()
    assertThat(eventLog.hasProfileId()).isTrue()
    assertThat(eventLog).hasProfileIdThat().isEqualTo(profileId)
  }

  @Test
  fun testController_logImportantEvent_withNoNetwork_exceedLimit_checkEventLogStoreSize() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logFourEvents()

    val eventLogsProvider = analyticsController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider)
    assertThat(eventLogs.eventLogsToUploadList).hasSize(2)
    assertThat(eventLogs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_logImportantEvent_withNoNetwork_exceedLimit_studyOn_checkEventLogStoreSize() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logFourEvents()

    val eventLogsProvider = analyticsController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(eventLogsProvider)
    assertThat(eventLogs.eventLogsToUploadList).hasSize(2)
    assertThat(eventLogs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_logImportantEvent_logLowPriorityEvent_withNoNetwork_checkOrderinCache() {
    setUpTestApplicationComponent()
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
    val firstEventLog = eventLogs.getEventLogsToUpload(0)
    val secondEventLog = eventLogs.getEventLogsToUpload(1)

    assertThat(firstEventLog).isOptionalPriority()
    assertThat(secondEventLog).isEssentialPriority()
  }

  @Test
  fun testController_logImportantEvent_switchToNoNetwork_logLowPriorityEvent_checkManagement() {
    setUpTestApplicationComponent()
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
    val cachedEventLog =
      monitorFactory.waitForNextSuccessfulResult(logsProvider).getEventLogsToUpload(0)

    assertThat(uploadedEventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(uploadedEventLog).isEssentialPriority()
    assertThat(uploadedEventLog).hasOpenQuestionPlayerContext()

    assertThat(cachedEventLog).hasTimestampThat().isEqualTo(TEST_TIMESTAMP)
    assertThat(cachedEventLog).isOptionalPriority()
    assertThat(cachedEventLog).hasOpenQuestionPlayerContext()
  }

  @Test
  fun testController_logEvents_exceedLimit_withNoNetwork_checkCorrectEventIsEvicted() {
    setUpTestApplicationComponent()
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logFourEvents()

    val logsProvider = analyticsController.getEventLogStore()

    val eventLogs = monitorFactory.waitForNextSuccessfulResult(logsProvider)
    val firstEventLog = eventLogs.getEventLogsToUpload(0)
    val secondEventLog = eventLogs.getEventLogsToUpload(1)
    assertThat(eventLogs.eventLogsToUploadList).hasSize(2)
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
  fun testController_logEvent_withoutNetwork_studyOn_verifySyncStatusIsUnchanged() {
    // Sync statuses only make sense in the context of the learner study feature being enabled.
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
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

    assertThat(testSyncStatusManager.getSyncStatuses())
      .containsExactly(INITIAL_UNKNOWN, NO_CONNECTIVITY)
  }

  @Test
  fun testController_logEvent_studyOn_verifySyncStatusChangesToRepresentLoggedEvent() {
    // Sync statuses only make sense in the context of the learner study feature being enabled.
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
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

    // 'Uploading' isn't an intermediate state when an event is directly logged.
    val syncStatuses = testSyncStatusManager.getSyncStatuses()
    assertThat(syncStatuses).containsExactly(INITIAL_UNKNOWN, DATA_UPLOADED)
  }

  @Test
  fun testController_logImportantEvent_studyOff_doesNotRecordEventsAsUploaded() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = false)
    // The important event should be marked as uploaded.
    analyticsController.logImportantEvent(oppiaLogger.createOpenHomeContext(), profileId = null)
    testCoroutineDispatchers.runCurrent()

    // No events should be marked as uploaded when the study is off.
    val logsProvider = analyticsController.getEventLogStore()
    val uploadedLogs =
      monitorFactory.waitForNextSuccessfulResult(logsProvider).uploadedEventLogsList
    assertThat(uploadedLogs).isEmpty()
  }

  @Test
  fun testController_logImportantEvent_studyOn_recordsEventAsUploaded() {
    // Events are only tracked as uploaded when the learner study feature being enabled.
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    // The important event should be marked as uploaded.
    analyticsController.logImportantEvent(oppiaLogger.createOpenHomeContext(), profileId = null)
    testCoroutineDispatchers.runCurrent()

    val logsProvider = analyticsController.getEventLogStore()
    val uploadedLogs =
      monitorFactory.waitForNextSuccessfulResult(logsProvider).uploadedEventLogsList
    assertThat(uploadedLogs).hasSize(1)
    assertThat(uploadedLogs.first()).hasOpenHomeContext()
  }

  @Test
  fun testController_uploadEventLogs_noLogs_cacheUnchanged() {
    setUpTestApplicationComponent()
    val monitor = monitorFactory.createMonitor(analyticsController.getEventLogStore())

    monitorFactory.ensureDataProviderExecutes(analyticsController.uploadEventLogs())

    val logs = monitor.ensureNextResultIsSuccess()
    assertThat(logs.eventLogsToUploadList).isEmpty()
    assertThat(logs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_uploadEventLogs_noLogs_studyOn_cacheUnchanged() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    val monitor = monitorFactory.createMonitor(analyticsController.getEventLogStore())

    monitorFactory.ensureDataProviderExecutes(analyticsController.uploadEventLogs())

    val logs = monitor.ensureNextResultIsSuccess()
    assertThat(logs.eventLogsToUploadList).isEmpty()
    assertThat(logs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_uploadEventLogs_noLogs_returnsPendingAndSimpleSuccessDataProvider() {
    setUpTestApplicationComponent()
    val uploadResults = monitorFactory.waitForAllNextResults {
      analyticsController.uploadEventLogs()
    }

    assertThat(uploadResults).hasSize(2)
    assertThat(uploadResults[0]).isPending()
    assertThat(uploadResults[1]).isSuccessThat().isEqualTo(0 to 0)
  }

  @Test
  fun testController_uploadEventLogs_withPreviousLogs_studyOn_setsSyncStatusToUploadingUploaded() {
    // Sync statuses only make sense in the context of the learner study feature being enabled.
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    logTwoEvents()

    monitorFactory.waitForAllNextResults { analyticsController.uploadEventLogs() }

    // Note that no data is actually uploaded here since the earlier events were immediately
    // logged (which is verified by the "DATA_UPLOADED" state that starts before uploading begins
    // from the uploadEventLogs() function). Also, the "check first" and "check last" is because
    // observing DataProvider changes over time is inherently unreliable because DataProviders are
    // designed for eventual consistency, not for guaranteed state delivery. However, the
    // synchronization in the test ensures that the expected states are at least observed once.
    val statuses = testSyncStatusManager.getSyncStatuses()
    assertThat(statuses.take(2)).containsExactly(INITIAL_UNKNOWN, DATA_UPLOADED).inOrder()
    assertThat(statuses.takeLast(2)).containsExactly(DATA_UPLOADING, DATA_UPLOADED).inOrder()
  }

  @Test
  fun testController_uploadEventLogs_withLogs_studyOn_setsSyncStatusToUploadingThenUploaded() {
    // Sync statuses only make sense in the context of the learner study feature being enabled.
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    logTwoEventsOffline()

    monitorFactory.waitForAllNextResults { analyticsController.uploadEventLogs() }

    val statuses = testSyncStatusManager.getSyncStatuses()
    assertThat(statuses)
      .containsExactly(INITIAL_UNKNOWN, NO_CONNECTIVITY, DATA_UPLOADING, DATA_UPLOADED)
      .inOrder()
  }

  @Test
  fun testController_uploadEventLogs_withLogs_studyOff_removesEventsButDoesNotTrackThem() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = false)
    logTwoEventsOffline()

    monitorFactory.waitForAllNextResults { analyticsController.uploadEventLogs() }

    // No events should be marked as uploaded when the study is off.
    val eventLogs =
      monitorFactory.waitForNextSuccessfulResult(analyticsController.getEventLogStore())
    assertThat(eventLogs.eventLogsToUploadList).isEmpty()
    assertThat(eventLogs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_uploadEventLogs_withLogs_studyOn_removesEventsForUploading() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    logTwoEventsOffline()

    monitorFactory.waitForAllNextResults { analyticsController.uploadEventLogs() }

    val eventLogs =
      monitorFactory.waitForNextSuccessfulResult(analyticsController.getEventLogStore())
    assertThat(eventLogs.eventLogsToUploadList).isEmpty()
    assertThat(eventLogs.uploadedEventLogsList).hasSize(2)
  }

  @Test
  fun testController_uploadEventLogs_withPreviousLogs_recordsEventsAsUploaded() {
    setUpTestApplicationComponent()
    logTwoEvents()

    monitorFactory.waitForAllNextResults { analyticsController.uploadEventLogs() }

    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(2)
  }

  @Test
  fun testController_uploadEventLogs_withLogs_recordsEventsAsUploaded() {
    setUpTestApplicationComponent()
    logTwoEventsOffline()

    monitorFactory.waitForAllNextResults { analyticsController.uploadEventLogs() }

    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(2)
  }

  @Test
  fun testController_uploadEventLogs_withPreviousLogs_returnsProgressContext() {
    setUpTestApplicationComponent()
    logTwoEvents()

    val uploadResults = monitorFactory.waitForAllNextResults {
      analyticsController.uploadEventLogs()
    }

    // There's no progress to report since the events have already been uploaded.
    assertThat(uploadResults).hasSize(2)
    assertThat(uploadResults[0]).isPending()
    assertThat(uploadResults[1]).isSuccessThat().isEqualTo(0 to 0)
  }

  @Test
  fun testController_uploadEventLogs_withLogs_returnsProgressContext() {
    setUpTestApplicationComponent()
    logTwoEventsOffline()

    val uploadResults = monitorFactory.waitForAllNextResults {
      analyticsController.uploadEventLogs()
    }

    // Progress should be reported for each uploaded event. Note that this is inherently flaky since
    // only the *last* result is guaranteed in DataProviders. As a result, *some* simple control
    // flow is added to increase the test's robustness in such cases. NOTE TO DEVELOPERS: please do
    // NOT copy this type of approach elsewhere--control flow is generally considered bad practice
    // in tests and an exception is made here only because: (1) it's unavoidable with current
    // synchronization mechanisms, (2) it's simple control flow, (3) it increases the test's
    // robustness, and (4) the "happy path" base case is still verified without control flow.
    assertThat(uploadResults.size).isAtLeast(1)
    when (uploadResults.size) {
      1 -> {} // Do nothing since only the result was received, and is verified below.
      2 -> assertThat(uploadResults[0]).isPending()
      3 -> {
        assertThat(uploadResults[0]).isPending()
        assertThat(uploadResults[1]).isSuccessThat().isEqualTo(1 to 2)
      }
      4 -> {
        assertThat(uploadResults[0]).isPending()
        assertThat(uploadResults[1]).isSuccessThat().isEqualTo(0 to 2)
        assertThat(uploadResults[2]).isSuccessThat().isEqualTo(1 to 2)
      }
      else -> fail("Encountered too many upload results: ${uploadResults.size}.")
    }
    assertThat(uploadResults.last()).isSuccessThat().isEqualTo(2 to 2)
  }

  @Test
  fun testController_uploadEventLogsAndWait_noLogs_cacheUnchanged() {
    setUpTestApplicationComponent()
    val monitor = monitorFactory.createMonitor(analyticsController.getEventLogStore())

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    val logs = monitor.ensureNextResultIsSuccess()
    assertThat(logs.eventLogsToUploadList).isEmpty()
    assertThat(logs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_uploadEventLogsAndWait_noLogs_studyOn_cacheUnchanged() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    val monitor = monitorFactory.createMonitor(analyticsController.getEventLogStore())

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    val logs = monitor.ensureNextResultIsSuccess()
    assertThat(logs.eventLogsToUploadList).isEmpty()
    assertThat(logs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_uploadEventLogsAndWait_prevLogs_studyOn_setsSyncStatusToUploadingUploaded() {
    // Sync statuses only make sense in the context of the learner study feature being enabled.
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    logTwoEvents()

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    // Note testController_uploadEventLogs_withPreviousLogs_setsSyncStatusToUploadingThenUploaded
    // can't reliably check all status results since DataProviders don't guarantee observed values,
    // but flows do (at least per uploadEventLogsAndWait's implementation & API contract). However,
    // just as testController_uploadEventLogs_withPreviousLogs_setsSyncStatusToUploadingThenUploaded
    // needs to verify the starts & ends of statuses, so does this test (for the rather rare case
    // where it can result in duplicate values). Realistically, this seems to happen rarer than
    // 1/100 with flows but it's still possible, so the test is designed to be a bit more robust
    // against this possibility.
    val statuses = testSyncStatusManager.getSyncStatuses()
    assertThat(statuses.take(2)).containsExactly(INITIAL_UNKNOWN, DATA_UPLOADED).inOrder()
    assertThat(statuses.takeLast(2)).containsExactly(DATA_UPLOADING, DATA_UPLOADED).inOrder()
  }

  @Test
  fun testController_uploadEventLogsAndWait_withLogs_studyOn_setsSyncStatusToUploadingUploaded() {
    // Sync statuses only make sense in the context of the learner study feature being enabled.
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    logTwoEventsOffline()

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    val statuses = testSyncStatusManager.getSyncStatuses()
    assertThat(statuses)
      .containsExactly(INITIAL_UNKNOWN, NO_CONNECTIVITY, DATA_UPLOADING, DATA_UPLOADED)
      .inOrder()
  }

  @Test
  fun testController_uploadEventLogsAndWait_withLogs_studyOff_removesEventsButDoesNotTrackThem() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = false)
    logTwoEventsOffline()

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    // No events should be marked as uploaded when the study is off.
    val eventLogs =
      monitorFactory.waitForNextSuccessfulResult(analyticsController.getEventLogStore())
    assertThat(eventLogs.eventLogsToUploadList).isEmpty()
    assertThat(eventLogs.uploadedEventLogsList).isEmpty()
  }

  @Test
  fun testController_uploadEventLogsAndWait_withLogs_studyOn_removesEventsForUploading() {
    setUpTestApplicationComponent(enableLearnerStudyAnalytics = true)
    logTwoEventsOffline()

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    val eventLogs =
      monitorFactory.waitForNextSuccessfulResult(analyticsController.getEventLogStore())
    assertThat(eventLogs.eventLogsToUploadList).isEmpty()
    assertThat(eventLogs.uploadedEventLogsList).hasSize(2)
  }

  @Test
  fun testController_uploadEventLogsAndWait_withPreviousLogs_recordsEventsAsUploaded() {
    setUpTestApplicationComponent()
    logTwoEvents()

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(2)
  }

  @Test
  fun testController_uploadEventLogsAndWait_withLogs_recordsEventsAsUploaded() {
    setUpTestApplicationComponent()
    logTwoEventsOffline()

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(2)
  }

  @Test
  fun testController_cachedEventsFromLastAppInstance_logNewEvent_onlyLatestEventLogged() {
    setUpTestApplicationComponent()
    // Simulate events being logged in a previous instance of the app.
    logTwoCachedEventsDirectlyOnDisk()

    analyticsController.logImportantEvent(oppiaLogger.createOpenHomeContext(), profileId = null)
    testCoroutineDispatchers.runCurrent()

    // Without a complete upload, only the most recent event will sync.
    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(1)
  }

  @Test
  fun testController_cachedEventsFromLastAppInstance_logNewEvent_thenForceSync_everythingUploads() {
    setUpTestApplicationComponent()
    // Simulate events being logged in a previous instance of the app.
    logTwoCachedEventsDirectlyOnDisk()
    analyticsController.logImportantEvent(oppiaLogger.createOpenHomeContext(), profileId = null)
    testCoroutineDispatchers.runCurrent()

    runSynchronously { analyticsController.uploadEventLogsAndWait() }

    // The force sync should ensure everything is uploaded. NOTE TO DEVELOPER: If this test is
    // failing, it may be due to AnalyticsController being created before
    // logTwoCachedEventsDirectlyOnDisk is called above. If that's the case, use the indirect
    // injection pattern at the top of the test suite (for AnalyticsController itself) to ensure
    // whichever dependency is injecting AnalyticsController is also only injected when needed (i.e.
    // using a Provider).
    assertThat(fakeAnalyticsEventLogger.getEventListCount()).isEqualTo(3)
  }

  private fun setUpTestApplicationComponent(enableLearnerStudyAnalytics: Boolean = false) {
    TestPlatformParameterModule.forceEnableLearnerStudyAnalytics(enableLearnerStudyAnalytics)
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun logFourEvents() {
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

  private fun logTwoEvents() {
    analyticsController.logImportantEvent(oppiaLogger.createOpenHomeContext(), profileId = null)
    analyticsController.logLowPriorityEvent(oppiaLogger.createOpenHomeContext(), profileId = null)
    testCoroutineDispatchers.runCurrent()
  }

  private fun logTwoEventsOffline() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logTwoEvents()
    networkConnectionUtil.setCurrentConnectionStatus(LOCAL)
  }

  private fun logTwoCachedEventsDirectlyOnDisk() {
    persistentCacheStoryFactory.create(
      "event_logs", OppiaEventLogs.getDefaultInstance()
    ).storeDataAsync {
      OppiaEventLogs.newBuilder().apply {
        addEventLogsToUpload(createEventLog(context = oppiaLogger.createOpenHomeContext()))
        addEventLogsToUpload(createEventLog(context = oppiaLogger.createOpenHomeContext()))
      }.build()
    }.waitForSuccessfulResult()
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

    return ProfileId.newBuilder().apply { loggedInInternalProfileId = 0 }.build()
      .also { expectedProfileId ->
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

  private fun runSynchronously(operation: suspend () -> Unit) =
    CoroutineScope(backgroundDispatcher).async { operation() }.waitForSuccessfulResult()

  private fun <T> Deferred<T>.waitForSuccessfulResult() {
    return when (val result = waitForResult()) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> {} // Nothing to do; the result succeeded.
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult() = toStateFlow().waitForLatestValue()

  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(): T =
    also { testCoroutineDispatchers.runCurrent() }.value

  private fun createEventLog(
    context: EventLog.Context,
    priority: EventLog.Priority = EventLog.Priority.ESSENTIAL,
    timestamp: Long = oppiaClock.getCurrentTimeMs()
  ) = EventLog.newBuilder().apply {
    this.timestamp = timestamp
    this.priority = priority
    this.context = context
  }.build()

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
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
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
