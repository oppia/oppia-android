package org.oppia.domain.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileDatabase
import org.oppia.app.model.ProfileId
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for retrieving, adding, updating, and deleting profiles. */
@Singleton
class ProfileManagementController @Inject constructor(
  private val logger: Logger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders
){
  private var currentProfileId: Int = -1
  private val cellularDataStore = cacheStoreFactory.create("cellular_data_preference", ProfileDatabase.getDefaultInstance())
  private var profileData = ProfileDatabase.getDefaultInstance()

  init {
    dataProviders.convertToLiveData(cellularDataStore).observeForever {
      if (it.isSuccess()) {
        profileData = it.getOrDefault(ProfileDatabase.getDefaultInstance())
      }
    }
  }

  /** Returns the list of created profiles. */
  fun getProfiles(): LiveData<AsyncResult<List<Profile>>> {
    // iterate through map.keys() of profileDatabase
    return MutableLiveData(AsyncResult.success(mutableListOf()))
  }

  /** Returns a single profile, specified by profiledId. */
  fun getProfile(profileId: ProfileId): LiveData<AsyncResult<Profile>> {
    // use map in profileDatabase to get correct profile
    return MutableLiveData(AsyncResult.success(Profile.getDefaultInstance()))
  }

  /**
   * Adds a new profile with the specified parameters.
   *
   * @param name Name of the new profile.
   * @param pin Pin of the new profile.
   * @param avatarImagePath Uri path to user selected image.
   * @param allowDownloadAccess Indicates whether the new profile can download content.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  fun addProfile(
    name: String, pin: String, avatarImagePath: Uri?, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    // check name is unique first, then add profile to list and store it, mergeFrom(Message other)
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Updates the name of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param newName New name for the profile being updated.
   * @return a [LiveData] that indicates the success/failure of this update operation.
   */
  fun updateName(profileId: ProfileId, newName: String): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Updates the PIN of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param newPin New pin for the profile being updated.
   * @return a [LiveData] that indicates the success/failure of this update operation.
   */
  fun updatePin(profileId: ProfileId, newPin: String): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Updates the download access of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param allowDownloadAccess New download access status for the profile being updated.
   * @return a [LiveData] that indicates the success/failure of this update operation.
   */
  fun updateDownloadAccess(
    profileId: ProfileId, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Deletes an existing profile.
   *
   * @param profileId the ID corresponding to the profile being deleted.
   * @return a [LiveData] that indicates the success/failure of this delete operation.
   */
  fun deleteProfile(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  /**
   * Returns the ProfileId of the current profile. The default value is -1 if currentProfileId
   * hasn't been set.
   */
  fun getCurrentProfileId(): ProfileId {
    return ProfileId.newBuilder().setInternalId(currentProfileId).build()
  }

  /**
   * Sets the currentProfileId to the selected profile in ProfileChooserFragment.
   * Checks to ensure that profileId corresponds to an existing profile.
   *
   * @param profileId the ID corresponding to the profile being set.
   * @return a [LiveData] that indicates the success/failure of this set operation.
   */
  fun setCurrentProfileId(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    currentProfileId = profileId.internalId
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }
}
