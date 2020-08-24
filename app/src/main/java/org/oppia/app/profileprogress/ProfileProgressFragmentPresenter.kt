package org.oppia.app.profileprogress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.app.R
import org.oppia.app.databinding.ProfileProgressFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

private const val TAG_PROFILE_PICTURE_EDIT_DIALOG = "PROFILE_PICTURE_EDIT_DIALOG"

/** The presenter for [ProfileProgressFragment]. */
@FragmentScope
class ProfileProgressFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileProgressViewModel>
) {
  private lateinit var profileProgressListAdapter: ProfileProgressListAdapter

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

    val viewModel = getProfileProgressViewModel()
    viewModel.setProfileId(internalProfileId)
    viewModel.handleOnConfigurationChange()

    profileProgressListAdapter = ProfileProgressListAdapter(activity, viewModel.itemViewModelList)

    val spanCount = activity.resources.getInteger(R.integer.profile_progress_span_count)
    profileProgressListAdapter.setSpanCount(spanCount)
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
      adapter = profileProgressListAdapter
    }
    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }

    return binding.root
  }

  private fun getProfileProgressViewModel(): ProfileProgressViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileProgressViewModel::class.java)
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
