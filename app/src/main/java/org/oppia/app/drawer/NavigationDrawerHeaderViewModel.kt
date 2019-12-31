package org.oppia.app.drawer

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.model.Profile
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

class NavigationDrawerHeaderViewModel @Inject constructor(
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger
) : ObservableViewModel() {

  init {
    subscribeToProfileLiveData()
  }

  val name = ObservableField<String>("")

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(fragment, Observer<Profile> {
      name.set(it.name)
    })
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileManagementController.getCurrentProfileId()), ::processGetProfileResult)
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("NavigationDrawerHeaderViewModel", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }
}
