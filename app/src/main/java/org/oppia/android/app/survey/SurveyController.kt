package org.oppia.android.app.survey

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.MarketFitAnswer
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Survey
import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveyQuestionOption
import org.oppia.android.app.model.SurveyQuestionOptionList
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith

private const val CREATE_SURVEY_PROVIDER_ID = "create_survey_provider_id"
private const val START_SURVEY_SESSION_PROVIDER_ID = "start_survey_session_provider_id"
private const val TEMP_QUESTIONS_LIST_DATA_PROVIDER_ID = "temporary_id"

/** Controller that creates and retrieves all attributes of a survey. */
@Singleton
class SurveyController @Inject constructor(
  private val dataProviders: DataProviders,
  private val surveyProgressController: SurveyProgressController
) {
  private val surveyId = UUID.randomUUID().toString()

  private fun createSurvey(): DataProvider<Survey> {
    return dataProviders.createInMemoryDataProvider(CREATE_SURVEY_PROVIDER_ID) {
      Survey.newBuilder()
        .setSurveyId(surveyId)
        .build()
    }
  }

  // todo add kdocs
  // todo do something with list of question names
  fun startSurveySession(
    profileId: ProfileId
  ): DataProvider<Any?> {
    return try {
      val createSurveyDataProvider = createSurvey()
      // todo replace with real list
      val questionsListDataProvider =
        dataProviders.createInMemoryDataProvider(TEMP_QUESTIONS_LIST_DATA_PROVIDER_ID) {
          createQuestions()
        }
      val beginSessionDataProvider =
        surveyProgressController.beginSurveySession(profileId, questionsListDataProvider)

      beginSessionDataProvider.combineWith(
        createSurveyDataProvider, START_SURVEY_SESSION_PROVIDER_ID
      ) { sessionResult, _ -> sessionResult }
    } catch (e: Exception) {
      // check out exceptionsController
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

  private fun createQuestions() = listOf(
    createUserTypeQuestion(),
    createMarketFitQuestion(),
    createNpsScoreQuestion()
  )

  private fun createUserTypeQuestion(): SurveyQuestion {
    val userTypeOptions = UserTypeAnswer.values()
      .filter { it.isValid() }
      .map { userType ->
        SurveyQuestionOption.newBuilder()
          .setUserType(userType)
          .build()
      }

    return SurveyQuestion.newBuilder()
      .setQuestionId("user_type_question")
      .setQuestionName(SurveyQuestionName.USER_TYPE)
      .setMultipleChoiceWithOther(
        SurveyQuestionOptionList.newBuilder()
          .addAllOptions(userTypeOptions)
          .build()
      )
      .setLanguage(OppiaLanguage.ENGLISH) // TODO: get app language
      .build()
  }

  private fun createMarketFitQuestion(): SurveyQuestion {
    val marketFitOptions = MarketFitAnswer.values()
      .filter { it.isValid() }
      .map { marketFitAnswer ->
        SurveyQuestionOption.newBuilder()
          .setMarketFit(marketFitAnswer)
          .build()
      }

    return SurveyQuestion.newBuilder()
      .setQuestionId("market_fit_question")
      .setQuestionName(SurveyQuestionName.MARKET_FIT)
      .setMultipleChoice(
        SurveyQuestionOptionList.newBuilder()
          .addAllOptions(marketFitOptions)
          .build()
      )
      .setLanguage(OppiaLanguage.ENGLISH) // TODO: get app language
      .build()
  }

  private fun createNpsScoreQuestion(): SurveyQuestion {
    val npsOptions = (0..10).map { npsScore ->
      SurveyQuestionOption.newBuilder()
        .setNpsScore(npsScore)
        .build()
    }

    return SurveyQuestion.newBuilder()
      .setQuestionId("nps_question")
      .setQuestionName(SurveyQuestionName.NPS)
      .setMultipleChoice(
        SurveyQuestionOptionList.newBuilder()
          .addAllOptions(npsOptions)
          .build()
      )
      .setLanguage(OppiaLanguage.ENGLISH) // TODO: get app language
      .build()
  }

  companion object {
    /** Returns whether a [MarketFitAnswer] is valid */

    fun MarketFitAnswer.isValid(): Boolean {
      return when (this) {
        MarketFitAnswer.UNRECOGNIZED, MarketFitAnswer.MARKET_FIT_ANSWER_UNSPECIFIED -> false
        else -> true
      }
    }

    /** Returns whether a [UserTypeAnswer] is valid */

    fun UserTypeAnswer.isValid(): Boolean {
      return when (this) {
        UserTypeAnswer.UNRECOGNIZED, UserTypeAnswer.USER_TYPE_UNSPECIFIED -> false
        else -> true
      }
    }
  }
}
