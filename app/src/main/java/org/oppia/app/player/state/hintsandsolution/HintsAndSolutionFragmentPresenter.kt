package org.oppia.app.player.state.hintsandsolution

import android.util.Log
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
  private var currentState: State? = null
  private var explorationId: String? = ""
  private var newAvailableHintIndex: Int = -1
  private var allHintsExhausted: Boolean = false

  val viewModel by lazy {
    getHintsAndSolutionViewModel()
  }

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    isOrientationChanged: Boolean,
    currentState: State?,
    explorationId: String?,
    currentExpandedHintListIndex: Int?,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean,
    expandedHintListIndexListener: ExpandedHintListIndexListener
  ): View? {


    Log.d("orientation=","=="+isOrientationChanged)
    if (isOrientationChanged){
      this.currentState = viewModel.currentState.get()!!
      this.explorationId = viewModel.explorationId.get()!!
      this.newAvailableHintIndex = viewModel.newAvailableHintIndex.get()!!
      this.allHintsExhausted = viewModel.allHintsExhausted.get()!!

      Log.d("hint list=",""+this.currentState!!.interaction.hintList[0].hintIsRevealed)
      Log.d("hint list=",""+viewModel.currentState.get()!!.interaction.hintList[0].hintIsRevealed)
    }else{
      this.currentState = currentState
      this.explorationId = explorationId
      this.newAvailableHintIndex = newAvailableHintIndex
      this.allHintsExhausted = allHintsExhausted
    }

    val binding = HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

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

    viewModel.currentState.set(this.currentState)
    viewModel.newAvailableHintIndex.set(this.newAvailableHintIndex)
    viewModel.allHintsExhausted.set(this.allHintsExhausted)
    viewModel.explorationId.set(this.explorationId)

    viewModel.setHintsList(viewModel.currentState.get()!!.interaction.hintList)
    viewModel.setSolution(viewModel.currentState.get()!!.interaction.solution)

    hintsAndSolutionAdapter =
      HintsAndSolutionAdapter(
        fragment,
        viewModel.processHintList(),
        expandedHintListIndexListener,
        currentExpandedHintListIndex,
        viewModel.explorationId.get(),
        htmlParserFactory,
        entityType
      )
    binding.hintsAndSolutionRecyclerView.apply {
      adapter = hintsAndSolutionAdapter
    }

    if(this.newAvailableHintIndex!=-1)
    handleNewAvailableHint(this.newAvailableHintIndex)

    if (this.allHintsExhausted) {
      handleAllHintsExhausted(this.allHintsExhausted)
    }
    return binding.root
  }

  private fun handleAllHintsExhausted(allHintsExhausted: Boolean) {
    hintsAndSolutionAdapter.setSolutionCanBeRevealed(allHintsExhausted)
  }

  fun getHintsAndSolutionViewModel(): HintsViewModel {
    return viewModelProvider.getForFragment(fragment, HintsViewModel::class.java)
  }

  fun handleRevealSolution(saveUserChoice: Boolean) {
    hintsAndSolutionAdapter.setRevealSolution(saveUserChoice)
  }

  private fun handleNewAvailableHint(hintIndex: Int) {
    hintsAndSolutionAdapter.setNewHintIsAvailable(hintIndex)
  }
}
