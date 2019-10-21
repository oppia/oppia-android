package org.oppia.domain.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Profiles
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for retrieving, adding, updating, and deleting profiles */
@Singleton
class ProfileManagementController @Inject constructor(
  private val logger: Logger
){
  private var activeProfileId: Int = 0

  /** Gets information for all stored profiles */
  fun getProfiles(): LiveData<AsyncResult<Profiles>> {
    return MutableLiveData(AsyncResult.success(Profiles.getDefaultInstance()))
  }

  /** Gets a single profile, specified by profiledId */
  fun getProfile(profileId: ProfileId): LiveData<AsyncResult<Profile>> {
    return MutableLiveData(AsyncResult.success(Profile.getDefaultInstance()))
  }

  /**
   * Adds a profile with given parameters to profiles map in Profiles
   * Observe for success or failure (failed to store addition in cache or name not unique)
   */
  fun addProfile(
    name: String, pin: String, imagePath: Uri?, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Updates name of a Profile, specified by profileId
   * Observe for success or failure (failed to store update in cache or name not unique)
   */
  fun updateName(profileId: ProfileId, newName: String): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Updates PIN of a Profile, specified by profileId
   * Observe for success or failure (failed to store update in cache)
   */
  fun updatePin(profileId: ProfileId, newName: String): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Updates allow download access of a Profile, specified by profileId
   * Observe for success or failure (failed to store update in cache)
   */
  fun updateDownloadAccess(
    profileId: ProfileId, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Updates allow download access of a Profile, specified by profileId
   * Observe for success or failure (failed to store deletion in cache)
   */
  fun deleteProfile(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /** Gets the ProfileId of the selected profile */
  fun getActiveProfileId(): ProfileId {
    return ProfileId.newBuilder().setId(activeProfileId).build()
  }

  /** Sets the ProfileId of the selected profile in ProfileChooserFragment */
  fun setActiveProfileId(profileId: ProfileId) {
    activeProfileId = profileId.id
  }
}