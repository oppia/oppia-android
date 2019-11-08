package org.oppia.domain.profile

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.model.ProfileId
import javax.inject.Inject

class ProfileTestHelper @Inject constructor(
  private val profileManagementController: ProfileManagementController
) {
  @ExperimentalCoroutinesApi
  fun initializeProfiles() {
    profileManagementController.addProfile("Sean", "12345", null, allowDownloadAccess = true, isAdmin = true)
    profileManagementController.addProfile("Ben", "123", null, allowDownloadAccess = false, isAdmin = false)
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(0).build())
  }

  @ExperimentalCoroutinesApi
  fun loginToAdmin() = profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(0).build())

  @ExperimentalCoroutinesApi
  fun loginToUser() = profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(1).build())
}