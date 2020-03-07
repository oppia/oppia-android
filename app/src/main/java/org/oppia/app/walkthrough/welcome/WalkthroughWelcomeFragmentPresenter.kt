package org.oppia.app.walkthrough.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.WalkthroughWelcomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.walkthrough.WalkthroughFragmentChangeListener
import org.oppia.app.walkthrough.WalkthroughPages
import javax.inject.Inject

/** The presenter for [WalkthroughWelcomeFragment]. */
@FragmentScope
class WalkthroughWelcomeFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private lateinit var binding: WalkthroughWelcomeFragmentBinding
  private val routeToNextPage = activity as WalkthroughFragmentChangeListener

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = WalkthroughWelcomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }
    return binding.root
  }

  fun changePage() {
    routeToNextPage.changeTo(WalkthroughPages.TOPICLIST.value)
  }
}
