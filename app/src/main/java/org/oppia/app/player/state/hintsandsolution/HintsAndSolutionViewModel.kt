package org.oppia.app.player.state.hintsandsolution

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Hint
import org.oppia.app.model.Solution
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class HintsAndSolutionViewModel @Inject constructor(
) : HintsAndSolutionItemViewModel() {

  var title: String = ""
  var hintsAndSolutionSummary: String = ""
  private lateinit var hintList: List<Hint>
  private lateinit var solution: Solution
  private val itemList: MutableList<HintsAndSolutionItemViewModel> = ArrayList()
  fun setHintsList(hintList: List<Hint>) {
    this.hintList = hintList
  }

  fun setSolution(solution: Solution) {
    this.solution = solution
  }

  fun processHintList(): MutableList<HintsAndSolutionItemViewModel> {

    val hintsAndSolutionViewModel =
      HintsAndSolutionViewModel()
    val solutionViewModel =
      SolutionViewModel()

    for (index in 0 until  hintList.size) {
      hintsAndSolutionViewModel.title = hintList[index].hintContent.contentId
      hintsAndSolutionViewModel.hintsAndSolutionSummary = hintList[index].hintContent.html
      itemList.add(hintsAndSolutionViewModel as HintsAndSolutionItemViewModel)
    }
    if (solution.hasExplanation()) {
      solutionViewModel.title = solution.explanation.contentId
      solutionViewModel.correctAnswer = solution.correctAnswer
      solutionViewModel.solutionSummary = solution.explanation.html
      itemList.add(solutionViewModel as SolutionViewModel)
    }
    return itemList
  }
}
