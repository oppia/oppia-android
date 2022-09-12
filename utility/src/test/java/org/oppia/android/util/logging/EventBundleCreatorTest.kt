package org.oppia.android.util.logging

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.truth.os.BundleSubject.assertThat
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.CardContext
import org.oppia.android.app.model.EventLog.ConceptCardContext
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_BACKGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.DELETE_PROFILE_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.EXIT_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.FINISH_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.HINT_OFFERED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.INSTALL_ID_FOR_FAILED_ANALYTICS_LOG
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
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PLAY_VOICE_OVER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SOLUTION_OFFERED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_OVER_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SUBMIT_ANSWER_CONTEXT
import org.oppia.android.app.model.EventLog.ExplorationContext
import org.oppia.android.app.model.EventLog.HintContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.EventLog.PlayVoiceOverContext
import org.oppia.android.app.model.EventLog.Priority.ESSENTIAL
import org.oppia.android.app.model.EventLog.Priority.OPTIONAL
import org.oppia.android.app.model.EventLog.QuestionContext
import org.oppia.android.app.model.EventLog.RevisionCardContext
import org.oppia.android.app.model.EventLog.StoryContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext
import org.oppia.android.app.model.EventLog.TopicContext
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER
import org.oppia.android.app.model.OppiaMetricLog.NetworkType
import org.oppia.android.app.model.OppiaMetricLog.NetworkType.CELLULAR
import org.oppia.android.app.model.OppiaMetricLog.NetworkType.WIFI
import org.oppia.android.app.model.OppiaMetricLog.Priority
import org.oppia.android.app.model.OppiaMetricLog.Priority.HIGH_PRIORITY
import org.oppia.android.app.model.OppiaMetricLog.Priority.MEDIUM_PRIORITY
import org.oppia.android.app.model.OppiaMetricLog.StorageTier
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.HIGH_STORAGE
import org.oppia.android.app.model.OppiaMetricLog.StorageTier.MEDIUM_STORAGE
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.model.ScreenName.SCREEN_NAME_UNSPECIFIED
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.EventLog.Context.Builder as EventContextBuilder

private const val TEST_ANDROID_SDK_VERSION = 30

/**
 * Tests for [EventBundleCreator].
 *
 * Note that some of the properties of [EventLog]s logged via [EventBundleCreator] will 'namespace'
 * their property names (for cases when properties are nested). The tests of this suite include
 * verification for property names (based on the fact that certain properties need to be present in
 * the filled [Bundle] in order for the test to pass since it's verifying values corresponding to
 * those property names).
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = EventBundleCreatorTest.TestApplication::class,
  sdk = [TEST_ANDROID_SDK_VERSION]
)
class EventBundleCreatorTest {
  private companion object {
    private const val TEST_TIMESTAMP_1 = 1556094120000
    private const val TEST_TIMESTAMP_2 = 1234567898765
    private const val TEST_TOPIC_ID = "test_topic_id"
    private const val TEST_STORY_ID = "test_story_id"
    private const val TEST_EXPLORATION_ID = "test_exploration_id"
    private const val TEST_QUESTION_ID = "test_question_id"
    private const val TEST_SKILL_ID_1 = "test_skill_id_1"
    private const val TEST_SKILL_ID_2 = "test_skill_id_2"
    private const val TEST_SUB_TOPIC_INDEX = 1
    private const val TEST_SUB_TOPIC_INDEX_STR = "1"
    private const val TEST_LEARNER_ID = "test_ed_ld_learner_id"
    private const val TEST_INSTALLATION_ID = "test_installation_id"
    private const val TEST_LEARNER_SESSION_ID = "test_session_id"
    private const val TEST_EXPLORATION_VERSION = 5
    private const val TEST_EXPLORATION_VERSION_STR = "5"
    private const val TEST_STATE_NAME = "test_state_name"
    private const val TEST_HINT_INDEX = 1
    private const val TEST_HINT_INDEX_STR = "1"
    private const val TEST_IS_ANSWER_CORRECT = true
    private const val TEST_IS_ANSWER_CORRECT_STR = "true"
    private const val TEST_CONTENT_ID = "test_content_id"
    private const val TEST_APP_VERSION_NAME = "oppia-android-test-0123456789"
    private const val TEST_APP_VERSION_CODE = 125
    private const val TEST_CPU_USAGE = Long.MAX_VALUE
    private const val TEST_APK_SIZE = Long.MAX_VALUE
    private const val TEST_STORAGE_USAGE = Long.MAX_VALUE
    private const val TEST_STARTUP_LATENCY = Long.MAX_VALUE
    private const val TEST_NETWORK_USAGE = Long.MAX_VALUE
    private const val TEST_MEMORY_USAGE = Long.MAX_VALUE
  }

  @Inject lateinit var context: Context
  @Inject lateinit var eventBundleCreator: EventBundleCreator

  @Parameter
  lateinit var screenNameCase: String

  @Parameter
  lateinit var screenNameCaseValue: String

  @After
  fun tearDown() {
    TestModule.enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
  }

  @Test
  fun testFillEventBundle_defaultEvent_defaultsBundleAndReturnsUnknownActivityContext() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val typeName = eventBundleCreator.fillEventBundle(EventLog.getDefaultInstance(), bundle)

    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(0)
    assertThat(bundle).string("priority").isEqualTo("unspecified_priority")
    assertThat(bundle).integer("event_type").isEqualTo(ACTIVITYCONTEXT_NOT_SET.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillPerformanceMetricsBundle_defaultEvent_defaultsBundleAndRetsUnknownActivityContext() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      OppiaMetricLog.getDefaultInstance(), bundle
    )

    assertThat(typeName).isEqualTo("unknown_loggable_metric")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(0)
    assertThat(bundle).string("priority").isEqualTo("unspecified_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("false")
    assertThat(bundle).string("memory_tier").isEqualTo("unspecified_memory_tier")
    assertThat(bundle).string("storage_tier").isEqualTo("unspecified_storage_tier")
    assertThat(bundle).string("network_type").isEqualTo("unspecified_network_type")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
  }

  @Test
  fun testFillEventBundle_eventWithDefaultedContext_fillsPriorityAndTimeAndRetsUnknownContext() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val eventLog = createEventLog(timestamp = TEST_TIMESTAMP_1, priority = ESSENTIAL)

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(ACTIVITYCONTEXT_NOT_SET.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_eventWithDifferentTimestamp_savesDifferentTimestampInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val eventLog = createEventLog(timestamp = TEST_TIMESTAMP_2)

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_2)
  }

  @Test
  fun testFillEventBundle_eventWithDifferentPriority_savesDifferentPriorityInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val eventLog = createEventLog(priority = OPTIONAL)

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("priority").isEqualTo("optional")
  }

  @Test
  fun testFillMetricsBundle_eventWithDefaultLoggableMetric_fillsDetailsAndRetsUnknownLog() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog = createPerformanceMetricLog(
      timestamp = TEST_TIMESTAMP_1,
      priority = HIGH_PRIORITY,
      currentScreen = SCREEN_NAME_UNSPECIFIED,
      memoryTier = HIGH_MEMORY_TIER,
      storageTier = HIGH_STORAGE,
      networkType = WIFI,
      isAppInForeground = true
    )

    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      performanceMetricLog,
      bundle
    )

    assertThat(typeName).isEqualTo("unknown_loggable_metric")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
  }

  @Test
  fun testFillPerformanceMetricBundle_eventWithDiffTimestamp_savesDifferentTimestampInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog = createPerformanceMetricLog(timestamp = TEST_TIMESTAMP_2)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_2)
  }

  @Test
  fun testFillPerformanceMetricBundle_eventWithDifferentPriority_savesDifferentPriorityInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog =
      createPerformanceMetricLog(priority = MEDIUM_PRIORITY)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("priority").isEqualTo("medium_priority")
  }

  @Test
  fun testFillPerformanceMetricBundle_eventWithDiffMemoryTier_savesDiffMemoryTierInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog =
      createPerformanceMetricLog(memoryTier = MEDIUM_MEMORY_TIER)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("memory_tier").isEqualTo("medium_memory")
  }

  @Test
  fun testFillPerformanceMetricBundle_eventWithDiffStorageTier_savesDiffStorageTierInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog =
      createPerformanceMetricLog(storageTier = MEDIUM_STORAGE)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("storage_tier").isEqualTo("medium_storage")
  }

  @Test
  fun testFillPerformanceMetricBundle_eventWithDiffCurrentScreen_savesDiffCurrentScreenInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog =
      createPerformanceMetricLog(currentScreen = SCREEN_NAME_UNSPECIFIED)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
  }

  @Test
  fun testFillPerformanceMetricBundle_eventWithDiffNetworkType_savesDiffNetworkTypeInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog =
      createPerformanceMetricLog(networkType = CELLULAR)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("network_type").isEqualTo("cellular")
  }

  @Test
  fun testFillPerformanceMetricBundle_eventWithDiffAppInForeground_savesDiffValueInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog = createPerformanceMetricLog(isAppInForeground = false)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("is_app_in_foreground").isEqualTo("false")
  }

  @Test
  fun testFillEventBundle_openExpActivityEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenExplorationActivity())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_exploration_player_screen")
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_EXPLORATION_ACTIVITY.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_openExpActivityEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenExplorationActivity())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_exploration_player_screen")
    assertThat(bundle).hasSize(14)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_EXPLORATION_ACTIVITY.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_openInfoTabContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenInfoTab())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("select_topic_info_tab")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_INFO_TAB.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
  }

  @Test
  fun testFillEventBundle_openLessonsTabContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenLessonsTab())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("select_topic_lessons_tab")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_LESSONS_TAB.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
  }

  @Test
  fun testFillPerformanceMetricBundle_createApkSizeLoggableMetric_bundlesAllDetailsCorrectly() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val performanceMetric = createPerformanceMetricLog(
      loggableMetric = createApkSizeLoggableMetric()
    )
    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      performanceMetric, bundle
    )

    assertThat(typeName).isEqualTo("apk_size_metric")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("apk_size_bytes").isEqualTo(TEST_APK_SIZE)
  }

  @Test
  fun testFillPerformanceMetricBundle_createStorageUsageLogMetric_bundlesAllDetailsCorrectly() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val performanceMetric = createPerformanceMetricLog(
      loggableMetric = createStorageUsageLoggableMetric()
    )
    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      performanceMetric, bundle
    )

    assertThat(typeName).isEqualTo("storage_usage_metric")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("storage_usage_bytes").isEqualTo(TEST_STORAGE_USAGE)
  }

  @Test
  fun testFillPerformanceMetricBundle_createStartupLatencyMetric_bundlesAllDetailsCorrectly() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val performanceMetric = createPerformanceMetricLog(
      loggableMetric = createStartupLatencyLoggableMetric()
    )
    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      performanceMetric, bundle
    )

    assertThat(typeName).isEqualTo("startup_latency_metric")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("startup_latency_millis").isEqualTo(TEST_STARTUP_LATENCY)
  }

  @Test
  fun testFillPerformanceMetricBundle_createMemoryUsageMetric_fillsAllDetailsInBundleCorrectly() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val performanceMetric = createPerformanceMetricLog(
      loggableMetric = createMemoryUsageLoggableMetric()
    )
    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      performanceMetric, bundle
    )

    assertThat(typeName).isEqualTo("memory_usage_metric")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("total_pss_bytes").isEqualTo(TEST_MEMORY_USAGE)
  }

  @Test
  fun testFillPerformanceMetricBundle_createNetworkUsageMetric_fillsAllDetailsInBundleCorrectly() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val performanceMetric = createPerformanceMetricLog(
      loggableMetric = createNetworkUsageTestLoggableMetric()
    )
    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      performanceMetric, bundle
    )

    assertThat(typeName).isEqualTo("network_usage_metric")
    assertThat(bundle).hasSize(9)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("bytes_received").isEqualTo(TEST_NETWORK_USAGE)
    assertThat(bundle).longInt("bytes_sent").isEqualTo(TEST_NETWORK_USAGE)
  }

  @Test
  fun testFillPerformanceMetricBundle_createCpuUsageLogMetric_fillsAllDetailsInBundleCorrectly() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val performanceMetric = createPerformanceMetricLog(
      loggableMetric = createCpuUsageLoggableMetric()
    )
    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      performanceMetric, bundle
    )

    assertThat(typeName).isEqualTo("cpu_usage_metric")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("cpu_usage").isEqualTo(TEST_CPU_USAGE)
  }

  @Test
  fun testFillEventBundle_openPracticeTabContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenPracticeTab())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("select_topic_practice_tab")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_PRACTICE_TAB.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
  }

  @Test
  fun testFillEventBundle_openRevisionTabContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenRevisionTab())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("select_topic_revision_tab")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_REVISION_TAB.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
  }

  @Test
  fun testFillEventBundle_openQuestionPlayerContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenQuestionPlayer())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_question_player_screen")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_QUESTION_PLAYER.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("question_id").isEqualTo(TEST_QUESTION_ID)
    assertThat(bundle).string("skill_ids").isEqualTo("$TEST_SKILL_ID_1,$TEST_SKILL_ID_2")
  }

  @Test
  fun testFillEventBundle_openStoryActivityContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenStoryActivity())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_story_chapter_list_screen")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_STORY_ACTIVITY.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
  }

  @Test
  fun testFillEventBundle_openConceptCardContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenConceptCard())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_concept_card")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_CONCEPT_CARD.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("skill_id").isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testFillEventBundle_openRevisionCardContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenRevisionCard())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_revision_card")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_REVISION_CARD.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("subtopic_index").isEqualTo(TEST_SUB_TOPIC_INDEX_STR)
  }

  @Test
  fun testFillEventBundle_startCardContextEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createStartCardContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("start_exploration_card")
    assertThat(bundle).hasSize(13)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(START_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("skill_id").isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testFillEventBundle_startCardContextEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createStartCardContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("start_exploration_card")
    assertThat(bundle).hasSize(15)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(START_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("skill_id").isEqualTo(TEST_SKILL_ID_1)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_endCardContextEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createEndCardContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("end_exploration_card")
    assertThat(bundle).hasSize(13)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(END_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("skill_id").isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testFillEventBundle_endCardContextEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createEndCardContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("end_exploration_card")
    assertThat(bundle).hasSize(15)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(END_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("skill_id").isEqualTo(TEST_SKILL_ID_1)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_hintOfferedEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createHintOfferedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("receive_hint_offer")
    assertThat(bundle).hasSize(13)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(HINT_OFFERED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("hint_index").isEqualTo(TEST_HINT_INDEX_STR)
  }

  @Test
  fun testFillEventBundle_hintOfferedEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createHintOfferedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("receive_hint_offer")
    assertThat(bundle).hasSize(15)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(HINT_OFFERED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("hint_index").isEqualTo(TEST_HINT_INDEX_STR)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_accessHintContextEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAccessHintContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_hint")
    assertThat(bundle).hasSize(13)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(ACCESS_HINT_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("hint_index").isEqualTo(TEST_HINT_INDEX_STR)
  }

  @Test
  fun testFillEventBundle_accessHintContextEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAccessHintContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_hint")
    assertThat(bundle).hasSize(15)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(ACCESS_HINT_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("hint_index").isEqualTo(TEST_HINT_INDEX_STR)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_solutionOfferedEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSolutionOfferedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("receive_solution_offer")
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SOLUTION_OFFERED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_solutionOfferedEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSolutionOfferedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("receive_solution_offer")
    assertThat(bundle).hasSize(14)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SOLUTION_OFFERED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_accessSolutionEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAccessSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_solution")
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(ACCESS_SOLUTION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_accessSolutionEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAccessSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_solution")
    assertThat(bundle).hasSize(14)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(ACCESS_SOLUTION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_submitAnswerEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSubmitAnswerContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("submit_answer")
    assertThat(bundle).hasSize(14)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SUBMIT_ANSWER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("submitted_answer").isEmpty()
    assertThat(bundle).string("is_answer_correct").isEqualTo(TEST_IS_ANSWER_CORRECT_STR)
  }

  @Test
  fun testFillEventBundle_submitAnswerEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSubmitAnswerContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("submit_answer")
    assertThat(bundle).hasSize(16)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SUBMIT_ANSWER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("submitted_answer").isEmpty()
    assertThat(bundle).string("is_answer_correct").isEqualTo(TEST_IS_ANSWER_CORRECT_STR)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_playVoiceOverEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createPlayVoiceOverContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("click_play_voiceover_button")
    assertThat(bundle).hasSize(13)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(PLAY_VOICE_OVER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("content_id").isEqualTo(TEST_CONTENT_ID)
  }

  @Test
  fun testFillEventBundle_playVoiceOverEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createPlayVoiceOverContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("click_play_voiceover_button")
    assertThat(bundle).hasSize(15)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(PLAY_VOICE_OVER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("content_id").isEqualTo(TEST_CONTENT_ID)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_appInBackgroundEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAppInBackgroundContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("send_app_to_background")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(APP_IN_BACKGROUND_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_appInBackgroundEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAppInBackgroundContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("send_app_to_background")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(APP_IN_BACKGROUND_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_appInForegroundEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAppInForegroundContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("bring_app_to_foreground")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(APP_IN_FOREGROUND_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_appInForegroundEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAppInForegroundContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("bring_app_to_foreground")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(APP_IN_FOREGROUND_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_exitExplorationEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createExitExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("leave_exploration")
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(EXIT_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_exitExplorationEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createExitExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("leave_exploration")
    assertThat(bundle).hasSize(14)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(EXIT_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_finishExplorationEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createFinishExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("complete_exploration")
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(FINISH_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_finishExplorationEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createFinishExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("complete_exploration")
    assertThat(bundle).hasSize(14)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(FINISH_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_resumeExplorationEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createResumeExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("resume_in_progress_exploration")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(RESUME_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_resumeExplorationEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createResumeExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("resume_in_progress_exploration")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(RESUME_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_startOverExpEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createStartOverExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("restart_in_progress_exploration")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(START_OVER_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_startOverExpEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createStartOverExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("restart_in_progress_exploration")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(START_OVER_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_deleteProfileEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createDeleteProfileContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("delete_profile")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(DELETE_PROFILE_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_deleteProfileEvent_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createDeleteProfileContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("delete_profile")
    assertThat(bundle).hasSize(8)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(DELETE_PROFILE_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_openHomeContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenHomeContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_home_screen")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_HOME.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_openProfileChooserContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenProfileChooserContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_profile_chooser_screen")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_PROFILE_CHOOSER.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_failedEventInstallId_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createInstallationIdForFailedAnalyticsLogContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(6)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(INSTALL_ID_FOR_FAILED_ANALYTICS_LOG.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_failedEventInstallId_studyOn_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createInstallationIdForFailedAnalyticsLogContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(7)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(INSTALL_ID_FOR_FAILED_ANALYTICS_LOG.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  @RunParameterized(
    Iteration("forHomeScreen", "screenNameCase=HOME_ACTIVITY", "screenNameCaseValue=home_activity"),
    Iteration(
      "forSplashScreen",
      "screenNameCase=SPLASH_ACTIVITY",
      "screenNameCaseValue=splash_activity"
    ),
    Iteration(
      "forProfileChooserScreen",
      "screenNameCase=PROFILE_CHOOSER_ACTIVITY",
      "screenNameCaseValue=profile_chooser_activity"
    ),
    Iteration(
      "forAddProfileScreen",
      "screenNameCase=ADD_PROFILE_ACTIVITY",
      "screenNameCaseValue=add_profile_activity"
    ),
    Iteration(
      "forBackgroundScreen",
      "screenNameCase=BACKGROUND_SCREEN",
      "screenNameCaseValue=background_screen"
    ),
    Iteration(
      "forAppVersionScreen",
      "screenNameCase=APP_VERSION_ACTIVITY",
      "screenNameCaseValue=app_version_activity"
    ),
    Iteration(
      "forAdministratorControlsScreen",
      "screenNameCase=ADMINISTRATOR_CONTROLS_ACTIVITY",
      "screenNameCaseValue=administrator_controls_activity"
    ),
    Iteration(
      "forProfileAndDeviceIdScreen",
      "screenNameCase=PROFILE_AND_DEVICE_ID_ACTIVITY",
      "screenNameCaseValue=profile_and_device_id_activity"
    ),
    Iteration(
      "forCompletedStoryListActivity",
      "screenNameCase=COMPLETED_STORY_LIST_ACTIVITY",
      "screenNameCaseValue=completed_story_list_activity"
    ),
    Iteration(
      "forFaqSingleActivity",
      "screenNameCase=FAQ_SINGLE_ACTIVITY",
      "screenNameCaseValue=faq_single_activity"
    ),
    Iteration(
      "forFaqListActivity",
      "screenNameCase=FAQ_LIST_ACTIVITY",
      "screenNameCaseValue=faq_list_activity"
    ),
    Iteration(
      "forLicenseListActivity",
      "screenNameCase=LICENSE_LIST_ACTIVITY",
      "screenNameCaseValue=license_list_activity"
    ),
    Iteration(
      "forLicenseTextViewerActivity",
      "screenNameCase=LICENSE_TEXT_VIEWER_ACTIVITY",
      "screenNameCaseValue=license_text_viewer_activity"
    ),
    Iteration(
      "forThirdPartyDependencyListActivity",
      "screenNameCase=THIRD_PARTY_DEPENDENCY_LIST_ACTIVITY",
      "screenNameCaseValue=third_party_dependency_list_activity"
    ),
    Iteration(
      "forHelpActivity",
      "screenNameCase=HELP_ACTIVITY",
      "screenNameCaseValue=help_activity"
    ),
    Iteration(
      "forRecentlyPlayedActivity",
      "screenNameCase=RECENTLY_PLAYED_ACTIVITY",
      "screenNameCaseValue=recently_played_activity"
    ),
    Iteration(
      "forMyDownloadsActivity",
      "screenNameCase=MY_DOWNLOADS_ACTIVITY",
      "screenNameCaseValue=my_downloads_activity"
    ),
    Iteration(
      "forOnboardingActivity",
      "screenNameCase=ONBOARDING_ACTIVITY",
      "screenNameCaseValue=onboarding_activity"
    ),
    Iteration(
      "forOngoingTopicListActivity",
      "screenNameCase=ONGOING_TOPIC_LIST_ACTIVITY",
      "screenNameCaseValue=ongoing_topic_list_activity"
    ),
    Iteration(
      "forAudioLanguageActivity",
      "screenNameCase=AUDIO_LANGUAGE_ACTIVITY",
      "screenNameCaseValue=audio_language_activity"
    ),
    Iteration(
      "forAppLanguageActivity",
      "screenNameCase=APP_LANGUAGE_ACTIVITY",
      "screenNameCaseValue=app_language_activity"
    ),
    Iteration(
      "forOptionsActivity",
      "screenNameCase=OPTIONS_ACTIVITY",
      "screenNameCaseValue=options_activity"
    ),
    Iteration(
      "forReadingTextSizeActivity",
      "screenNameCase=READING_TEXT_SIZE_ACTIVITY",
      "screenNameCaseValue=reading_text_size_activity"
    ),
    Iteration(
      "forExplorationActivity",
      "screenNameCase=EXPLORATION_ACTIVITY",
      "screenNameCaseValue=exploration_activity"
    ),
    Iteration(
      "forAdminAuthActivity",
      "screenNameCase=ADMIN_AUTH_ACTIVITY",
      "screenNameCaseValue=admin_auth_activity"
    ),
    Iteration(
      "forPinPasswordActivity",
      "screenNameCase=PIN_PASSWORD_ACTIVITY",
      "screenNameCaseValue=pin_password_activity"
    ),
    Iteration(
      "forProfilePictureActivity",
      "screenNameCase=PROFILE_PICTURE_ACTIVITY",
      "screenNameCaseValue=profile_picture_activity"
    ),
    Iteration(
      "forProfileProgressActivity",
      "screenNameCase=PROFILE_PROGRESS_ACTIVITY",
      "screenNameCaseValue=profile_progress_activity"
    ),
    Iteration(
      "forResumeLessonActivity",
      "screenNameCase=RESUME_LESSON_ACTIVITY",
      "screenNameCaseValue=resume_lesson_activity"
    ),
    Iteration(
      "forProfileEditActivity",
      "screenNameCase=PROFILE_EDIT_ACTIVITY",
      "screenNameCaseValue=profile_edit_activity"
    ),
    Iteration(
      "forProfileResetPinActivity",
      "screenNameCase=PROFILE_RESET_PIN_ACTIVITY",
      "screenNameCaseValue=profile_reset_pin_activity"
    ),
    Iteration(
      "forProfileRenameActivity",
      "screenNameCase=PROFILE_RENAME_ACTIVITY",
      "screenNameCaseValue=profile_rename_activity"
    ),
    Iteration(
      "forProfileListActivity",
      "screenNameCase=PROFILE_LIST_ACTIVITY",
      "screenNameCaseValue=profile_list_activity"
    ),
    Iteration(
      "forStoryActivity",
      "screenNameCase=STORY_ACTIVITY",
      "screenNameCaseValue=story_activity"
    ),
    Iteration(
      "forTopicActivity",
      "screenNameCase=TOPIC_ACTIVITY",
      "screenNameCaseValue=topic_activity"
    ),
    Iteration(
      "forRevisionCardActivity",
      "screenNameCase=REVISION_CARD_ACTIVITY",
      "screenNameCaseValue=revision_card_activity"
    ),
    Iteration(
      "forQuestionPlayerActivity",
      "screenNameCase=QUESTION_PLAYER_ACTIVITY",
      "screenNameCaseValue=question_player_activity"
    ),
    Iteration(
      "forWalkthroughActivity",
      "screenNameCase=WALKTHROUGH_ACTIVITY",
      "screenNameCaseValue=walkthrough_activity"
    ),
    Iteration(
      "forDeveloperOptionsActivity",
      "screenNameCase=DEVELOPER_OPTIONS_ACTIVITY",
      "screenNameCaseValue=developer_options_activity"
    ),
    Iteration(
      "forViewEventLogsActivity",
      "screenNameCase=VIEW_EVENT_LOGS_ACTIVITY",
      "screenNameCaseValue=view_event_logs_activity"
    ),
    Iteration(
      "forMarkTopicsCompletedActivity",
      "screenNameCase=MARK_TOPICS_COMPLETED_ACTIVITY",
      "screenNameCaseValue=mark_topics_completed_activity"
    ),
    Iteration(
      "forMathExpressionParserActivity",
      "screenNameCase=MATH_EXPRESSION_PARSER_ACTIVITY",
      "screenNameCaseValue=math_expression_parser_activity"
    ),
    Iteration(
      "forMarkChaptersCompletedActivity",
      "screenNameCase=MARK_CHAPTERS_COMPLETED_ACTIVITY",
      "screenNameCaseValue=mark_chapters_completed_activity"
    ),
    Iteration(
      "forMarkStoriesCompletedActivity",
      "screenNameCase=MARK_STORIES_COMPLETED_ACTIVITY",
      "screenNameCaseValue=mark_stories_completed_activity"
    ),
    Iteration(
      "forForceNetworkTypeActivity",
      "screenNameCase=FORCE_NETWORK_TYPE_ACTIVITY",
      "screenNameCaseValue=force_network_type_activity"
    ),
    Iteration(
      "forAdminPinActivity",
      "screenNameCase=ADMIN_PIN_ACTIVITY",
      "screenNameCaseValue=admin_pin_activity"
    ),
    Iteration(
      "forPoliciesActivity",
      "screenNameCase=POLICIES_ACTIVITY",
      "screenNameCaseValue=policies_activity"
    ),
    Iteration("forUnspecified", "screenNameCase=", "screenNameCaseValue=screen_name_unspecified"),
  )
  fun testMetricsBundle_addScreenName_verifyConversionToCorrectAnalyticalName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val currentScreen = getScreenName(screenNameCase)
    val performanceMetricLog =
      createPerformanceMetricLog(currentScreen = currentScreen)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("current_screen").isEqualTo(screenNameCaseValue)
  }

  private fun getScreenName(string: String): ScreenName {
    for (screenName in ScreenName.values()) {
      if (string == screenName.name) {
        return screenName
      }
    }
    return SCREEN_NAME_UNSPECIFIED
  }

  private fun createEventLog(
    timestamp: Long = TEST_TIMESTAMP_1,
    priority: EventLog.Priority = ESSENTIAL,
    context: EventLog.Context = EventLog.Context.getDefaultInstance()
  ) = EventLog.newBuilder().apply {
    this.timestamp = timestamp
    this.priority = priority
    this.context = context
  }.build()

  private fun createPerformanceMetricLog(
    timestamp: Long = TEST_TIMESTAMP_1,
    priority: Priority = HIGH_PRIORITY,
    currentScreen: ScreenName = SCREEN_NAME_UNSPECIFIED,
    memoryTier: MemoryTier = HIGH_MEMORY_TIER,
    storageTier: StorageTier = HIGH_STORAGE,
    isAppInForeground: Boolean = true,
    networkType: NetworkType = WIFI,
    loggableMetric: LoggableMetric = LoggableMetric.getDefaultInstance()
  ) = OppiaMetricLog.newBuilder().apply {
    this.timestampMillis = timestamp
    this.priority = priority
    this.currentScreen = currentScreen
    this.memoryTier = memoryTier
    this.storageTier = storageTier
    this.isAppInForeground = isAppInForeground
    this.networkType = networkType
    this.loggableMetric = loggableMetric
  }.build()

  private fun createOpenExplorationActivity(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setOpenExplorationActivity)

  private fun createOpenInfoTab(topicContext: TopicContext = createTopicContext()) =
    createEventContext(topicContext, EventContextBuilder::setOpenInfoTab)

  private fun createOpenLessonsTab(topicContext: TopicContext = createTopicContext()) =
    createEventContext(topicContext, EventContextBuilder::setOpenLessonsTab)

  private fun createOpenPracticeTab(topicContext: TopicContext = createTopicContext()) =
    createEventContext(topicContext, EventContextBuilder::setOpenPracticeTab)

  private fun createOpenRevisionTab(topicContext: TopicContext = createTopicContext()) =
    createEventContext(topicContext, EventContextBuilder::setOpenRevisionTab)

  private fun createOpenQuestionPlayer(questionContext: QuestionContext = createQuestionContext()) =
    createEventContext(questionContext, EventContextBuilder::setOpenQuestionPlayer)

  private fun createOpenStoryActivity(storyContext: StoryContext = createStoryContext()) =
    createEventContext(storyContext, EventContextBuilder::setOpenStoryActivity)

  private fun createOpenConceptCard(
    conceptCardContext: ConceptCardContext = createConceptCardContext()
  ) = createEventContext(conceptCardContext, EventContextBuilder::setOpenConceptCard)

  private fun createOpenRevisionCard(
    revisionCardContext: RevisionCardContext = createRevisionCardContext()
  ) = createEventContext(revisionCardContext, EventContextBuilder::setOpenRevisionCard)

  private fun createStartCardContext(cardContext: CardContext = createCardContext()) =
    createEventContext(cardContext, EventContextBuilder::setStartCardContext)

  private fun createEndCardContext(cardContext: CardContext = createCardContext()) =
    createEventContext(cardContext, EventContextBuilder::setEndCardContext)

  private fun createHintOfferedContext(hintContext: HintContext = createHintContext()) =
    createEventContext(hintContext, EventContextBuilder::setHintOfferedContext)

  private fun createAccessHintContext(hintContext: HintContext = createHintContext()) =
    createEventContext(hintContext, EventContextBuilder::setAccessHintContext)

  private fun createSolutionOfferedContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setSolutionOfferedContext)

  private fun createAccessSolutionContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setAccessSolutionContext)

  private fun createSubmitAnswerContext(
    submitAnswerContext: SubmitAnswerContext = createSubmitAnswerContextDetails()
  ) = createEventContext(submitAnswerContext, EventContextBuilder::setSubmitAnswerContext)

  private fun createPlayVoiceOverContext(
    playVoiceOverContext: PlayVoiceOverContext = createPlayVoiceOverContextDetails()
  ) = createEventContext(playVoiceOverContext, EventContextBuilder::setPlayVoiceOverContext)

  private fun createAppInBackgroundContext(
    learnerDetails: LearnerDetailsContext = createLearnerDetailsContext()
  ) = createEventContext(learnerDetails, EventContextBuilder::setAppInBackgroundContext)

  private fun createAppInForegroundContext(
    learnerDetails: LearnerDetailsContext = createLearnerDetailsContext()
  ) = createEventContext(learnerDetails, EventContextBuilder::setAppInForegroundContext)

  private fun createExitExplorationContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setExitExplorationContext)

  private fun createFinishExplorationContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setFinishExplorationContext)

  private fun createResumeExplorationContext(
    learnerDetails: LearnerDetailsContext = createLearnerDetailsContext()
  ) = createEventContext(learnerDetails, EventContextBuilder::setResumeExplorationContext)

  private fun createStartOverExplorationContext(
    learnerDetails: LearnerDetailsContext = createLearnerDetailsContext()
  ) = createEventContext(learnerDetails, EventContextBuilder::setStartOverExplorationContext)

  private fun createDeleteProfileContext(
    learnerDetails: LearnerDetailsContext = createLearnerDetailsContext()
  ) = createEventContext(learnerDetails, EventContextBuilder::setDeleteProfileContext)

  private fun createOpenHomeContext() =
    createEventContext(value = true, EventContextBuilder::setOpenHome)

  private fun createOpenProfileChooserContext() =
    createEventContext(value = true, EventContextBuilder::setOpenProfileChooser)

  private fun createInstallationIdForFailedAnalyticsLogContext(
    installationId: String = TEST_INSTALLATION_ID
  ) = createEventContext(installationId, EventContextBuilder::setInstallIdForFailedAnalyticsLog)

  private fun <T> createEventContext(
    value: T,
    setter: EventContextBuilder.(T) -> EventContextBuilder
  ) = EventLog.Context.newBuilder().setter(value).build()

  private fun createExplorationContext(
    topicId: String = TEST_TOPIC_ID,
    storyId: String = TEST_STORY_ID,
    explorationId: String = TEST_EXPLORATION_ID,
    sessionId: String = TEST_LEARNER_SESSION_ID,
    explorationVersion: Int = TEST_EXPLORATION_VERSION,
    stateName: String = TEST_STATE_NAME,
    learnerDetails: LearnerDetailsContext = createLearnerDetailsContext()
  ) = ExplorationContext.newBuilder().apply {
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId
    this.sessionId = sessionId
    this.explorationVersion = explorationVersion
    this.stateName = stateName
    this.learnerDetails = learnerDetails
  }.build()

  private fun createLearnerDetailsContext(
    learnerId: String = TEST_LEARNER_ID,
    installId: String = TEST_INSTALLATION_ID
  ) = LearnerDetailsContext.newBuilder().apply {
    this.learnerId = learnerId
    this.installId = installId
  }.build()

  private fun createTopicContext(topicId: String = TEST_TOPIC_ID) =
    TopicContext.newBuilder().apply { this.topicId = topicId }.build()

  private fun createQuestionContext(
    questionId: String = TEST_QUESTION_ID,
    skillIds: List<String> = listOf(TEST_SKILL_ID_1, TEST_SKILL_ID_2)
  ) = QuestionContext.newBuilder().apply {
    this.questionId = questionId
    addAllSkillId(skillIds)
  }.build()

  private fun createStoryContext(
    topicId: String = TEST_TOPIC_ID,
    storyId: String = TEST_STORY_ID
  ) = StoryContext.newBuilder().apply {
    this.topicId = topicId
    this.storyId = storyId
  }.build()

  private fun createConceptCardContext(skillId: String = TEST_SKILL_ID_1) =
    ConceptCardContext.newBuilder().apply { this.skillId = skillId }.build()

  private fun createRevisionCardContext(
    topicId: String = TEST_TOPIC_ID,
    subTopicIndex: Int = TEST_SUB_TOPIC_INDEX
  ) = RevisionCardContext.newBuilder().apply {
    this.topicId = topicId
    subTopicId = subTopicIndex
  }.build()

  private fun createCardContext(
    explorationDetails: ExplorationContext = createExplorationContext(),
    skillId: String = TEST_SKILL_ID_1
  ) = CardContext.newBuilder().apply {
    this.explorationDetails = explorationDetails
    this.skillId = skillId
  }.build()

  private fun createHintContext(
    explorationDetails: ExplorationContext = createExplorationContext(),
    hintIndex: Int = TEST_HINT_INDEX
  ) = HintContext.newBuilder().apply {
    this.explorationDetails = explorationDetails
    this.hintIndex = hintIndex
  }.build()

  private fun createSubmitAnswerContextDetails(
    explorationDetails: ExplorationContext = createExplorationContext(),
    isAnswerCorrect: Boolean = TEST_IS_ANSWER_CORRECT
  ) = SubmitAnswerContext.newBuilder().apply {
    this.explorationDetails = explorationDetails
    this.isAnswerCorrect = isAnswerCorrect
  }.build()

  private fun createPlayVoiceOverContextDetails(
    explorationDetails: ExplorationContext = createExplorationContext(),
    contentId: String = TEST_CONTENT_ID
  ) = PlayVoiceOverContext.newBuilder().apply {
    this.explorationDetails = explorationDetails
    this.contentId = contentId
  }.build()

  private fun registerTestApplication() {
    val packageManager = Shadows.shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .build()
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageInfo.versionName = TEST_APP_VERSION_NAME
    packageInfo.versionCode = TEST_APP_VERSION_CODE
    packageManager.installPackage(packageInfo)
  }

  private fun createApkSizeLoggableMetric() = OppiaMetricLog.LoggableMetric.newBuilder()
    .setApkSizeMetric(
      OppiaMetricLog.ApkSizeMetric.newBuilder()
        .setApkSizeBytes(TEST_APK_SIZE)
        .build()
    ).build()

  private fun createStorageUsageLoggableMetric() = LoggableMetric.newBuilder()
    .setStorageUsageMetric(
      OppiaMetricLog.StorageUsageMetric.newBuilder()
        .setStorageUsageBytes(TEST_STORAGE_USAGE)
        .build()
    ).build()

  private fun createStartupLatencyLoggableMetric() = LoggableMetric.newBuilder()
    .setStartupLatencyMetric(
      OppiaMetricLog.StartupLatencyMetric.newBuilder()
        .setStartupLatencyMillis(TEST_STARTUP_LATENCY)
        .build()
    ).build()

  private fun createCpuUsageLoggableMetric() = LoggableMetric.newBuilder()
    .setCpuUsageMetric(
      OppiaMetricLog.CpuUsageMetric.newBuilder()
        .setCpuUsageMetric(TEST_CPU_USAGE)
        .build()
    ).build()

  private fun createNetworkUsageTestLoggableMetric() = LoggableMetric.newBuilder()
    .setNetworkUsageMetric(
      OppiaMetricLog.NetworkUsageMetric.newBuilder()
        .setBytesReceived(TEST_NETWORK_USAGE)
        .setBytesSent(TEST_NETWORK_USAGE)
        .build()
    ).build()

  private fun createMemoryUsageLoggableMetric() = LoggableMetric.newBuilder()
    .setMemoryUsageMetric(
      OppiaMetricLog.MemoryUsageMetric.newBuilder()
        .setTotalPssBytes(TEST_MEMORY_USAGE)
        .build()
    ).build()

  private fun setUpTestApplicationComponentWithoutLearnerAnalyticsStudy() {
    TestModule.enableLearnerStudyAnalytics = false
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponentWithLearnerAnalyticsStudy() {
    TestModule.enableLearnerStudyAnalytics = true
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    registerTestApplication()
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    internal companion object {
      // This is expected to be off by default, so this helps the tests above confirm that the
      // feature's default value is, indeed, off.
      var enableLearnerStudyAnalytics = LEARNER_STUDY_ANALYTICS_DEFAULT_VALUE
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // The scoping here is to ensure changes to the module value above don't change the parameter
    // within the same application instance.
    @Provides
    @Singleton
    @LearnerStudyAnalytics
    fun provideLearnerStudyAnalytics(): PlatformParameterValue<Boolean> {
      // Snapshot the value so that it doesn't change between injection and use.
      val enableFeature = enableLearnerStudyAnalytics
      return object : PlatformParameterValue<Boolean> {
        override val value: Boolean = enableFeature
      }
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, EventLoggingConfigurationModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: EventBundleCreatorTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerEventBundleCreatorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: EventBundleCreatorTest) {
      component.inject(test)
    }
  }
}
