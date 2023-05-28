package org.oppia.android.app.survey.surveyitemviewmodel

import org.oppia.android.app.model.SurveyQuestionOption
import javax.inject.Inject

class NpsItemsViewModel @Inject constructor() :
  SurveyAnswerItemViewModel(ViewType.NPS_OPTIONS) {
  val optionItems = getNpsOptions()

  private fun getNpsOptions() = (0..10).map { npsScore ->
    SurveyQuestionOption.newBuilder()
      .setNpsScore(npsScore)
      .build()
  }
}
