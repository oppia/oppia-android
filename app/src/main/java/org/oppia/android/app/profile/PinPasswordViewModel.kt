package org.oppia.android.app.profile

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.Profile
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.view.models.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

// TODO(#5817): Remove along with PinPasswordActivity when v2 onboarding flow has stabilized.
/** The ViewModel for [PinPasswordActivity]. */
@ActivityScope
class PinPasswordViewModel @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  private lateinit var profileId: LegacyProfileId
  val errorMessage = ObservableField<String>("")
  val showPassword = ObservableField(false)
  val correctPin = ObservableField<String>("")
  val isAdmin = ObservableField<Boolean>(false)
  val maxPinLength = ObservableField<Int>(5)
  val name = ObservableField<String>("")
  val showAdminPinForgotPasswordPopUp = ObservableField<Boolean>(false)

  val profile: LiveData<Profile> by lazy {
    Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processGetProfileResult
    )
  }

  val helloText: LiveData<String> by lazy {
    Transformations.map(profile) { profile ->
      resourceHandler.getStringInLocaleWithWrapping(R.string.pin_password_hello, profile.name)
    }
  }

  fun setProfileId(id: Int) {
    profileId = LegacyProfileId.newBuilder().setInternalId(id).build()
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    val profile = when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("PinPasswordActivity", "Failed to retrieve profile", profileResult.error)
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profileResult.value
    }
    correctPin.set(profile.pin)
    isAdmin.set(profile.isAdmin)
    maxPinLength.set(profile.pin.length)
    name.set(profile.name)
    return profile
  }
}
