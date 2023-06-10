package org.oppia.android.domain.survey

import org.oppia.android.app.model.Survey
import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
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

  private fun createSurvey(): DataProvider<Survey> {
    return dataProviders.createInMemoryDataProvider(CREATE_SURVEY_PROVIDER_ID) {
      Survey.newBuilder()
        .setSurveyId(surveyId)
        .build()
    }
  }

  /**
   * Starts a new survey session with a list of questions.
   *
   * @return a [DataProvider] indicating whether the session start was successful
   */
  fun startSurveySession(): DataProvider<Any?> {
    return try {
      val createSurveyDataProvider = createSurvey()
      val questionsListDataProvider =
        dataProviders.createInMemoryDataProvider(CREATE_QUESTIONS_LIST_PROVIDER_ID) {
          createSurveyQuestions()
        }
      val beginSessionDataProvider =
        surveyProgressController.beginSurveySession(questionsListDataProvider)

      beginSessionDataProvider.combineWith(
        createSurveyDataProvider, START_SURVEY_SESSION_PROVIDER_ID
      ) { sessionResult, _ -> sessionResult }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      dataProviders.createInMemoryDataProviderAsync(START_SURVEY_SESSION_PROVIDER_ID) {
        AsyncResult.Failure(e)
      }
    }
  }

  private fun createSurveyQuestions(): List<SurveyQuestion> {
    return SurveyQuestionName.values()
      .filter { it.isValid() }
      .mapIndexed { index, questionName ->
        createSurveyQuestion(
          index.toString(),
          questionName
        )
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

  companion object {
    /** Returns whether a [SurveyQuestionName] is valid. */
    fun SurveyQuestionName.isValid(): Boolean {
      return when (this) {
        SurveyQuestionName.UNRECOGNIZED, SurveyQuestionName.QUESTION_NAME_UNSPECIFIED -> false
        else -> true
      }
    }
  }
}
