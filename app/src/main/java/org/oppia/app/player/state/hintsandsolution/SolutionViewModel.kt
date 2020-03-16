package org.oppia.app.player.state.hintsandsolution

import androidx.lifecycle.ViewModel

/** [ViewModel] for title in [HintsAndSolutionFragment]. */
class SolutionViewModel : HintsAndSolutionItemViewModel(){
  var solutionSummary: String = ""
  var correctAnswer: String = ""
  var title: String = ""
}
