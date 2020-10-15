package org.oppia.android.app.hintsandsolution

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.Solution
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.ExplorationHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for Hints in [HintsAndSolutionDialogFragment]. */
@FragmentScope
class HintsViewModel @Inject constructor(
  @DefaultResourceBucketName val gcsResourceName: String,
  @ExplorationHtmlParserEntityType val gcsEntityType: String // TODO: fix for questions.
) : HintsAndSolutionItemViewModel() {

  val newAvailableHintIndex = ObservableField<Int>(-1)
  val allHintsExhausted = ObservableField<Boolean>(false)
  val entityId = ObservableField<String>("")

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
        val availableHintIndex = newAvailableHintIndex.get() ?: 0
        if (isLastHintRevealed && index <= availableHintIndex / 2) {
          addHintToList(hintList[index])
        } else {
          break
        }
      }
    }
    if (itemList.size > 1) {
      val isLastHintRevealed =
        (itemList[itemList.size - 2] as HintsViewModel).isHintRevealed.get() ?: false
      val areAllHintsExhausted = allHintsExhausted.get() ?: false
      if (solution.hasExplanation() &&
        hintList.size * 2 == itemList.size &&
        isLastHintRevealed &&
        areAllHintsExhausted
      ) {
        addSolutionToList(solution)
      }
    }
    return itemList
  }

  private fun addHintToList(hint: Hint) {
    // TODO: fix this. Should not be creating instances of itself, plus this class should be
    //  injected.
    val hintsViewModel = HintsViewModel(gcsResourceName, gcsEntityType)
    hintsViewModel.title.set(hint.hintContent.contentId)
    hintsViewModel.hintsAndSolutionSummary.set(hint.hintContent.html)
    hintsViewModel.isHintRevealed.set(hint.hintIsRevealed)
    itemList.add(hintsViewModel)
    addDividerItem()
  }

  private fun addSolutionToList(solution: Solution) {
    val solutionViewModel = SolutionViewModel(gcsResourceName, gcsEntityType, entityId)
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
