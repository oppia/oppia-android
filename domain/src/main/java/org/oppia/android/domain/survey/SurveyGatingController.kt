package org.oppia.android.domain.survey

import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.TopicLearningTimeController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.system.OppiaClock
import java.util.Calendar
import javax.inject.Inject

private const val SURVEY_LAST_SHOWN_DATE_LIMIT_DAYS = 30
private const val SURVEY_MINIMUM_TOPIC_LEARNING_TIME_MINUTES = 5

private const val SURVEY_LAST_SHOWN_DATE_GATING_PROVIDER_ID =
  "survey_last_shown_date_gating_provider_id"
private const val SURVEY_TIME_OF_DAY_GATING_PROVIDER_ID = "survey_time_of_day_gating_provider_id"
private const val SURVEY_TIME_AND_DATE_COMBINED_GATING_PROVIDER_ID =
  "survey_time_and_date_gating_provider_id"
private const val SURVEY_TOPIC_LEARNING_TIME_GATING_PROVIDER_ID =
  "survey_topic_learning_time_gating_provider_id"
private const val SURVEY_COMBINED_GATING_PROVIDER_ID =
  "survey_topic_learning_time_provider_id"

/**
 * Controller for retrieving survey gating criteria and deciding if a survey should be shown.
 */
class SurveyGatingController @Inject constructor(
  private val dataProviders: DataProviders,
  private val profileManagementController: ProfileManagementController,
  private val oppiaClock: OppiaClock,
  private val topicLearningTimeController: TopicLearningTimeController
) {

  /**
   * Returns a boolean indicating whether a survey can be shown.
   */
  fun shouldShowSurvey(
    profileId: ProfileId,
    topicId: String
  ): DataProvider<Boolean> {
    val timeAndDateGatingProvider = isSurveyTimeOfDayWindowOpen().combineWith(
      getSurveyLastShownDateMs(profileId),
      SURVEY_TIME_AND_DATE_COMBINED_GATING_PROVIDER_ID
    ) { isSurveyWindowOpen, isSurveyDateLimitPassed ->
      isSurveyWindowOpen && isSurveyLastShownDateLimitPassed(isSurveyDateLimitPassed)
    }
    return timeAndDateGatingProvider.combineWith(
      getAggregateLearningTime(profileId, topicId),
      SURVEY_COMBINED_GATING_PROVIDER_ID
    ) { isTimeAndDateAllowed, aggregateLearningTime ->
      isTimeAndDateAllowed && isMinimumAggregateTopicLearningTime(aggregateLearningTime)
    }
  }

  private val morningLimit = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 9)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
  }

  private val eveningLimit = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 22)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
  }

  private fun isSurveyTimeOfDayWindowOpen(): DataProvider<Boolean> {
    return dataProviders.createInMemoryDataProvider(SURVEY_TIME_OF_DAY_GATING_PROVIDER_ID) {
      oppiaClock.getCurrentCalendar().after(morningLimit) &&
        oppiaClock.getCurrentCalendar().before(eveningLimit)
    }
  }

  private fun getSurveyLastShownDateMs(profileId: ProfileId): DataProvider<Long> {
    return profileManagementController.fetchSurveyLastShownTimestamp(profileId)
      .transform(SURVEY_LAST_SHOWN_DATE_GATING_PROVIDER_ID) { lastShownTimestampMs ->
        lastShownTimestampMs
      }
  }

  private fun isSurveyLastShownDateLimitPassed(timestamp: Long): Boolean {
    val surveyLastShownDateCalendar = oppiaClock.getCurrentCalendar()
    surveyLastShownDateCalendar.timeInMillis = timestamp

    // Add the grace period allowed before a survey can be shown again.
    surveyLastShownDateCalendar.add(Calendar.DAY_OF_YEAR, SURVEY_LAST_SHOWN_DATE_LIMIT_DAYS)

    val currentDateCalendar = oppiaClock.getCurrentCalendar()
    return currentDateCalendar.after(surveyLastShownDateCalendar) ||
      currentDateCalendar == surveyLastShownDateCalendar
  }

  private fun isMinimumAggregateTopicLearningTime(
    learningTime: Long
  ): Boolean {
    return learningTime >= SURVEY_MINIMUM_TOPIC_LEARNING_TIME_MINUTES
  }

  private fun getAggregateLearningTime(profileId: ProfileId, topicId: String): DataProvider<Long> {
    return topicLearningTimeController.retrieveAggregateTopicLearningTimeDataProvider(
      profileId, topicId
    ).transform(SURVEY_TOPIC_LEARNING_TIME_GATING_PROVIDER_ID) { topicLearningTime ->
      topicLearningTime.topicLearningTimeMs
    }
  }
}
