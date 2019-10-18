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

/**
 * Controller for retrieving, adding, updating, and deleting profiles
 */
@Singleton
class ProfileManagementController @Inject constructor(
  private val logger: Logger
){
  private var activeProfileId: Int = 0

  fun getProfiles(): LiveData<AsyncResult<Profiles>> {
    return MutableLiveData(AsyncResult.success(Profiles.getDefaultInstance()))
  }

  fun getProfile(profileId: ProfileId): LiveData<AsyncResult<Profile>> {
    return MutableLiveData(AsyncResult.success(Profile.getDefaultInstance()))
  }

  fun addProfile(
    name: String, pin: String, imagePath: Uri?, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  fun updateName(profileId: ProfileId, newName: String): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  fun updatePin(profileId: ProfileId, newName: String): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  fun updateDownloadAccess(
    profileId: ProfileId, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  fun deleteProfile(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(AsyncResult.success<Any?>(null))
  }

  fun getActiveProfileId(): ProfileId {
    return ProfileId.newBuilder().setId(activeProfileId).build()
  }

  fun setActiveProfileId(profileId: ProfileId) {
    activeProfileId = profileId.id
  }
}