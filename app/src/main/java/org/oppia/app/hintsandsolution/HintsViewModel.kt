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
        addHintToList(hintList[index])
      } else if (itemList.size > 1) {
        val isLastHintRevealed =
          (itemList[itemList.size - 2] as HintsViewModel).isHintRevealed.get() ?: false
        if (isLastHintRevealed && index <= newAvailableHintIndex.get()!! / 2) {
          addHintToList(hintList[index])
        } else {
          break
        }
      }
    }
    if (itemList.size > 1) {
      val isLastHintRevealed =
        (itemList[itemList.size - 2] as HintsViewModel).isHintRevealed.get() ?: false
      if (solution.hasExplanation() &&
        hintList.size * 2 == itemList.size &&
        isLastHintRevealed &&
        allHintsExhausted.get()!!
      ) {
        addSolutionToList(solution)
      }
    }
    return itemList
  }

  private fun addHintToList(hint: Hint) {
    val hintsViewModel = HintsViewModel()
    hintsViewModel.title.set(hint.hintContent.contentId)
    hintsViewModel.hintsAndSolutionSummary.set(hint.hintContent.html)
    hintsViewModel.isHintRevealed.set(hint.hintIsRevealed)
    itemList.add(hintsViewModel)
    addDividerItem()
  }

  private fun addSolutionToList(solution: Solution) {
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
    addDividerItem()
  }

  private fun addDividerItem() {
    itemList.add(HintsDividerViewModel())
  }
}
