package org.oppia.domain.question

import org.oppia.app.model.Question
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller that tracks and reports the learner's ephemeral/non-persisted progress through a question training
 * session. Note that this controller only supports one active training session at a time.
 *
 * The current training session session is started via the question training controller.
 *
 * This class is thread-safe, but the order of applied operations is arbitrary. Calling code should take care to ensure
 * that uses of this class do not specifically depend on ordering.
 */
@Singleton
class QuestionAssessmentProgressController @Inject constructor(
) {
  fun beginQuestionTrainingSession(questionsList: List<Question>) {
  }

  fun finishQuestionTrainingSession() {

  }
}
