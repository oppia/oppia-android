package org.oppia.app.story

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.StoryFragmentBinding
import javax.inject.Inject

/** The controller for [StoryFragment] */
class StoryFragmentPresenter @Inject constructor(private val fragment: Fragment) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = StoryFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
    }
    return binding.root
  }
}
