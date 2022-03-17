package org.oppia.android.util.logging

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
import org.oppia.android.app.model.EventLog
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

private const val TEST_TIMESTAMP = 1556094120000
private const val TEST_TOPIC_ID = "test_topicId"
private const val TEST_STORY_ID = "test_storyId"
private const val TEST_EXPLORATION_ID = "test_explorationId"
private const val TEST_QUESTION_ID = "test_questionId"
private const val TEST_SKILL_ID_ONE = "test_skillId_one"
private const val TEST_SKILL_ID_TWO = "test_skillId_two"
private const val TEST_SUB_TOPIC_ID = 1
private const val TEST_SKILL_ID = "test_skillId"
private const val TEST_LEARNER_ID = "test_learnerId"
private const val TEST_DEVICE_ID = "test_deviceId"
private const val TEST_SESSION_ID = "test_sessionId"
private const val TEST_EXPLORATION_VERSION = "test_exploration_version"
private const val TEST_STATE_NAME = "test_state_name"
private const val TEST_HINT_INDEX = "test_hint_index"
private const val TEST_IS_ANSWER_CORRECT = true
private const val TEST_CONTENT_ID = "test_contentId"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class EventBundleCreatorTest {

  private val eventBundleCreator = EventBundleCreator()
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

  private val eventLogExplorationContext = EventLog.newBuilder()
    .setContext(
      EventLog.Context.newBuilder()
        .setOpenExplorationActivity(
          EventLog.ExplorationContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .setExplorationId(TEST_EXPLORATION_ID)
            .setStoryId(TEST_STORY_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogQuestionContext = EventLog.newBuilder()
    .setContext(
      EventLog.Context.newBuilder()
        .setOpenQuestionPlayer(
          EventLog.QuestionContext.newBuilder()
            .setQuestionId(TEST_QUESTION_ID)
            .addAllSkillId(listOf(TEST_SKILL_ID_ONE, TEST_SKILL_ID_TWO))
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogTopicContext = EventLog.newBuilder()
    .setContext(
      EventLog.Context.newBuilder()
        .setOpenInfoTab(
          EventLog.TopicContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogStoryContext = EventLog.newBuilder()
    .setContext(
      EventLog.Context.newBuilder()
        .setOpenStoryActivity(
          EventLog.StoryContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .setStoryId(TEST_STORY_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogConceptCardContext = EventLog.newBuilder()
    .setContext(
      EventLog.Context.newBuilder()
        .setOpenConceptCard(
          EventLog.ConceptCardContext.newBuilder()
            .setSkillId(TEST_SKILL_ID_ONE)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogRevisionCardContext = EventLog.newBuilder()
    .setContext(
      EventLog.Context.newBuilder()
        .setOpenRevisionCard(
          EventLog.RevisionCardContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .setSubTopicId(TEST_SUB_TOPIC_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogStartCardContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.START_CARD)
    .setContext(
      EventLog.Context.newBuilder()
        .setStartCardContext(
          EventLog.StartCardContext.newBuilder()
            .setSkillId(TEST_SKILL_ID)
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogEndCardContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.END_CARD)
    .setContext(
      EventLog.Context.newBuilder()
        .setEndCardContext(
          EventLog.EndCardContext.newBuilder()
            .setSkillId(TEST_SKILL_ID)
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogHintOfferedContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.HINT_OFFERED)
    .setContext(
      EventLog.Context.newBuilder()
        .setHintOfferedContext(
          EventLog.HintOfferedContext.newBuilder()
            .setHintIndex(TEST_HINT_INDEX)
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogAccessHintContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.ACCESS_HINT)
    .setContext(
      EventLog.Context.newBuilder()
        .setAccessHintContext(
          EventLog.AccessHintContext.newBuilder()
            .setHintIndex(TEST_HINT_INDEX)
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogSolutionOfferedContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.SOLUTION_OFFERED)
    .setContext(
      EventLog.Context.newBuilder()
        .setSolutionOfferedContext(
          EventLog.SolutionOfferedContext.newBuilder()
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogAccessSolutionContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.ACCESS_SOLUTION)
    .setContext(
      EventLog.Context.newBuilder()
        .setAccessSolutionContext(
          EventLog.AccessSolutionContext.newBuilder()
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogSubmitAnswerContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.SUBMIT_ANSWER)
    .setContext(
      EventLog.Context.newBuilder()
        .setSubmitAnswerContext(
          EventLog.SubmitAnswerContext.newBuilder()
            .setIsAnswerCorrect(TEST_IS_ANSWER_CORRECT)
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogPlayVoiceOverContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.PLAY_VOICE_OVER)
    .setContext(
      EventLog.Context.newBuilder()
        .setPlayVoiceOverContext(
          EventLog.PlayVoiceOverContext.newBuilder()
            .setContentId(TEST_CONTENT_ID)
            .setGenericData(GENERIC_DATA)
            .setExplorationData(EXPLORATION_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogAppInBackgroundContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.APP_IN_BACKGROUND)
    .setContext(
      EventLog.Context.newBuilder()
        .setAppInBackgroundContext(
          EventLog.AppInBackgroundContext.newBuilder()
            .setGenericData(GENERIC_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogAppInForegroundContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.APP_IN_FOREGROUND)
    .setContext(
      EventLog.Context.newBuilder()
        .setAppInForegroundContext(
          EventLog.AppInForegroundContext.newBuilder()
            .setGenericData(GENERIC_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogExitExplorationContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EXIT_EXPLORATION)
    .setContext(
      EventLog.Context.newBuilder()
        .setExitExplorationContext(
          EventLog.ExitExplorationContext.newBuilder()
            .setExplorationData(EXPLORATION_DATA)
            .setGenericData(GENERIC_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogFinishExplorationContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.FINISH_EXPLORATION)
    .setContext(
      EventLog.Context.newBuilder()
        .setFinishExplorationContext(
          EventLog.FinishExplorationContext.newBuilder()
            .setExplorationData(EXPLORATION_DATA)
            .setGenericData(GENERIC_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogResumeExplorationContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.RESUME_EXPLORATION)
    .setContext(
      EventLog.Context.newBuilder()
        .setResumeExplorationContext(
          EventLog.ResumeExplorationContext.newBuilder()
            .setGenericData(GENERIC_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogStartOverExplorationContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.START_OVER_EXPLORATION)
    .setContext(
      EventLog.Context.newBuilder()
        .setStartOverExplorationContext(
          EventLog.StartOverExplorationContext.newBuilder()
            .setGenericData(GENERIC_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogDeleteProfileContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.DELETE_PROFILE)
    .setContext(
      EventLog.Context.newBuilder()
        .setDeleteProfileContext(
          EventLog.DeleteProfileContext.newBuilder()
            .setGenericData(GENERIC_DATA)
            .build()
        ).build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogNoContext = EventLog.newBuilder()
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testBundleCreation_logEvent_withExplorationContext_isSuccessful() {
    val eventBundle = EventBundleCreator().createEventBundle(eventLogExplorationContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(STORY_ID_KEY)).isEqualTo(TEST_STORY_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY)).isEqualTo(TEST_EXPLORATION_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withQuestionContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogQuestionContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(QUESTION_ID_KEY)).isEqualTo(TEST_QUESTION_ID)
    assertThat(eventBundle.get(SKILL_ID_KEY))
      .isEqualTo(listOf(TEST_SKILL_ID_ONE, TEST_SKILL_ID_TWO).joinToString())
  }

  @Test
  fun testBundleCreation_logEvent_withTopicContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogTopicContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withStoryContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogStoryContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(STORY_ID_KEY)).isEqualTo(TEST_STORY_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withConceptCardContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogConceptCardContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(SKILL_ID_KEY)).isEqualTo(TEST_SKILL_ID_ONE)
  }

  @Test
  fun testBundleCreation_logEvent_withRevisionCardContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogRevisionCardContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(SUB_TOPIC_ID_KEY)).isEqualTo(TEST_SUB_TOPIC_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withStartCardContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogStartCardContext)

    assertThat(eventBundle.get(START_CARD_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(SKILL_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SKILL_ID)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withEndCardContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogEndCardContext)

    assertThat(eventBundle.get(END_CARD_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(SKILL_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SKILL_ID)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withHintOfferedContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogHintOfferedContext)

    assertThat(eventBundle.get(HINT_OFFERED_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(HINT_INDEX_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_HINT_INDEX)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withAccessHintContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogAccessHintContext)

    assertThat(eventBundle.get(ACCESS_HINT_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(HINT_INDEX_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_HINT_INDEX)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withSolutionOfferedContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogSolutionOfferedContext)

    assertThat(eventBundle.get(SOLUTION_OFFERED_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withAccessSolutionContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogAccessSolutionContext)

    assertThat(eventBundle.get(ACCESS_SOLUTION_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withSubmitAnswerContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogSubmitAnswerContext)

    assertThat(eventBundle.get(SUBMIT_ANSWER_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(ANSWER_LABEL_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_IS_ANSWER_CORRECT.toString()
    )
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withPlayVoiceOverContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogPlayVoiceOverContext)

    assertThat(eventBundle.get(PLAY_VOICE_OVER_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(CONTENT_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_CONTENT_ID)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withAppInBackgroundContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogAppInBackgroundContext)

    assertThat(eventBundle.get(APP_IN_BACKGROUND_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withAppInForegroundContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogAppInForegroundContext)

    assertThat(eventBundle.get(APP_IN_FOREGROUND_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withExitExplorationContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogExitExplorationContext)

    assertThat(eventBundle.get(EXIT_EXPLORATION_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
    assertThat(eventBundle.get(SESSION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_SESSION_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(eventBundle.get(EXPLORATION_VERSION_KEY_LEARNER_ANALYTICS)).isEqualTo(
      TEST_EXPLORATION_VERSION
    )
    assertThat(eventBundle.get(STATE_NAME_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_STATE_NAME)
  }

  @Test
  fun testBundleCreation_logEvent_withFinishExplorationContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogFinishExplorationContext)

    assertThat(eventBundle.get(FINISH_EXPLORATION_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withResumeExplorationContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogResumeExplorationContext)

    assertThat(eventBundle.get(RESUME_EXPLORATION_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withStartOverExplorationContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogStartOverExplorationContext)

    assertThat(eventBundle.get(START_OVER_EXPLORATION_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withDeleteProfileContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogDeleteProfileContext)

    assertThat(eventBundle.get(DELETE_PROFILE_TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(DEVICE_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_DEVICE_ID)
    assertThat(eventBundle.get(LEARNER_ID_KEY_LEARNER_ANALYTICS)).isEqualTo(TEST_LEARNER_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withNoContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogNoContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
  }

  private fun setUpTestApplicationComponent() {
    DaggerEventBundleCreatorTest_TestApplicationComponent.builder()
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(eventBundleCreatorTest: EventBundleCreatorTest)
  }
}
