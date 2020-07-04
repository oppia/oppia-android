package org.oppia.app.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.SimpleItemAnimator
import javax.inject.Inject
import org.oppia.app.R
import org.oppia.app.databinding.HintsAndSolutionFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.State
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.Logger
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser

/** Presenter for [HintsAndSolutionDialogFragment], sets up bindings from ViewModel. */
@FragmentScope
class HintsAndSolutionDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HintsViewModel>,
  private val htmlParserFactory: HtmlParser.Factory,
  private val logger: Logger,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @ExplorationHtmlParserEntityType private val entityType: String
) {

  private var currentExpandedHintListIndex: Int? = null
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
    expandedHintListIndexListener: ExpandedHintListIndexListener
  ): View? {

    binding =
      HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    this.currentExpandedHintListIndex = currentExpandedHintListIndex
    this.expandedHintListIndexListener = expandedHintListIndexListener
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
          viewModel.processHintList(),
          expandedHintListIndexListener,
          currentExpandedHintListIndex,
          viewModel.explorationId.get(),
          htmlParserFactory,
          resourceBucketName,
          entityType
        )

      binding.hintsAndSolutionRecyclerView.apply {
        adapter = hintsAndSolutionAdapter
        //this line removes the blink from the recyclerView
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      }
      if (viewModel.newAvailableHintIndex.get() != -1) {
        handleNewAvailableHint(viewModel.newAvailableHintIndex.get())
      }
      if (viewModel.allHintsExhausted.get()!!) {
        handleAllHintsExhausted(viewModel.allHintsExhausted.get()!!)
      }
    }
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
}
