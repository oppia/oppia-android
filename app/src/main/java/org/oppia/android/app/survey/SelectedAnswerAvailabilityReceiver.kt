package org.oppia.android.app.survey

import org.oppia.android.app.model.SurveySelectedAnswer

/** A handler for receiving any change in answer availability to update the 'next' button. */
interface SelectedAnswerAvailabilityReceiver {
  /** Called when the input answer availability changes. */
  fun onPendingAnswerAvailabilityCheck(inputAnswerAvailable: Boolean)
}

/** A callback that will be called when a user submits an answer. */
interface SelectedAnswerHandler {
  /** Return the current selected answer that is ready for submission. */
  fun getMultipleChoiceAnswer(selectedAnswer: SurveySelectedAnswer)

  /** Return the current text answer that is ready for submission. */
  fun getFreeFormAnswer(answer: SurveySelectedAnswer)
}

/** A handler for restoring the previous saved answer for a question on back/forward navigation. */
interface PreviousAnswerHandler {
  /** Called when an ephemeral question is loaded to retrieve the previously saved answer. */
  fun getPreviousAnswer(): SurveySelectedAnswer? {
    return null
  }

  /** Called after a previously saved answer is retrieved to update the UI. */
  fun restorePreviousAnswer(previousAnswer: SurveySelectedAnswer)
}
