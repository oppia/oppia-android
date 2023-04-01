package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** [StateItemViewModel] for previously submitted answers. */
class SubmittedAnswerViewModel(
  val submittedUserAnswer: UserAnswer,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean,
  private val resourceHandler: AppLanguageResourceHandler
) : StateItemViewModel(ViewType.SUBMITTED_ANSWER) {
  val isCorrectAnswer = ObservableField(DEFAULT_IS_CORRECT_ANSWER)
  val submittedAnswer: ObservableField<CharSequence> = ObservableField(DEFAULT_SUBMITTED_ANSWER)
  val isExtraInteractionAnswerCorrect = ObservableField(DEFAULT_IS_CORRECT_ANSWER)
  val submittedAnswerContentDescription: ObservableField<String> =
    ObservableField(
      computeSubmittedAnswerContentDescription(
        DEFAULT_IS_CORRECT_ANSWER, DEFAULT_SUBMITTED_ANSWER, DEFAULT_ACCESSIBLE_ANSWER
      )
    )
  private var accessibleAnswer: String? = DEFAULT_ACCESSIBLE_ANSWER

  fun setSubmittedAnswer(submittedAnswer: CharSequence, accessibleAnswer: String?) {
    this.submittedAnswer.set(submittedAnswer)
    this.accessibleAnswer = accessibleAnswer
    updateSubmittedAnswerContentDescription()
  }

  fun setIsCorrectAnswer(isCorrectAnswer: Boolean) {
    this.isCorrectAnswer.set(isCorrectAnswer)
    updateSubmittedAnswerContentDescription()
  }

  private fun updateSubmittedAnswerContentDescription() {
    submittedAnswerContentDescription.set(
      computeSubmittedAnswerContentDescription(
        isCorrectAnswer.get() ?: DEFAULT_IS_CORRECT_ANSWER,
        submittedAnswer.get() ?: DEFAULT_SUBMITTED_ANSWER,
        accessibleAnswer
      )
    )
  }

  private fun computeSubmittedAnswerContentDescription(
    isCorrectAnswer: Boolean,
    submittedAnswer: CharSequence,
    accessibleAnswer: String?
  ): String {
    val answer = if (accessibleAnswer.isNullOrBlank()) submittedAnswer else accessibleAnswer
    return if (isCorrectAnswer) {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.correct_submitted_answer_with_append, answer
      )
    } else {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.incorrect_submitted_answer_with_append, answer
      )
    }
  }

  private companion object {
    private const val DEFAULT_IS_CORRECT_ANSWER = false
    private const val DEFAULT_SUBMITTED_ANSWER = ""
    private val DEFAULT_ACCESSIBLE_ANSWER: String? = null
  }
}
