package org.oppia.android.domain.survey

import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicLearningTime
import org.oppia.android.domain.exploration.ExplorationActiveTimeController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.NpsSurveyGracePeriodInDays
import org.oppia.android.util.platformparameter.NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val GET_TOPIC_LEARNING_TIME_PROVIDER_ID =
  "get_topic_learning_time_provider_id"
private const val GATING_RESULT_PROVIDER_ID =
  "gating_result_provider_id"

/**
 * Controller for retrieving survey gating criteria and deciding if a survey should be shown.
 */
class SurveyGatingController @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val oppiaClock: OppiaClock,
  private val activeTimeController: ExplorationActiveTimeController,
  private val machineLocale: OppiaLocale.MachineLocale,
  @NpsSurveyGracePeriodInDays private val surveyGracePeriodInDays: PlatformParameterValue<Int>,
  @NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
  private val surveyMinimumAggregateLearningTimeInATopicInMinutes: PlatformParameterValue<Int>
) {
  private val gracePeriodMillis = TimeUnit.DAYS.toMillis(surveyGracePeriodInDays.value.toLong())
  private val minimumLearningTimeForGatingMillis = TimeUnit.MINUTES.toMillis(
    surveyMinimumAggregateLearningTimeInATopicInMinutes.value.toLong()
  )

  /**
   * Returns a data provider containing a boolean outcome of gating, which informs callers whether
   * a survey can be shown.
   */
  fun maybeShowSurvey(profileId: ProfileId, topicId: String): DataProvider<Boolean> {
    val lastShownDateProvider = retrieveSurveyLastShownDate(profileId)
    val learningTimeProvider = retrieveAggregateLearningTime(profileId, topicId)
    return lastShownDateProvider.combineWith(
      learningTimeProvider, GATING_RESULT_PROVIDER_ID
    ) { lastShownTimestampMs, learningTimeMs ->
      isSurveyGracePeriodEnded(lastShownTimestampMs) &&
        hasReachedMinimumTopicLearningThreshold(learningTimeMs) &&
        isWithinSurveyTimeWindow()
    }
  }

  private fun isWithinSurveyTimeWindow(): Boolean = when (machineLocale.getCurrentTimeOfDay()) {
    OppiaLocale.MachineLocale.TimeOfDay.MID_MORNING,
    OppiaLocale.MachineLocale.TimeOfDay.AFTERNOON,
    OppiaLocale.MachineLocale.TimeOfDay.EVENING -> true
    OppiaLocale.MachineLocale.TimeOfDay.EARLY_MORNING,
    OppiaLocale.MachineLocale.TimeOfDay.LATE_NIGHT,
    OppiaLocale.MachineLocale.TimeOfDay.UNKNOWN -> false
  }

  private fun isSurveyGracePeriodEnded(lastShownTimestampMs: Long): Boolean {
    val currentTimeStamp = oppiaClock.getCurrentTimeMs()
    val showNextTimestamp = lastShownTimestampMs + gracePeriodMillis

    return currentTimeStamp >= showNextTimestamp
  }

  private fun retrieveSurveyLastShownDate(profileId: ProfileId) =
    profileManagementController.retrieveSurveyLastShownTimestamp(profileId)

  private fun hasReachedMinimumTopicLearningThreshold(topicLearningTimeMs: Long): Boolean {
    return topicLearningTimeMs >= minimumLearningTimeForGatingMillis
  }

  private fun retrieveAggregateLearningTime(
    profileId: ProfileId,
    topicId: String
  ): DataProvider<Long> {
    return activeTimeController.retrieveAggregateTopicLearningTimeDataProvider(
      profileId, topicId
    ).transform(
      GET_TOPIC_LEARNING_TIME_PROVIDER_ID,
      TopicLearningTime::getTopicLearningTimeMs
    )
  }
}
