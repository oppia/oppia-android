package org.oppia.android.domain.survey

import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.ExplorationActiveTimeController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val SURVEY_MINIMUM_TOPIC_LEARNING_TIME_MINUTES = 5

private const val GET_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID =
  "get_survey_last_shown_timestamp_provider_id"
private const val SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID =
  "survey_last_shown_timestamp_provider_id"
private const val GET_TOPIC_LEARNING_TIME_PROVIDER_ID =
  "get_topic_learning_time_provider_id"
private const val TOPIC_LEARNING_TIME_PROVIDER_ID =
  "topic_learning_time_provider_id"

/**
 * Controller for retrieving survey gating criteria and deciding if a survey should be shown.
 */
class SurveyGatingController @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val oppiaClock: OppiaClock,
  private val explorationActiveTimeController: ExplorationActiveTimeController,
  private val machineLocale: OppiaLocale.MachineLocale
) {

  private val surveyGracePeriodMillis = TimeUnit.DAYS.toMillis(30)

  /**
   * Returns a boolean indicating whether a survey can be shown.
   */
  fun shouldShowSurvey(profileId: ProfileId, topicId: String) {}

  private fun isSurveyTimeOfDayWindowOpen(): Boolean = when (machineLocale.getCurrentTimeOfDay()) {
    OppiaLocale.MachineLocale.TimeOfDay.MORNING,
    OppiaLocale.MachineLocale.TimeOfDay.AFTERNOON,
    OppiaLocale.MachineLocale.TimeOfDay.EVENING -> true
    OppiaLocale.MachineLocale.TimeOfDay.EARLY_MORNING,
    OppiaLocale.MachineLocale.TimeOfDay.NIGHT,
    OppiaLocale.MachineLocale.TimeOfDay.UNKNOWN -> false
  }

  private suspend fun isSurveyLastShownDateLimitPassed(profileId: ProfileId): AsyncResult<Boolean> {
    return getSurveyLastShownDateMs(profileId)
      .transform(SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID) { lastShownTimestampMs ->
        val surveyLastShownCalendar = oppiaClock.getCurrentCalendar()
        surveyLastShownCalendar.timeInMillis = lastShownTimestampMs

        val currentCalendar = oppiaClock.getCurrentCalendar()
        val showNextCalendar = currentCalendar.timeInMillis.plus(surveyGracePeriodMillis)

        currentCalendar.after(showNextCalendar) ||
          currentCalendar.equals(showNextCalendar)
      }.retrieveData()
  }

  private fun getSurveyLastShownDateMs(profileId: ProfileId): DataProvider<Long> {
    return profileManagementController.fetchSurveyLastShownTimestamp(profileId)
      .transform(GET_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID) { lastShownTimestampMs ->
        lastShownTimestampMs
      }
  }

  private suspend fun isThereMinimumAggregateTopicLearningTime(
    profileId: ProfileId,
    topicId: String
  ): AsyncResult<Boolean> {
    return getAggregateLearningTime(profileId, topicId)
      .transform(TOPIC_LEARNING_TIME_PROVIDER_ID) { topicLearningTimeMs ->
        topicLearningTimeMs >= SURVEY_MINIMUM_TOPIC_LEARNING_TIME_MINUTES
      }.retrieveData()
  }

  private fun getAggregateLearningTime(profileId: ProfileId, topicId: String): DataProvider<Long> {
    return explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
      profileId, topicId
    ).transform(GET_TOPIC_LEARNING_TIME_PROVIDER_ID) { topicLearningTime ->
      topicLearningTime.topicLearningTimeMs
    }
  }
}
