package org.oppia.android.testing.profile

import androidx.lifecycle.LiveData
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** This helper allows tests to easily create new profiles and switch between them. */
class ProfileTestHelper @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val testCoroutineDispatchers: TestCoroutineDispatchers
) {
  /** Creates one admin profile and one user profile. Logs in to admin profile. */
  fun initializeProfiles(): LiveData<AsyncResult<Any?>> {
    profileManagementController.addProfile(
      name = "Admin",
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
    val result = profileManagementController.loginToProfile(
      ProfileId.newBuilder().setInternalId(0)
        .build()
    ).toLiveData()
    testCoroutineDispatchers.runCurrent()
    return result
  }

  /** Creates one admin profile and logs in to admin profile. */
  fun addOnlyAdminProfile(): LiveData<AsyncResult<Any?>> {
    profileManagementController.addProfile(
      name = "Admin",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).toLiveData()
    val result = profileManagementController.loginToProfile(
      ProfileId.newBuilder().setInternalId(0).build()
    ).toLiveData()
    testCoroutineDispatchers.runCurrent()
    return result
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
    testCoroutineDispatchers.runCurrent()
  }

  /** Login to admin profile. */
  fun loginToAdmin() = logIntoProfile(internalProfileId = 0)

  /** Login to user profile. */
  fun loginToUser() = logIntoProfile(internalProfileId = 1)

  private fun logIntoProfile(internalProfileId: Int): LiveData<AsyncResult<Any?>> {
    val result = profileManagementController.loginToProfile(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    ).toLiveData()
    testCoroutineDispatchers.runCurrent()
    return result
  }
}
