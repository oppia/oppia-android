package org.oppia.android.app.survey

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.Survey
import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveyQuestionOption
import org.oppia.android.app.model.SurveyQuestionOptionList
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith

private const val CREATE_SURVEY_PROVIDER_ID = "create_survey_provider_id"
private const val START_SURVEY_SESSION_PROVIDER_ID = "start_survey_session_provider_id"
private const val CREATE_QUESTIONS_LIST_PROVIDER_ID = "create_questions_list_provider_id"

/** Controller for creating and retrieving all attributes of a survey.
 *
 * Only one survey is shown at a time, and its progress is controlled by the
 * [SurveyProgressController]
 */
@Singleton
class SurveyController @Inject constructor(
  private val dataProviders: DataProviders,
  private val surveyProgressController: SurveyProgressController,
  private val exceptionsController: ExceptionsController,
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
   * @return a [DataProvider] indicating whether the session start was successful.
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

  // todo refine and move to progress controller
  fun getQuestion(): DataProvider<SurveyQuestionName> {
    return dataProviders.createInMemoryDataProvider("create_random_question_provider") {
      SurveyQuestionName.USER_TYPE
    }
  }

  private fun createSurveyQuestions(): List<SurveyQuestion> {
    return SurveyQuestionName.values()
      .filter { it.isValid() }
      .map { questionName ->
        createSurveyQuestion(
          questionName,
          OppiaLanguage.ENGLISH,
          SurveyQuestion.QuestionTypeCase.MULTIPLE_CHOICE_WITH_OTHER
        )
      }
  }

  private fun createSurveyQuestion(
    questionName: SurveyQuestionName,
    language: OppiaLanguage,
    questionType: SurveyQuestion.QuestionTypeCase
  ): SurveyQuestion {
    val surveyQuestionBuilder = SurveyQuestion.newBuilder()
      .setQuestionName(questionName)
      .setLanguage(language)

    return when (questionType) {
      SurveyQuestion.QuestionTypeCase.MULTIPLE_CHOICE_WITH_OTHER -> {
        surveyQuestionBuilder.setMultipleChoice(createUserTypeAnswerOptions()).build()
      }
      SurveyQuestion.QuestionTypeCase.MULTIPLE_CHOICE -> {
        surveyQuestionBuilder.setMultipleChoice(createMarketFitAnswerOptions()).build()
      }
      SurveyQuestion.QuestionTypeCase.FREE_FORM_TEXT -> {
        surveyQuestionBuilder.setFreeFormText(true).build()
      }
      else -> SurveyQuestion.getDefaultInstance()
    }
  }

  // todo maybe add question_option_id
  private fun createUserTypeAnswerOptions(): SurveyQuestionOptionList {
    val userTypeOptions = UserTypeAnswer.values()
      .filter { it.isValid() }
      .map { userType ->
        SurveyQuestionOption.newBuilder()
          .setUserType(userType)
          .build()
      }
    return SurveyQuestionOptionList.newBuilder()
      .addAllOptions(userTypeOptions)
      .build()
  }

  private fun createMarketFitAnswerOptions(): SurveyQuestionOptionList {
    val marketFitOptions = MarketFitAnswer.values()
      .filter { it.isValid() }
      .map { marketFitAnswer ->
        SurveyQuestionOption.newBuilder()
          .setMarketFit(marketFitAnswer)
          .build()
      }
    return SurveyQuestionOptionList.newBuilder()
      .addAllOptions(marketFitOptions)
      .build()
  }

  private fun createNpsScoreAnswerOptions(): SurveyQuestionOptionList {
    val npsOptions = (0..10).map { npsScore ->
      SurveyQuestionOption.newBuilder()
        .setNpsScore(npsScore)
        .build()
    }
    return SurveyQuestionOptionList.newBuilder()
      .addAllOptions(npsOptions)
      .build()
  }

  companion object {
    /** Returns whether a [MarketFitAnswer] is valid. */
    fun MarketFitAnswer.isValid(): Boolean {
      return when (this) {
        MarketFitAnswer.UNRECOGNIZED, MarketFitAnswer.MARKET_FIT_ANSWER_UNSPECIFIED -> false
        else -> true
      }
    }

    /** Returns whether a [UserTypeAnswer] is valid. */
    fun UserTypeAnswer.isValid(): Boolean {
      return when (this) {
        UserTypeAnswer.UNRECOGNIZED, UserTypeAnswer.USER_TYPE_UNSPECIFIED -> false
        else -> true
      }
    }

    /** Returns whether a [SurveyQuestionName] is valid. */
    fun SurveyQuestionName.isValid(): Boolean {
      return when (this) {
        SurveyQuestionName.UNRECOGNIZED, SurveyQuestionName.QUESTION_NAME_UNSPECIFIED -> false
        else -> true
      }
    }
  }
}
