package org.oppia.android.app.survey.surveyitemviewmodel

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.survey.SelectedAnswerAvailabilityReceiver
import org.oppia.android.app.survey.SelectedAnswerHandler
import javax.inject.Inject

class FreeFormItemsViewModel @Inject constructor(
  private val selectedAnswerAvailabilityReceiver: SelectedAnswerAvailabilityReceiver,
  private val questionName: SurveyQuestionName,
  private val answerHandler: SelectedAnswerHandler
) : SurveyAnswerItemViewModel(ViewType.FREE_FORM_ANSWER) {
  var answerText: CharSequence = ""
  val isAnswerAvailable = ObservableField(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          selectedAnswerAvailabilityReceiver.onPendingAnswerAvailabilityCheck(
            answerText.isNotEmpty()
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  fun getAnswerTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(answer: CharSequence, start: Int, before: Int, count: Int) {
        answerText = answer.toString().trim()
        val isAnswerTextAvailable = answerText.isNotEmpty()
        if (isAnswerTextAvailable != isAnswerAvailable.get()) {
          isAnswerAvailable.set(isAnswerTextAvailable)
        }
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }

  fun handleSubmitButtonClicked() {
    getPendingAnswer()
  }

  private fun getPendingAnswer() {
    if (answerText.isNotEmpty()) {
      val answer = SurveySelectedAnswer.newBuilder()
        .setQuestionName(questionName)
        .setFreeFormAnswer(answerText.toString())
        .build()
      answerHandler.getFreeFormAnswer(answer)
    }
  }
}
