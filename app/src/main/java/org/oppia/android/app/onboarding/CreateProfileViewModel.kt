package org.oppia.android.app.onboarding

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The ViewModel for [CreateProfileFragment]. */
@FragmentScope
class CreateProfileViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {
  /** [ObservableField] that tracks whether creating a profile has triggered an error condition. */
  val hasErrorMessage = ObservableField(false)

  /** [ObservableField] that tracks the error message to be displayed to the user. */
  val errorMessage = ObservableField("")

  /** [ObservableField] that tracks the screen header to be displayed to the user. */
  val screenHeader = ObservableField("")

  /** The learner's PIN updated as the user types in the PIN input field. */
  val inputPin = ObservableField("")

  /** Used to validate that the confirmation matches the original PIN. */
  val inputConfirmPin = ObservableField("")

  /** Error message containing validation feedback related to the input PIN. */
  val pinErrorMsg = ObservableField("")

  /** Error message containing validation feedback related to the input confirm PIN. */
  val confirmPinErrorMsg = ObservableField("")

  /** Whether to show PIN fields in the UI. */
  val showPinFields = ObservableField(false)

  /** Whether the PIN section should be available at all, only for supervisor adding a learner. */
  val showPinUi = ObservableField(false)

  /** List of RGB colors that have already been assigned to a profile. */
  val usedColors: LiveData<List<Int>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(),
      ::processGetProfilesResult
    )
  }

  private fun processGetProfilesResult(
    profilesResult: AsyncResult<List<Profile>>
  ): List<Int> {
    val profileList = when (profilesResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "CreateProfileViewModel",
          "Failed to retrieve the list of profiles",
          profilesResult.error
        )
        emptyList()
      }

      is AsyncResult.Pending -> emptyList()
      is AsyncResult.Success -> profilesResult.value
    }

    // Return a list of RGB avatar colors only.
    return profileList.mapNotNull { profile ->
      if (profile.avatar.avatarTypeCase == ProfileAvatar.AvatarTypeCase.AVATAR_COLOR_RGB) {
        profile.avatar.avatarColorRgb
      } else null
    }
  }
}
