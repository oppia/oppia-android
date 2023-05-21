package org.oppia.android.domain.survey

import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName

/** Computes the next question in the deck and provides lookup access for [SurveyQuestion]s */
class SurveyQuestionGraph constructor(
  private var questionGraph: Map<SurveyQuestionName, SurveyQuestion>
) {
  /** Decides which question should be shown next. */
  fun computeNextQuestion(): SurveyQuestion {
    return SurveyQuestion.getDefaultInstance()
  }

  /** Returns the [SurveyQuestion] corresponding to the provided name */
  fun getQuestion(questionName: SurveyQuestionName): SurveyQuestion {
    return questionGraph.getValue(questionName)
  }
}
