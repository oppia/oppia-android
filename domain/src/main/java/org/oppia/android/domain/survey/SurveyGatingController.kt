package org.oppia.android.domain.survey

import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.ExplorationActiveTimeController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.NpsSurveyGracePeriodInDays
import org.oppia.android.util.platformparameter.NpsSurveyMinimumAggregateLearningTimeInATopicInMinutes
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val GET_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID =
  "get_survey_last_shown_timestamp_provider_id"
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
  private val explorationActiveTimeController: ExplorationActiveTimeController,
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
   * Returns a data provider containing the outcome of gating, which will be used by callers to
   * determine if a survey can be shown.
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
    val surveyLastShownCalendar = oppiaClock.getCurrentCalendar()
    surveyLastShownCalendar.timeInMillis = lastShownTimestampMs

    val currentCalendar = oppiaClock.getCurrentCalendar()
    val showNextCalendar = currentCalendar.timeInMillis.plus(gracePeriodMillis)

    return currentCalendar.after(showNextCalendar) ||
      currentCalendar.equals(showNextCalendar)
  }

  private fun retrieveSurveyLastShownDate(profileId: ProfileId): DataProvider<Long> {
    return profileManagementController.retrieveSurveyLastShownTimestamp(profileId)
      .transformAsync(GET_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID) { lastShownTimestampMs ->
        AsyncResult.Success(lastShownTimestampMs)
      }
  }

  private fun hasReachedMinimumTopicLearningThreshold(topicLearningTimeMs: Long): Boolean {
    return topicLearningTimeMs >= minimumLearningTimeForGatingMillis
  }

  private fun retrieveAggregateLearningTime(
    profileId: ProfileId,
    topicId: String
  ): DataProvider<Long> {
    return explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
      profileId, topicId
    ).transformAsync(GET_TOPIC_LEARNING_TIME_PROVIDER_ID) { topicLearningTime ->
      val aggregateLearningTimeMs = topicLearningTime.topicLearningTimeMs
      AsyncResult.Success(aggregateLearningTimeMs)
    }
  }
}
