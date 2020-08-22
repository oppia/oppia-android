package org.oppia.app.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.HintsAndSolutionFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.State
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Presenter for [HintsAndSolutionDialogFragment], sets up bindings from ViewModel. */
@FragmentScope
class HintsAndSolutionDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HintsViewModel>,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @ExplorationHtmlParserEntityType private val entityType: String
) {

  private var currentExpandedHintListIndex: Int? = null
  private var index: Int? = null
  private var isHintRevealed: Boolean? = null
  private var solutionIndex: Int? = null
  private var isSolutionRevealed: Boolean? = null
  private lateinit var expandedHintListIndexListener: ExpandedHintListIndexListener
  private lateinit var hintsAndSolutionAdapter: HintsAndSolutionAdapter
  private lateinit var binding: HintsAndSolutionFragmentBinding
  private lateinit var state: State

  val viewModel by lazy {
    getHintsAndSolutionViewModel()
  }

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit HintsAndSolutionListener to dismiss this fragment.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    state: State,
    id: String?,
    currentExpandedHintListIndex: Int?,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean,
    expandedHintListIndexListener: ExpandedHintListIndexListener,
    index: Int?,
    isHintRevealed: Boolean?,
    solutionIndex: Int?,
    isSolutionRevealed: Boolean?
  ): View? {

    binding =
      HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    this.currentExpandedHintListIndex = currentExpandedHintListIndex
    this.expandedHintListIndexListener = expandedHintListIndexListener
    this.index = index
    this.isHintRevealed = isHintRevealed
    this.solutionIndex = solutionIndex
    this.isSolutionRevealed = isSolutionRevealed
    binding.hintsAndSolutionToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.hintsAndSolutionToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? HintsAndSolutionListener)?.dismiss()
    }
    binding.let {
      it.viewModel = this.viewModel
      it.lifecycleOwner = fragment
    }

    this.state = state
    viewModel.newAvailableHintIndex.set(newAvailableHintIndex)
    viewModel.allHintsExhausted.set(allHintsExhausted)
    viewModel.explorationId.set(id)

    loadHintsAndSolution(state)

    return binding.root
  }

  private fun loadHintsAndSolution(state: State) {
    // Check if hints are available for this state.
    if (state.interaction.hintList.size != 0) {
      viewModel.setHintsList(state.interaction.hintList)
      viewModel.setSolution(state.interaction.solution)

      hintsAndSolutionAdapter =
        HintsAndSolutionAdapter(
          fragment,
          getHintListWithDividers(viewModel.processHintList()),
          expandedHintListIndexListener,
          currentExpandedHintListIndex,
          viewModel.explorationId.get(),
          htmlParserFactory,
          resourceBucketName,
          entityType,
          index,
          isHintRevealed,
          solutionIndex,
          isSolutionRevealed
        )

      binding.hintsAndSolutionRecyclerView.apply {
        adapter = hintsAndSolutionAdapter
      }
      if (viewModel.newAvailableHintIndex.get() != -1) {
        handleNewAvailableHint(viewModel.newAvailableHintIndex.get())
      }
      if (viewModel.allHintsExhausted.get()!!) {
        handleAllHintsExhausted(viewModel.allHintsExhausted.get()!!)
      }
    }
  }

  private fun getHintListWithDividers(hintList: List<HintsAndSolutionItemViewModel>):
    List<HintsAndSolutionItemViewModel> {
      val newHintList = mutableListOf<HintsAndSolutionItemViewModel>()
      hintList.forEach {
        newHintList.add(it)
        newHintList.add(HintsDividerViewModel())
      }
      return newHintList
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

  private fun handleNewAvailableHint(hintIndex: Int?) {
    hintsAndSolutionAdapter.setNewHintIsAvailable(hintIndex!!)
  }

  fun onExpandClicked(index: Int?) {
    currentExpandedHintListIndex = index
    if (index != null)
      hintsAndSolutionAdapter.notifyItemChanged(index)
  }

  fun onRevealHintClicked(index: Int?, isHintRevealed: Boolean?) {
    this.index = index
    this.isHintRevealed = isHintRevealed
  }

  fun onRevealSolutionClicked(solutionIndex: Int?, isSolutionRevealed: Boolean?) {
    this.solutionIndex = solutionIndex
    this.isSolutionRevealed = isSolutionRevealed
  }
}
