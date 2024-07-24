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
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.CardContext
import org.oppia.android.app.model.EventLog.ConceptCardContext
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_BACKGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.CLOSE_REVISION_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.DELETE_PROFILE_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.EXIT_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.FINISH_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.HINT_UNLOCKED_CONTEXT
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
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PAUSE_VOICE_OVER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PLAY_VOICE_OVER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REACH_INVESTED_ENGAGEMENT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REVEAL_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REVEAL_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SOLUTION_UNLOCKED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_OVER_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SUBMIT_ANSWER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SWITCH_IN_LESSON_LANGUAGE
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.VIEW_EXISTING_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.VIEW_EXISTING_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.ExplorationContext
import org.oppia.android.app.model.EventLog.HintContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.EventLog.Priority.ESSENTIAL
import org.oppia.android.app.model.EventLog.Priority.OPTIONAL
import org.oppia.android.app.model.EventLog.QuestionContext
import org.oppia.android.app.model.EventLog.RevisionCardContext
import org.oppia.android.app.model.EventLog.StoryContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext
import org.oppia.android.app.model.EventLog.SwitchInLessonLanguageEventContext
import org.oppia.android.app.model.EventLog.TopicContext
import org.oppia.android.app.model.EventLog.VoiceoverActionContext
import org.oppia.android.app.model.OppiaLanguage
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
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
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
    private const val TEST_CLASSROOM_ID = "test_classroom_id"
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
    private const val TEST_LANGUAGE_CODE = "en"
    private const val TEST_APP_VERSION_NAME = "oppia-android-test-0123456789"
    private const val TEST_APP_VERSION_CODE = 125L
    private const val TEST_CPU_USAGE = Double.MAX_VALUE
    private const val TEST_APK_SIZE = Long.MAX_VALUE
    private const val TEST_STORAGE_USAGE = Long.MAX_VALUE
    private const val TEST_STARTUP_LATENCY = Long.MAX_VALUE
    private const val TEST_NETWORK_USAGE = Long.MAX_VALUE
    private const val TEST_MEMORY_USAGE = Long.MAX_VALUE
  }

  @Inject lateinit var context: Context
  @Inject lateinit var eventBundleCreator: EventBundleCreator

  @Parameter lateinit var name: String
  @Parameter lateinit var expNameStr: String
  @Parameter lateinit var inLang: String
  @Parameter lateinit var expLang: String

  private val screenName by lazy { ScreenName.valueOf(name) }
  private val inputLanguage by lazy { OppiaLanguage.valueOf(inLang) }

  @After
  fun tearDown() {
    TestModule.enableLoggingLearnerStudyIds = LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
  }

  @Test
  fun testFillEventBundle_defaultEvent_defaultsBundleAndReturnsUnknownActivityContext() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val typeName = eventBundleCreator.fillEventBundle(EventLog.getDefaultInstance(), bundle)

    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(0)
    assertThat(bundle).string("priority").isEqualTo("unspecified_priority")
    assertThat(bundle).integer("event_type").isEqualTo(ACTIVITYCONTEXT_NOT_SET.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("oppia_app_lang").isEqualTo("unset_app_language_selection")
    assertThat(bundle).string("oppia_content_lang")
      .isEqualTo("unset_written_translation_language_selection")
    assertThat(bundle).string("oppia_audio_lang")
      .isEqualTo("unset_audio_translation_language_selection")
  }

  @Test
  fun testFillPerformanceMetricsBundle_defaultEvent_defaultsBundleAndRetsUnknownActivityContext() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val typeName = eventBundleCreator.fillPerformanceMetricsEventBundle(
      OppiaMetricLog.getDefaultInstance(), bundle
    )

    assertThat(typeName).isEqualTo("unknown_loggable_metric")
    assertThat(bundle).hasSize(10)
    assertThat(bundle).longInt("timestamp").isEqualTo(0)
    assertThat(bundle).string("priority").isEqualTo("unspecified_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("false")
    assertThat(bundle).string("memory_tier").isEqualTo("unspecified_memory_tier")
    assertThat(bundle).string("storage_tier").isEqualTo("unspecified_storage_tier")
    assertThat(bundle).string("network_type").isEqualTo("unspecified_network_type")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_eventWithDefaultedContext_fillsPriorityAndTimeAndRetsUnknownContext() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val eventLog = createEventLog(timestamp = TEST_TIMESTAMP_1, priority = ESSENTIAL)

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(ACTIVITYCONTEXT_NOT_SET.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("oppia_app_lang").isEqualTo("unset_app_language_selection")
    assertThat(bundle).string("oppia_content_lang")
      .isEqualTo("unset_written_translation_language_selection")
    assertThat(bundle).string("oppia_audio_lang")
      .isEqualTo("unset_audio_translation_language_selection")
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
  fun testFillEventBundle_eventWithSystemAppLanguage_savesCorrectAppLanguageInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val eventLog =
      createEventLog(
        appLanguageSelection = AppLanguageSelection.newBuilder().apply {
          useSystemLanguageOrAppDefault = true
        }.build()
      )

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("oppia_app_lang").isEqualTo("use_system_language_or_app_default")
  }

  @Test
  @Iteration("lang_unspecified", "inLang=LANGUAGE_UNSPECIFIED", "expLang=unspecified_language")
  @Iteration("ar", "inLang=ARABIC", "expLang=Arabic")
  @Iteration("en", "inLang=ENGLISH", "expLang=English")
  @Iteration("hi", "inLang=HINDI", "expLang=Hindi")
  @Iteration("hi_en", "inLang=HINGLISH", "expLang=Hinglish")
  @Iteration("pt", "inLang=PORTUGUESE", "expLang=Portuguese")
  @Iteration("pt_br", "inLang=BRAZILIAN_PORTUGUESE", "expLang=Brazilian Portuguese")
  @Iteration("sw", "inLang=SWAHILI", "expLang=Swahili")
  @Iteration("pcm", "inLang=NIGERIAN_PIDGIN", "expLang=Nigerian Pidgin")
  fun testFillEventBundle_eventWithSelectedAppLanguage_savesCorrectAppLanguageInBundle() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val eventLog =
      createEventLog(
        appLanguageSelection = AppLanguageSelection.newBuilder().apply {
          selectedLanguage = inputLanguage
        }.build()
      )

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("oppia_app_lang").isEqualTo(expLang)
  }

  @Test
  fun testFillEventBundle_eventWithUseAppLanguageForWrittenTranslations_savesCorrectWrittenLang() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val languageSelection = WrittenTranslationLanguageSelection.newBuilder().apply {
      useAppLanguage = true
    }.build()
    val eventLog = createEventLog(writtenTranslationLanguageSelection = languageSelection)

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("oppia_content_lang").isEqualTo("use_app_language")
  }

  @Test
  @Iteration("lang_unspecified", "inLang=LANGUAGE_UNSPECIFIED", "expLang=unspecified_language")
  @Iteration("ar", "inLang=ARABIC", "expLang=Arabic")
  @Iteration("en", "inLang=ENGLISH", "expLang=English")
  @Iteration("hi", "inLang=HINDI", "expLang=Hindi")
  @Iteration("hi_en", "inLang=HINGLISH", "expLang=Hinglish")
  @Iteration("pt", "inLang=PORTUGUESE", "expLang=Portuguese")
  @Iteration("pt_br", "inLang=BRAZILIAN_PORTUGUESE", "expLang=Brazilian Portuguese")
  @Iteration("sw", "inLang=SWAHILI", "expLang=Swahili")
  @Iteration("pcm", "inLang=NIGERIAN_PIDGIN", "expLang=Nigerian Pidgin")
  fun testFillEventBundle_eventWithSelectedWrittenTranslationsLanguage_savesCorrectWrittenLang() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val languageSelection = WrittenTranslationLanguageSelection.newBuilder().apply {
      selectedLanguage = inputLanguage
    }.build()
    val eventLog = createEventLog(writtenTranslationLanguageSelection = languageSelection)

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("oppia_content_lang").isEqualTo(expLang)
  }

  @Test
  fun testFillEventBundle_eventWithUseAppLanguageForAudioTranslations_savesCorrectAudioLang() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val languageSelection = AudioTranslationLanguageSelection.newBuilder().apply {
      useAppLanguage = true
    }.build()
    val eventLog = createEventLog(audioTranslationLanguageSelection = languageSelection)

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("oppia_audio_lang").isEqualTo("use_app_language")
  }

  @Test
  @Iteration("lang_unspecified", "inLang=LANGUAGE_UNSPECIFIED", "expLang=unspecified_language")
  @Iteration("ar", "inLang=ARABIC", "expLang=Arabic")
  @Iteration("en", "inLang=ENGLISH", "expLang=English")
  @Iteration("hi", "inLang=HINDI", "expLang=Hindi")
  @Iteration("hi_en", "inLang=HINGLISH", "expLang=Hinglish")
  @Iteration("pt", "inLang=PORTUGUESE", "expLang=Portuguese")
  @Iteration("pt_br", "inLang=BRAZILIAN_PORTUGUESE", "expLang=Brazilian Portuguese")
  @Iteration("sw", "inLang=SWAHILI", "expLang=Swahili")
  @Iteration("pcm", "inLang=NIGERIAN_PIDGIN", "expLang=Nigerian Pidgin")
  fun testFillEventBundle_eventWithSelectedAudioTranslationsLanguage_savesCorrectAudioLang() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val languageSelection = AudioTranslationLanguageSelection.newBuilder().apply {
      selectedLanguage = inputLanguage
    }.build()
    val eventLog = createEventLog(audioTranslationLanguageSelection = languageSelection)

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("oppia_audio_lang").isEqualTo(expLang)
  }

  @Test
  fun testFillEventBundle_eventWithMultipleLanguageConfigurations_savesCorrectLanguages() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val appLanguageSelection = AppLanguageSelection.newBuilder().apply {
      selectedLanguage = OppiaLanguage.SWAHILI
    }.build()
    val writtenLanguageSelection = WrittenTranslationLanguageSelection.newBuilder().apply {
      selectedLanguage = OppiaLanguage.ENGLISH
    }.build()
    val audioLanguageSelection = AudioTranslationLanguageSelection.newBuilder().apply {
      selectedLanguage = OppiaLanguage.HINGLISH
    }.build()
    val eventLog =
      createEventLog(
        appLanguageSelection = appLanguageSelection,
        writtenTranslationLanguageSelection = writtenLanguageSelection,
        audioTranslationLanguageSelection = audioLanguageSelection
      )

    eventBundleCreator.fillEventBundle(eventLog, bundle)

    assertThat(bundle).string("oppia_app_lang").isEqualTo("Swahili")
    assertThat(bundle).string("oppia_content_lang").isEqualTo("English")
    assertThat(bundle).string("oppia_audio_lang").isEqualTo("Hinglish")
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
    assertThat(bundle).hasSize(10)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
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
  fun testFillEventBundle_basicEvent_includesEventCount() {
    setUpTestApplicationComponent()
    val eventLog = createEventLog(context = createOpenExplorationActivity())

    val bundle = Bundle().also { eventBundleCreator.fillEventBundle(eventLog, it) }

    assertThat(bundle).integer("dbg_event_count_since_app_open").isEqualTo(1)
  }

  @Test
  fun testFillEventBundle_secondEvent_includesLargerEventCount() {
    setUpTestApplicationComponent()
    val eventLog1 = createEventLog(context = createOpenExplorationActivity())
    eventBundleCreator.fillEventBundle(eventLog1, Bundle())
    val eventLog2 = createEventLog(context = createOpenExplorationActivity())

    val bundle = Bundle().also { eventBundleCreator.fillEventBundle(eventLog2, it) }

    // The number is larger since there are now two events that have been marked for logging.
    assertThat(bundle).integer("dbg_event_count_since_app_open").isEqualTo(2)
  }

  @Test
  fun testFillEventBundle_secondEvent_inDifferentApplication_includesInitialEventCount() {
    // Prepare one event for logging in one application.
    executeInPreviousAppInstance { testComponent ->
      val eventLog1 = createEventLog(context = createOpenExplorationActivity())
      testComponent.getEventBundleCreator().fillEventBundle(eventLog1, Bundle())
    }

    // Create a second application (to simulate an app restart).
    setUpTestApplicationComponent()
    val eventLog2 = createEventLog(context = createOpenExplorationActivity())
    val bundle = Bundle().also { eventBundleCreator.fillEventBundle(eventLog2, it) }

    // The second event should have an initial event count since the app 'reopened'.
    assertThat(bundle).integer("dbg_event_count_since_app_open").isEqualTo(1)
  }

  @Test
  fun testFillEventBundle_openExpActivityEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenExplorationActivity())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_exploration_player_screen")
    assertThat(bundle).hasSize(18)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_EXPLORATION_ACTIVITY.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_openExpActivityEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenExplorationActivity())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_exploration_player_screen")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_EXPLORATION_ACTIVITY.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
    assertThat(bundle).hasSize(12)
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
    assertThat(bundle).hasSize(12)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("apk_size_bytes").isEqualTo(TEST_APK_SIZE)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("storage_usage_bytes").isEqualTo(TEST_STORAGE_USAGE)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("startup_latency_millis").isEqualTo(TEST_STARTUP_LATENCY)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("total_pss_bytes").isEqualTo(TEST_MEMORY_USAGE)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
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
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle).longInt("bytes_received").isEqualTo(TEST_NETWORK_USAGE)
    assertThat(bundle).longInt("bytes_sent").isEqualTo(TEST_NETWORK_USAGE)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("high_priority")
    assertThat(bundle).string("is_app_in_foreground").isEqualTo("true")
    assertThat(bundle).string("memory_tier").isEqualTo("high_memory")
    assertThat(bundle).string("storage_tier").isEqualTo("high_storage")
    assertThat(bundle).string("network_type").isEqualTo("wifi")
    assertThat(bundle).string("current_screen").isEqualTo("screen_name_unspecified")
    assertThat(bundle.getDouble("cpu_usage")).isWithin(1e-5).of(TEST_CPU_USAGE)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_openPracticeTabContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenPracticeTab())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("select_topic_practice_tab")
    assertThat(bundle).hasSize(12)
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
    assertThat(bundle).hasSize(12)
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
    assertThat(bundle).hasSize(13)
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
    assertThat(bundle).hasSize(13)
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
    assertThat(bundle).hasSize(12)
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
    assertThat(bundle).hasSize(13)
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
  fun testFillEventBundle_closeRevisionCardContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createCloseRevisionCard())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("close_revision_card")
    assertThat(bundle).hasSize(13)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(CLOSE_REVISION_CARD.number)
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
    assertThat(bundle).hasSize(19)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(START_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("skill_id").isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testFillEventBundle_startCardContextEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createStartCardContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("start_exploration_card")
    assertThat(bundle).hasSize(21)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(START_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
    assertThat(bundle).hasSize(19)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(END_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("skill_id").isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testFillEventBundle_endCardContextEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createEndCardContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("end_exploration_card")
    assertThat(bundle).hasSize(21)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(END_CARD_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
  fun testFillEventBundle_hintUnlockedEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createHintUnlockedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("unlock_hint")
    assertThat(bundle).hasSize(19)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(HINT_UNLOCKED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("hint_index").isEqualTo(TEST_HINT_INDEX_STR)
  }

  @Test
  fun testFillEventBundle_hintUnlockedEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createHintUnlockedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("unlock_hint")
    assertThat(bundle).hasSize(21)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(HINT_UNLOCKED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
  fun testFillEventBundle_revealHintContextEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createRevealHintContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_hint")
    assertThat(bundle).hasSize(19)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(REVEAL_HINT_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("hint_index").isEqualTo(TEST_HINT_INDEX_STR)
  }

  @Test
  fun testFillEventBundle_revealHintContextEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createRevealHintContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_hint")
    assertThat(bundle).hasSize(21)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(REVEAL_HINT_CONTEXT.number)
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
  fun testFillEventBundle_viewExistingHintEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createViewExistingHintContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("view_existing_hint")
    assertThat(bundle).hasSize(19)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(VIEW_EXISTING_HINT_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("hint_index").isEqualTo(TEST_HINT_INDEX_STR)
  }

  @Test
  fun testFillEventBundle_viewExistingHintEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createViewExistingHintContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("view_existing_hint")
    assertThat(bundle).hasSize(21)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(VIEW_EXISTING_HINT_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
  fun testFillEventBundle_solutionUnlockedEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSolutionUnlockedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("unlock_solution")
    assertThat(bundle).hasSize(18)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SOLUTION_UNLOCKED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_solutionUnlockedEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSolutionUnlockedContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("unlock_solution")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SOLUTION_UNLOCKED_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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

    val eventLog = createEventLog(context = createRevealSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_solution")
    assertThat(bundle).hasSize(18)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(REVEAL_SOLUTION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_accessSolutionEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createRevealSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_solution")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(REVEAL_SOLUTION_CONTEXT.number)
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
  fun testFillEventBundle_viewExistingSolutionEvent_studyOff_fillsNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createViewExistingSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("view_existing_solution")
    assertThat(bundle).hasSize(18)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(VIEW_EXISTING_SOLUTION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_viewExistingSolutionEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createViewExistingSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("view_existing_solution")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(VIEW_EXISTING_SOLUTION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SUBMIT_ANSWER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
  fun testFillEventBundle_submitAnswerEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSubmitAnswerContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("submit_answer")
    assertThat(bundle).hasSize(22)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SUBMIT_ANSWER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(PLAY_VOICE_OVER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("content_id").isEqualTo(TEST_CONTENT_ID)
    assertThat(bundle).string("language_code").isEqualTo(TEST_LANGUAGE_CODE)
  }

  @Test
  fun testFillEventBundle_playVoiceOverEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createPlayVoiceOverContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("click_play_voiceover_button")
    assertThat(bundle).hasSize(22)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(PLAY_VOICE_OVER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("content_id").isEqualTo(TEST_CONTENT_ID)
    assertThat(bundle).string("language_code").isEqualTo(TEST_LANGUAGE_CODE)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_pauseVoiceOverEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createPauseVoiceOverContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("click_pause_voiceover_button")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(PAUSE_VOICE_OVER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("content_id").isEqualTo(TEST_CONTENT_ID)
    assertThat(bundle).string("language_code").isEqualTo(TEST_LANGUAGE_CODE)
  }

  @Test
  fun testFillEventBundle_pauseVoiceOverEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createPauseVoiceOverContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("click_pause_voiceover_button")
    assertThat(bundle).hasSize(22)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(PAUSE_VOICE_OVER_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("content_id").isEqualTo(TEST_CONTENT_ID)
    assertThat(bundle).string("language_code").isEqualTo(TEST_LANGUAGE_CODE)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(APP_IN_BACKGROUND_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_appInBackgroundEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAppInBackgroundContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("send_app_to_background")
    assertThat(bundle).hasSize(13)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(APP_IN_FOREGROUND_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_appInForegroundEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createAppInForegroundContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("bring_app_to_foreground")
    assertThat(bundle).hasSize(13)
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
    assertThat(bundle).hasSize(18)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(EXIT_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_exitExplorationEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createExitExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("leave_exploration")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(EXIT_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
    assertThat(bundle).hasSize(18)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(FINISH_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_finishExplorationEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createFinishExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("complete_exploration")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(FINISH_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(RESUME_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_resumeExplorationEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createResumeExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("resume_in_progress_exploration")
    assertThat(bundle).hasSize(13)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(START_OVER_EXPLORATION_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_startOverExpEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createStartOverExplorationContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("restart_in_progress_exploration")
    assertThat(bundle).hasSize(13)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(DELETE_PROFILE_CONTEXT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_deleteProfileEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createDeleteProfileContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("delete_profile")
    assertThat(bundle).hasSize(13)
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
    assertThat(bundle).hasSize(11)
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
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_PROFILE_CHOOSER.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_reachInvestedEngagementEvent_studyOff_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createReachInvestedEngagementContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reach_invested_engagement")
    assertThat(bundle).hasSize(18)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(REACH_INVESTED_ENGAGEMENT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("state_name").isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testFillEventBundle_reachInvestedEngagementEvent_studyOn_fillsNonSensitiveDataAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createReachInvestedEngagementContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reach_invested_engagement")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(REACH_INVESTED_ENGAGEMENT.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("classroom_id").isEqualTo(TEST_CLASSROOM_ID)
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
  fun testFillEventBundle_switchInLessonLanguageEvent_studyOff_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSwitchInLessonLanguageContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("click_switch_language_in_lesson")
    assertThat(bundle).hasSize(20)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SWITCH_IN_LESSON_LANGUAGE.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("from_language").isEqualTo("English")
    assertThat(bundle).string("to_language").isEqualTo("Swahili")
  }

  @Test
  fun testFillEventBundle_switchInLessonLanguageEvent_studyOn_fillsNonSensitiveDataAndRetsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSwitchInLessonLanguageContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("click_switch_language_in_lesson")
    assertThat(bundle).hasSize(22)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(SWITCH_IN_LESSON_LANGUAGE.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("ed_classroom_id").isEqualTo(TEST_CLASSROOM_ID)
    assertThat(bundle).string("ed_topic_id").isEqualTo(TEST_TOPIC_ID)
    assertThat(bundle).string("ed_story_id").isEqualTo(TEST_STORY_ID)
    assertThat(bundle).string("ed_exploration_id").isEqualTo(TEST_EXPLORATION_ID)
    assertThat(bundle).string("ed_session_id").isEqualTo(TEST_LEARNER_SESSION_ID)
    assertThat(bundle).string("ed_exploration_version").isEqualTo(TEST_EXPLORATION_VERSION_STR)
    assertThat(bundle).string("ed_state_name").isEqualTo(TEST_STATE_NAME)
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
    assertThat(bundle).string("from_language").isEqualTo("English")
    assertThat(bundle).string("to_language").isEqualTo("Swahili")
  }

  @Test
  fun testFillEventBundle_failedEventInstallId_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createInstallationIdForFailedAnalyticsLogContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(INSTALL_ID_FOR_FAILED_ANALYTICS_LOG.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_failedEventInstallId_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createInstallationIdForFailedAnalyticsLogContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("ERROR_internal_logging_failure")
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(INSTALL_ID_FOR_FAILED_ANALYTICS_LOG.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  @Iteration("home", "name=HOME_ACTIVITY", "expNameStr=home_activity")
  @Iteration("splash", "name=SPLASH_ACTIVITY", "expNameStr=splash_activity")
  @Iteration(
    "profileChooser",
    "name=PROFILE_CHOOSER_ACTIVITY",
    "expNameStr=profile_chooser_activity"
  )
  @Iteration("addProfile", "name=ADD_PROFILE_ACTIVITY", "expNameStr=add_profile_activity")
  @Iteration("background", "name=BACKGROUND_SCREEN", "expNameStr=background_screen")
  @Iteration("appVersion", "name=APP_VERSION_ACTIVITY", "expNameStr=app_version_activity")
  @Iteration(
    "administratorControls",
    "name=ADMINISTRATOR_CONTROLS_ACTIVITY",
    "expNameStr=administrator_controls_activity"
  )
  @Iteration(
    "profileAndDeviceId",
    "name=PROFILE_AND_DEVICE_ID_ACTIVITY",
    "expNameStr=profile_and_device_id_activity"
  )
  @Iteration(
    "completedStoryList",
    "name=COMPLETED_STORY_LIST_ACTIVITY",
    "expNameStr=completed_story_list_activity"
  )
  @Iteration("faqSingle", "name=FAQ_SINGLE_ACTIVITY", "expNameStr=faq_single_activity")
  @Iteration("faqList", "name=FAQ_LIST_ACTIVITY", "expNameStr=faq_list_activity")
  @Iteration("licenseList", "name=LICENSE_LIST_ACTIVITY", "expNameStr=license_list_activity")
  @Iteration(
    "licenseTextViewer",
    "name=LICENSE_TEXT_VIEWER_ACTIVITY",
    "expNameStr=license_text_viewer_activity"
  )
  @Iteration(
    "thirdPartyDependencyList",
    "name=THIRD_PARTY_DEPENDENCY_LIST_ACTIVITY",
    "expNameStr=third_party_dependency_list_activity"
  )
  @Iteration("help", "name=HELP_ACTIVITY", "expNameStr=help_activity")
  @Iteration(
    "recentlyPlayed",
    "name=RECENTLY_PLAYED_ACTIVITY",
    "expNameStr=recently_played_activity"
  )
  @Iteration("myDownloads", "name=MY_DOWNLOADS_ACTIVITY", "expNameStr=my_downloads_activity")
  @Iteration("onboarding", "name=ONBOARDING_ACTIVITY", "expNameStr=onboarding_activity")
  @Iteration(
    "ongoingTopicList",
    "name=ONGOING_TOPIC_LIST_ACTIVITY",
    "expNameStr=ongoing_topic_list_activity"
  )
  @Iteration(
    "audioLanguage",
    "name=AUDIO_LANGUAGE_ACTIVITY",
    "expNameStr=audio_language_activity"
  )
  @Iteration("appLanguage", "name=APP_LANGUAGE_ACTIVITY", "expNameStr=app_language_activity")
  @Iteration("options", "name=OPTIONS_ACTIVITY", "expNameStr=options_activity")
  @Iteration(
    "readingTextSize",
    "name=READING_TEXT_SIZE_ACTIVITY",
    "expNameStr=reading_text_size_activity"
  )
  @Iteration("exploration", "name=EXPLORATION_ACTIVITY", "expNameStr=exploration_activity")
  @Iteration("adminAuth", "name=ADMIN_AUTH_ACTIVITY", "expNameStr=admin_auth_activity")
  @Iteration("pinPassword", "name=PIN_PASSWORD_ACTIVITY", "expNameStr=pin_password_activity")
  @Iteration(
    "profilePicture",
    "name=PROFILE_PICTURE_ACTIVITY",
    "expNameStr=profile_picture_activity"
  )
  @Iteration(
    "profileProgress",
    "name=PROFILE_PROGRESS_ACTIVITY",
    "expNameStr=profile_progress_activity"
  )
  @Iteration("resumeLesson", "name=RESUME_LESSON_ACTIVITY", "expNameStr=resume_lesson_activity")
  @Iteration("profileEdit", "name=PROFILE_EDIT_ACTIVITY", "expNameStr=profile_edit_activity")
  @Iteration(
    "profileResetPin",
    "name=PROFILE_RESET_PIN_ACTIVITY",
    "expNameStr=profile_reset_pin_activity"
  )
  @Iteration(
    "profileRename",
    "name=PROFILE_RENAME_ACTIVITY",
    "expNameStr=profile_rename_activity"
  )
  @Iteration("profileList", "name=PROFILE_LIST_ACTIVITY", "expNameStr=profile_list_activity")
  @Iteration("story", "name=STORY_ACTIVITY", "expNameStr=story_activity")
  @Iteration("topic", "name=TOPIC_ACTIVITY", "expNameStr=topic_activity")
  @Iteration("revisionCard", "name=REVISION_CARD_ACTIVITY", "expNameStr=revision_card_activity")
  @Iteration(
    "questionPlayer",
    "name=QUESTION_PLAYER_ACTIVITY",
    "expNameStr=question_player_activity"
  )
  @Iteration("walkthrough", "name=WALKTHROUGH_ACTIVITY", "expNameStr=walkthrough_activity")
  @Iteration(
    "developerOptions",
    "name=DEVELOPER_OPTIONS_ACTIVITY",
    "expNameStr=developer_options_activity"
  )
  @Iteration(
    "viewEventLogs",
    "name=VIEW_EVENT_LOGS_ACTIVITY",
    "expNameStr=view_event_logs_activity"
  )
  @Iteration(
    "markTopicsCompleted",
    "name=MARK_TOPICS_COMPLETED_ACTIVITY",
    "expNameStr=mark_topics_completed_activity"
  )
  @Iteration(
    "mathExpressionParser",
    "name=MATH_EXPRESSION_PARSER_ACTIVITY",
    "expNameStr=math_expression_parser_activity"
  )
  @Iteration(
    "markChaptersCompleted",
    "name=MARK_CHAPTERS_COMPLETED_ACTIVITY",
    "expNameStr=mark_chapters_completed_activity"
  )
  @Iteration(
    "markStoriesCompleted",
    "name=MARK_STORIES_COMPLETED_ACTIVITY",
    "expNameStr=mark_stories_completed_activity"
  )
  @Iteration(
    "forceNetworkType",
    "name=FORCE_NETWORK_TYPE_ACTIVITY",
    "expNameStr=force_network_type_activity"
  )
  @Iteration("adminPin", "name=ADMIN_PIN_ACTIVITY", "expNameStr=admin_pin_activity")
  @Iteration("policies", "name=POLICIES_ACTIVITY", "expNameStr=policies_activity")
  @Iteration("unspecified", "name=SCREEN_NAME_UNSPECIFIED", "expNameStr=screen_name_unspecified")
  @Iteration("foreground", "name=FOREGROUND_SCREEN", "expNameStr=foreground_screen")
  fun testMetricsBundle_addScreenName_verifyConversionToCorrectAnalyticalName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()
    val performanceMetricLog =
      createPerformanceMetricLog(currentScreen = screenName)

    eventBundleCreator.fillPerformanceMetricsEventBundle(performanceMetricLog, bundle)

    assertThat(bundle).string("current_screen").isEqualTo(expNameStr)
  }

  private fun createEventLog(
    timestamp: Long = TEST_TIMESTAMP_1,
    priority: EventLog.Priority = ESSENTIAL,
    context: EventLog.Context = EventLog.Context.getDefaultInstance(),
    appLanguageSelection: AppLanguageSelection = AppLanguageSelection.getDefaultInstance(),
    writtenTranslationLanguageSelection: WrittenTranslationLanguageSelection =
      WrittenTranslationLanguageSelection.getDefaultInstance(),
    audioTranslationLanguageSelection: AudioTranslationLanguageSelection =
      AudioTranslationLanguageSelection.getDefaultInstance()
  ) = EventLog.newBuilder().apply {
    this.timestamp = timestamp
    this.priority = priority
    this.appLanguageSelection = appLanguageSelection
    this.writtenTranslationLanguageSelection = writtenTranslationLanguageSelection
    this.audioTranslationLanguageSelection = audioTranslationLanguageSelection
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

  private fun createCloseRevisionCard(
    revisionCardContext: RevisionCardContext = createRevisionCardContext()
  ) = createEventContext(revisionCardContext, EventContextBuilder::setCloseRevisionCard)

  private fun createStartCardContext(cardContext: CardContext = createCardContext()) =
    createEventContext(cardContext, EventContextBuilder::setStartCardContext)

  private fun createEndCardContext(cardContext: CardContext = createCardContext()) =
    createEventContext(cardContext, EventContextBuilder::setEndCardContext)

  private fun createHintUnlockedContext(hintContext: HintContext = createHintContext()) =
    createEventContext(hintContext, EventContextBuilder::setHintUnlockedContext)

  private fun createRevealHintContext(hintContext: HintContext = createHintContext()) =
    createEventContext(hintContext, EventContextBuilder::setRevealHintContext)

  private fun createViewExistingHintContext(hintContext: HintContext = createHintContext()) =
    createEventContext(hintContext, EventContextBuilder::setViewExistingHintContext)

  private fun createSolutionUnlockedContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setSolutionUnlockedContext)

  private fun createRevealSolutionContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setRevealSolutionContext)

  private fun createViewExistingSolutionContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setViewExistingSolutionContext)

  private fun createSubmitAnswerContext(
    submitAnswerContext: SubmitAnswerContext = createSubmitAnswerContextDetails()
  ) = createEventContext(submitAnswerContext, EventContextBuilder::setSubmitAnswerContext)

  private fun createPlayVoiceOverContext(
    playVoiceOverContext: VoiceoverActionContext = createPlayVoiceOverContextDetails()
  ) = createEventContext(playVoiceOverContext, EventContextBuilder::setPlayVoiceOverContext)

  private fun createPauseVoiceOverContext(
    pauseVoiceOverContext: VoiceoverActionContext = createPauseVoiceOverContextDetails()
  ) = createEventContext(pauseVoiceOverContext, EventContextBuilder::setPauseVoiceOverContext)

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

  private fun createReachInvestedEngagementContext(
    explorationContext: ExplorationContext = createExplorationContext()
  ) = createEventContext(explorationContext, EventContextBuilder::setReachInvestedEngagement)

  private fun createSwitchInLessonLanguageContext(
    switchLanguageContext: SwitchInLessonLanguageEventContext =
      createSwitchInLessonLanguageEventContext(),
  ) = createEventContext(switchLanguageContext, EventContextBuilder::setSwitchInLessonLanguage)

  private fun createInstallationIdForFailedAnalyticsLogContext(
    installationId: String = TEST_INSTALLATION_ID
  ) = createEventContext(installationId, EventContextBuilder::setInstallIdForFailedAnalyticsLog)

  private fun <T> createEventContext(
    value: T,
    setter: EventContextBuilder.(T) -> EventContextBuilder
  ) = EventLog.Context.newBuilder().setter(value).build()

  private fun createExplorationContext(
    classroomId: String = TEST_CLASSROOM_ID,
    topicId: String = TEST_TOPIC_ID,
    storyId: String = TEST_STORY_ID,
    explorationId: String = TEST_EXPLORATION_ID,
    sessionId: String = TEST_LEARNER_SESSION_ID,
    explorationVersion: Int = TEST_EXPLORATION_VERSION,
    stateName: String = TEST_STATE_NAME,
    learnerDetails: LearnerDetailsContext = createLearnerDetailsContext()
  ) = ExplorationContext.newBuilder().apply {
    this.classroomId = classroomId
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
    contentId: String = TEST_CONTENT_ID,
    languageCode: String = TEST_LANGUAGE_CODE
  ) = VoiceoverActionContext.newBuilder().apply {
    this.explorationDetails = explorationDetails
    this.contentId = contentId
    this.languageCode = languageCode
  }.build()

  private fun createPauseVoiceOverContextDetails(
    explorationDetails: ExplorationContext = createExplorationContext(),
    contentId: String = TEST_CONTENT_ID,
    languageCode: String = TEST_LANGUAGE_CODE
  ) = VoiceoverActionContext.newBuilder().apply {
    this.explorationDetails = explorationDetails
    this.contentId = contentId
    this.languageCode = languageCode
  }.build()

  private fun createSwitchInLessonLanguageEventContext(
    explorationDetails: ExplorationContext = createExplorationContext(),
    switchFromLanguage: OppiaLanguage = OppiaLanguage.ENGLISH,
    switchToLanguage: OppiaLanguage = OppiaLanguage.SWAHILI
  ) = SwitchInLessonLanguageEventContext.newBuilder().apply {
    this.explorationDetails = explorationDetails
    this.switchFromLanguage = switchFromLanguage
    this.switchToLanguage = switchToLanguage
  }.build()

  private fun registerTestApplication(context: Context) {
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
    packageInfo.longVersionCode = TEST_APP_VERSION_CODE
    packageManager.installPackage(packageInfo)
  }

  private fun createApkSizeLoggableMetric() = LoggableMetric.newBuilder()
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
    TestModule.enableLoggingLearnerStudyIds = false
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponentWithLearnerAnalyticsStudy() {
    TestModule.enableLoggingLearnerStudyIds = true
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    registerTestApplication(context)
  }

  private fun executeInPreviousAppInstance(block: (TestApplicationComponent) -> Unit) {
    val testApplication = TestApplication()
    // The true application is hooked as a base context. This is to make sure the new application
    // can behave like a real Android application class (per Robolectric) without having a shared
    // Dagger dependency graph with the application under test.
    testApplication.attachBaseContext(ApplicationProvider.getApplicationContext())
    block(
      DaggerEventBundleCreatorTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build()
        .also { registerTestApplication(testApplication) }
    )
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    internal companion object {
      // This is expected to be off by default, so this helps the tests above confirm that the
      // feature's default value is, indeed, off.
      var enableLoggingLearnerStudyIds = LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
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
    @EnableLoggingLearnerStudyIds
    fun provideLoggingLearnerStudyIds(): PlatformParameterValue<Boolean> {
      // Snapshot the value so that it doesn't change between injection and use.
      val enableFeature = enableLoggingLearnerStudyIds
      return PlatformParameterValue.createDefaultParameter(
        defaultValue = enableFeature
      )
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

    fun getEventBundleCreator(): EventBundleCreator

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

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }
  }
}
