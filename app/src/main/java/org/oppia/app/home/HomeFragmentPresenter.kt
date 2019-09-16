package org.oppia.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.home_fragment.*
import org.oppia.app.R
import org.oppia.app.databinding.HomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.topiclist.TopicListFragment
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.UserAppHistoryController
import javax.inject.Inject

/** The presenter for [HomeFragment]. */
@FragmentScope
class HomeFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<UserAppHistoryViewModel>,
  private val userAppHistoryController: UserAppHistoryController
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = HomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.viewModel = getUserAppHistoryViewModel()
      it.lifecycleOwner = fragment
    }

    if (getTopicListFragment() == null) {
      fragment.childFragmentManager
          .beginTransaction()
          .add(R.id.all_topics_container_placeholder, TopicListFragment())
          .commitNow()
    }

    userAppHistoryController.markUserOpenedApp()

    return binding.root
  }

  private fun getUserAppHistoryViewModel(): UserAppHistoryViewModel {
    return viewModelProvider.getForFragment(fragment, UserAppHistoryViewModel::class.java)
  }

  private fun getTopicListFragment(): TopicListFragment? {
    return fragment.childFragmentManager.findFragmentById(R.id.all_topics_container_placeholder) as TopicListFragment?
  }
}
