package org.oppia.app.walkthrough.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.ui.R
import org.oppia.app.databinding.databinding.WalkthroughWelcomeFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.walkthrough.WalkthroughActivity
import org.oppia.app.walkthrough.WalkthroughFragmentChangeListener
import org.oppia.app.walkthrough.WalkthroughPages
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

/** The presenter for [WalkthroughWelcomeFragment]. */
@FragmentScope
class WalkthroughWelcomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val logger: ConsoleLogger
): WalkthroughFragmentChangeListener {
  private lateinit var binding: WalkthroughWelcomeFragmentBinding
  private val routeToNextPage = activity as WalkthroughFragmentChangeListener
  private lateinit var walkthroughWelcomeViewModel: WalkthroughWelcomeViewModel
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private lateinit var profileName: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding =
      WalkthroughWelcomeFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )

    internalProfileId = activity.intent.getIntExtra(
      WalkthroughActivity.WALKTHROUGH_ACTIVITY_INTERNAL_PROFILE_ID_KEY,
      -1
    )
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    walkthroughWelcomeViewModel = WalkthroughWelcomeViewModel()

    binding.let {
      it.lifecycleOwner = fragment
      it.presenter = this
      it.viewModel = walkthroughWelcomeViewModel
    }

    subscribeToProfileLiveData()
    return binding.root
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(profileId),
      ::processGetProfileResult
    )
  }

  private fun subscribeToProfileLiveData() {
    profileLiveData.observe(
      activity,
      Observer<Profile> { result ->
        profileName = result.name
        setProfileName()
      }
    )
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e(
        "WalkthroughWelcomeFragment",
        "Failed to retrieve profile",
        profileResult.getErrorOrNull()!!
      )
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun setProfileName() {
    if (::walkthroughWelcomeViewModel.isInitialized && ::profileName.isInitialized) {
      walkthroughWelcomeViewModel.profileName.set(activity.getString(R.string.welcome, profileName))
    }
  }

  override fun currentPage(walkthroughPage: Int) {
    // TODO: Temporary, just for interface
  }

  override fun pageWithTopicId(walkthroughPage: Int, topicId: String) {
    // TODO: Temporary, just for interface
  }

  override fun changePage() {
    routeToNextPage.currentPage(WalkthroughPages.TOPIC_LIST.value)
  }
}
