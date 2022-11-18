package org.oppia.android.testing.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.data.DataProviderTestMonitor_Factory_Factory

/** This helper allows tests to easily create new profiles and switch between them. */
class ProfileTestHelper @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val testCoroutineDispatchers: TestCoroutineDispatchers,
  private val monitorFactory: DataProviderTestMonitor.Factory
) {

  private val observer = Observer<AsyncResult<Any?>> { }

  /**
   * Creates one admin profile and one user profile. Logs in to admin profile.
   *
   * @returns a [LiveData] that indicates when the login is complete.
   * Note that this if is not observed, the login will not be performed.
   */
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
    profileManagementController.addProfile(
      name = "Natrajan Subramanniyam Balaguruswamy",
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

  /**
   * Creates one admin profile and logs in to admin profile.
   *
   * @returns a [LiveData] that indicates when the login is complete.
   * Note that this if is not observed, the login will not be performed.
   */
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

  /** Log in to admin profile. */
  fun logIntoAdmin() = logIntoProfile(internalProfileId = 0)

  /** Log in to user profile. */
  fun logIntoUser() = logIntoProfile(internalProfileId = 1)

  /**
   * Log in to a new user profile that has no progress for any topics or stories. This relies on other
   * tests utilizing profile 1 as the default user profile so that profile 2 never has any progress.
   */
  fun logIntoNewUser() = logIntoProfile(internalProfileId = 2)

  private fun logIntoProfile(internalProfileId: Int): DataProvider<Any?> {
    return profileManagementController.loginToProfile(
      ProfileId.newBuilder().setInternalId(internalProfileId).build()
    )
  }

  /** Returns the continue button animation seen for profile. */
  fun getContinueButtonAnimationSeenStatus(profileId: ProfileId): Boolean {
    return monitorFactory.waitForNextSuccessfulResult(
      profileManagementController.getProfile(profileId)
    ).isContinueButtonAnimationSeen
  }

  /**
   * While performing any action based on the LiveData (see other methods above),
   * this helper function should be used to ensure the operation corresponding to the LiveData
   * properly completes.
   *
   * @param data is the LiveData which needs to accessed while performing action.
   */
  fun waitForOperationToComplete(data: LiveData<AsyncResult<Any?>>) {
    data.observeForever(observer)
  }
}
