package org.oppia.android.app.profileprogress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.ProfileProgressFragmentBinding
import org.oppia.android.databinding.ProfileProgressHeaderBinding
import org.oppia.android.databinding.ProfileProgressRecentlyPlayedStoryCardBinding
import javax.inject.Inject

private const val TAG_PROFILE_PICTURE_EDIT_DIALOG = "PROFILE_PICTURE_EDIT_DIALOG"

/** The presenter for [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {

  @Inject
  lateinit var viewModel: ProfileProgressViewModel

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

    val spanCount = activity.resources.getInteger(R.integer.profile_progress_span_count)
    val profileProgressLayoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    profileProgressLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position == 0) {
          /* number of spaces this item should occupy= */ spanCount
        } else {
          /* number of spaces this item should occupy= */ 1
        }
      }
    }

    binding.profileProgressList.apply {
      layoutManager = profileProgressLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    viewModel.setProfileId(internalProfileId)
    viewModel.handleOnConfigurationChange()
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

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_RECENTLY_PLAYED_STORY
  }

  fun showPictureEditDialog() {
    val previousFragment =
      activity.supportFragmentManager.findFragmentByTag(TAG_PROFILE_PICTURE_EDIT_DIALOG)
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = ProfilePictureEditDialogFragment.newInstance()
    dialogFragment.showNow(activity.supportFragmentManager, TAG_PROFILE_PICTURE_EDIT_DIALOG)
  }
}
