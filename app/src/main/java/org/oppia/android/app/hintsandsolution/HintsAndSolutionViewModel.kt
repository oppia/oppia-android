package org.oppia.android.app.hintsandsolution

import androidx.databinding.ObservableBoolean
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.hintsandsolution.isHintRevealed
import org.oppia.android.domain.hintsandsolution.isSolutionRevealed
import org.oppia.android.domain.translation.TranslationController
import javax.inject.Inject
import org.oppia.android.app.model.State
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.hintsandsolution.dropLastUnavailable
import org.oppia.android.domain.hintsandsolution.isSolutionAvailable

/** View model for a lesson's hints/solution list. */
class HintsAndSolutionViewModel private constructor(
  private val state: State,
  private val helpIndex: HelpIndex,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private val solutionViewModelFactory: SolutionViewModel.Factory
): ObservableViewModel() {
  private val hintList by lazy { helpIndex.dropLastUnavailable(state.interaction.hintList) }
  private val solution by lazy {
    state.interaction.solution.takeIf { it.hasExplanation() && helpIndex.isSolutionAvailable() }
  }

  val isSolutionRevealed: ObservableBoolean by lazy {
    ObservableBoolean(helpIndex.isSolutionRevealed())
  }
  val itemList: List<HintsAndSolutionItemViewModel> by lazy { createViewModels() }
  val solutionIndex: Int get() = itemList.lastIndex - 1

  private fun createViewModels(): List<HintsAndSolutionItemViewModel> {
    return hintList.mapIndexed { index, hint ->
      createHintViewModel(
        index, hint, isHintRevealed = ObservableBoolean(helpIndex.isHintRevealed(index, hintList))
      )
    } + listOfNotNull(solution?.let(this::createSolutionViewModel)) + ReturnToLessonViewModel
  }

  private fun createHintViewModel(
    hintIndex: Int, hint: Hint, isHintRevealed: ObservableBoolean
  ): HintViewModel {
    return HintViewModel(
      title = resourceHandler.getStringInLocaleWithWrapping(
        R.string.hint_list_item_number,
        resourceHandler.toHumanReadableString(hintIndex + 1)
      ),
      hintSummary = translationController.extractString(
        hint.hintContent, writtenTranslationContext
      ),
      isHintRevealed = isHintRevealed
    )
  }

  private fun createSolutionViewModel(solution: Solution): SolutionViewModel {
    return solutionViewModelFactory.create(
      solutionSummary = translationController.extractString(
        solution.explanation, writtenTranslationContext
      ),
      isSolutionRevealed = isSolutionRevealed,
      isSolutionExclusive = solution.answerIsExclusive,
      correctAnswer = solution.correctAnswer,
      interaction = state.interaction,
      writtenTranslationContext = writtenTranslationContext
    )
  }

  class Factory @Inject constructor(
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController,
    private val solutionViewModelFactory: SolutionViewModel.Factory
  ) {
    fun create(
      state: State, helpIndex: HelpIndex, writtenTranslationContext: WrittenTranslationContext
    ): HintsAndSolutionViewModel {
      return HintsAndSolutionViewModel(
        state,
        helpIndex,
        writtenTranslationContext,
        resourceHandler,
        translationController,
        solutionViewModelFactory
      )
    }
  }
}
