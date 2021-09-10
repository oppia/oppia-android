package org.oppia.android.app.hintsandsolution

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** [ViewModel] for Solution in [HintsAndSolutionDialogFragment]. */
class SolutionViewModel : HintsAndSolutionItemViewModel() {
  val solutionSummary = ObservableField<String>("")
  val correctAnswer = ObservableField<String>("")
  val numerator = ObservableField<Int>()
  val wholeNumber = ObservableField<Int>()
  val denominator = ObservableField<Int>()
  val isNegative = ObservableField<Boolean>(false)
  val title = ObservableField<String>("")
  val isSolutionRevealed = ObservableField<Boolean>(false)
  val solutionCanBeRevealed = ObservableField<Boolean>(false)
}
