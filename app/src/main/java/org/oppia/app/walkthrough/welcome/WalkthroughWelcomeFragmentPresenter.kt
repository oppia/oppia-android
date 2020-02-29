package org.oppia.app.walkthrough.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.WalkthroughWelcomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [WalkthroughWelcomeFragment]. */
@FragmentScope
class WalkthroughWelcomeFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: WalkthroughWelcomeFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = WalkthroughWelcomeFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    return binding.root
  }
}
