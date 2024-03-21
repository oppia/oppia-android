package org.oppia.android.domain.survey

import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Survey
import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val CREATE_SURVEY_PROVIDER_ID = "create_survey_provider_id"
private const val START_SURVEY_SESSION_PROVIDER_ID = "start_survey_session_provider_id"
private const val CREATE_QUESTIONS_LIST_PROVIDER_ID = "create_questions_list_provider_id"

/**
 * Controller for creating and retrieving all attributes of a survey.
 *
 * Only one survey is shown at a time, and its progress is controlled by the
 * [SurveyProgressController].
 */
@Singleton
class SurveyController @Inject constructor(
  private val dataProviders: DataProviders,
  private val surveyProgressController: SurveyProgressController,
  private val exceptionsController: ExceptionsController
) {
  private val surveyId = UUID.randomUUID().toString()

  /**
   * Starts a new survey session with a list of questions.
   *
   * @property mandatoryQuestionNames a list of uniques names of the questions that will be
   * generated for this survey. Callers should be aware that the order of questions is important as
   * the list will be indexed and displayed in the provided order.
   * @return a [DataProvider] indicating whether the session start was successful
   */
  fun startSurveySession(
    mandatoryQuestionNames: List<SurveyQuestionName>,
    showOptionalQuestion: Boolean = true,
    profileId: ProfileId
  ): DataProvider<Any?> {
    return try {
      val createSurveyDataProvider =
        createSurvey(mandatoryQuestionNames, showOptionalQuestion)
      val questionsListDataProvider =
        createSurveyDataProvider.transform(CREATE_QUESTIONS_LIST_PROVIDER_ID) { survey ->
          if (survey.hasOptionalQuestion()) {
            survey.mandatoryQuestionsList + survey.optionalQuestion
          } else survey.mandatoryQuestionsList
        }
      surveyProgressController.beginSurveySession(surveyId, profileId, questionsListDataProvider)
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      dataProviders.createInMemoryDataProviderAsync(START_SURVEY_SESSION_PROVIDER_ID) {
        AsyncResult.Failure(e)
      }
    }
  }

  private fun createSurvey(
    mandatoryQuestionNames: List<SurveyQuestionName>,
    showOptionalQuestion: Boolean
  ): DataProvider<Survey> {
    val mandatoryQuestionsList = mandatoryQuestionNames.mapIndexed { index, questionName ->
      createSurveyQuestion(index.toString(), questionName)
    }
    // The questionId corresponds to the order of the questions in list, so the optional question
    // will always come at the end of the list.
    val surveyBuilder = Survey.newBuilder()
      .setSurveyId(surveyId)
      .addAllMandatoryQuestions(mandatoryQuestionsList)

    if (showOptionalQuestion) {
      surveyBuilder.optionalQuestion =
        createDefaultFeedbackQuestion(mandatoryQuestionsList.size.toString())
    }

    return dataProviders.createInMemoryDataProvider(CREATE_SURVEY_PROVIDER_ID) {
      surveyBuilder.build()
    }
  }

  private fun createSurveyQuestion(
    questionId: String,
    questionName: SurveyQuestionName
  ): SurveyQuestion {
    return SurveyQuestion.newBuilder()
      .setQuestionId(questionId)
      .setQuestionName(questionName)
      .build()
  }

  private fun createDefaultFeedbackQuestion(
    questionId: String
  ): SurveyQuestion {
    return SurveyQuestion.newBuilder()
      .setQuestionId(questionId)
      .setQuestionName(SurveyQuestionName.PROMOTER_FEEDBACK)
      .setFreeFormText(true)
      .build()
  }

  /**
   * Finishes the most recent session started by [startSurveySession].
   *
   * This method should only be called if there is an active session, otherwise the
   * resulting provider will fail. Note that this doesn't actually need to be called between
   * sessions unless the caller wants to ensure other providers monitored from
   * [SurveyProgressController] are reset to a proper out-of-session state.
   *
   * Note that the returned provider monitors the long-term stopping state of survey sessions and
   * will be reset to 'pending' when a session is currently active, or before any session has
   * started.
   */
  fun stopSurveySession(surveyCompleted: Boolean): DataProvider<Any?> =
    surveyProgressController.endSurveySession(surveyCompleted)
}
