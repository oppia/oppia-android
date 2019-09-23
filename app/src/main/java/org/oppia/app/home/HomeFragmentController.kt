package org.oppia.app.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.home_fragment.view.*
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.UserAppHistoryController
import javax.inject.Inject

/** The controller for [HomeFragment]. */
@FragmentScope
class HomeFragmentController @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<UserAppHistoryViewModel>,
  private val userAppHistoryController: UserAppHistoryController
){
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.viewModel = getUserAppHistoryViewModel()
      it.lifecycleOwner = fragment
    }

    userAppHistoryController.markUserOpenedApp()

    binding.root.btnExploration.setOnClickListener {
      val intent = Intent(fragment.requireContext(), ExplorationActivity::class.java)
      fragment.requireContext().startActivity(intent) }

    return binding.root
  }

  private fun getUserAppHistoryViewModel(): UserAppHistoryViewModel {
    return viewModelProvider.getForFragment(fragment, UserAppHistoryViewModel::class.java)
  }
}
