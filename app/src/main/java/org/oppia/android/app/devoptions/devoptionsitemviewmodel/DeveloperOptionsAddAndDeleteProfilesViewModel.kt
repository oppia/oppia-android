package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.devoptions.AddOneProfileButtonClickListener
import org.oppia.android.app.devoptions.AddThreeProfilesButtonClickListener
import org.oppia.android.app.devoptions.DeleteAllNonAdminProfilesButtonClickListener
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/**
 * [DeveloperOptionsItemViewModel] to provide features to add and delete profiles such as
 * add one profile, add three profiles, delete all non-admin profiles.
 */
class DeveloperOptionsAddAndDeleteProfilesViewModel(
  private val addOneProfileButtonClickListener: AddOneProfileButtonClickListener,
  private val addThreeProfilesButtonClickListener: AddThreeProfilesButtonClickListener,
  private val deleteAllNonAdminProfilesButtonClickListener:
    DeleteAllNonAdminProfilesButtonClickListener,
  profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger
) : DeveloperOptionsItemViewModel() {

  /** A [LiveData] that represents the profile count as a string. */
  val profileCountString: LiveData<String> = Transformations.map(
    profileManagementController.getProfileCount().toLiveData(),
    ::processGetProfileCountResult
  )

  private fun processGetProfileCountResult(profileCountResult: AsyncResult<Int>): String {
    return when (profileCountResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "DeveloperOptionsAddAndDeleteProfilesViewModel",
          "Failed to retrieve profile count",
          profileCountResult.error
        )
        ""
      }
      is AsyncResult.Pending -> ""
      is AsyncResult.Success -> profileCountResult.value.toString()
    }
  }

  /** Adds one profile by triggering the [AddOneProfileButtonClickListener]. */
  fun addOneProfile() {
    addOneProfileButtonClickListener.createOneProfile()
  }

  /** Adds three profiles by triggering the [AddThreeProfilesButtonClickListener]. */
  fun addThreeProfiles() {
    addThreeProfilesButtonClickListener.createThreeProfiles()
  }

  /**
   * Deletes all non-admin profiles by triggering the
   * [DeleteAllNonAdminProfilesButtonClickListener].
   */
  fun deleteAllNonAdminProfiles() {
    deleteAllNonAdminProfilesButtonClickListener.deleteAllNonAdminProfiles()
  }
}
