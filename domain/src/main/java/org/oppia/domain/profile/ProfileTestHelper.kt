package org.oppia.domain.profile

import androidx.lifecycle.LiveData
import org.oppia.app.model.ProfileId
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** This helper allows tests to easily create new profiles and switch between them. */
class ProfileTestHelper @Inject constructor(
  private val profileManagementController: ProfileManagementController
) {
  /** Creates one admin profile and one user profile. Logs in to admin profile. */
  fun initializeProfiles(): LiveData<AsyncResult<Any?>> {
    profileManagementController.addProfile(
      "Sean",
      "12345",
      null,
      allowDownloadAccess = true,
      isAdmin = true
    )
    profileManagementController.addProfile(
      "Ben",
      "123",
      null,
      allowDownloadAccess = false,
      isAdmin = false
    )
    return profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(0).build())
  }

  /** Create [numProfiles] number of user profiles. */
  fun addMoreProfiles(numProfiles: Int) {
    for (x in 0 until numProfiles) {
      profileManagementController.addProfile(
        (x + 65).toChar().toString(),
        "123",
        null,
        allowDownloadAccess = false,
        isAdmin = false
      )
    }
  }

  /** Login to Admin profile. */
  fun loginToAdmin() =
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(0).build())

  /* Login to user profile. */
  fun loginToUser() =
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(1).build())
}
