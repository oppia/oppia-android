package org.oppia.android.app.settings.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** The ViewModel for [ProfileEditActivity]. */
@ActivityScope
class ProfileEditViewModel @Inject constructor(
  private val logger: ConsoleLogger,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId

  private val isAllowedDownloadAccessMutableLiveData = MutableLiveData<Boolean>()

  /** Specifies whether download access has been enabled by the user. */
  val isAllowedDownloadAccess: LiveData<Boolean> = isAllowedDownloadAccessMutableLiveData

  val profile: LiveData<Profile> by lazy {
    Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processGetProfileResult
    )
  }

  var isAdmin = false

  fun setProfileId(id: Int) {
    profileId = ProfileId.newBuilder().setInternalId(id).build()
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e(
        "ProfileEditViewModel",
        "Failed to retrieve the profile with ID: ${profileId.internalId}",
        profileResult.getErrorOrNull()!!
      )
    }
    val profile = profileResult.getOrDefault(Profile.getDefaultInstance())
    isAllowedDownloadAccessMutableLiveData.value = profile.allowDownloadAccess
    isAdmin = profile.isAdmin
    return profile
  }
}
