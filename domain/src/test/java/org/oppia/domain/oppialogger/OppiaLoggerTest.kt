package org.oppia.domain.oppialogger

import org.junit.Test
import org.oppia.app.model.EventLog
import org.oppia.domain.oppialogger.analytics.TEST_EXPLORATION_ID
import org.oppia.domain.oppialogger.analytics.TEST_QUESTION_ID
import org.oppia.domain.oppialogger.analytics.TEST_SKILL_ID
import org.oppia.domain.oppialogger.analytics.TEST_SKILL_LIST_ID
import org.oppia.domain.oppialogger.analytics.TEST_STORY_ID
import org.oppia.domain.oppialogger.analytics.TEST_SUB_TOPIC_ID
import org.oppia.domain.oppialogger.analytics.TEST_TOPIC_ID

class OppiaLoggerTest {
  @Test
  fun testController_createExplorationContext_returnsCorrectExplorationContext() {
    val eventContext = oppiaLogger.createExplorationContext(
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT)
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

    assertThat(eventContext.activityContextCase).isEqualTo(EventLog.Context.ActivityContextCase.QUESTION_CONTEXT)
    assertThat(eventContext.questionContext.questionId).matches(TEST_QUESTION_ID)
    assertThat(eventContext.questionContext.skillIdList)
      .containsAllIn(arrayOf(TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID))
  }

  @Test
  fun testController_createStoryContext_returnsCorrectStoryContext() {
    val eventContext = oppiaLogger.createStoryContext(
      TEST_TOPIC_ID,
      TEST_STORY_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(EventLog.Context.ActivityContextCase.STORY_CONTEXT)
    assertThat(eventContext.storyContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.storyContext.storyId).matches(TEST_STORY_ID)
  }

  @Test
  fun testController_createTopicContext_returnsCorrectTopicContext() {
    val eventContext = oppiaLogger.createTopicContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(EventLog.Context.ActivityContextCase.TOPIC_CONTEXT)
    assertThat(eventContext.topicContext.topicId).matches(TEST_TOPIC_ID)
  }

  @Test
  fun testController_createConceptCardContext_returnsCorrectConceptCardContext() {
    val eventContext = oppiaLogger.createConceptCardContext(TEST_SKILL_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(EventLog.Context.ActivityContextCase.CONCEPT_CARD_CONTEXT)
    assertThat(eventContext.conceptCardContext.skillId).matches(TEST_SKILL_ID)
  }

  @Test
  fun testController_createRevisionCardContext_returnsCorrectRevisionCardContext() {
    val eventContext =
      oppiaLogger.createRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(EventLog.Context.ActivityContextCase.REVISION_CARD_CONTEXT)
    assertThat(eventContext.revisionCardContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.revisionCardContext.subTopicId).matches(TEST_SUB_TOPIC_ID)
  }
}