package org.oppia.android.app.hintsandsolution

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.Solution
import org.oppia.android.domain.hintsandsolution.isHintRevealed
import org.oppia.android.domain.hintsandsolution.isSolutionRevealed
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler

/**
 * RecyclerView items are 2 times of (No. of Hints + Solution),
 * this is because in UI after each hint or solution there is a horizontal line/view
 * which is considered as a separate item in recyclerview.
 */
const val RECYCLERVIEW_INDEX_CORRECTION_MULTIPLIER = 2

private const val DEFAULT_HINT_AND_SOLUTION_SUMMARY = ""

/** [ViewModel] for Hints in [HintsAndSolutionDialogFragment]. */
@FragmentScope
class HintsViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler
) : HintsAndSolutionItemViewModel() {

  val newAvailableHintIndex = ObservableField<Int>(-1)
  val allHintsExhausted = ObservableField<Boolean>(false)
  val explorationId = ObservableField<String>("")

  val title = ObservableField<String>("")
  val hintsAndSolutionSummary = ObservableField(DEFAULT_HINT_AND_SOLUTION_SUMMARY)
  val isHintRevealed = ObservableField<Boolean>(false)
  val hintCanBeRevealed = ObservableField<Boolean>(false)

  private lateinit var hintList: List<Hint>
  private lateinit var solution: Solution
  private lateinit var helpIndex: HelpIndex
  val itemList: MutableList<HintsAndSolutionItemViewModel> = ArrayList()

  /** Initializes the view model to display hints and a solution. */
  fun initialize(helpIndex: HelpIndex, hintList: List<Hint>, solution: Solution) {
    this.helpIndex = helpIndex
    this.hintList = hintList
    this.solution = solution
  }

  fun processHintList(): List<HintsAndSolutionItemViewModel> {
    itemList.clear()
    for (index in hintList.indices) {
      if (itemList.isEmpty()) {
        addHintToList(index, hintList[index])
      } else if (itemList.size > 1) {
        val isLastHintRevealed =
          (itemList[itemList.size - RECYCLERVIEW_INDEX_CORRECTION_MULTIPLIER] as HintsViewModel)
            .isHintRevealed.get()
            ?: false
        val availableHintIndex = newAvailableHintIndex.get() ?: 0
        if (isLastHintRevealed &&
          index <= availableHintIndex / RECYCLERVIEW_INDEX_CORRECTION_MULTIPLIER
        ) {
          addHintToList(index, hintList[index])
        } else {
          break
        }
      }
    }
    if (itemList.size > 1) {
      val isLastHintRevealed =
        (itemList[itemList.size - RECYCLERVIEW_INDEX_CORRECTION_MULTIPLIER] as HintsViewModel)
          .isHintRevealed.get()
          ?: false
      val areAllHintsExhausted = allHintsExhausted.get() ?: false
      if (solution.hasExplanation() &&
        hintList.size * RECYCLERVIEW_INDEX_CORRECTION_MULTIPLIER == itemList.size &&
        isLastHintRevealed &&
        areAllHintsExhausted
      ) {
        addSolutionToList(solution)
      }
    }
    return itemList
  }

  fun computeHintListDropDownIconContentDescription(): String {
    return resourceHandler.getStringInLocale(
      R.string.show_hide_hint_list,
      hintsAndSolutionSummary.get() ?: DEFAULT_HINT_AND_SOLUTION_SUMMARY
    )
  }

  private fun addHintToList(hintIndex: Int, hint: Hint) {
    val hintsViewModel = HintsViewModel(resourceHandler)
    hintsViewModel.title.set(hint.hintContent.contentId)
    hintsViewModel.hintsAndSolutionSummary.set(hint.hintContent.html)
    hintsViewModel.isHintRevealed.set(helpIndex.isHintRevealed(hintIndex, hintList))
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
    solutionViewModel.isSolutionRevealed.set(helpIndex.isSolutionRevealed())
    itemList.add(solutionViewModel)
    addDividerItem()
  }

  private fun addDividerItem() {
    itemList.add(HintsDividerViewModel())
  }
}
