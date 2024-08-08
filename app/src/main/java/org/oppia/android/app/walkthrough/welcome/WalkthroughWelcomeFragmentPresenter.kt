package org.oppia.android.app.walkthrough.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.walkthrough.WalkthroughFragmentChangeListener
import org.oppia.android.app.walkthrough.WalkthroughPageChanger
import org.oppia.android.app.walkthrough.WalkthroughPages
import org.oppia.android.databinding.WalkthroughWelcomeFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The presenter for [WalkthroughWelcomeFragment]. */
@FragmentScope
class WalkthroughWelcomeFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler
) : WalkthroughPageChanger {
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

    internalProfileId = activity.intent?.extractCurrentUserProfileId()?.loggedInInternalProfileId ?: -1
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
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
      profileManagementController.getProfile(profileId).toLiveData(),
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
    return when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "WalkthroughWelcomeFragment", "Failed to retrieve profile", profileResult.error
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profileResult.value
    }
  }

  private fun setProfileName() {
    if (::walkthroughWelcomeViewModel.isInitialized && ::profileName.isInitialized) {
      walkthroughWelcomeViewModel.profileName.set(
        resourceHandler.getStringInLocaleWithWrapping(R.string.welcome, profileName)
      )
    }
  }

  override fun changePage() {
    routeToNextPage.currentPage(WalkthroughPages.TOPIC_LIST.value)
  }
}
