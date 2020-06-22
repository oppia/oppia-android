package org.oppia.app.profileprogress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.databinding.ProfileProgressFragmentBinding
import org.oppia.app.databinding.ProfileProgressHeaderBinding
import org.oppia.app.databinding.ProfileProgressRecentlyPlayedStoryCardBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileProgressViewModel>
) {
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    val binding =
      ProfileProgressFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.lifecycleOwner = fragment
    binding.profileProgressList.apply {
      layoutManager = LinearLayoutManager(activity)
      adapter = createRecyclerViewAdapter()
    }

    val viewModel = getProfileProgressViewModel()
    viewModel.setProfileId(internalProfileId)

    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileProgressItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ProfileProgressItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is ProfileProgressHeaderViewModel -> ViewType.VIEW_TYPE_HEADER
          is RecentlyPlayedStorySummaryViewModel -> ViewType.VIEW_TYPE_RECENTLY_PLAYED_STORY
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_HEADER,
        inflateDataBinding = ProfileProgressHeaderBinding::inflate,
        setViewModel = ProfileProgressHeaderBinding::setViewModel,
        transformViewModel = { it as ProfileProgressHeaderViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_RECENTLY_PLAYED_STORY,
        inflateDataBinding = ProfileProgressRecentlyPlayedStoryCardBinding::inflate,
        setViewModel = ProfileProgressRecentlyPlayedStoryCardBinding::setViewModel,
        transformViewModel = { it as RecentlyPlayedStorySummaryViewModel }
      )
      .build()
  }

  private fun getProfileProgressViewModel(): ProfileProgressViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileProgressViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_RECENTLY_PLAYED_STORY
  }
}
