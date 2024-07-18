package org.oppia.android.util.logging

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
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

// TODO(#4419): Remove this test suite post-Kenya user study.
/**
 * Variant of [EventBundleCreatorTest] but for testing behaviors specific to alpha builds of the app
 * intended for users in the Kenya user study.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = KenyaAlphaEventBundleCreatorTest.TestApplication::class,
  sdk = [TEST_ANDROID_SDK_VERSION]
)
class KenyaAlphaEventBundleCreatorTest {
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
  }

  @Inject lateinit var context: Context
  @Inject lateinit var eventBundleCreator: EventBundleCreator

  @After
  fun tearDown() {
    TestModule.enableLoggingLearnerStudyIds = LOGGING_LEARNER_STUDY_IDS_DEFAULT_VALUE
  }

  @Test
  fun testFillEventBundle_defaultEvent_defaultsBundleAndReturnsUnknownActivityContext() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val typeName = eventBundleCreator.fillEventBundle(EventLog.getDefaultInstance(), bundle)

    assertThat(typeName).isEqualTo("unknown_activity_context")
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(0)
    assertThat(bundle).string("priority").isEqualTo("unspecified_priority")
    assertThat(bundle).integer("event_type").isEqualTo(ACTIVITYCONTEXT_NOT_SET.number)
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

    assertThat(typeName).isEqualTo("unknown_activity_context")
    assertThat(bundle).hasSize(11)
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
  fun testFillEventBundle_openExpActivityEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenExplorationActivity())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_exploration_activity")
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
    assertThat(typeName).isEqualTo("open_exploration_activity")
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
    assertThat(typeName).isEqualTo("open_info_tab")
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
    assertThat(typeName).isEqualTo("open_lessons_tab")
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
  fun testFillEventBundle_openPracticeTabContextEvent_fillsAllFieldsInBundleAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createOpenPracticeTab())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("open_practice_tab")
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
    assertThat(typeName).isEqualTo("open_revision_tab")
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
    assertThat(typeName).isEqualTo("open_question_player")
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
    assertThat(typeName).isEqualTo("open_story_activity")
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
    assertThat(typeName).isEqualTo("start_card_context")
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
    assertThat(typeName).isEqualTo("start_card_context")
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
    assertThat(typeName).isEqualTo("end_card_context")
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
    assertThat(typeName).isEqualTo("end_card_context")
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
    assertThat(typeName).isEqualTo("hint_offered_context")
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
    assertThat(typeName).isEqualTo("hint_offered_context")
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
    assertThat(typeName).isEqualTo("reveal_hint_context")
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
    assertThat(typeName).isEqualTo("reveal_hint_context")
    assertThat(bundle).hasSize(21)
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
    assertThat(bundle).string("ed_ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ed_ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_viewExistingHintEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createViewExistingHintContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("view_existing_hint_context")
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
    assertThat(typeName).isEqualTo("view_existing_hint_context")
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
    assertThat(typeName).isEqualTo("solution_offered_context")
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
    assertThat(typeName).isEqualTo("solution_offered_context")
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
  fun testFillEventBundle_revealSolutionEvent_studyOff_fillsOnlyNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createRevealSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_solution_context")
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
  fun testFillEventBundle_revealSolutionEvent_studyOn_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponentWithLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createRevealSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("reveal_solution_context")
    assertThat(bundle).hasSize(20)
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
    assertThat(bundle).string("ld_learner_id").isEqualTo(TEST_LEARNER_ID)
    assertThat(bundle).string("ld_install_id").isEqualTo(TEST_INSTALLATION_ID)
  }

  @Test
  fun testFillEventBundle_viewExistingSolutionEvent_studyOff_fillsNonSensitiveFieldsAndRetsName() {
    setUpTestApplicationComponentWithoutLearnerAnalyticsStudy()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createViewExistingSolutionContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("view_existing_solution_context")
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
    assertThat(typeName).isEqualTo("view_existing_solution_context")
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
    assertThat(typeName).isEqualTo("submit_answer_context")
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
    assertThat(typeName).isEqualTo("submit_answer_context")
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
    assertThat(typeName).isEqualTo("play_voice_over_context")
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
    assertThat(typeName).isEqualTo("play_voice_over_context")
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
    assertThat(typeName).isEqualTo("pause_voice_over_context")
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
    assertThat(typeName).isEqualTo("pause_voice_over_context")
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
    assertThat(typeName).isEqualTo("app_in_background_context")
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
    assertThat(typeName).isEqualTo("app_in_background_context")
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
    assertThat(typeName).isEqualTo("app_in_foreground_context")
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
    assertThat(typeName).isEqualTo("app_in_foreground_context")
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
    assertThat(typeName).isEqualTo("exit_exploration_context")
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
    assertThat(typeName).isEqualTo("exit_exploration_context")
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
    assertThat(typeName).isEqualTo("finish_exploration_context")
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
    assertThat(typeName).isEqualTo("finish_exploration_context")
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
    assertThat(typeName).isEqualTo("resume_exploration_context")
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
    assertThat(typeName).isEqualTo("resume_exploration_context")
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
    assertThat(typeName).isEqualTo("start_over_exploration_context")
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
    assertThat(typeName).isEqualTo("start_over_exploration_context")
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
    assertThat(typeName).isEqualTo("delete_profile_context")
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
    assertThat(typeName).isEqualTo("delete_profile_context")
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
    assertThat(typeName).isEqualTo("open_home")
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
    assertThat(typeName).isEqualTo("open_profile_chooser")
    assertThat(bundle).hasSize(11)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(OPEN_PROFILE_CHOOSER.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testFillEventBundle_switchInLessonLanguageEvent_studyOff_fillsAllFieldsAndReturnsName() {
    setUpTestApplicationComponent()
    val bundle = Bundle()

    val eventLog = createEventLog(context = createSwitchInLessonLanguageContext())

    val typeName = eventBundleCreator.fillEventBundle(eventLog, bundle)
    assertThat(typeName).isEqualTo("switch_in_lesson_language")
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
    assertThat(typeName).isEqualTo("switch_in_lesson_language")
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
    assertThat(typeName).isEqualTo("failed_analytics_log")
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
    assertThat(typeName).isEqualTo("failed_analytics_log")
    assertThat(bundle).hasSize(12)
    assertThat(bundle).longInt("timestamp").isEqualTo(TEST_TIMESTAMP_1)
    assertThat(bundle).string("priority").isEqualTo("essential")
    assertThat(bundle).integer("event_type").isEqualTo(INSTALL_ID_FOR_FAILED_ANALYTICS_LOG.number)
    assertThat(bundle).integer("android_sdk").isEqualTo(TEST_ANDROID_SDK_VERSION)
    assertThat(bundle).string("app_version_name").isEqualTo(TEST_APP_VERSION_NAME)
    assertThat(bundle).integer("app_version_code").isEqualTo(TEST_APP_VERSION_CODE)
    assertThat(bundle).string("install_id").isEqualTo(TEST_INSTALLATION_ID)
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
    packageInfo.longVersionCode = TEST_APP_VERSION_CODE
    packageManager.installPackage(packageInfo)
  }

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
    registerTestApplication()
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
  @Component(modules = [TestModule::class, KenyaAlphaEventLoggingConfigurationModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: KenyaAlphaEventBundleCreatorTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerKenyaAlphaEventBundleCreatorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: KenyaAlphaEventBundleCreatorTest) {
      component.inject(test)
    }
  }
}
