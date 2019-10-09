package org.oppia.domain.question

import org.oppia.app.model.Question
import javax.inject.Inject
import javax.inject.Singleton


/** Controller for retrieving an exploration. */
@Singleton
class QuestionAssessmentProgressController @Inject constructor(
) {
  fun beginQuestionTrainingSession(questionsList: List<Question>) {
  }

  fun finishQuestionTrainingSession() {

  }
}
