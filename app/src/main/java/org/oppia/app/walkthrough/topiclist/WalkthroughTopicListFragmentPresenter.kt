package org.oppia.app.walkthrough.topiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.WalkthroughTopicListFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [WalkthroughTopicListFragment]. */
@FragmentScope
class WalkthroughTopicListFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: WalkthroughTopicListFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = WalkthroughTopicListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    return binding.root
  }
}
