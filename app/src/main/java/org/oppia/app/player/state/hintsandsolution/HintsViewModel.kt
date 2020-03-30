package org.oppia.app.player.state.hintsandsolution

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Hint
import org.oppia.app.model.Solution
import javax.inject.Inject

/** [ViewModel] for concept card, providing rich text and worked examples */
@FragmentScope
class HintsViewModel @Inject constructor(
) : HintsAndSolutionItemViewModel() {

  var title: String = ""
  var hintsAndSolutionSummary: String = ""
  var isHintRevealed: Boolean = false
  var hintCanBeRevealed: Boolean = false
  private lateinit var hintList: List<Hint>
  private lateinit var solution: Solution
  private lateinit var explorationId: String
  private val itemList: MutableList<HintsAndSolutionItemViewModel> = ArrayList()
  fun setHintsList(hintList: List<Hint>) {
    this.hintList = hintList
  }

  fun setSolution(solution: Solution) {
    this.solution = solution
  }

  fun processHintList(): List<HintsAndSolutionItemViewModel> {
    itemList.clear()
    for (index in 0 until  hintList.size) {
      val hintsAndSolutionViewModel = HintsViewModel()
      hintsAndSolutionViewModel.title = hintList[index].hintContent.contentId
      hintsAndSolutionViewModel.hintsAndSolutionSummary = hintList[index].hintContent.html
      hintsAndSolutionViewModel.isHintRevealed = hintList[index].hintIsRevealed
      hintsAndSolutionViewModel.hintCanBeRevealed = hintList[index].newHintIsAvailable
      itemList.add(hintsAndSolutionViewModel as HintsAndSolutionItemViewModel)
    }
    if (solution.hasExplanation()) {
      val solutionViewModel = SolutionViewModel()
      solutionViewModel.title = solution.explanation.contentId
      solutionViewModel.correctAnswer = solution.correctAnswer.correctAnswer
      solutionViewModel.solutionSummary = solution.explanation.html
      solutionViewModel.isSolutionRevealed = solution.solutionIsRevealed
      itemList.add(solutionViewModel)
    }
    return itemList
  }

  fun setExplorationId(explorationId: String) {
      this.explorationId = explorationId
  }
}
