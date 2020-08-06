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
      name = "Sean",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    )
    profileManagementController.addProfile(
      name = "Ben",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false
    )
    profileManagementController.addProfile(
      name = "Nikita",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false
    )
    return profileManagementController.loginToProfile(
      ProfileId.newBuilder().setInternalId(0)
        .build()
    )
  }

  /** Creates one admin profile and logs in to admin profile. */
  fun addOnlyAdminProfile(): LiveData<AsyncResult<Any?>> {
    profileManagementController.addProfile(
      name = "Sean",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    )
    return profileManagementController.loginToProfile(
      ProfileId.newBuilder().setInternalId(0).build()
    )
  }

  /** Create [numProfiles] number of user profiles. */
  fun addMoreProfiles(numProfiles: Int) {
    for (x in 0 until numProfiles) {
      profileManagementController.addProfile(
        name = (x + 65).toChar().toString(),
        pin = "123",
        avatarImagePath = null,
        allowDownloadAccess = false,
        colorRgb = -10710042,
        isAdmin = false
      )
    }
  }

  /** Login to admin profile. */
  fun loginToAdmin() =
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(0).build())

  /** Login to user profile. */
  fun loginToUser() =
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(1).build())
}
