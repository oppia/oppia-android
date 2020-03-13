package org.oppia.app.player.state.hintsandsolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.HintsAndSolutionFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** Presenter for [HintsAndSolutionFragment], sets up bindings from ViewModel */
@FragmentScope
class HintsAndSolutionFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<HintsAndSolutionViewModel>
) {

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = HintsAndSolutionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val viewModel = getHintsAndSolutionViewModel()

    binding.hintsAndSolutionToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.hintsAndSolutionToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? HintsAndSolutionListener)?.dismiss()
    }

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getHintsAndSolutionViewModel(): HintsAndSolutionViewModel {
    return viewModelProvider.getForFragment(fragment, HintsAndSolutionViewModel::class.java)
  }
}
