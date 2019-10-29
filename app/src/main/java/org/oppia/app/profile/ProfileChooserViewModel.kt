package org.oppia.app.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileChooserModel
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserViewModel @Inject constructor(
  private val profileManagementController: ProfileManagementController, private val logger: Logger
): ObservableViewModel() {

  val profiles: LiveData<List<ProfileChooserModel>> by lazy {
    val liveData = profileManagementController.getProfiles()
    val size = liveData.value?.isSuccess()
    val newLD = Transformations.map(liveData, ::processGetProfilesResult)
    MutableLiveData(listOf<ProfileChooserModel>(
      ProfileChooserModel.newBuilder().setProfile(Profile.getDefaultInstance()).build(),
      ProfileChooserModel.newBuilder().setAddProfile(true).build(),
      ProfileChooserModel.newBuilder().setAddProfile(true).build())
    )
  }

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<ProfileChooserModel> {
    if (profilesResult.isFailure()) {
      logger.e("ProfileChooserViewModel", "Failed to retrieve the list of profiles: " + profilesResult.getErrorOrNull())
    }
    checkNotNull(null)
    val profileList = profilesResult.getOrDefault(emptyList()).map {
      ProfileChooserModel.newBuilder().setProfile(it).build()
    }
    return profileList
  }
}
