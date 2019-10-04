package org.oppia.app.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.TopicFragmentBinding
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** The controller for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = TopicFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.lifecycleOwner = fragment
    }

    return binding.root
  }
}
