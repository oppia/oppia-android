package org.oppia.android.util.logging

import android.os.Bundle
import org.oppia.android.app.model.EventLog
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
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.CPU_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.LOGGABLEMETRICTYPE_NOT_SET
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.NETWORK_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STARTUP_LATENCY_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
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
import org.oppia.android.util.logging.EventBundleCreator.PerformanceMetricsLoggableMetricType.ApkSizeLoggableMetric
import org.oppia.android.util.logging.EventBundleCreator.PerformanceMetricsLoggableMetricType.CpuUsageLoggableMetric
import org.oppia.android.util.logging.EventBundleCreator.PerformanceMetricsLoggableMetricType.MemoryUsageLoggableMetric
import org.oppia.android.util.logging.EventBundleCreator.PerformanceMetricsLoggableMetricType.NetworkUsageLoggableMetric
import org.oppia.android.util.logging.EventBundleCreator.PerformanceMetricsLoggableMetricType.StartupLatencyLoggableMetric
import org.oppia.android.util.logging.EventBundleCreator.PerformanceMetricsLoggableMetricType.StorageUsageLoggableMetric
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject
import org.oppia.android.app.model.EventLog.CardContext as CardEventContext
import org.oppia.android.app.model.EventLog.ConceptCardContext as ConceptCardEventContext
import org.oppia.android.app.model.EventLog.ExplorationContext as ExplorationEventContext
import org.oppia.android.app.model.EventLog.HintContext as HintEventContext
import org.oppia.android.app.model.EventLog.LearnerDetailsContext as LearnerDetailsEventContext
import org.oppia.android.app.model.EventLog.PlayVoiceOverContext as PlayVoiceOverEventContext
import org.oppia.android.app.model.EventLog.QuestionContext as QuestionEventContext
import org.oppia.android.app.model.EventLog.RevisionCardContext as RevisionCardEventContext
import org.oppia.android.app.model.EventLog.StoryContext as StoryEventContext
import org.oppia.android.app.model.EventLog.SubmitAnswerContext as SubmitAnswerEventContext
import org.oppia.android.app.model.EventLog.TopicContext as TopicEventContext
import org.oppia.android.app.model.OppiaMetricLog.ApkSizeMetric as ApkSizePerformanceLoggableMetric
import org.oppia.android.app.model.OppiaMetricLog.CpuUsageMetric as CpuUsagePerformanceLoggableMetric
import org.oppia.android.app.model.OppiaMetricLog.MemoryUsageMetric as MemoryUsagePerformanceLoggableMetric
import org.oppia.android.app.model.OppiaMetricLog.NetworkUsageMetric as NetworkUsagePerformanceLoggableMetric
import org.oppia.android.app.model.OppiaMetricLog.StartupLatencyMetric as StartupLatencyPerformanceLoggableMetric
import org.oppia.android.app.model.OppiaMetricLog.StorageUsageMetric as StorageUsagePerformanceLoggableMetric

// See https://firebase.google.com/docs/reference/cpp/group/parameter-names for context.
private const val MAX_CHARACTERS_IN_PARAMETER_NAME = 40

/**
 * Utility for creating [Bundle]s from [EventLog] objects.
 *
 * This class is only expected to be used by internal logging mechanisms and should not be called
 * directly.
 */
class EventBundleCreator @Inject constructor(
  @LearnerStudyAnalytics private val learnerStudyAnalytics: PlatformParameterValue<Boolean>
) {
  /**
   * Fills the specified [bundle] with a logging-ready representation of [eventLog] and returns a
   * string representation of the high-level type of event logged (per
   * [EventLog.Context.getActivityContextCase]).
   */
  fun fillEventBundle(eventLog: EventLog, bundle: Bundle): String {
    bundle.putLong("timestamp", eventLog.timestamp)
    bundle.putString("priority", eventLog.priority.toAnalyticsName())
    return eventLog.context.convertToActivityContext()?.also { eventContext ->
      // Only allow user IDs to be logged when the learner study feature is enabled.
      eventContext.storeValue(PropertyStore(bundle, allowUserIds = learnerStudyAnalytics.value))
    }?.activityName ?: "unknown_activity_context"
  }

  /**
   * Fills the specified [bundle] with a logging-ready representation of [oppiaMetricLog] and
   * returns a string representation of the high-level type of event logged (per
   * [OppiaMetricLog.LoggableMetric.getLoggableMetricTypeCase]).
   */
  fun fillPerformanceMetricsEventBundle(oppiaMetricLog: OppiaMetricLog, bundle: Bundle): String {
    bundle.putLong("timestamp", oppiaMetricLog.timestampMillis)
    bundle.putString("priority", oppiaMetricLog.priority.toAnalyticsName())
    bundle.putBoolean("is_app_in_foreground", oppiaMetricLog.isAppInForeground)
    bundle.putString("memory_tier", oppiaMetricLog.memoryTier.toAnalyticsName())
    bundle.putString("storage_tier", oppiaMetricLog.storageTier.toAnalyticsName())
    bundle.putString("network_type", oppiaMetricLog.networkType.toAnalyticsName())
    bundle.putString("current_screen", oppiaMetricLog.currentScreen.toAnalyticsName())
    return oppiaMetricLog.loggableMetric.convertToLoggableMetricType()?.also { loggableMetric ->
      // No performance metrics need to be tied to user IDs.
      loggableMetric.storeValue(PropertyStore(bundle, allowUserIds = false))
    }?.metricName ?: "unknown_loggable_metric"
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
      INSTALL_ID_FOR_FAILED_ANALYTICS_LOG ->
        SensitiveStringContext("failed_analytics_log", installIdForFailedAnalyticsLog, "install_id")
      ACTIVITYCONTEXT_NOT_SET, null -> null // No context to create here.
    }
  }

  private fun OppiaMetricLog.LoggableMetric.convertToLoggableMetricType():
    PerformanceMetricsLoggableMetricType<*>? {
      return when (loggableMetricTypeCase) {
        APK_SIZE_METRIC -> ApkSizeLoggableMetric("apk_size_metric", apkSizeMetric)
        STORAGE_USAGE_METRIC -> StorageUsageLoggableMetric(
          "storage_usage_metric",
          storageUsageMetric
        )
        STARTUP_LATENCY_METRIC -> StartupLatencyLoggableMetric(
          "startup_latency_metric",
          startupLatencyMetric
        )
        MEMORY_USAGE_METRIC -> MemoryUsageLoggableMetric("memory_usage_metric", memoryUsageMetric)
        NETWORK_USAGE_METRIC -> NetworkUsageLoggableMetric(
          "network_usage_metric",
          networkUsageMetric
        )
        CPU_USAGE_METRIC -> CpuUsageLoggableMetric("cpu_usage_metric", cpuUsageMetric)
        LOGGABLEMETRICTYPE_NOT_SET, null -> null // No context to create here.
      }
    }

  /**
   * Utility for storing properties within a [Bundle] (indicated by [bundle]), omitting those which
   * contain sensitive information (if they should be per [allowUserIds].
   */
  private class PropertyStore(private val bundle: Bundle, private val allowUserIds: Boolean) {
    private val namespaces = mutableListOf<String>()

    /**
     * Indicates a new contextual namespace has been started for logging, as given by [name].
     *
     * The namespace's name will be summarized in the final key representation of logged properties.
     *
     * [exitNamespace] should be called when the namespace is no longer used.
     */
    fun enterNamespace(name: String) {
      namespaces.add(name)
    }

    /** Indicates a namespace previously started by [enterNamespace] has ended. */
    fun exitNamespace() {
      namespaces.removeLastOrNull()
    }

    /** Save a non-sensitive property with name [valueName] and value [value]. */
    fun <T> putNonSensitiveValue(valueName: String, value: T) =
      putValue(valueName, value, isSensitive = false)

    /**
     * Saves a value in the same way as [putNonSensitiveValue] except this property will be ignored
     * if sensitive property logging is currently disabled.
     */
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

  /**
   * Represents an [EventLog] activity context (denoted by
   * [EventLog.Context.getActivityContextCase]).
   */
  private sealed class EventActivityContext<T>(val activityName: String, private val value: T) {
    /**
     * Stores the value of this context (i.e. its constituent properties which may correspond to
     * other [EventActivityContext]s).
     */
    fun storeValue(store: PropertyStore) = value.storeValue(store)

    /** Method that should be overridden by base classes to satisfy the contract of [storeValue]. */
    protected abstract fun T.storeValue(store: PropertyStore)

    /**
     * Helper function for child classes to easily store all of the constituent properties of an
     * [EventActivityContext] property.
     */
    protected fun <T : EventActivityContext<V>, V> PropertyStore.putProperties(
      propertyName: String,
      value: V,
      factory: (String, V) -> T
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

    /** The [EventActivityContext] corresponding to [CardEventContext]s. */
    class CardContext(
      activityName: String,
      value: CardEventContext
    ) : EventActivityContext<CardEventContext>(activityName, value) {
      override fun CardEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("skill_id", skillId)
      }
    }

    /** The [EventActivityContext] corresponding to [ConceptCardEventContext]s. */
    class ConceptCardContext(
      activityName: String,
      value: ConceptCardEventContext
    ) : EventActivityContext<ConceptCardEventContext>(activityName, value) {
      override fun ConceptCardEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("skill_id", skillId)
      }
    }

    /** The [EventActivityContext] corresponding to [ExplorationContext]s. */
    class ExplorationContext(
      activityName: String,
      value: ExplorationEventContext
    ) : EventActivityContext<ExplorationEventContext>(activityName, value) {
      override fun ExplorationEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
        store.putNonSensitiveValue("story_id", storyId)
        store.putNonSensitiveValue("exploration_id", explorationId)
        store.putNonSensitiveValue("session_id", sessionId)
        store.putNonSensitiveValue("exploration_version", explorationVersion.toString())
        store.putNonSensitiveValue("state_name", stateName)
        store.putProperties("learner_details", learnerDetails, ::LearnerDetailsContext)
      }
    }

    /** The [EventActivityContext] corresponding to [HintEventContext]s. */
    class HintContext(
      activityName: String,
      value: HintEventContext
    ) : EventActivityContext<HintEventContext>(activityName, value) {
      override fun HintEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("hint_index", hintIndex.toString())
      }
    }

    /** The [EventActivityContext] corresponding to [LearnerDetailsEventContext]s. */
    class LearnerDetailsContext(
      activityName: String,
      value: LearnerDetailsEventContext
    ) : EventActivityContext<LearnerDetailsEventContext>(activityName, value) {
      override fun LearnerDetailsEventContext.storeValue(store: PropertyStore) {
        store.putSensitiveValue("learner_id", learnerId)
        store.putSensitiveValue("install_id", installId)
      }
    }

    /** The [EventActivityContext] corresponding to [PlayVoiceOverEventContext]s. */
    class PlayVoiceOverContext(
      activityName: String,
      value: PlayVoiceOverEventContext
    ) : EventActivityContext<PlayVoiceOverEventContext>(activityName, value) {
      override fun PlayVoiceOverEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("content_id", contentId)
      }
    }

    /** The [EventActivityContext] corresponding to [QuestionEventContext]s. */
    class QuestionContext(
      activityName: String,
      value: QuestionEventContext
    ) : EventActivityContext<QuestionEventContext>(activityName, value) {
      override fun QuestionEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("question_id", questionId)
        store.putNonSensitiveValue("skill_ids", skillIdList)
      }
    }

    /** The [EventActivityContext] corresponding to [RevisionCardEventContext]s. */
    class RevisionCardContext(
      activityName: String,
      value: RevisionCardEventContext
    ) : EventActivityContext<RevisionCardEventContext>(activityName, value) {
      override fun RevisionCardEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
        store.putNonSensitiveValue("subtopic_index", subTopicId.toString())
      }
    }

    /** The [EventActivityContext] corresponding to [StoryEventContext]s. */
    class StoryContext(
      activityName: String,
      value: StoryEventContext
    ) : EventActivityContext<StoryEventContext>(activityName, value) {
      override fun StoryEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
        store.putNonSensitiveValue("story_id", storyId)
      }
    }

    /** The [EventActivityContext] corresponding to [SubmitAnswerEventContext]s. */
    class SubmitAnswerContext(
      activityName: String,
      value: SubmitAnswerEventContext
    ) : EventActivityContext<SubmitAnswerEventContext>(activityName, value) {
      override fun SubmitAnswerEventContext.storeValue(store: PropertyStore) {
        store.putProperties("exploration_details", explorationDetails, ::ExplorationContext)
        store.putNonSensitiveValue("is_answer_correct", isAnswerCorrect.toString())
      }
    }

    /** The [EventActivityContext] corresponding to [TopicEventContext]s. */
    class TopicContext(
      activityName: String,
      value: TopicEventContext
    ) : EventActivityContext<TopicEventContext>(activityName, value) {
      override fun TopicEventContext.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("topic_id", topicId)
      }
    }

    /** [EventActivityContext] corresponding to sensitive string properties. */
    class SensitiveStringContext(
      activityName: String,
      value: String,
      private val propertyName: String
    ) : EventActivityContext<String>(activityName, value) {
      override fun String.storeValue(store: PropertyStore) {
        store.putSensitiveValue(propertyName, this)
      }
    }

    /** [EventActivityContext] corresponding to events with no constituent properties. */
    class EmptyContext(activityName: String) : EventActivityContext<Unit>(activityName, Unit) {
      override fun Unit.storeValue(store: PropertyStore) {}
    }
  }

  /*** Represents an [OppiaMetricLog] loggable metric (denoted by [LoggableMetricTypeCase]).*/
  private sealed class PerformanceMetricsLoggableMetricType<T>(
    val metricName: String,
    private val value: T
  ) {
    /**
     * Stores the value of this context (i.e. its constituent properties which may correspond to
     * other [LoggableMetricTypeCase]s).
     */
    fun storeValue(store: PropertyStore) = value.storeValue(store)

    /** Method that should be overridden by base classes to satisfy the contract of [storeValue]. */
    protected abstract fun T.storeValue(store: PropertyStore)

    /** The [LoggableMetricTypeCase] corresponding to [ApkSizePerformanceLoggableMetric]. */
    class ApkSizeLoggableMetric(
      metricName: String,
      value: OppiaMetricLog.ApkSizeMetric
    ) : PerformanceMetricsLoggableMetricType<OppiaMetricLog.ApkSizeMetric>(metricName, value) {
      override fun OppiaMetricLog.ApkSizeMetric.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("apk_size_bytes", apkSizeBytes)
      }
    }

    /** The [LoggableMetricTypeCase] corresponding to [StorageUsagePerformanceLoggableMetric]. */
    class StorageUsageLoggableMetric(
      metricName: String,
      value: OppiaMetricLog.StorageUsageMetric
    ) : PerformanceMetricsLoggableMetricType<OppiaMetricLog.StorageUsageMetric>(metricName, value) {
      override fun OppiaMetricLog.StorageUsageMetric.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("storage_usage_bytes", storageUsageBytes)
      }
    }

    /** The [LoggableMetricTypeCase] corresponding to [StartupLatencyPerformanceLoggableMetric]. */
    class StartupLatencyLoggableMetric(
      metricName: String,
      value: OppiaMetricLog.StartupLatencyMetric
    ) :
      PerformanceMetricsLoggableMetricType<OppiaMetricLog.StartupLatencyMetric>(metricName, value) {
      override fun OppiaMetricLog.StartupLatencyMetric.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("startup_latency_millis", startupLatencyMillis)
      }
    }

    /** The [LoggableMetricTypeCase] corresponding to [MemoryUsagePerformanceLoggableMetric]. */
    class MemoryUsageLoggableMetric(
      metricName: String,
      value: OppiaMetricLog.MemoryUsageMetric
    ) : PerformanceMetricsLoggableMetricType<OppiaMetricLog.MemoryUsageMetric>(metricName, value) {
      override fun OppiaMetricLog.MemoryUsageMetric.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("total_pss_bytes", totalPssBytes)
      }
    }

    /** The [LoggableMetricTypeCase] corresponding to [NetworkUsagePerformanceLoggableMetric]. */
    class NetworkUsageLoggableMetric(
      metricName: String,
      value: OppiaMetricLog.NetworkUsageMetric
    ) : PerformanceMetricsLoggableMetricType<OppiaMetricLog.NetworkUsageMetric>(metricName, value) {
      override fun OppiaMetricLog.NetworkUsageMetric.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("bytes_received", bytesReceived)
        store.putNonSensitiveValue("bytes_sent", bytesSent)
      }
    }

    /** The [LoggableMetricTypeCase] corresponding to [CpuUsagePerformanceLoggableMetric]. */
    class CpuUsageLoggableMetric(
      metricName: String,
      value: OppiaMetricLog.CpuUsageMetric
    ) : PerformanceMetricsLoggableMetricType<OppiaMetricLog.CpuUsageMetric>(metricName, value) {
      override fun OppiaMetricLog.CpuUsageMetric.storeValue(store: PropertyStore) {
        store.putNonSensitiveValue("cpu_usage", cpuUsageMetric)
      }
    }
  }

  private fun EventLog.Priority.toAnalyticsName() = when (this) {
    EventLog.Priority.PRIORITY_UNSPECIFIED -> "unspecified_priority"
    EventLog.Priority.ESSENTIAL -> "essential"
    EventLog.Priority.OPTIONAL -> "optional"
    EventLog.Priority.UNRECOGNIZED -> "unknown_priority"
  }

  private fun OppiaMetricLog.Priority.toAnalyticsName() = when (this) {
    OppiaMetricLog.Priority.PRIORITY_UNSPECIFIED -> "unspecified_priority"
    OppiaMetricLog.Priority.LOW_PRIORITY -> "low_priority"
    OppiaMetricLog.Priority.MEDIUM_PRIORITY -> "medium_priority"
    OppiaMetricLog.Priority.HIGH_PRIORITY -> "high_priority"
    OppiaMetricLog.Priority.UNRECOGNIZED -> "unknown_priority"
  }

  private fun OppiaMetricLog.MemoryTier.toAnalyticsName() = when (this) {
    OppiaMetricLog.MemoryTier.MEMORY_TIER_UNSPECIFIED -> "unspecified_memory_tier"
    OppiaMetricLog.MemoryTier.LOW_MEMORY_TIER -> "low_memory"
    OppiaMetricLog.MemoryTier.MEDIUM_MEMORY_TIER -> "medium_memory"
    OppiaMetricLog.MemoryTier.HIGH_MEMORY_TIER -> "high_memory"
    OppiaMetricLog.MemoryTier.UNRECOGNIZED -> "unknown_memory_tier"
  }

  private fun OppiaMetricLog.StorageTier.toAnalyticsName() = when (this) {
    OppiaMetricLog.StorageTier.STORAGE_TIER_UNSPECIFIED -> "unspecified_storage_tier"
    OppiaMetricLog.StorageTier.LOW_STORAGE -> "low_storage"
    OppiaMetricLog.StorageTier.MEDIUM_STORAGE -> "medium_storage"
    OppiaMetricLog.StorageTier.HIGH_STORAGE -> "high_storage"
    OppiaMetricLog.StorageTier.UNRECOGNIZED -> "unknown_storage_tier"
  }

  private fun OppiaMetricLog.NetworkType.toAnalyticsName() = when (this) {
    OppiaMetricLog.NetworkType.NETWORK_UNSPECIFIED -> "unspecified_network_type"
    OppiaMetricLog.NetworkType.WIFI -> "wifi"
    OppiaMetricLog.NetworkType.CELLULAR -> "cellular"
    OppiaMetricLog.NetworkType.NONE -> "none"
    OppiaMetricLog.NetworkType.UNRECOGNIZED -> "unknown_network_type"
  }

  private fun OppiaMetricLog.CurrentScreen.toAnalyticsName() = when (this) {
    OppiaMetricLog.CurrentScreen.SCREEN_UNSPECIFIED -> "unspecified_current_screen"
    OppiaMetricLog.CurrentScreen.HOME_SCREEN -> "home_screen"
    OppiaMetricLog.CurrentScreen.UNRECOGNIZED -> "unknown_screen_name"
    // TODO(#4340): Add support for all screens which are going to be used for metric logging.
  }
}
