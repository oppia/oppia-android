package org.oppia.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.UserAppHistoryController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The controller for [HomeFragment]. */
@FragmentScope
class HomeFragmentController @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<UserAppHistoryViewModel>,
  private val userAppHistoryController: UserAppHistoryController,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) {
  private val userAppHistoryViewModel: UserAppHistoryViewModel by lazy {
    viewModelProvider.getForFragment(fragment, UserAppHistoryViewModel::class.java)
  }

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.viewModel = userAppHistoryViewModel
      it.presenter = this
      it.lifecycleOwner = fragment
    }

    userAppHistoryController.markUserOpenedApp()
    userAppHistoryViewModel.currentStateLabel = "Waiting for input"

    return binding.root
  }

  fun onLoadExplorationButtonClicked(v: View) {
    explorationDataController.startPlayingExploration(
      userAppHistoryViewModel.userProvidedExplorationId ?: "null"
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      if (result.isPending()) {
        userAppHistoryViewModel.currentStateLabel = "Loading exploration"
      } else if (result.isFailure()) {
        userAppHistoryViewModel.currentStateLabel = "Failed: ${result.getErrorOrNull()}"
        logger.e("HomeFragment", "Failed to load exploration", result.getErrorOrNull()!!)
      } else {
        userAppHistoryViewModel.currentStateLabel = "Successfully called startPlayingExploration()"
      }
    })
  }

  fun onUnloadExplorationButtonClicked(v: View) {
    explorationDataController.stopPlayingExploration().observe(fragment, Observer<AsyncResult<Any?>> { result ->
      if (result.isPending()) {
        userAppHistoryViewModel.currentStateLabel = "Unloading exploration"
      } else if (result.isFailure()) {
        userAppHistoryViewModel.currentStateLabel = "Failed: ${result.getErrorOrNull()}"
        logger.e("HomeFragment", "Failed to unload exploration", result.getErrorOrNull()!!)
      } else {
        userAppHistoryViewModel.currentStateLabel = "Successfully called stopPlayingExploration()"
      }
    })
  }
}
