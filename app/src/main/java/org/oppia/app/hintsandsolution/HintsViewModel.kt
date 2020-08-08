package org.oppia.app.hintsandsolution

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Hint
import org.oppia.app.model.Solution
import javax.inject.Inject

/** [ViewModel] for Hints in [HintsAndSolutionDialogFragment]. */
@FragmentScope
class HintsViewModel @Inject constructor() : HintsAndSolutionItemViewModel() {

  val newAvailableHintIndex = ObservableField<Int>(-1)
  val allHintsExhausted = ObservableField<Boolean>(false)
  val explorationId = ObservableField<String>("")

  val title = ObservableField<String>("")
  val hintsAndSolutionSummary = ObservableField<String>("")
  val isHintRevealed = ObservableField<Boolean>(false)
  val hintCanBeRevealed = ObservableField<Boolean>(false)

  private lateinit var hintList: List<Hint>
  private lateinit var solution: Solution
  private val itemList: MutableList<HintsAndSolutionItemViewModel> = ArrayList()

  fun setHintsList(hintList: List<Hint>) {
    this.hintList = hintList
  }

  fun setSolution(solution: Solution) {
    this.solution = solution
  }

  fun processHintList(): List<HintsAndSolutionItemViewModel> {
    itemList.clear()
    for (index in hintList.indices) {
      if (itemList.isEmpty()) {
        val hintsAndSolutionViewModel = HintsViewModel()
        hintsAndSolutionViewModel.title.set(hintList[index].hintContent.contentId)
        hintsAndSolutionViewModel.hintsAndSolutionSummary.set(hintList[index].hintContent.html)
        hintsAndSolutionViewModel.isHintRevealed.set(hintList[index].hintIsRevealed)
        itemList.add(hintsAndSolutionViewModel as HintsAndSolutionItemViewModel)
      } else if(index == itemList.size){
        val isLastHintRevealed = (itemList[index - 1] as HintsViewModel).isHintRevealed.get()!!
        if (isLastHintRevealed) {
          val hintsAndSolutionViewModel = HintsViewModel()
          hintsAndSolutionViewModel.title.set(hintList[index].hintContent.contentId)
          hintsAndSolutionViewModel.hintsAndSolutionSummary.set(hintList[index].hintContent.html)
          hintsAndSolutionViewModel.isHintRevealed.set(hintList[index].hintIsRevealed)
          itemList.add(hintsAndSolutionViewModel as HintsAndSolutionItemViewModel)
        }
      }
    }

    val isLastHintRevealed = (itemList[itemList.size - 1] as HintsViewModel).isHintRevealed.get()!!
    if (solution.hasExplanation() && hintList.size == itemList.size && isLastHintRevealed) {
      val solutionViewModel = SolutionViewModel()
      solutionViewModel.title.set(solution.explanation.contentId)
      solutionViewModel.correctAnswer.set(solution.correctAnswer.correctAnswer)
      solutionViewModel.numerator.set(solution.correctAnswer.numerator)
      solutionViewModel.denominator.set(solution.correctAnswer.denominator)
      solutionViewModel.wholeNumber.set(solution.correctAnswer.wholeNumber)
      solutionViewModel.isNegative.set(solution.correctAnswer.isNegative)
      solutionViewModel.solutionSummary.set(solution.explanation.html)
      solutionViewModel.isSolutionRevealed.set(solution.solutionIsRevealed)
      itemList.add(solutionViewModel)
    }
    return itemList
  }
}
