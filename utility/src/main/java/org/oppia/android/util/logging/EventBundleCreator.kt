package org.oppia.android.util.logging

import android.os.Bundle
import javax.inject.Inject
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.CardContext as CardEventContext
import org.oppia.android.app.model.EventLog.ConceptCardContext as ConceptCardEventContext
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_HINT_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACCESS_SOLUTION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_BACKGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.APP_IN_FOREGROUND_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.DELETE_PROFILE_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.DEVICE_ID_FOR_FAILED_ANALYTICS_LOG
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_CARD_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.EXIT_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.FINISH_EXPLORATION_CONTEXT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.HINT_OFFERED_CONTEXT
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
import org.oppia.android.app.model.EventLog.ExplorationContext as ExplorationEventContext
import org.oppia.android.app.model.EventLog.HintContext as HintEventContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext as LearnerDetailsEventContext
import org.oppia.android.app.model.EventLog.PlayVoiceOverContext as PlayVoiceOverEventContext
import org.oppia.android.app.model.EventLog.QuestionContext as QuestionEventContext
import org.oppia.android.app.model.EventLog.RevisionCardContext as RevisionCardEventContext
import org.oppia.android.app.model.EventLog.StoryContext as StoryEventContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext as SubmitAnswerEventContext
import org.oppia.android.app.model.EventLog.TopicContext as TopicEventContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.CardContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.ConceptCardContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.EmptyContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.ExplorationContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.HintContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.LearnerDetailsContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.PlayVoiceOverContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.QuestionContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.RevisionCardContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.SensitiveStringContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.StoryContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.SubmitAnswerContext
import org.oppia.android.util.logging.EventBundleCreator.EventActivityContext.TopicContext
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue

// See https://firebase.google.com/docs/reference/cpp/group/parameter-names for context.
private const val MAX_CHARACTERS_IN_PARAMETER_NAME = 40

/**
 * Utility for creating bundles from [EventLog] objects.
 * Note that this utility may later upload them to remote services.
 */
class EventBundleCreator @Inject constructor(
  @LearnerStudyAnalytics private val learnerStudyAnalytics: PlatformParameterValue<Boolean>
) {
  fun fillEventBundle(eventLog: EventLog, bundle: Bundle): String {
    bundle.putLong("timestamp", eventLog.timestamp)
    bundle.putString("priority", eventLog.priority.toAnalyticsName())
    return eventLog.context.convertToActivityContext()?.also { eventContext ->
      // Only allow user IDs to be logged when the learner study feature is enabled.
      eventContext.storeValue(PropertyStore(bundle, allowUserIds = learnerStudyAnalytics.value))
    }?.activityName ?: "unknown_activity_context"
  }

  private fun EventLog.Context.convertToActivityContext(): EventActivityContext<*>? {
    return when (activityContextCase) {
      OPEN_EXPLORATION_ACTIVITY ->
        ExplorationContext("open_exploration_activity", openExplorationActivity)
      OPEN_INFO_TAB -> TopicContext("open_info_tab", openInfoTab)
      OPEN_LESSONS_TAB -> TopicContext("open_lessons_tab", openLessonsTab)
      OPEN_PRACTICE_TAB -> TopicContext("open_practice_tab", openPracticeTab)
      OPEN_REVISION_TAB -> TopicContext("open_revision_tab", openRevisionTab)
      OPEN_QUESTION_PLAYER -> QuestionContext("open_question_player", openQuestionPlayer)
      OPEN_STORY_ACTIVITY -> StoryContext("open_story_activity", openStoryActivity)
      OPEN_CONCEPT_CARD -> ConceptCardContext("open_concept_card", openConceptCard)
      OPEN_REVISION_CARD -> RevisionCardContext("open_revision_card", openRevisionCard)
      START_CARD_CONTEXT -> CardContext("start_card_context", startCardContext)
      END_CARD_CONTEXT -> CardContext("end_card_context", endCardContext)
      HINT_OFFERED_CONTEXT -> HintContext("hint_offered_context", hintOfferedContext)
      ACCESS_HINT_CONTEXT -> HintContext("access_hint_context", accessHintContext)
      SOLUTION_OFFERED_CONTEXT ->
        ExplorationContext("solution_offered_context", solutionOfferedContext)
      ACCESS_SOLUTION_CONTEXT ->
        ExplorationContext("access_solution_context", accessSolutionContext)
      SUBMIT_ANSWER_CONTEXT -> SubmitAnswerContext("submit_answer_context", submitAnswerContext)
      PLAY_VOICE_OVER_CONTEXT ->
        PlayVoiceOverContext("play_voice_over_context", playVoiceOverContext)
      APP_IN_BACKGROUND_CONTEXT ->
        LearnerDetailsContext("app_in_background_context", appInBackgroundContext)
      APP_IN_FOREGROUND_CONTEXT ->
        LearnerDetailsContext("app_in_foreground_context", appInForegroundContext)
      EXIT_EXPLORATION_CONTEXT ->
        ExplorationContext("exit_exploration_context", exitExplorationContext)
      FINISH_EXPLORATION_CONTEXT ->
        ExplorationContext("finish_exploration_context", finishExplorationContext)
      RESUME_EXPLORATION_CONTEXT ->
        LearnerDetailsContext("resume_exploration_context", resumeExplorationContext)
      START_OVER_EXPLORATION_CONTEXT ->
        LearnerDetailsContext("start_over_exploration_context", startOverExplorationContext)
      DELETE_PROFILE_CONTEXT ->
        LearnerDetailsContext("delete_profile_context", deleteProfileContext)
      OPEN_HOME -> EmptyContext("open_home")
      OPEN_PROFILE_CHOOSER -> EmptyContext("open_profile_chooser")
      DEVICE_ID_FOR_FAILED_ANALYTICS_LOG ->
        SensitiveStringContext("failed_analytics_log", deviceIdForFailedAnalyticsLog, "device_id")
      ACTIVITYCONTEXT_NOT_SET, null -> null // No context to create here.
    }
  }

  private class PropertyStore(private val bundle: Bundle, private val allowUserIds: Boolean) {
    private val namespaces = mutableListOf<String>()

    fun enterNamespace(name: String) {
      namespaces.add(name)
    }

    fun exitNamespace() {
      namespaces.removeLastOrNull()
    }

    fun <T> putNonSensitiveValue(valueName: String, value: T) =
      putValue(valueName, value, isSensitive = false)

    fun <T> putSensitiveValue(valueName: String, value: T) =
      putValue(valueName, value, isSensitive = true)

    private fun <T> putValue(valueName: String, value: T, isSensitive: Boolean) {
      if (!isSensitive || allowUserIds) {
        val propertyName = computePropertyName(valueName)
        when (value) {
          is Long -> bundle.putLong(propertyName, value)
          is Iterable<*> -> bundle.putString(propertyName, value.joinToString(separator = ","))
          else -> bundle.putString(propertyName, value.toString())
        }
      }
    }

    private fun computePropertyName(valueName: String): String {
      val validValueName = valueName.takeUnless(String::isEmpty) ?: "missing_prop_name"

      // Namespaces are reduced to their first letters and combined into a single word to simplify
      // them and reduce the number of characters that need to be removed.
      val qualifiers = namespaces.joinToString(separator = "_", transform = ::abbreviateNamespace)

      // Ensure that property names don't exceed the max allowed length (otherwise they'll be
      // dropped).
      val sizedName = "${qualifiers}_$validValueName".takeLast(MAX_CHARACTERS_IN_PARAMETER_NAME)

      // Ensure that the property never begins with '_' since that's not valid in Firebase.
      return sizedName.dropWhile { it == '_' }
    }

    private fun abbreviateNamespace(namespace: String): String =
      namespace.split('_').map(String::first).joinToString(separator = "")
  }

  private sealed class EventActivityContext<T>(val activityName: String, private val value: T) {
    fun storeValue(store: PropertyStore) = value.storeValue(store)

    protected abstract fun T.storeValue(store: PropertyStore)

    protected fun <T : EventActivityContext<V>, V> PropertyStore.putProperties(
      propertyName: String, value: V, factory: (String, V) -> T
    ) {
      factory(propertyName, value).run {
        try {
          // Namespaces are only considered for nested properties since the outermost context is
          // already contextualized via the top-level context parameter.
          enterNamespace(propertyName)
          value.storeValue(this@putProperties)
        } finally {
          exitNamespace()
        }
      }
    }

    class CardContext(
      activityName: String, value: CardEventContext
    ) : EventActivityContext<CardEventContext>(activityName, value) {
      override fun CardEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("skill_id", skillId)
      }
    }

    class ConceptCardContext(
      activityName: String, value: ConceptCardEventContext
    ) : EventActivityContext<ConceptCardEventContext>(activityName, value) {
      override fun ConceptCardEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("skill_id", skillId)
      }
    }

    class ExplorationContext(
      activityName: String, value: ExplorationEventContext
    ) : EventActivityContext<ExplorationEventContext>(activityName, value) {
      override fun ExplorationEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
        store.putNonSensitiveValue("story_id", storyId)
        store.putNonSensitiveValue("exploration_id", explorationId)
        store.putNonSensitiveValue("session_id", sessionId)
        store.putNonSensitiveValue("exploration_version", explorationVersion)
        store.putNonSensitiveValue("state_name", stateName)
        store.putProperties("learner_details", learnerDetails, ::LearnerDetailsContext)
      }
    }

    class HintContext(
      activityName: String, value: HintEventContext
    ) : EventActivityContext<HintEventContext>(activityName, value) {
      override fun HintEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("hint_index", hintIndex)
      }
    }

    class LearnerDetailsContext(
      activityName: String, value: LearnerDetailsEventContext
    ) : EventActivityContext<LearnerDetailsEventContext>(activityName, value) {
      override fun LearnerDetailsEventContext.storeValue(store: PropertyStore) {
        store.putSensitiveValue("learner_id", learnerId)
        store.putSensitiveValue("device_id", deviceId)
      }
    }

    class PlayVoiceOverContext(
      activityName: String, value: PlayVoiceOverEventContext
    ) : EventActivityContext<PlayVoiceOverEventContext>(activityName, value) {
      override fun PlayVoiceOverEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("content_id", contentId)
      }
    }

    class QuestionContext(
      activityName: String, value: QuestionEventContext
    ) : EventActivityContext<QuestionEventContext>(activityName, value) {
      override fun QuestionEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("question_id", questionId)
        store.putNonSensitiveValue("skill_ids", skillIdList)
      }
    }

    class RevisionCardContext(
      activityName: String, value: RevisionCardEventContext
    ) : EventActivityContext<RevisionCardEventContext>(activityName, value) {
      override fun RevisionCardEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
        store.putNonSensitiveValue("subtopic_index", subTopicId)
      }
    }

    class StoryContext(
      activityName: String, value: StoryEventContext
    ) : EventActivityContext<StoryEventContext>(activityName, value) {
      override fun StoryEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
        store.putNonSensitiveValue("story_id", storyId)
      }
    }

    class SubmitAnswerContext(
      activityName: String, value: SubmitAnswerEventContext
    ) : EventActivityContext<SubmitAnswerEventContext>(activityName, value) {
      override fun SubmitAnswerEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("is_answer_correct", isAnswerCorrect)
      }
    }

    class TopicContext(
      activityName: String, value: TopicEventContext
    ) : EventActivityContext<TopicEventContext>(activityName, value) {
      override fun TopicEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
      }
    }

    class SensitiveStringContext(
      activityName: String, value: String, private val propertyName: String
    ) : EventActivityContext<String>(activityName, value) {
      override fun String.storeValue(store: PropertyStore) {
        store.putSensitiveValue(propertyName, this)
      }
    }

    class EmptyContext(activityName: String) : EventActivityContext<Unit>(activityName, Unit) {
      override fun Unit.storeValue(store: PropertyStore) {}
    }
  }

  private fun EventLog.Priority.toAnalyticsName() = when (this) {
    EventLog.Priority.PRIORITY_UNSPECIFIED -> "unspecified_priority"
    EventLog.Priority.ESSENTIAL -> "essential"
    EventLog.Priority.OPTIONAL -> "optional"
    EventLog.Priority.UNRECOGNIZED -> "unknown_priority"
  }
}
