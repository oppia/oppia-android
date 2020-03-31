package org.oppia.app.player.state.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.HintsAndSolutionFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.State
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Presenter for [HintsAndSolutionFragment], sets up bindings from ViewModel. */
@FragmentScope
class HintsAndSolutionFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HintsViewModel>,
  private val htmlParserFactory: HtmlParser.Factory,
  @ExplorationHtmlParserEntityType private val entityType: String
) {

  private var currentExpandedHintListIndex: Int? = null
  private lateinit var expandedHintListIndexListener: ExpandedHintListIndexListener
  private lateinit var hintsAndSolutionAdapter: HintsAndSolutionAdapter
  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?,
                       currentState: State,
                       explorationId: String,
                       currentExpandedHintListIndex: Int?,
                       newAvailbleHintIndex: Int,
                       allHintsExhausted: Boolean,
                       expandedHintListIndexListener: ExpandedHintListIndexListener): View? {
    val binding = HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getHintsAndSolutionViewModel()

    this.currentExpandedHintListIndex = currentExpandedHintListIndex
    this.expandedHintListIndexListener = expandedHintListIndexListener

    binding.hintsAndSolutionToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.hintsAndSolutionToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? HintsAndSolutionListener)?.dismiss()
    }

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }

    viewModel.setHintsList(currentState.interaction.hintList)
    viewModel.setSolution(currentState.interaction.solution)
    viewModel.setExplorationId(explorationId)

     hintsAndSolutionAdapter =
      HintsAndSolutionAdapter(
        fragment,
        viewModel.processHintList(),
        expandedHintListIndexListener,
        currentExpandedHintListIndex,
        explorationId,
        htmlParserFactory,
        entityType
      )
    binding.hintsAndSolutionRecyclerView.apply {
      adapter = hintsAndSolutionAdapter
    }

    handleNewAvailableHint(newAvailbleHintIndex)

    if (allHintsExhausted) {
      handleAllHintsExhausted(allHintsExhausted)
    }
    return binding.root
  }

  private fun handleAllHintsExhausted(allHintsExhausted: Boolean) {
    hintsAndSolutionAdapter.setSolutionCanBeRevealed(allHintsExhausted)
  }

  private fun getHintsAndSolutionViewModel(): HintsViewModel {
    return viewModelProvider.getForFragment(fragment, HintsViewModel::class.java)
  }

  fun handleRevealSolution(saveUserChoice: Boolean) {
    hintsAndSolutionAdapter.setRevealSolution(saveUserChoice)
  }

  private fun handleNewAvailableHint(hintIndex: Int) {
    hintsAndSolutionAdapter.setNewHintIsAvailable(hintIndex)
  }
}
