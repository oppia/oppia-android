package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.survey.PreviousAnswerHandler
import org.oppia.android.app.survey.SelectedAnswerAvailabilityReceiver
import org.oppia.android.app.survey.SelectedAnswerHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import javax.inject.Inject

class NpsItemsViewModel @Inject constructor(
  private val selectedAnswerAvailabilityReceiver: SelectedAnswerAvailabilityReceiver,
  private val answerHandler: SelectedAnswerHandler
) : SurveyAnswerItemViewModel(ViewType.NPS_OPTIONS), PreviousAnswerHandler {
  val optionItems: ObservableList<MultipleChoiceOptionContentViewModel> = getNpsOptions()

  private val selectedItems: MutableList<Int> = mutableListOf()

  override fun updateSelection(itemIndex: Int): Boolean {
    optionItems.forEach { item -> item.isAnswerSelected.set(false) }
    if (!selectedItems.contains(itemIndex)) {
      selectedItems.clear()
      selectedItems += itemIndex
    } else {
      selectedItems.clear()
    }

    updateIsAnswerAvailable()

    if (selectedItems.isNotEmpty()) {
      getPendingAnswer(itemIndex)
    }

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
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  private fun updateIsAnswerAvailable() {
    val selectedItemListWasEmpty = isAnswerAvailable.get()
    if (selectedItems.isNotEmpty() != selectedItemListWasEmpty) {
      isAnswerAvailable.set(selectedItems.isNotEmpty())
    }
  }

  private fun getPendingAnswer(npsScore: Int) {
    val answer = SurveySelectedAnswer.newBuilder()
      .setQuestionName(SurveyQuestionName.NPS)
      .setNpsScore(npsScore)
      .build()
    answerHandler.getMultipleChoiceAnswer(answer)
  }

  override fun getPreviousAnswer(): SurveySelectedAnswer {
    return SurveySelectedAnswer.getDefaultInstance()
  }

  override fun restorePreviousAnswer(previousAnswer: SurveySelectedAnswer) {
    val selectedAnswerOption = previousAnswer.npsScore
    selectedItems.apply {
      clear()
      add(selectedAnswerOption)
    }

    updateIsAnswerAvailable()

    selectedAnswerOption.let { optionIndex ->
      getPendingAnswer(optionIndex)
      optionItems[optionIndex].isAnswerSelected.set(true)
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
