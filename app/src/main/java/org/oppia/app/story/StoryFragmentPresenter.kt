package org.oppia.app.story

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.StoryFragmentBinding
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The controller for [StoryFragment] */
class StoryFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StoryViewModel>
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, storyId: String): View? {
    val viewModel = getStoryViewModel()
    viewModel.setStoryId(storyId)
    val binding = StoryFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun getStoryViewModel(): StoryViewModel {
    return viewModelProvider.getForFragment(fragment, StoryViewModel::class.java)
  }
}
