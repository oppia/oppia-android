package org.oppia.android.app.survey

import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.SurveyQuestionName
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
  }

  private fun getQuestionText(
    questionName: SurveyQuestionName
  ): String {
    return when (questionName) {
      SurveyQuestionName.USER_TYPE -> resourceHandler.getStringInLocale(
        R.string.user_type_question
      )
      SurveyQuestionName.MARKET_FIT -> resourceHandler.getStringInLocale(
        R.string.market_fit_question
      )
      SurveyQuestionName.NPS -> resourceHandler.getStringInLocale(
        R.string.nps_score_question
      )
      SurveyQuestionName.PROMOTER_FEEDBACK -> resourceHandler.getStringInLocale(
        R.string.nps_promoter_feedback_question
      )
      SurveyQuestionName.PASSIVE_FEEDBACK -> resourceHandler.getStringInLocale(
        R.string.nps_passive_feedback_question
      )
      SurveyQuestionName.DETRACTOR_FEEDBACK -> resourceHandler.getStringInLocale(
        R.string.nps_detractor_feedback_question
      )
      SurveyQuestionName.UNRECOGNIZED, SurveyQuestionName.QUESTION_NAME_UNSPECIFIED -> ""
    }
  }

  private companion object {
    private const val DEFAULT_QUESTION_PROGRESS = 25
    private const val DEFAULT_QUESTION = "some_question"
  }
}
