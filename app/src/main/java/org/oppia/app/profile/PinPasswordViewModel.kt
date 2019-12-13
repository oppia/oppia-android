package org.oppia.app.profile

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for [PinPasswordActivity]. */
@ActivityScope
class PinPasswordViewModel @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val logger: Logger
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId
  val showError = ObservableField(false)
  val showPassword = ObservableField(false)
  var correctPin = "PIN"
  var isAdmin = false
  var name = ""
  val profile: LiveData<Profile> by lazy {
    Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }
  fun setProfileId(id: Int) {
    profileId = ProfileId.newBuilder().setInternalId(id).build()
  }
  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("PinPasswordActivity", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    val profile = profileResult.getOrDefault(Profile.getDefaultInstance())
    correctPin = profile.pin
    isAdmin = profile.isAdmin
    name = profile.name
    return profile
  }
}
