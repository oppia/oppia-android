package org.oppia.android.app.survey

import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.survey.surveyitemviewmodel.SurveyAnswerItemViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class SurveyViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  val itemList: ObservableList<SurveyAnswerItemViewModel> = ObservableArrayList()
  val itemIndex = ObservableField<Int>()
  private val canMoveToNextQuestion = ObservableField(false)

  val progressPercentage = ObservableField(0)

  val questionProgressText: ObservableField<String> =
    ObservableField("$DEFAULT_QUESTION_PROGRESS%")

  val questionText: ObservableField<String> =
    ObservableField(DEFAULT_QUESTION)

  fun updateQuestionProgress(
    progressPercentage: Int
  ) {
    this.progressPercentage.set(progressPercentage)
    questionProgressText.set("$progressPercentage%")
  }

  fun updateQuestionText(questionName: SurveyQuestionName) {
    questionText.set(getQuestionText(questionName))
    setCanMoveToNextQuestion(false)
  }

  fun setCanMoveToNextQuestion(canMoveToNext: Boolean) =
    this.canMoveToNextQuestion.set(canMoveToNext)

  fun getCanMoveToNextQuestion(): ObservableField<Boolean> = canMoveToNextQuestion

  fun retrievePreviousAnswer(
    previousAnswer: SurveySelectedAnswer,
    retrieveAnswerHandler: (List<SurveyAnswerItemViewModel>) -> PreviousAnswerHandler?
  ) {
    restorePreviousAnswer(
      previousAnswer,
      retrieveAnswerHandler(
        itemList
      )
    )
  }

  private fun restorePreviousAnswer(
    previousAnswer: SurveySelectedAnswer,
    answerHandler: PreviousAnswerHandler?
  ) {
    answerHandler?.restorePreviousAnswer(previousAnswer)
  }

  private fun getQuestionText(
    questionName: SurveyQuestionName
  ): String {
    val appName = resourceHandler.getStringInLocale(R.string.app_name)
    return when (questionName) {
      SurveyQuestionName.USER_TYPE -> resourceHandler.getStringInLocale(
        R.string.survey_activity_user_type_question
      )
      SurveyQuestionName.MARKET_FIT -> resourceHandler.getStringInLocaleWithWrapping(
        R.string.survey_activity_market_fit_question, appName
      )
      SurveyQuestionName.NPS -> resourceHandler.getStringInLocaleWithWrapping(
        R.string.survey_activity_nps_score_question, appName
      )
      SurveyQuestionName.PROMOTER_FEEDBACK -> resourceHandler.getStringInLocaleWithWrapping(
        R.string.survey_activity_nps_promoter_feedback_question, appName
      )
      SurveyQuestionName.PASSIVE_FEEDBACK -> resourceHandler.getStringInLocaleWithWrapping(
        R.string.survey_activity_nps_passive_feedback_question
      )
      SurveyQuestionName.DETRACTOR_FEEDBACK -> resourceHandler.getStringInLocaleWithWrapping(
        R.string.survey_activity_nps_detractor_feedback_question
      )
      SurveyQuestionName.UNRECOGNIZED, SurveyQuestionName.QUESTION_NAME_UNSPECIFIED ->
        DEFAULT_QUESTION
    }
  }

  private companion object {
    private const val DEFAULT_QUESTION_PROGRESS = 25
    private const val DEFAULT_QUESTION = "some_question"
  }
}
