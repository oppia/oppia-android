package org.oppia.android.testing.logging

import com.google.common.truth.BooleanSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.IterableSubject
import com.google.common.truth.LongSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_SOLUTION_CONTEXT
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
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REACH_INVESTED_ENGAGEMENT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SOLUTION_OFFERED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_OVER_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SUBMIT_ANSWER_CONTEXT

// TODO(#4272): Add tests for this class.

/**
 * Truth subject for verifying properties of [EventLog]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying [EventLog]
 * proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate") // TODO(#4272): Remove suppression when tested.
class EventLogSubject private constructor(
  metadata: FailureMetadata,
  private val actual: EventLog
) : LiteProtoSubject(metadata, actual) {
  /**
   * Returns a [LongSubject] to test [EventLog.getTimestamp].
   *
   * This method never fails since the underlying property defaults to 0 if it's not defined in the
   * log.
   */
  fun hasTimestampThat(): LongSubject = assertThat(actual.timestamp)

  /**
   * Verifies that the [EventLog] under test has priority [EventLog.Priority.ESSENTIAL] per
   * [EventLog.getPriority].
   */
  fun isEssentialPriority() {
    assertThat(actual.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
  }

  /**
   * Verifies that the [EventLog] under test has priority [EventLog.Priority.OPTIONAL] per
   * [EventLog.getPriority].
   */
  fun isOptionalPriority() {
    assertThat(actual.priority).isEqualTo(EventLog.Priority.OPTIONAL)
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [OPEN_EXPLORATION_ACTIVITY] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenExplorationActivityContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_EXPLORATION_ACTIVITY)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenExplorationActivityContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   *
   * See the documentation for this method's other overload for details on a variant of this method
   * that allows for easier verification of constituent properties of the log's context.
   */
  fun hasOpenExplorationActivityContextThat(): ExplorationContextSubject {
    hasOpenExplorationActivityContext()
    return ExplorationContextSubject.assertThat(actual.context.openExplorationActivity)
  }

  /**
   * Verifies the [EventLog]'s context in the same way as [hasOpenExplorationActivityContextThat]
   * and executes the specified [block] with the resulting test subject as the receiver.
   *
   * This can be useful to verify multiple underlying properties of the context, e.g.:
   *
   * ```kotlin
   * assertThat(someEventLog).hasOpenExplorationActivityContextThat {
   *   hasTopicIdThat().isEqualTo("expected_topic_id")
   *   hasStateNameThat().isEqualTo("expected_state_name")
   *   ...
   * }
   * ```
   *
   * This is logically equivalent to the following (but is meant as a more Truthy & readable
   * alternative):
   *
   * ```kotlin
   * assertThat(someEventLog).apply {
   *   hasTopicIdThat().isEqualTo("expected_topic_id")
   *   ...
   * }
   * ```
   */
  fun hasOpenExplorationActivityContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasOpenExplorationActivityContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_INFO_TAB] (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenInfoTabContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_INFO_TAB)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenInfoTabContext] and returns a
   * [TopicContextSubject] to test the corresponding context.
   */
  fun hasOpenInfoTabContextThat(): TopicContextSubject {
    hasOpenInfoTabContext()
    return TopicContextSubject.assertThat(actual.context.openInfoTab)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenInfoTabContextThat].
   */
  fun hasOpenInfoTabContextThat(block: TopicContextSubject.() -> Unit) {
    hasOpenInfoTabContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_LESSONS_TAB] (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenLessonsTabContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_LESSONS_TAB)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenLessonsTabContext] and returns a
   * [TopicContextSubject] to test the corresponding context.
   */
  fun hasOpenLessonsTabContextThat(): TopicContextSubject {
    hasOpenLessonsTabContext()
    return TopicContextSubject.assertThat(actual.context.openLessonsTab)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenLessonsTabContextThat].
   */
  fun hasOpenLessonsTabContextThat(block: TopicContextSubject.() -> Unit) {
    hasOpenLessonsTabContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_PRACTICE_TAB] (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenPracticeTabContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_PRACTICE_TAB)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenPracticeTabContext] and returns a
   * [TopicContextSubject] to test the corresponding context.
   */
  fun hasOpenPracticeTabContextThat(): TopicContextSubject {
    hasOpenPracticeTabContext()
    return TopicContextSubject.assertThat(actual.context.openPracticeTab)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenPracticeTabContextThat].
   */
  fun hasOpenPracticeTabContextThat(block: TopicContextSubject.() -> Unit) {
    hasOpenPracticeTabContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_REVISION_TAB] (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenRevisionTabContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_REVISION_TAB)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenRevisionTabContext] and returns a
   * [TopicContextSubject] to test the corresponding context.
   */
  fun hasOpenRevisionTabContextThat(): TopicContextSubject {
    hasOpenRevisionTabContext()
    return TopicContextSubject.assertThat(actual.context.openRevisionTab)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenRevisionTabContextThat].
   */
  fun hasOpenRevisionTabContextThat(block: TopicContextSubject.() -> Unit) {
    hasOpenRevisionTabContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_QUESTION_PLAYER]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenQuestionPlayerContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_QUESTION_PLAYER)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenQuestionPlayerContext] and returns a
   * [QuestionContextSubject] to test the corresponding context.
   */
  fun hasOpenQuestionPlayerContextThat(): QuestionContextSubject {
    hasOpenQuestionPlayerContext()
    return QuestionContextSubject.assertThat(actual.context.openQuestionPlayer)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenQuestionPlayerContextThat].
   */
  fun hasOpenQuestionPlayerContextThat(block: QuestionContextSubject.() -> Unit) {
    hasOpenQuestionPlayerContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_STORY_ACTIVITY]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenStoryActivityContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_STORY_ACTIVITY)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenStoryActivityContext] and returns a
   * [StoryContextSubject] to test the corresponding context.
   */
  fun hasOpenStoryActivityContextThat(): StoryContextSubject {
    hasOpenStoryActivityContext()
    return StoryContextSubject.assertThat(actual.context.openStoryActivity)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenStoryActivityContextThat].
   */
  fun hasOpenStoryActivityContextThat(block: StoryContextSubject.() -> Unit) {
    hasOpenStoryActivityContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_CONCEPT_CARD] (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenConceptCardContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_CONCEPT_CARD)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenConceptCardContext] and returns a
   * [ConceptCardContextSubject] to test the corresponding context.
   */
  fun hasOpenConceptCardContextThat(): ConceptCardContextSubject {
    hasOpenConceptCardContext()
    return ConceptCardContextSubject.assertThat(actual.context.openConceptCard)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenConceptCardContextThat].
   */
  fun hasOpenConceptCardContextThat(block: ConceptCardContextSubject.() -> Unit) {
    hasOpenConceptCardContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_REVISION_CARD]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenRevisionCardContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_REVISION_CARD)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenRevisionCardContext] and returns a
   * [RevisionCardContextSubject] to test the corresponding context.
   */
  fun hasOpenRevisionCardContextThat(): RevisionCardContextSubject {
    hasOpenRevisionCardContext()
    return RevisionCardContextSubject.assertThat(actual.context.openRevisionCard)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasOpenRevisionCardContextThat].
   */
  fun hasOpenRevisionCardContextThat(block: RevisionCardContextSubject.() -> Unit) {
    hasOpenRevisionCardContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [START_CARD_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasStartCardContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(START_CARD_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasStartCardContext] and returns a [CardContextSubject]
   * to test the corresponding context.
   */
  fun hasStartCardContextThat(): CardContextSubject {
    hasStartCardContext()
    return CardContextSubject.assertThat(actual.context.startCardContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasStartCardContextThat].
   */
  fun hasStartCardContextThat(block: CardContextSubject.() -> Unit) {
    hasStartCardContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [END_CARD_CONTEXT] (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun hasEndCardContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(END_CARD_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasEndCardContext] and returns a [CardContextSubject] to
   * test the corresponding context.
   */
  fun hasEndCardContextThat(): CardContextSubject {
    hasEndCardContext()
    return CardContextSubject.assertThat(actual.context.endCardContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasEndCardContextThat].
   */
  fun hasEndCardContextThat(block: CardContextSubject.() -> Unit) {
    hasEndCardContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [HINT_OFFERED_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasHintOfferedContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(HINT_OFFERED_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasHintOfferedContext] and returns a
   * [HintContextSubject] to test the corresponding context.
   */
  fun hasHintOfferedContextThat(): HintContextSubject {
    hasHintOfferedContext()
    return HintContextSubject.assertThat(actual.context.hintOfferedContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasHintOfferedContextThat].
   */
  fun hasHintOfferedContextThat(block: HintContextSubject.() -> Unit) {
    hasHintOfferedContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [ACCESS_HINT_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasAccessHintContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(ACCESS_HINT_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasAccessHintContext] and returns a [HintContextSubject]
   * to test the corresponding context.
   */
  fun hasAccessHintContextThat(): HintContextSubject {
    hasAccessHintContext()
    return HintContextSubject.assertThat(actual.context.accessHintContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasAccessHintContextThat].
   */
  fun hasAccessHintContextThat(block: HintContextSubject.() -> Unit) {
    hasAccessHintContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [SOLUTION_OFFERED_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasSolutionOfferedContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(SOLUTION_OFFERED_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasSolutionOfferedContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasSolutionOfferedContextThat(): ExplorationContextSubject {
    hasSolutionOfferedContext()
    return ExplorationContextSubject.assertThat(actual.context.solutionOfferedContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasSolutionOfferedContextThat].
   */
  fun hasSolutionOfferedContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasSolutionOfferedContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [ACCESS_SOLUTION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasAccessSolutionContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(ACCESS_SOLUTION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasAccessSolutionContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasAccessSolutionContextThat(): ExplorationContextSubject {
    hasAccessSolutionContext()
    return ExplorationContextSubject.assertThat(actual.context.accessSolutionContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasAccessSolutionContextThat].
   */
  fun hasAccessSolutionContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasAccessSolutionContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [SUBMIT_ANSWER_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasSubmitAnswerContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(SUBMIT_ANSWER_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasSubmitAnswerContext] and returns a
   * [SubmitAnswerContextSubject] to test the corresponding context.
   */
  fun hasSubmitAnswerContextThat(): SubmitAnswerContextSubject {
    hasSubmitAnswerContext()
    return SubmitAnswerContextSubject.assertThat(actual.context.submitAnswerContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasSubmitAnswerContextThat].
   */
  fun hasSubmitAnswerContextThat(block: SubmitAnswerContextSubject.() -> Unit) {
    hasSubmitAnswerContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [PLAY_VOICE_OVER_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasPlayVoiceOverContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(PLAY_VOICE_OVER_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasPlayVoiceOverContext] and returns a
   * [PlayVoiceOverContextSubject] to test the corresponding context.
   */
  fun hasPlayVoiceOverContextThat(): PlayVoiceOverContextSubject {
    hasPlayVoiceOverContext()
    return PlayVoiceOverContextSubject.assertThat(actual.context.playVoiceOverContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasPlayVoiceOverContextThat].
   */
  fun hasPlayVoiceOverContextThat(block: PlayVoiceOverContextSubject.() -> Unit) {
    hasPlayVoiceOverContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [APP_IN_BACKGROUND_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasAppInBackgroundContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(APP_IN_BACKGROUND_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasAppInBackgroundContext] and returns a
   * [LearnerDetailsContextSubject] to test the corresponding context.
   */
  fun hasAppInBackgroundContextThat(): LearnerDetailsContextSubject {
    hasAppInBackgroundContext()
    return LearnerDetailsContextSubject.assertThat(actual.context.appInBackgroundContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasAppInBackgroundContextThat].
   */
  fun hasAppInBackgroundContextThat(block: LearnerDetailsContextSubject.() -> Unit) {
    hasAppInBackgroundContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [APP_IN_FOREGROUND_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasAppInForegroundContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(APP_IN_FOREGROUND_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasAppInForegroundContext] and returns a
   * [LearnerDetailsContextSubject] to test the corresponding context.
   */
  fun hasAppInForegroundContextThat(): LearnerDetailsContextSubject {
    hasAppInForegroundContext()
    return LearnerDetailsContextSubject.assertThat(actual.context.appInForegroundContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasAppInForegroundContextThat].
   */
  fun hasAppInForegroundContextThat(block: LearnerDetailsContextSubject.() -> Unit) {
    hasAppInForegroundContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [EXIT_EXPLORATION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasExitExplorationContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(EXIT_EXPLORATION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasExitExplorationContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasExitExplorationContextThat(): ExplorationContextSubject {
    hasExitExplorationContext()
    return ExplorationContextSubject.assertThat(actual.context.exitExplorationContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasExitExplorationContextThat].
   */
  fun hasExitExplorationContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasExitExplorationContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [FINISH_EXPLORATION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasFinishExplorationContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(FINISH_EXPLORATION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasFinishExplorationContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasFinishExplorationContextThat(): ExplorationContextSubject {
    hasFinishExplorationContext()
    return ExplorationContextSubject.assertThat(actual.context.finishExplorationContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasFinishExplorationContextThat].
   */
  fun hasFinishExplorationContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasFinishExplorationContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [RESUME_EXPLORATION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasResumeExplorationContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(RESUME_EXPLORATION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasResumeExplorationContext] and returns a
   * [LearnerDetailsContextSubject] to test the corresponding context.
   */
  fun hasResumeExplorationContextThat(): LearnerDetailsContextSubject {
    hasResumeExplorationContext()
    return LearnerDetailsContextSubject.assertThat(actual.context.resumeExplorationContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasResumeExplorationContextThat].
   */
  fun hasResumeExplorationContextThat(block: LearnerDetailsContextSubject.() -> Unit) {
    hasResumeExplorationContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [START_OVER_EXPLORATION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasStartOverExplorationContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(START_OVER_EXPLORATION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasStartOverExplorationContext] and returns a
   * [LearnerDetailsContextSubject] to test the corresponding context.
   */
  fun hasStartOverExplorationContextThat(): LearnerDetailsContextSubject {
    hasStartOverExplorationContext()
    return LearnerDetailsContextSubject.assertThat(actual.context.startOverExplorationContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasStartOverExplorationContextThat].
   */
  fun hasStartOverExplorationContextThat(block: LearnerDetailsContextSubject.() -> Unit) {
    hasStartOverExplorationContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [DELETE_PROFILE_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasDeleteProfileContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(DELETE_PROFILE_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasDeleteProfileContext] and returns a
   * [LearnerDetailsContextSubject] to test the corresponding context.
   */
  fun hasDeleteProfileContextThat(): LearnerDetailsContextSubject {
    hasDeleteProfileContext()
    return LearnerDetailsContextSubject.assertThat(actual.context.deleteProfileContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasDeleteProfileContextThat].
   */
  fun hasDeleteProfileContextThat(block: LearnerDetailsContextSubject.() -> Unit) {
    hasDeleteProfileContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_HOME] (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenHomeContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_HOME)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenHomeContext] and returns a [BooleanSubject] to
   * test the corresponding context.
   */
  fun hasOpenHomeContextThat(): BooleanSubject {
    hasOpenHomeContext()
    return assertThat(actual.context.openHome)
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [OPEN_PROFILE_CHOOSER]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasOpenProfileChooserContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPEN_PROFILE_CHOOSER)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOpenProfileChooserContext] and returns a
   * [BooleanSubject] to test the corresponding context.
   */
  fun hasOpenProfileChooserContextThat(): BooleanSubject {
    hasOpenProfileChooserContext()
    return assertThat(actual.context.openProfileChooser)
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [REACH_INVESTED_ENGAGEMENT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasReachedInvestedEngagementContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(REACH_INVESTED_ENGAGEMENT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasReachedInvestedEngagementContext] and returns a
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasReachedInvestedEngagementContextThat(): ExplorationContextSubject {
    hasReachedInvestedEngagementContext()
    return ExplorationContextSubject.assertThat(actual.context.reachInvestedEngagement)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned
   * by, [hasReachedInvestedEngagementContextThat].
   */
  fun hasReachedInvestedEngagementContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasReachedInvestedEngagementContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [INSTALL_ID_FOR_FAILED_ANALYTICS_LOG] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasInstallIdForAnalyticsLogFailure() {
    assertThat(actual.context.activityContextCase).isEqualTo(INSTALL_ID_FOR_FAILED_ANALYTICS_LOG)
  }

  /**
   * Verifies the [EventLog]'s context per [hasInstallIdForAnalyticsLogFailure] and returns a
   * [StringSubject] to test the corresponding context.
   */
  fun hasInstallIdForAnalyticsLogFailureThat(): StringSubject {
    hasInstallIdForAnalyticsLogFailure()
    return assertThat(actual.context.installIdForFailedAnalyticsLog)
  }

  /**
   * Truth subject for verifying properties of [EventLog.CardContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.CardContext] proto can be verified through inherited methods.
   *
   * Call [CardContextSubject.assertThat] to create the subject.
   */
  class CardContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.CardContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [ExplorationContextSubject] to test [EventLog.CardContext.getExplorationDetails].
     *
     * This method never fails since the underlying property defaults to empty proto if it's not
     * defined in the context.
     */
    fun hasExplorationDetailsThat(): ExplorationContextSubject =
      ExplorationContextSubject.assertThat(actual.explorationDetails)

    /**
     * Executes [block] in the context returned by [hasExplorationDetailsThat], similar to
     * [hasOpenExplorationActivityContextThat].
     */
    fun hasExplorationDetailsThat(block: ExplorationContextSubject.() -> Unit) {
      hasExplorationDetailsThat().block()
    }

    /**
     * Returns a [StringSubject] to test [EventLog.CardContext.getSkillId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSkillIdThat(): StringSubject = assertThat(actual.skillId)

    companion object {
      /**
       * Returns a new [CardContextSubject] to verify aspects of the specified
       * [EventLog.CardContext] value.
       */
      fun assertThat(actual: EventLog.CardContext): CardContextSubject =
        assertAbout(::CardContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.ConceptCardContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.ConceptCardContext] proto can be verified through inherited methods.
   *
   * Call [ConceptCardContextSubject.assertThat] to create the subject.
   */
  class ConceptCardContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.ConceptCardContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.ConceptCardContext.getSkillId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSkillIdThat(): StringSubject = assertThat(actual.skillId)

    companion object {
      /**
       * Returns a new [ConceptCardContextSubject] to verify aspects of the specified
       * [EventLog.ConceptCardContext] value.
       */
      fun assertThat(actual: EventLog.ConceptCardContext): ConceptCardContextSubject =
        assertAbout(::ConceptCardContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.ExplorationContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.ExplorationContext] proto can be verified through inherited methods.
   *
   * Call [ExplorationContextSubject.assertThat] to create the subject.
   */
  class ExplorationContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.ExplorationContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.ExplorationContext.getTopicId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasTopicIdThat(): StringSubject = assertThat(actual.topicId)

    /**
     * Returns a [StringSubject] to test [EventLog.ExplorationContext.getStoryId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasStoryIdThat(): StringSubject = assertThat(actual.storyId)

    /**
     * Returns a [StringSubject] to test [EventLog.ExplorationContext.getExplorationId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasExplorationIdThat(): StringSubject = assertThat(actual.explorationId)

    /**
     * Returns a [StringSubject] to test [EventLog.ExplorationContext.getSessionId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSessionIdThat(): StringSubject = assertThat(actual.sessionId)

    /**
     * Returns a [IntegerSubject] to test [EventLog.ExplorationContext.getExplorationVersion].
     *
     * This method never fails since the underlying property defaults to 0 if it's not defined in
     * the context.
     */
    fun hasVersionThat(): IntegerSubject = assertThat(actual.explorationVersion)

    /**
     * Returns a [StringSubject] to test [EventLog.ExplorationContext.getStateName].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasStateNameThat(): StringSubject = assertThat(actual.stateName)

    /**
     * Returns a [LearnerDetailsContextSubject] to test
     * [EventLog.ExplorationContext.getLearnerDetails].
     *
     * This method never fails since the underlying property defaults to empty proto if it's not
     * defined in the context.
     */
    fun hasLearnerDetailsThat(): LearnerDetailsContextSubject =
      LearnerDetailsContextSubject.assertThat(actual.learnerDetails)

    /**
     * Executes [block] in the context returned by [hasLearnerDetailsThat], similar to
     * [hasOpenExplorationActivityContextThat].
     */
    fun hasLearnerDetailsThat(block: LearnerDetailsContextSubject.() -> Unit) {
      hasLearnerDetailsThat().block()
    }

    companion object {
      /**
       * Returns a new [ExplorationContextSubject] to verify aspects of the specified
       * [EventLog.ExplorationContext] value.
       */
      fun assertThat(actual: EventLog.ExplorationContext): ExplorationContextSubject =
        assertAbout(::ExplorationContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.HintContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.HintContext] proto can be verified through inherited methods.
   *
   * Call [HintContextSubject.assertThat] to create the subject.
   */
  class HintContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.HintContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [ExplorationContextSubject] to test [EventLog.HintContext.getExplorationDetails].
     *
     * This method never fails since the underlying property defaults to empty proto if it's not
     * defined in the context.
     */
    fun hasExplorationDetailsThat(): ExplorationContextSubject =
      ExplorationContextSubject.assertThat(actual.explorationDetails)

    /**
     * Executes [block] in the context returned by [hasExplorationDetailsThat], similar to
     * [hasOpenExplorationActivityContextThat].
     */
    fun hasExplorationDetailsThat(block: ExplorationContextSubject.() -> Unit) {
      hasExplorationDetailsThat().block()
    }

    /**
     * Returns a [IntegerSubject] to test [EventLog.HintContext.getHintIndex].
     *
     * This method never fails since the underlying property defaults to 0 if it's not defined in
     * the context.
     */
    fun hasHintIndexThat(): IntegerSubject = assertThat(actual.hintIndex)

    companion object {
      /**
       * Returns a new [HintContextSubject] to verify aspects of the specified
       * [EventLog.HintContext] value.
       */
      fun assertThat(actual: EventLog.HintContext): HintContextSubject =
        assertAbout(::HintContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.LearnerDetailsContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.LearnerDetailsContext] proto can be verified through inherited methods.
   *
   * Call [LearnerDetailsContextSubject.assertThat] to create the subject.
   */
  class LearnerDetailsContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.LearnerDetailsContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.LearnerDetailsContext.getLearnerId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasLearnerIdThat(): StringSubject = assertThat(actual.learnerId)

    /**
     * Returns a [StringSubject] to test [EventLog.LearnerDetailsContext.getInstallId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasInstallationIdThat(): StringSubject = assertThat(actual.installId)

    companion object {
      /**
       * Returns a new [LearnerDetailsContextSubject] to verify aspects of the specified
       * [EventLog.LearnerDetailsContext] value.
       */
      fun assertThat(actual: EventLog.LearnerDetailsContext): LearnerDetailsContextSubject =
        assertAbout(::LearnerDetailsContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.PlayVoiceOverContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.PlayVoiceOverContext] proto can be verified through inherited methods.
   *
   * Call [PlayVoiceOverContextSubject.assertThat] to create the subject.
   */
  class PlayVoiceOverContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.PlayVoiceOverContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [ExplorationContextSubject] to test
     * [EventLog.PlayVoiceOverContext.getExplorationDetails].
     *
     * This method never fails since the underlying property defaults to empty proto if it's not
     * defined in the context.
     */
    fun hasExplorationDetailsThat(): ExplorationContextSubject =
      ExplorationContextSubject.assertThat(actual.explorationDetails)

    /**
     * Executes [block] in the context returned by [hasExplorationDetailsThat], similar to
     * [hasOpenExplorationActivityContextThat].
     */
    fun hasExplorationDetailsThat(block: ExplorationContextSubject.() -> Unit) {
      hasExplorationDetailsThat().block()
    }

    /**
     * Returns a [StringSubject] to test [EventLog.PlayVoiceOverContext.getContentId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasContentIdThat(): StringSubject = assertThat(actual.contentId)

    companion object {
      /**
       * Returns a new [PlayVoiceOverContextSubject] to verify aspects of the specified
       * [EventLog.PlayVoiceOverContext] value.
       */
      fun assertThat(actual: EventLog.PlayVoiceOverContext): PlayVoiceOverContextSubject =
        assertAbout(::PlayVoiceOverContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.QuestionContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.QuestionContext] proto can be verified through inherited methods.
   *
   * Call [QuestionContextSubject.assertThat] to create the subject.
   */
  class QuestionContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.QuestionContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.QuestionContext.getQuestionId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasQuestionIdThat(): StringSubject = assertThat(actual.questionId)

    /**
     * Returns a [IterableSubject] to test [EventLog.QuestionContext.getSkillIdList].
     *
     * This method never fails since the underlying property defaults to an empty list if it's not
     * defined in the context.
     */
    fun hasSkillIdListThat(): IterableSubject = assertThat(actual.skillIdList)

    companion object {
      /**
       * Returns a new [QuestionContextSubject] to verify aspects of the specified
       * [EventLog.QuestionContext] value.
       */
      fun assertThat(actual: EventLog.QuestionContext): QuestionContextSubject =
        assertAbout(::QuestionContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.RevisionCardContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.RevisionCardContext] proto can be verified through inherited methods.
   *
   * Call [RevisionCardContextSubject.assertThat] to create the subject.
   */
  class RevisionCardContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.RevisionCardContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.RevisionCardContext.getTopicId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasTopicIdThat(): StringSubject = assertThat(actual.topicId)

    /**
     * Returns a [IntegerSubject] to test [EventLog.RevisionCardContext.getSubTopicId].
     *
     * This method never fails since the underlying property defaults to 0 if it's not defined in
     * the context.
     */
    fun hasSubtopicIndexThat(): IntegerSubject = assertThat(actual.subTopicId)

    companion object {
      /**
       * Returns a new [RevisionCardContextSubject] to verify aspects of the specified
       * [EventLog.RevisionCardContext] value.
       */
      fun assertThat(actual: EventLog.RevisionCardContext): RevisionCardContextSubject =
        assertAbout(::RevisionCardContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.StoryContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.StoryContext] proto can be verified through inherited methods.
   *
   * Call [StoryContextSubject.assertThat] to create the subject.
   */
  class StoryContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.StoryContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.StoryContext.getTopicId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasTopicIdThat(): StringSubject = assertThat(actual.topicId)

    /**
     * Returns a [StringSubject] to test [EventLog.StoryContext.getStoryId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasStoryIdThat(): StringSubject = assertThat(actual.storyId)

    companion object {
      /**
       * Returns a new [StoryContextSubject] to verify aspects of the specified
       * [EventLog.StoryContext] value.
       */
      fun assertThat(actual: EventLog.StoryContext): StoryContextSubject =
        assertAbout(::StoryContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.SubmitAnswerContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.SubmitAnswerContext] proto can be verified through inherited methods.
   *
   * Call [SubmitAnswerContextSubject.assertThat] to create the subject.
   */
  class SubmitAnswerContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.SubmitAnswerContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [ExplorationContextSubject] to test
     * [EventLog.SubmitAnswerContext.getExplorationDetails].
     *
     * This method never fails since the underlying property defaults to empty proto if it's not
     * defined in the context.
     */
    fun hasExplorationDetailsThat(): ExplorationContextSubject =
      ExplorationContextSubject.assertThat(actual.explorationDetails)

    /**
     * Executes [block] in the context returned by [hasExplorationDetailsThat], similar to
     * [hasOpenExplorationActivityContextThat].
     */
    fun hasExplorationDetailsThat(block: ExplorationContextSubject.() -> Unit) {
      hasExplorationDetailsThat().block()
    }

    /**
     * Returns a [BooleanSubject] to test [EventLog.SubmitAnswerContext.getIsAnswerCorrect].
     *
     * This method never fails since the underlying property defaults to false if it's not defined
     * in the context.
     */
    fun hasAnswerCorrectValueThat(): BooleanSubject = assertThat(actual.isAnswerCorrect)

    companion object {
      /**
       * Returns a new [SubmitAnswerContextSubject] to verify aspects of the specified
       * [EventLog.SubmitAnswerContext] value.
       */
      fun assertThat(actual: EventLog.SubmitAnswerContext): SubmitAnswerContextSubject =
        assertAbout(::SubmitAnswerContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.TopicContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.TopicContext] proto can be verified through inherited methods.
   *
   * Call [TopicContextSubject.assertThat] to create the subject.
   */
  class TopicContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.TopicContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.TopicContext.getTopicId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasTopicIdThat(): StringSubject = assertThat(actual.topicId)

    companion object {
      /**
       * Returns a new [TopicContextSubject] to verify aspects of the specified
       * [EventLog.TopicContext] value.
       */
      fun assertThat(actual: EventLog.TopicContext): TopicContextSubject =
        assertAbout(::TopicContextSubject).that(actual)
    }
  }

  companion object {
    /** Returns a new [EventLogSubject] to verify aspects of the specified [EventLog] value. */
    fun assertThat(actual: EventLog): EventLogSubject = assertAbout(::EventLogSubject).that(actual)
  }
}
