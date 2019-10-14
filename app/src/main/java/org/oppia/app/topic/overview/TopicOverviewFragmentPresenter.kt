package org.oppia.app.topic.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.TopicOverviewFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.topic.RouteToTopicPlayListener
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [TopicOverviewFragment]. */
@FragmentScope
class TopicOverviewFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicOverviewViewModel>
) {
  private val routeToTopicPlayListener = activity as RouteToTopicPlayListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicOverviewFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = getTopicOverviewViewModel()
    }
    return binding.root
  }

  fun seeMoreClicked(v: View) {
    routeToTopicPlayListener.routeToTopicPlayFragment()
  }

  private fun getTopicOverviewViewModel(): TopicOverviewViewModel {
    return viewModelProvider.getForFragment(fragment, TopicOverviewViewModel::class.java)
  }
}
