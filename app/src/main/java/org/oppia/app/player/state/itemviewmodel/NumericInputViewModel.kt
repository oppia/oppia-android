package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import javax.inject.Inject
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToNumberParser
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler

/** [StateItemViewModel] for the numeric input interaction. */
class NumericInputViewModel(
  private val context: Context,
  private val interactionAnswerErrorReceiver: InteractionAnswerErrorReceiver
) : StateItemViewModel(ViewType.NUMERIC_INPUT_INTERACTION), InteractionAnswerHandler {
  var answerText: CharSequence = ""
  private var pendingAnswerError: String? = null
  val errorMessage = ObservableField<String>("")
  @Inject lateinit var stringToNumberParser: StringToNumberParser

  init {
    val callback: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
      override fun onPropertyChanged(sender: Observable, propertyId: Int) {
        interactionAnswerErrorReceiver.onPendingAnswerError(pendingAnswerError)
      }
    }
    errorMessage.addOnPropertyChangedCallback(callback)
  }

  /** It checks the pending error for the current numeric input, and correspondingly updates the error string based on the specified error category. */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    if (answerText.isNotEmpty()) {
      pendingAnswerError = when (category) {
        AnswerErrorCategory.REAL_TIME -> stringToNumberParser.getRealTimeAnswerError(answerText.toString()).getErrorMessageFromStringRes(
          context
        )
        AnswerErrorCategory.SUBMIT_TIME -> stringToNumberParser.getSubmitTimeError(answerText.toString()).getErrorMessageFromStringRes(
          context
        )
      }
    }
    errorMessage.set(pendingAnswerError)
    return pendingAnswerError
  }

  @Bindable
  fun getAnswerTextWatcher(): TextWatcher {
    return object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(answer: CharSequence, start: Int, before: Int, count: Int) {
        answerText = answer.toString().trim()
        checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
      }

      override fun afterTextChanged(s: Editable) {
      }
    }
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    if (answerText.isNotEmpty()) {
      val answerTextString = answerText.toString()
      userAnswerBuilder.answer = InteractionObject.newBuilder().setReal(answerTextString.toDouble()).build()
      userAnswerBuilder.plainAnswer = answerTextString
    }
    return userAnswerBuilder.build()
  }
}
