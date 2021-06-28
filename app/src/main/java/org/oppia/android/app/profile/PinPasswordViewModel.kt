package org.oppia.android.app.profile

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The ViewModel for [PinPasswordActivity]. */
@ActivityScope
class PinPasswordViewModel @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger
) : ObservableViewModel() {
  private lateinit var profileId: ProfileId
  val showError = ObservableField(false)
  val showPassword = ObservableField(false)
  val correctPin = ObservableField<String>("")
  val isAdmin = ObservableField<Boolean>(false)
  val name = ObservableField<String>("")
  val showAdminPinForgotPasswordPopUp = ObservableField<Boolean>(false)

  val profile: LiveData<Profile> by lazy {
    Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processGetProfileResult
    )
  }

  fun setProfileId(id: Int) {
    profileId = ProfileId.newBuilder().setInternalId(id).build()
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      oppiaLogger.e(
        "PinPasswordActivity",
        "Failed to retrieve profile",
        profileResult.getErrorOrNull()!!
      )
    }
    val profile = profileResult.getOrDefault(Profile.getDefaultInstance())
    correctPin.set(profile.pin)
    isAdmin.set(profile.isAdmin)
    name.set(profile.name)
    return profile
  }
}
