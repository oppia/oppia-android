package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.survey.SelectedAnswerAvailabilityReceiver
import org.oppia.android.app.survey.SelectedAnswerHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import javax.inject.Inject

class NpsItemsViewModel @Inject constructor(
  private val selectedAnswerAvailabilityReceiver: SelectedAnswerAvailabilityReceiver,
  private val answerHandler: SelectedAnswerHandler
) : SurveyAnswerItemViewModel(ViewType.NPS_OPTIONS) {
  val optionItems: ObservableList<MultipleChoiceOptionContentViewModel> = getNpsOptions()

  private val selectedItems: MutableList<Int> = mutableListOf()

  override fun updateSelection(itemIndex: Int, isCurrentlySelected: Boolean): Boolean {
    optionItems.forEach { item -> item.isAnswerSelected.set(false) }
    if (!selectedItems.contains(itemIndex)) {
      selectedItems.clear()
      selectedItems += itemIndex
    } else {
      selectedItems.clear()
    }
    updateIsAnswerAvailable()
    return selectedItems.isNotEmpty()
  }

  val isAnswerAvailable = ObservableField(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          selectedAnswerAvailabilityReceiver.onPendingAnswerAvailabilityCheck(
            selectedItems.isNotEmpty()
          )
          getPendingAnswer()
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  private fun updateIsAnswerAvailable() {
    val wasSelectedItemListEmpty = isAnswerAvailable.get()
    if (selectedItems.isNotEmpty() != wasSelectedItemListEmpty) {
      isAnswerAvailable.set(selectedItems.isNotEmpty())
    }
  }

  private fun getPendingAnswer() {
    if (selectedItems.isNotEmpty()) {
      val npsScore = selectedItems.first()
      val answer = SurveySelectedAnswer.newBuilder()
        .setQuestionName(SurveyQuestionName.NPS)
        .setNpsScore(npsScore)
        .build()
      answerHandler.getPendingAnswer(answer)
    }
  }

  private fun getNpsOptions(): ObservableArrayList<MultipleChoiceOptionContentViewModel> {
    val observableList = ObservableArrayList<MultipleChoiceOptionContentViewModel>()
    observableList += (0..10).mapIndexed { index, score ->
      MultipleChoiceOptionContentViewModel(
        optionContent = score.toString(),
        itemIndex = index,
        this
      )
    }
    return observableList
  }
}
