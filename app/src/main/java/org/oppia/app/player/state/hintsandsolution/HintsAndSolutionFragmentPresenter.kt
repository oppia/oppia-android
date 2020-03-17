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
import javax.inject.Inject

/** Presenter for [HintsAndSolutionFragment], sets up bindings from ViewModel */
@FragmentScope
class HintsAndSolutionFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HintsAndSolutionViewModel>
) {

  private var currentExpandedHintListIndex: Int? = null
  private lateinit var expandedHintListIndexListener: ExpandedHintListIndexListener
  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?,
                       currentState: State,
                       currentExpandedHintListIndex: Int?,
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

    Log.e(
      "Hints view model",
      "Hints====" +viewModel.processHintList()

    )
    val hintsAndSolutionAdapter =
      HintsAndSolutionAdapter(
        viewModel.processHintList(),
        expandedHintListIndexListener,
        currentExpandedHintListIndex
      )
    binding.hintsAndSolutionRecyclerView.apply {
      adapter = hintsAndSolutionAdapter
    }

//    binding.hintsAndSolutionRecyclerView.layoutManager!!.scrollToPosition(currentExpandedHintListIndex!!)

    return binding.root
  }

  private fun getHintsAndSolutionViewModel(): HintsAndSolutionViewModel {
    return viewModelProvider.getForFragment(fragment, HintsAndSolutionViewModel::class.java)
  }
}
