package org.oppia.android.app.settings.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.util.platformparameter.EnableDownloadsSupport
import org.oppia.android.util.platformparameter.PlatformParameterValue

/** The ViewModel for [ProfileEditActivity]. */
@FragmentScope
class ProfileEditViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  @EnableDownloadsSupport private val enableDownloadsSupport: PlatformParameterValue<Boolean>
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId

  private val isAllowedDownloadAccessMutableLiveData = MutableLiveData<Boolean>()

  /** Download access enabled for the profile by the administrator. */
  val isAllowedDownloadAccess: LiveData<Boolean> = isAllowedDownloadAccessMutableLiveData

  /** List of all the current profiles registered in the app [ProfileListFragment]. */
  val profile: LiveData<Profile> by lazy {
    Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processGetProfileResult
    )
  }

  val showEditDownloadAccess: LiveData<Boolean> by lazy {
    Transformations.map(profile) { profile ->
      enableDownloadsSupport.value && !profile.isAdmin
    }
  }

  /** Whether the user is an admin. */
  var isAdmin = false

  /** Sets the identifier of the profile. */
  fun setProfileId(id: Int) {
    profileId = ProfileId.newBuilder().setInternalId(id).build()
  }

  /** Fetches the profile of a user asynchronously. */
  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    val profile = when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileEditViewModel",
          "Failed to retrieve the profile with ID: ${profileId.internalId}",
          profileResult.error
        )
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profileResult.value
    }
    isAllowedDownloadAccessMutableLiveData.value = profile.allowDownloadAccess
    isAdmin = profile.isAdmin
    return profile
  }
}
