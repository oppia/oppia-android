package org.oppia.app.walkthrough.topiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.WalkthroughTopicListFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.walkthrough.WalkthroughFragmentChangeListener
import org.oppia.app.walkthrough.WalkthroughPages
import javax.inject.Inject

/** The presenter for [WalkthroughTopicListFragment]. */
@FragmentScope
class WalkthroughTopicListFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private lateinit var binding: WalkthroughTopicListFragmentBinding
  private val routeToNextPage = activity as WalkthroughFragmentChangeListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = WalkthroughTopicListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }
    return binding.root
  }

  fun changePage() {
    routeToNextPage.walkthroughPage(WalkthroughPages.FINAL.value)
  }
}
