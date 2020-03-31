package org.oppia.app.player.state.hintsandsolution

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

/** [ViewModel] for Solution in [HintsAndSolutionFragment]. */
class SolutionViewModel : HintsAndSolutionItemViewModel() {
  var solutionSummary = ObservableField<String>("")
  var correctAnswer = ObservableField<String>("")
  var title = ObservableField<String>("")
  var isSolutionRevealed = ObservableField<Boolean>(false)
  var solutionCanBeRevealed = ObservableField<Boolean>(false)
}
