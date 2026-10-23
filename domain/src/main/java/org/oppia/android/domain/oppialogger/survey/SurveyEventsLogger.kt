package org.oppia.android.domain.oppialogger.survey

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.LearnerDetailsContext
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.oppialogger.analytics.FirestoreDataController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Convenience logger for survey events.
 *
 * This logger is meant to be used directly in places where survey events have to be logged
 */
@Singleton
class SurveyEventsLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
  private val dataController: FirestoreDataController
) {

  /**
   * Logs an event representing a survey session being started and ended before the
   * mandatory questions are completed.
   */
  fun logAbandonSurvey(
    surveyId: String,
    profileId: LegacyProfileId,
    learnerDetails: LearnerDetailsContext,
    questionName: SurveyQuestionName
  ) {
    analyticsController.logImportantEvent(
      createAbandonSurveyContext(surveyId, profileId, learnerDetails, questionName),
      profileId
    )
  }

  /** Logs an event representing the responses to the m sandatory survey questions. */
  fun logMandatoryResponses(
    surveyId: String,
    profileId: LegacyProfileId,
    learnerDetails: LearnerDetailsContext,
    userTypeAnswer: UserTypeAnswer,
    marketFitAnswer: MarketFitAnswer,
    npsScore: Int
  ) {
    analyticsController.logImportantEvent(
      createMandatorySurveyResponseContext(
        surveyId,
        profileId,
        learnerDetails,
        userTypeAnswer,
        marketFitAnswer,
        npsScore
      ),
      profileId
    )
  }

  /** Logs an event representing the response to the optional survey question. */
  fun logOptionalResponse(
    surveyId: String,
    profileId: LegacyProfileId?,
    learnerDetails: LearnerDetailsContext,
    answer: String
  ) {
    dataController.logEvent(
      createOptionalSurveyResponseContext(surveyId, profileId, learnerDetails, answer),
      profileId
    )
  }

  private fun createMandatorySurveyResponseContext(
    surveyId: String,
    profileId: LegacyProfileId,
    learnerDetails: LearnerDetailsContext,
    userTypeAnswer: UserTypeAnswer,
    marketFitAnswer: MarketFitAnswer,
    npsScore: Int
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setMandatoryResponse(
        EventLog.MandatorySurveyResponseContext.newBuilder()
          .setUserTypeAnswer(userTypeAnswer)
          .setMarketFitAnswer(marketFitAnswer)
          .setNpsScoreAnswer(npsScore)
          .setSurveyDetails(
            createSurveyResponseContext(surveyId, profileId, learnerDetails)
          )
      )
      .build()
  }

  private fun createAbandonSurveyContext(
    surveyId: String,
    profileId: LegacyProfileId,
    learnerDetails: LearnerDetailsContext,
    questionName: SurveyQuestionName
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setAbandonSurvey(
        EventLog.AbandonSurveyContext.newBuilder()
          .setQuestionName(questionName)
          .setSurveyDetails(
            createSurveyResponseContext(surveyId, profileId, learnerDetails)
          )
      )
      .build()
  }

  private fun createSurveyResponseContext(
    surveyId: String,
    profileId: LegacyProfileId?,
    learnerDetails: LearnerDetailsContext
  ): EventLog.SurveyResponseContext {
    return EventLog.SurveyResponseContext.newBuilder()
      .setProfileId(profileId?.internalId.toString())
      .setSurveyId(surveyId)
      .setLearnerDetails(learnerDetails)
      .build()
  }

  private fun createOptionalSurveyResponseContext(
    surveyId: String,
    profileId: LegacyProfileId?,
    learnerDetails: LearnerDetailsContext,
    answer: String
  ): EventLog.Context {
    return EventLog.Context.newBuilder()
      .setOptionalResponse(
        EventLog.OptionalSurveyResponseContext.newBuilder()
          .setFeedbackAnswer(answer)
          .setSurveyDetails(
            createSurveyResponseContext(surveyId, profileId, learnerDetails)
          )
      )
      .build()
  }
}
