package org.oppia.app.completedstorylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.CompletedStoryListFragmentBinding
import javax.inject.Inject

/** The presenter for [CompletedStoryListFragment]. */
class CompletedStoryListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, internalProfileId: Int): View? {
    val binding = CompletedStoryListFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.completedStoryListToolbar.setNavigationOnClickListener {
      (activity as CompletedStoryListActivity).finish()
    }

    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
    }
    return binding.root
  }
}
