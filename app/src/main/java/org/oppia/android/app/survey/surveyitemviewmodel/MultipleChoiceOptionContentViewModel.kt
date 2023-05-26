package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for MultipleChoiceInput values. */
class MultipleChoiceOptionContentViewModel(
  val optionContent: String,
  private val itemIndex: Int,
  private val optionsViewModel: SurveyAnswerItemViewModel
) : ObservableViewModel() {
  var isAnswerSelected = ObservableBoolean()

  fun handleItemClicked() {
    val isCurrentlySelected = isAnswerSelected.get()
    val shouldNowBeSelected =
      optionsViewModel.updateSelection(itemIndex, isCurrentlySelected)
    if (isCurrentlySelected != shouldNowBeSelected) {
      isAnswerSelected.set(shouldNowBeSelected)
    }
  }
}
