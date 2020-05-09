package org.oppia.app.walkthrough.end

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.WalkthroughFinalFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The presenter for [WalkthroughFinalFragment]. */
@FragmentScope
class WalkthroughFinalFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  private lateinit var binding: WalkthroughFinalFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, topicId: String): View? {
    binding = WalkthroughFinalFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
    }
    return binding.root
  }
}
