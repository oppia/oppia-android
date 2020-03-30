package org.oppia.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.ProfileListActivityBinding
import org.oppia.app.databinding.ProfileListProfileViewBinding
import org.oppia.app.model.Profile
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [ProfileListActivity]. */
@ActivityScope
class ProfileListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<ProfileListViewModel>
) {
  fun handleOnCreate() {
    activity.title = activity.getString(R.string.profile_list_activity_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding = DataBindingUtil.setContentView<ProfileListActivityBinding>(activity, R.layout.profile_list_activity)
    binding.profileListToolbar.setNavigationOnClickListener {
      (activity as ProfileListActivity).finish()
    }
    binding.apply {
      viewModel = getProfileListViewModel()
      lifecycleOwner = activity
    }

    binding.profileListRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<Profile> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<Profile>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ProfileListProfileViewBinding::inflate,
        setViewModel = ::bindProfileView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileListProfileViewBinding,
    profile: Profile
  ) {
    binding.profile = profile
    binding.root.setOnClickListener {
      activity.startActivity(ProfileEditActivity.createProfileEditActivity(activity, profile.id.internalId))
    }
  }

  private fun getProfileListViewModel(): ProfileListViewModel {
    return viewModelProvider.getForActivity(activity, ProfileListViewModel::class.java)
  }
}
