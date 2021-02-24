package org.oppia.android.domain.question

import javax.inject.Inject
import javax.inject.Singleton

const val QUESTION_COUNT_PER_TRAINING_SESSION = 10

/** Provider to return any constants required during the training session. */
@Singleton
class QuestionTrainingConstantsProvider @Inject constructor() {
  fun getQuestionCountPerTrainingSession(): Int {
    return QUESTION_COUNT_PER_TRAINING_SESSION
  }
}
