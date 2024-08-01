package org.oppia.android.testing.profile

import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject

/** This helper allows tests to easily create new profiles and switch between them. */
class ProfileTestHelper @Inject constructor(
  private val profileManagementController: ProfileManagementController,
  private val monitorFactory: DataProviderTestMonitor.Factory
) {
  /**
   * Creates an assortment of non-admin profiles, and an admin profile. May optionally automatically
   * log into the newly created admin profile.
   *
   * @param autoLogIn whether to automatically log in the admin profile
   * @returns the [AsyncResult] designating the result of attempting to log into the admin profile
   */
  fun initializeProfiles(autoLogIn: Boolean = true): AsyncResult<Any?> {
    addProfileAndWait(
      name = "Admin",
      pin = "12345",
      allowDownloadAccess = true,
      isAdmin = true
    )
    addProfileAndWait(
      name = "Ben",
      pin = "123",
      allowDownloadAccess = false,
      isAdmin = false
    )
    addProfileAndWait(
      name = "Nikita",
      pin = "123",
      allowDownloadAccess = false,
      isAdmin = false
    )
    val lastAddResult = addProfileAndWait(
      name = "Natrajan Subramanniyam Balaguruswamy",
      pin = "123",
      allowDownloadAccess = false,
      isAdmin = false
    )
    return if (autoLogIn) {
      monitorFactory.createMonitor(logIntoAdmin()).waitForNextResult()
    } else lastAddResult
  }

  /**
   * Creates one admin profile and logs in to admin profile.
   *
   * @returns the [AsyncResult] designating the result of attempting to log into the admin profile
   */
  fun addOnlyAdminProfile(): AsyncResult<Any?> {
    addProfileAndWait(
      name = "Admin",
      pin = "12345",
      allowDownloadAccess = true,
      isAdmin = true
    )
    return monitorFactory.createMonitor(logIntoAdmin()).waitForNextResult()
  }

  /** Create [numProfiles] number of user profiles. */
  fun addMoreProfiles(numProfiles: Int) {
    for (x in 0 until numProfiles) {
      addProfileAndWait(
        name = (x + 65).toChar().toString(),
        pin = "123",
        allowDownloadAccess = false,
        isAdmin = false
      )
    }
  }

  /** Creates one admin profile with default values for all fields. */
  fun createDefaultAdminProfile() {
    addProfileAndWait(
      name = "",
      pin = "",
      allowDownloadAccess = false,
      isAdmin = true
    )
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

  private fun addProfileAndWait(
    name: String,
    pin: String,
    allowDownloadAccess: Boolean,
    isAdmin: Boolean
  ): AsyncResult<Any?> {
    val addResult =
      profileManagementController.addProfile(
        name, pin, avatarImagePath = null, allowDownloadAccess, colorRgb = -10710042, isAdmin
      )
    return monitorFactory.createMonitor(addResult).waitForNextResult()
  }
}
