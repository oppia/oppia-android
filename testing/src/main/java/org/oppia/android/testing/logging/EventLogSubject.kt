package org.oppia.android.testing.logging

import com.google.common.truth.BooleanSubject
import com.google.common.truth.ComparableSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.IterableSubject
import com.google.common.truth.LongSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertAbout
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoSubject
import com.google.common.truth.extensions.proto.LiteProtoTruth
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AppLanguageSelection.SelectionTypeCase.USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ABANDON_SURVEY
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_BACKGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.BEGIN_SURVEY
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.CLOSE_REVISION_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.DELETE_PROFILE_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_PROFILE_ONBOARDING_EVENT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.EXIT_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.FINISH_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.HINT_UNLOCKED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.INSTALL_ID_FOR_FAILED_ANALYTICS_LOG
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.LESSON_SAVED_ADVERTENTLY_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.MANDATORY_RESPONSE
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
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPTIONAL_RESPONSE
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PAUSE_VOICE_OVER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PLAY_VOICE_OVER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PROGRESS_SAVING_FAILURE_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.PROGRESS_SAVING_SUCCESS_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REACH_INVESTED_ENGAGEMENT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_LESSON_SUBMIT_CORRECT_ANSWER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.RESUME_LESSON_SUBMIT_INCORRECT_ANSWER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REVEAL_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.REVEAL_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SHOW_SURVEY_POPUP
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SOLUTION_UNLOCKED_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_OVER_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.START_PROFILE_ONBOARDING_EVENT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SUBMIT_ANSWER_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.SWITCH_IN_LESSON_LANGUAGE
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.VIEW_EXISTING_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.VIEW_EXISTING_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.FeatureFlagItemContext
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.PlatformParameter.SyncStatus
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.testing.logging.EventLogSubject.Companion.assertThat

/**
 * Truth subject for verifying properties of [EventLog]s.
 *
 * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying [EventLog]
 * proto can be verified through inherited methods.
 *
 * Call [assertThat] to create the subject.
 */
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
   * Verifies that the [EventLog] under test has no profile ID defined per [EventLog.getProfileId].
   */
  fun hasNoProfileId() {
    assertThat(actual.hasProfileId()).isFalse()
  }

  /**
   * Returns an [LiteProtoSubject] to verify the under-test [EventLog]'s [EventLog.getProfileId]
   * field.
   */
  fun hasProfileIdThat(): LiteProtoSubject = LiteProtoTruth.assertThat(actual.profileId)

  /**
   * Returns an [AppLanguageSelectionSubject] to verify the under-test [EventLog]'s
   * [EventLog.getAppLanguageSelection] field.
   */
  fun hasAppLanguageSelectionThat(): AppLanguageSelectionSubject =
    AppLanguageSelectionSubject.assertThat(actual.appLanguageSelection)

  /**
   * Returns an [WrittenTranslationLanguageSelectionSubject] to verify the under-test [EventLog]'s
   * [EventLog.getWrittenTranslationLanguageSelection] field.
   */
  fun hasWrittenTranslationLanguageSelectionThat(): WrittenTranslationLanguageSelectionSubject {
    return WrittenTranslationLanguageSelectionSubject.assertThat(
      actual.writtenTranslationLanguageSelection
    )
  }

  /**
   * Returns an [AudioTranslationLanguageSelectionSubject] to verify the under-test [EventLog]'s
   * [EventLog.getAudioTranslationLanguageSelection] field.
   */
  fun hasAudioTranslationLanguageSelectionThat(): AudioTranslationLanguageSelectionSubject {
    return AudioTranslationLanguageSelectionSubject.assertThat(
      actual.audioTranslationLanguageSelection
    )
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
   * Verifies that the [EventLog] under test has a context corresponding to [CLOSE_REVISION_CARD]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasCloseRevisionCardContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(CLOSE_REVISION_CARD)
  }

  /**
   * Verifies the [EventLog]'s context per [hasCloseRevisionCardContext] and returns a
   * [RevisionCardContextSubject] to test the corresponding context.
   */
  fun hasCloseRevisionCardContextThat(): RevisionCardContextSubject {
    hasCloseRevisionCardContext()
    return RevisionCardContextSubject.assertThat(actual.context.closeRevisionCard)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasCloseRevisionCardContextThat].
   */
  fun hasCloseRevisionCardContextThat(block: RevisionCardContextSubject.() -> Unit) {
    hasCloseRevisionCardContextThat().block()
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
   * Verifies that the [EventLog] under test has a context corresponding to [HINT_UNLOCKED_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasHintUnlockedContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(HINT_UNLOCKED_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasHintUnlockedContext] and returns a
   * [HintContextSubject] to test the corresponding context.
   */
  fun hasHintUnlockedContextThat(): HintContextSubject {
    hasHintUnlockedContext()
    return HintContextSubject.assertThat(actual.context.hintUnlockedContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasHintUnlockedContextThat].
   */
  fun hasHintUnlockedContextThat(block: HintContextSubject.() -> Unit) {
    hasHintUnlockedContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [REVEAL_HINT_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasRevealHintContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(REVEAL_HINT_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasRevealHintContext] and returns a [HintContextSubject]
   * to test the corresponding context.
   */
  fun hasRevealHintContextThat(): HintContextSubject {
    hasRevealHintContext()
    return HintContextSubject.assertThat(actual.context.revealHintContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasRevealHintContextThat].
   */
  fun hasRevealHintContextThat(block: HintContextSubject.() -> Unit) {
    hasRevealHintContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to [VIEW_EXISTING_HINT_CONTEXT]
   * (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasViewExistingHintContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(VIEW_EXISTING_HINT_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasViewExistingHintContext] and returns a [HintContextSubject]
   * to test the corresponding context.
   */
  fun hasViewExistingHintContextThat(): HintContextSubject {
    hasViewExistingHintContext()
    return HintContextSubject.assertThat(actual.context.viewExistingHintContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasViewExistingHintContextThat].
   */
  fun hasViewExistingHintContextThat(block: HintContextSubject.() -> Unit) {
    hasViewExistingHintContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [SOLUTION_UNLOCKED_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasSolutionUnlockedContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(SOLUTION_UNLOCKED_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasSolutionUnlockedContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasSolutionUnlockedContextThat(): ExplorationContextSubject {
    hasSolutionUnlockedContext()
    return ExplorationContextSubject.assertThat(actual.context.solutionUnlockedContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasSolutionUnlockedContextThat].
   */
  fun hasSolutionUnlockedContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasSolutionUnlockedContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [REVEAL_SOLUTION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasRevealSolutionContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(REVEAL_SOLUTION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasRevealSolutionContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasRevealSolutionContextThat(): ExplorationContextSubject {
    hasRevealSolutionContext()
    return ExplorationContextSubject.assertThat(actual.context.revealSolutionContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasRevealSolutionContextThat].
   */
  fun hasRevealSolutionContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasRevealSolutionContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [VIEW_EXISTING_SOLUTION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasViewExistingSolutionContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(VIEW_EXISTING_SOLUTION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasViewExistingSolutionContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasViewExistingSolutionContextThat(): ExplorationContextSubject {
    hasViewExistingSolutionContext()
    return ExplorationContextSubject.assertThat(actual.context.viewExistingSolutionContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasViewExistingSolutionContextThat].
   */
  fun hasViewExistingSolutionContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasViewExistingSolutionContextThat().block()
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
   * [VoiceoverActionContextSubject] to test the corresponding context.
   */
  fun hasPlayVoiceOverContextThat(): VoiceoverActionContextSubject {
    hasPlayVoiceOverContext()
    return VoiceoverActionContextSubject.assertThat(actual.context.playVoiceOverContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasPlayVoiceOverContextThat].
   */
  fun hasPlayVoiceOverContextThat(block: VoiceoverActionContextSubject.() -> Unit) {
    hasPlayVoiceOverContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [PAUSE_VOICE_OVER_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasPauseVoiceOverContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(PAUSE_VOICE_OVER_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasPauseVoiceOverContext] and returns a
   * [VoiceoverActionContextSubject] to test the corresponding context.
   */
  fun hasPauseVoiceOverContextThat(): VoiceoverActionContextSubject {
    hasPauseVoiceOverContext()
    return VoiceoverActionContextSubject.assertThat(actual.context.pauseVoiceOverContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasPauseVoiceOverContextThat].
   */
  fun hasPauseVoiceOverContextThat(block: VoiceoverActionContextSubject.() -> Unit) {
    hasPauseVoiceOverContextThat().block()
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
   * [START_EXPLORATION_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasStartExplorationContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(START_EXPLORATION_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasStartExplorationContext] and returns an
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasStartExplorationContextThat(): ExplorationContextSubject {
    hasStartExplorationContext()
    return ExplorationContextSubject.assertThat(actual.context.startExplorationContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasStartExplorationContextThat] except for the conditions of, and subject returned by,
   * [hasStartExplorationContextThat].
   */
  fun hasStartExplorationContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasStartExplorationContextThat().block()
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
   * [SWITCH_IN_LESSON_LANGUAGE] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasSwitchInLessonLanguageContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(SWITCH_IN_LESSON_LANGUAGE)
  }

  /**
   * Verifies the [EventLog]'s context per [hasSwitchInLessonLanguageContext] and returns a
   * [SwitchInLessonLanguageEventContextSubject] to test the corresponding context.
   */
  fun hasSwitchInLessonLanguageContextThat(): SwitchInLessonLanguageEventContextSubject {
    hasSwitchInLessonLanguageContext()
    return SwitchInLessonLanguageEventContextSubject.assertThat(
      actual.context.switchInLessonLanguage
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned
   * by, [hasSwitchInLessonLanguageContextThat].
   */
  fun hasSwitchInLessonLanguageContextThat(
    block: SwitchInLessonLanguageEventContextSubject.() -> Unit
  ) {
    hasSwitchInLessonLanguageContextThat().block()
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
   * Verifies that the [EventLog] under test has a context corresponding to
   * [ABANDON_SURVEY] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasAbandonSurveyContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(ABANDON_SURVEY)
  }

  /**
   * Verifies the [EventLog]'s context per [hasAbandonSurveyContext] and returns a
   * [AbandonSurveyContextSubject] to test the corresponding context.
   */
  fun hasAbandonSurveyContextThat(): AbandonSurveyContextSubject {
    hasAbandonSurveyContext()
    return AbandonSurveyContextSubject.assertThat(
      actual.context.abandonSurvey
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block].
   */
  fun hasAbandonSurveyContextThat(
    block: AbandonSurveyContextSubject.() -> Unit
  ) {
    hasAbandonSurveyContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [MANDATORY_RESPONSE] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasMandatorySurveyResponseContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(MANDATORY_RESPONSE)
  }

  /**
   * Verifies the [EventLog]'s context per [hasMandatorySurveyResponseContext] and returns a
   * [MandatorySurveyResponseContextSubject] to test the corresponding context.
   */
  fun hasMandatorySurveyResponseContextThat(): MandatorySurveyResponseContextSubject {
    hasMandatorySurveyResponseContext()
    return MandatorySurveyResponseContextSubject.assertThat(
      actual.context.mandatoryResponse
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block].
   */
  fun hasMandatorySurveyResponseContextThat(
    block: MandatorySurveyResponseContextSubject.() -> Unit
  ) {
    hasMandatorySurveyResponseContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [SHOW_SURVEY_POPUP] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasShowSurveyPopupContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(SHOW_SURVEY_POPUP)
  }

  /**
   * Verifies the [EventLog]'s context per [hasShowSurveyPopupContext] and returns a
   * [SurveyContextSubject] to test the corresponding context.
   */
  fun hasShowSurveyPopupContextThat(): SurveyContextSubject {
    hasShowSurveyPopupContext()
    return SurveyContextSubject.assertThat(
      actual.context.showSurveyPopup
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block].
   */
  fun hasShowSurveyPopupContextThat(
    block: SurveyContextSubject.() -> Unit
  ) {
    hasShowSurveyPopupContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [BEGIN_SURVEY] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasBeginSurveyContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(BEGIN_SURVEY)
  }

  /**
   * Verifies the [EventLog]'s context per [hasBeginSurveyContext] and returns a
   * [SurveyContextSubject] to test the corresponding context.
   */
  fun hasBeginSurveyContextThat(): SurveyContextSubject {
    hasBeginSurveyContext()
    return SurveyContextSubject.assertThat(
      actual.context.beginSurvey
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block].
   */
  fun hasBeginSurveyContextThat(
    block: SurveyContextSubject.() -> Unit
  ) {
    hasBeginSurveyContextThat().block()
  }

  /**
   * Verifies the [EventLog]'s context and returns a [FeatureFlagListContextSubject] to test the
   * corresponding context.
   */
  fun hasFeatureFlagContextThat(): FeatureFlagListContextSubject {
    return FeatureFlagListContextSubject.assertThat(
      actual.context.featureFlagListContext
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block].
   */
  fun hasFeatureFlagContextThat(
    block: FeatureFlagListContextSubject.() -> Unit
  ) {
    hasFeatureFlagContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [OPTIONAL_RESPONSE] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasOptionalSurveyResponseContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(OPTIONAL_RESPONSE)
  }

  /**
   * Verifies the [EventLog]'s context per [hasOptionalSurveyResponseContext] and returns a
   * [OptionalSurveyResponseContextSubject] to test the corresponding context.
   */
  fun hasOptionalSurveyResponseContextThat(): OptionalSurveyResponseContextSubject {
    hasOptionalSurveyResponseContext()
    return OptionalSurveyResponseContextSubject.assertThat(
      actual.context.optionalResponse
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block].
   */
  fun hasOptionalSurveyResponseContextThat(
    block: OptionalSurveyResponseContextSubject.() -> Unit
  ) {
    hasOptionalSurveyResponseContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [PROGRESS_SAVING_SUCCESS_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasProgressSavingSuccessContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(PROGRESS_SAVING_SUCCESS_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasProgressSavingSuccessContext] and returns a
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasProgressSavingSuccessContextThat(): ExplorationContextSubject {
    hasProgressSavingSuccessContext()
    return ExplorationContextSubject.assertThat(actual.context.progressSavingSuccessContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasProgressSavingSuccessContextThat].
   */
  fun hasProgressSavingSuccessContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasProgressSavingSuccessContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [PROGRESS_SAVING_FAILURE_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasProgressSavingFailureContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(PROGRESS_SAVING_FAILURE_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasProgressSavingFailureContext] and returns a
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasProgressSavingFailureContextThat(): ExplorationContextSubject {
    hasProgressSavingFailureContext()
    return ExplorationContextSubject.assertThat(actual.context.progressSavingFailureContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasProgressSavingFailureContextThat].
   */
  fun hasProgressSavingFailureContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasProgressSavingFailureContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [LESSON_SAVED_ADVERTENTLY_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasLessonSavedAdvertentlyContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(LESSON_SAVED_ADVERTENTLY_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasLessonSavedAdvertentlyContext] and returns a
   * [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasLessonSavedAdvertentlyContextThat(): ExplorationContextSubject {
    hasLessonSavedAdvertentlyContext()
    return ExplorationContextSubject.assertThat(actual.context.lessonSavedAdvertentlyContext)
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasLessonSavedAdvertentlyContextThat].
   */
  fun hasLessonSavedAdvertentlyContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasLessonSavedAdvertentlyContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [RESUME_LESSON_SUBMIT_CORRECT_ANSWER_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasResumeLessonSubmitCorrectAnswerContext() {
    assertThat(actual.context.activityContextCase)
      .isEqualTo(RESUME_LESSON_SUBMIT_CORRECT_ANSWER_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasResumeLessonSubmitCorrectAnswerContext] and returns
   * a [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasResumeLessonSubmitCorrectAnswerContextThat(): ExplorationContextSubject {
    hasResumeLessonSubmitCorrectAnswerContext()
    return ExplorationContextSubject.assertThat(
      actual.context.resumeLessonSubmitCorrectAnswerContext
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasResumeLessonSubmitCorrectAnswerContextThat].
   */
  fun hasResumeLessonSubmitCorrectAnswerContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasResumeLessonSubmitCorrectAnswerContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [RESUME_LESSON_SUBMIT_INCORRECT_ANSWER_CONTEXT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasResumeLessonSubmitIncorrectAnswerContext() {
    assertThat(actual.context.activityContextCase)
      .isEqualTo(RESUME_LESSON_SUBMIT_INCORRECT_ANSWER_CONTEXT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasResumeLessonSubmitIncorrectAnswerContext] and
   * returns a [ExplorationContextSubject] to test the corresponding context.
   */
  fun hasResumeLessonSubmitIncorrectAnswerContextThat(): ExplorationContextSubject {
    hasResumeLessonSubmitIncorrectAnswerContext()
    return ExplorationContextSubject.assertThat(
      actual.context.resumeLessonSubmitIncorrectAnswerContext
    )
  }

  /**
   * Verifies the [EventLog]'s context and executes [block] in the same way as
   * [hasOpenExplorationActivityContextThat] except for the conditions of, and subject returned by,
   * [hasResumeLessonSubmitIncorrectAnswerContextThat].
   */
  fun hasResumeLessonSubmitIncorrectAnswerContextThat(block: ExplorationContextSubject.() -> Unit) {
    hasResumeLessonSubmitIncorrectAnswerContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [START_PROFILE_ONBOARDING_EVENT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasStartProfileOnboardingContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(START_PROFILE_ONBOARDING_EVENT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasStartProfileOnboardingContext] and returns a
   * [ProfileOnboardingContextSubject] to test the corresponding context.
   */
  fun hasStartProfileOnboardingContextThat(): ProfileOnboardingContextSubject {
    hasStartProfileOnboardingContext()
    return ProfileOnboardingContextSubject.assertThat(
      actual.context.startProfileOnboardingEvent
    )
  }

  /** Verifies the [EventLog]'s context and executes [block]. */
  fun hasStartProfileOnboardingContextThat(
    block: ProfileOnboardingContextSubject.() -> Unit
  ) {
    hasStartProfileOnboardingContextThat().block()
  }

  /**
   * Verifies that the [EventLog] under test has a context corresponding to
   * [END_PROFILE_ONBOARDING_EVENT] (per [EventLog.Context.getActivityContextCase]).
   */
  fun hasEndProfileOnboardingContext() {
    assertThat(actual.context.activityContextCase).isEqualTo(END_PROFILE_ONBOARDING_EVENT)
  }

  /**
   * Verifies the [EventLog]'s context per [hasEndProfileOnboardingContext] and returns a
   * [ProfileOnboardingContextSubject] to test the corresponding context.
   */
  fun hasEndProfileOnboardingContextThat(): ProfileOnboardingContextSubject {
    hasEndProfileOnboardingContext()
    return ProfileOnboardingContextSubject.assertThat(
      actual.context.endProfileOnboardingEvent
    )
  }

  /** Verifies the [EventLog]'s context and executes [block]. */
  fun hasEndProfileOnboardingContextThat(
    block: ProfileOnboardingContextSubject.() -> Unit
  ) {
    hasEndProfileOnboardingContextThat().block()
  }

  /**
   * Truth subject for verifying properties of [AppLanguageSelection]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [AppLanguageSelection] proto can be verified through inherited methods.
   *
   * Call [AppLanguageSelectionSubject.assertThat] to create the subject.
   */
  class AppLanguageSelectionSubject private constructor(
    metadata: FailureMetadata,
    private val actual: AppLanguageSelection
  ) : LiteProtoSubject(metadata, actual) {
    /** Asserts that this selection corresponds to [USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT]. */
    fun isUseSystemLanguageOrAppDefault(): Unit =
      assertThat(actual.selectionTypeCase).isEqualTo(USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT)

    /**
     * Asserts that this selection corresponds to
     * [AppLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE].
     */
    fun isSelectedLanguage() {
      assertThat(actual.selectionTypeCase)
        .isEqualTo(AppLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE)
    }

    /**
     * Returns a [ComparableSubject] to test [AppLanguageSelection.getSelectedLanguage].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun isSelectedLanguageThat(): ComparableSubject<OppiaLanguage> =
      assertThat(actual.selectedLanguage)

    companion object {
      /**
       * Returns a new [AppLanguageSelectionSubject] to verify aspects of the specified
       * [AppLanguageSelection] value.
       */
      fun assertThat(actual: AppLanguageSelection): AppLanguageSelectionSubject =
        assertAbout(::AppLanguageSelectionSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [WrittenTranslationLanguageSelection]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [WrittenTranslationLanguageSelection] proto can be verified through inherited methods.
   *
   * Call [WrittenTranslationLanguageSelectionSubject.assertThat] to create the subject.
   */
  class WrittenTranslationLanguageSelectionSubject private constructor(
    metadata: FailureMetadata,
    private val actual: WrittenTranslationLanguageSelection
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Asserts that this selection corresponds to
     * [WrittenTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE].
     */
    fun isUseAppLanguage() {
      assertThat(actual.selectionTypeCase)
        .isEqualTo(WrittenTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE)
    }

    /**
     * Asserts that this selection corresponds to
     * [WrittenTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE].
     */
    fun isSelectedLanguage() {
      assertThat(actual.selectionTypeCase)
        .isEqualTo(WrittenTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE)
    }

    /**
     * Returns a [ComparableSubject] to test
     * [WrittenTranslationLanguageSelection.getSelectedLanguage].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun isSelectedLanguageThat(): ComparableSubject<OppiaLanguage> =
      assertThat(actual.selectedLanguage)

    companion object {
      /**
       * Returns a new [WrittenTranslationLanguageSelectionSubject] to verify aspects of the
       * specified [WrittenTranslationLanguageSelection] value.
       */
      fun assertThat(
        actual: WrittenTranslationLanguageSelection
      ): WrittenTranslationLanguageSelectionSubject =
        assertAbout(::WrittenTranslationLanguageSelectionSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [AudioTranslationLanguageSelection]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [AudioTranslationLanguageSelection] proto can be verified through inherited methods.
   *
   * Call [AudioTranslationLanguageSelectionSubject.assertThat] to create the subject.
   */
  class AudioTranslationLanguageSelectionSubject private constructor(
    metadata: FailureMetadata,
    private val actual: AudioTranslationLanguageSelection
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Asserts that this selection corresponds to
     * [AudioTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE].
     */
    fun isUseAppLanguage() {
      assertThat(actual.selectionTypeCase)
        .isEqualTo(AudioTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE)
    }

    /**
     * Asserts that this selection corresponds to
     * [AudioTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE].
     */
    fun isSelectedLanguage() {
      assertThat(actual.selectionTypeCase)
        .isEqualTo(AudioTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE)
    }

    /**
     * Returns a [ComparableSubject] to test
     * [AudioTranslationLanguageSelection.getSelectedLanguage].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun isSelectedLanguageThat(): ComparableSubject<OppiaLanguage> =
      assertThat(actual.selectedLanguage)

    companion object {
      /**
       * Returns a new [AudioTranslationLanguageSelectionSubject] to verify aspects of the
       * specified [AudioTranslationLanguageSelection] value.
       */
      fun assertThat(
        actual: AudioTranslationLanguageSelection
      ): AudioTranslationLanguageSelectionSubject =
        assertAbout(::AudioTranslationLanguageSelectionSubject).that(actual)
    }
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
     * Returns a [StringSubject] to test [EventLog.ExplorationContext.getClassroomId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasClassroomIdThat(): StringSubject = assertThat(actual.classroomId)

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
   * Truth subject for verifying properties of [EventLog.VoiceoverActionContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.VoiceoverActionContext] proto can be verified through inherited methods.
   *
   * Call [VoiceoverActionContextSubject.assertThat] to create the subject.
   */
  class VoiceoverActionContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.VoiceoverActionContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [ExplorationContextSubject] to test
     * [EventLog.VoiceoverActionContext.getExplorationDetails].
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
     * Returns a [StringSubject] to test [EventLog.VoiceoverActionContext.getContentId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasContentIdThat(): StringSubject = assertThat(actual.contentId)

    /**
     * Returns a [StringSubject] to test [EventLog.VoiceoverActionContext.getLanguageCode].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasLanguageCodeThat(): StringSubject = assertThat(actual.languageCode)

    companion object {
      /**
       * Returns a new [VoiceoverActionContextSubject] to verify aspects of the specified
       * [EventLog.VoiceoverActionContext] value.
       */
      fun assertThat(actual: EventLog.VoiceoverActionContext): VoiceoverActionContextSubject =
        assertAbout(::VoiceoverActionContextSubject).that(actual)
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

  /**
   * Truth subject for verifying properties of [EventLog.SwitchInLessonLanguageEventContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.SwitchInLessonLanguageEventContext] proto can be verified through inherited methods.
   *
   * Call [SwitchInLessonLanguageEventContextSubject.assertThat] to create the subject.
   */
  class SwitchInLessonLanguageEventContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.SwitchInLessonLanguageEventContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [ExplorationContextSubject] to test
     * [EventLog.SwitchInLessonLanguageEventContext.getExplorationDetails].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
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
     * Returns a [ComparableSubject] to test
     * [EventLog.SwitchInLessonLanguageEventContext.getSwitchFromLanguage].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSwitchFromLanguageThat(): ComparableSubject<OppiaLanguage> =
      assertThat(actual.switchFromLanguage)

    /**
     * Returns a [ComparableSubject] to test
     * [EventLog.SwitchInLessonLanguageEventContext.getSwitchToLanguage].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSwitchToLanguageThat(): ComparableSubject<OppiaLanguage> =
      assertThat(actual.switchToLanguage)

    companion object {
      /**
       * Returns a new [TopicContextSubject] to verify aspects of the specified
       * [EventLog.TopicContext] value.
       */
      fun assertThat(
        actual: EventLog.SwitchInLessonLanguageEventContext
      ): SwitchInLessonLanguageEventContextSubject =
        assertAbout(::SwitchInLessonLanguageEventContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.OptionalSurveyResponseContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.OptionalSurveyResponseContext] proto can be verified through inherited methods.
   *
   * Call [OptionalSurveyResponseContextSubject.assertThat] to create the subject.
   */
  class OptionalSurveyResponseContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.OptionalSurveyResponseContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [SurveyResponseContextSubject] to test
     * [EventLog.OptionalSurveyResponseContext.getSurveyDetails].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSurveyDetailsThat(): SurveyResponseContextSubject =
      SurveyResponseContextSubject.assertThat(actual.surveyDetails)

    /** Executes [block] in the context returned by [hasSurveyDetailsThat]. */
    fun hasSurveyDetailsThat(block: SurveyResponseContextSubject.() -> Unit) {
      hasSurveyDetailsThat().block()
    }

    /**
     * Returns a [StringSubject] to test
     * [EventLog.OptionalSurveyResponseContext.getFeedbackAnswer].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasFeedbackAnswerThat(): StringSubject =
      assertThat(actual.feedbackAnswer)

    companion object {
      /**
       * Returns a new [OptionalSurveyResponseContextSubject] to verify aspects of the specified
       * [EventLog.OptionalSurveyResponseContext] value.
       */
      fun assertThat(actual: EventLog.OptionalSurveyResponseContext):
        OptionalSurveyResponseContextSubject =
          assertAbout(::OptionalSurveyResponseContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.MandatorySurveyResponseContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.MandatorySurveyResponseContext] proto can be verified through inherited methods.
   *
   * Call [MandatorySurveyResponseContextSubject.assertThat] to create the subject.
   */
  class MandatorySurveyResponseContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.MandatorySurveyResponseContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [SurveyResponseContextSubject] to test
     * [EventLog.MandatorySurveyResponseContext.getSurveyDetails].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasSurveyDetailsThat(): SurveyResponseContextSubject =
      SurveyResponseContextSubject.assertThat(actual.surveyDetails)

    /** Executes [block] in the context returned by [hasSurveyDetailsThat]. */
    fun hasSurveyDetailsThat(block: SurveyResponseContextSubject.() -> Unit) {
      hasSurveyDetailsThat().block()
    }

    /**
     * Returns a [ComparableSubject] to test
     * [EventLog.MandatorySurveyResponseContext.getUserTypeAnswer].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasUserTypeAnswerThat(): ComparableSubject<UserTypeAnswer> =
      assertThat(actual.userTypeAnswer)

    /**
     * Returns a [ComparableSubject] to test
     * [EventLog.MandatorySurveyResponseContext.getMarketFitAnswer].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasMarketFitAnswerThat(): ComparableSubject<MarketFitAnswer> =
      assertThat(actual.marketFitAnswer)

    /**
     * Returns a [IntegerSubject] to test
     * [EventLog.MandatorySurveyResponseContext.getNpsScoreAnswer].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasNpsScoreAnswerThat(): IntegerSubject =
      assertThat(actual.npsScoreAnswer)

    companion object {
      /**
       * Returns a new [MandatorySurveyResponseContextSubject] to verify aspects of the specified
       * [EventLog.MandatorySurveyResponseContext] value.
       */
      fun assertThat(actual: EventLog.MandatorySurveyResponseContext):
        MandatorySurveyResponseContextSubject =
          assertAbout(::MandatorySurveyResponseContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.AbandonSurveyContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.AbandonSurveyContext] proto can be verified through inherited methods.
   *
   * Call [AbandonSurveyContextSubject.assertThat] to create the subject.
   */
  class AbandonSurveyContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.AbandonSurveyContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [SurveyResponseContextSubject] to test
     * [EventLog.AbandonSurveyContext.getSurveyDetails].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasSurveyDetailsThat(): SurveyResponseContextSubject =
      SurveyResponseContextSubject.assertThat(actual.surveyDetails)

    /** Executes [block] in the context returned by [hasSurveyDetailsThat]. */
    fun hasSurveyDetailsThat(block: SurveyResponseContextSubject.() -> Unit) {
      hasSurveyDetailsThat().block()
    }

    /**
     * Returns a [ComparableSubject] to test [EventLog.AbandonSurveyContext.getQuestionName].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasQuestionNameThat(): ComparableSubject<SurveyQuestionName> =
      assertThat(actual.questionName)

    companion object {
      /**
       * Returns a new [AbandonSurveyContextSubject] to verify aspects of the specified
       * [EventLog.AbandonSurveyContext] value.
       */
      fun assertThat(actual: EventLog.AbandonSurveyContext): AbandonSurveyContextSubject =
        assertAbout(::AbandonSurveyContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.SurveyResponseContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.SurveyResponseContext] proto can be verified through inherited methods.
   *
   * Call [SurveyResponseContextSubject.assertThat] to create the subject.
   */
  class SurveyResponseContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.SurveyResponseContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.SurveyResponseContext.getSurveyId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSurveyIdThat(): StringSubject = assertThat(actual.surveyId)

    /**
     * Returns a [StringSubject] to test [EventLog.SurveyResponseContext.getSurveyId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasInternalProfileIdThat(): StringSubject = assertThat(actual.profileId)

    companion object {
      /**
       * Returns a new [SurveyResponseContextSubject] to verify aspects of the specified
       * [EventLog.SurveyResponseContext] value.
       */
      fun assertThat(actual: EventLog.SurveyResponseContext): SurveyResponseContextSubject =
        assertAbout(::SurveyResponseContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.SurveyContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.SurveyContext] proto can be verified through inherited methods.
   *
   * Call [SurveyContextSubject.assertThat] to create the subject.
   */
  class SurveyContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.SurveyContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.SurveyContext.getExplorationId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasExplorationIdThat(): StringSubject = assertThat(actual.explorationId)

    /**
     * Returns a [StringSubject] to test [EventLog.SurveyContext.getTopicId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasTopicIdThat(): StringSubject = assertThat(actual.topicId)

    companion object {
      /**
       * Returns a new [SurveyContextSubject] to verify aspects of the specified
       * [EventLog.SurveyContext] value.
       */
      fun assertThat(actual: EventLog.SurveyContext): SurveyContextSubject =
        assertAbout(::SurveyContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.FeatureFlagListContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.FeatureFlagContext] proto can be verified through inherited methods.
   *
   * Call [FeatureFlagListContextSubject.assertThat] to create the subject.
   */
  class FeatureFlagListContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.FeatureFlagListContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test [EventLog.FeatureFlagListContext.getUniqueUserUuid].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasUniqueUserUuidThat(): StringSubject = assertThat(actual.uniqueUserUuid)

    /**
     * Returns a [StringSubject] to test [EventLog.FeatureFlagListContext.getAppSessionId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasSessionIdThat(): StringSubject = assertThat(actual.appSessionId)

    /**
     * Returns a [IntegerSubject] to test [EventLog.FeatureFlagListContext.getFeatureFlagsCount].
     *
     * This method never fails since the underlying property defaults to 0 if it's not defined in the
     * context.
     */
    fun hasFeatureFlagItemCountThat(): IntegerSubject = assertThat(actual.featureFlagsCount)

    /**
     * Returns a [FeatureFlagItemContextSubject] to test
     * [EventLog.FeatureFlagListContext.getFeatureFlagsList].
     *
     * This method never fails since the underlying property defaults to empty object if it's not
     * defined in the context.
     */
    fun hasFeatureFlagItemContextThatAtIndex(index: Int): FeatureFlagItemContextSubject {
      return FeatureFlagItemContextSubject.assertThat(actual.featureFlagsList[index])
    }

    /**
     * Verifies the [EventLog]'s context and executes [block] in the same way as
     * [hasFeatureFlagItemContextThatAtIndex] except for the conditions of, and subject returned by,
     * [hasFeatureFlagItemContextThatAtIndex].
     */
    fun hasFeatureFlagItemContextThatAtIndex(
      index: Int,
      block: FeatureFlagItemContextSubject.() -> Unit
    ) {
      hasFeatureFlagItemContextThatAtIndex(index).block()
    }

    companion object {
      /**
       * Returns a new [FeatureFlagListContextSubject] to verify aspects of the specified
       * [EventLog.FeatureFlagListContext] value.
       */
      fun assertThat(actual: EventLog.FeatureFlagListContext): FeatureFlagListContextSubject =
        assertAbout(::FeatureFlagListContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.FeatureFlagItemContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.FeatureFlagItemContext] proto can be verified through inherited
   * methods.
   *
   * Call [FeatureFlagItemContextSubject.assertThat] to create the subject.
   */
  class FeatureFlagItemContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: FeatureFlagItemContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [StringSubject] to test
     * [EventLog.FeatureFlagItemContext.getFlagName].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasFeatureFlagNameThat(): StringSubject = assertThat(actual.flagName)

    /**
     * Returns a [BooleanSubject] to test
     * [EventLog.FeatureFlagItemContext.getFlagEnabledState].
     *
     * This method never fails since the underlying property defaults to false if it's not
     * defined in the context.
     */
    fun hasFeatureFlagEnabledStateThat(): BooleanSubject = assertThat(actual.flagEnabledState)

    /**
     * Returns a [ComparableSubject] to test
     * [EventLog.FeatureFlagItemContext.getFlagSyncStatus].
     *
     * This method never fails since the underlying property defaults to the unspecified enum value
     * if it's not defined in the context.
     */
    fun hasFeatureFlagSyncStateThat(): ComparableSubject<SyncStatus> =
      assertThat(actual.flagSyncStatus)

    companion object {
      /**
       * Returns a new [FeatureFlagItemContextSubject] to verify aspects of the specified
       * [EventLog.FeatureFlagItemContext] value.
       */
      fun assertThat(actual: FeatureFlagItemContext?):
        FeatureFlagItemContextSubject =
          assertAbout(::FeatureFlagItemContextSubject).that(actual)
    }
  }

  /**
   * Truth subject for verifying properties of [EventLog.ProfileOnboardingContext]s.
   *
   * Note that this class is also a [LiteProtoSubject] so other aspects of the underlying
   * [EventLog.ProfileOnboardingContext] proto can be verified through inherited methods.
   *
   * Call [ProfileOnboardingContextSubject.assertThat] to create the subject.
   */
  class ProfileOnboardingContextSubject private constructor(
    metadata: FailureMetadata,
    private val actual: EventLog.ProfileOnboardingContext
  ) : LiteProtoSubject(metadata, actual) {
    /**
     * Returns a [LiteProtoSubject] to test [EventLog.ProfileOnboardingContext.getProfileId].
     *
     * This method never fails since the underlying property defaults to empty string if it's not
     * defined in the context.
     */
    fun hasProfileIdThat(): LiteProtoSubject = LiteProtoTruth.assertThat(actual.profileId)

    companion object {
      /**
       * Returns a new [ProfileOnboardingContextSubject] to verify aspects of the specified
       * [EventLog.ProfileOnboardingContext] value.
       */
      fun assertThat(actual: EventLog.ProfileOnboardingContext): ProfileOnboardingContextSubject =
        assertAbout(::ProfileOnboardingContextSubject).that(actual)
    }
  }

  companion object {
    /** Returns a new [EventLogSubject] to verify aspects of the specified [EventLog] value. */
    fun assertThat(actual: EventLog): EventLogSubject = assertAbout(::EventLogSubject).that(actual)
  }
}
