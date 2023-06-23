package org.oppia.android.domain.survey

import org.oppia.android.app.model.SurveyQuestion
import org.oppia.android.app.model.SurveyQuestionName

/** Computes the next question in the deck and provides lookup access for [SurveyQuestion]s. */
class SurveyQuestionGraph constructor(
  private var questionList: MutableList<SurveyQuestion>
) {
  /** Returns the [SurveyQuestion] corresponding to the provided index. */
  fun getQuestion(questionIndex: Int): SurveyQuestion = questionList[questionIndex]

  /** Decides which feedback question should be shown based on a user's nps score selection. */
  fun computeFeedbackQuestion(index: Int, npsScore: Int) {
    when (npsScore) {
      in 9..10 -> questionList[index] = createQuestion(index, SurveyQuestionName.PROMOTER_FEEDBACK)
      in 7..8 -> questionList[index] = createQuestion(index, SurveyQuestionName.PASSIVE_FEEDBACK)
      else -> questionList[index] = createQuestion(index, SurveyQuestionName.DETRACTOR_FEEDBACK)
    }
  }

  private fun createQuestion(questionId: Int, questionName: SurveyQuestionName): SurveyQuestion {
    return SurveyQuestion.newBuilder()
      .setQuestionId(questionId.toString())
      .setQuestionName(questionName)
      .build()
  }
}
