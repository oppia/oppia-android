package org.oppia.app.walkthrough

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.WalkthroughFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [WalkthroughFragment]. */
@FragmentScope
class WalkthroughFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: WalkthroughFragmentBinding
  private var currentProgress: Int = 0

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = WalkthroughFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }

    return binding.root
  }

  fun nextPage() {
    if (currentProgress < 3) {
      binding.walkthroughProgressBar.progress = ++currentProgress
    }
  }

  fun prevPage() {
    if (currentProgress > 0) {
      binding.walkthroughProgressBar.progress = --currentProgress
    }
  }
}
