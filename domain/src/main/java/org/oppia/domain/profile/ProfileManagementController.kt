package org.oppia.domain.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
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
import org.oppia.util.profile.DirectoryManagementUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject
import javax.inject.Singleton

private const val TRANSFORMED_GET_PROFILES_PROVIDER_ID = "transformed_get_profiles_provider_id"
private const val TRANSFORMED_GET_PROFILE_PROVIDER_ID = "transformed_get_profile_provider_id"
private const val GRAVATAR_URL_PREFIX = "https://www.gravatar.com/avatar/"
private const val GRAVATAR_QUERY_STRING = "?s=100&d=identicon&r=g"

/** Controller for retrieving, adding, updating, and deleting profiles. */
@Singleton
class ProfileManagementController @Inject constructor(
  private val logger: Logger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val context: Context,
  private val directoryManagementUtil: DirectoryManagementUtil
){
  private var currentProfileId: Int = -1
  private val profileDataStore = cacheStoreFactory.create("profile_database", ProfileDatabase.getDefaultInstance())

  class ProfileNameNotUniqueException(msg: String) : Exception(msg)
  class FailedToStoreImageException(msg: String) : Exception(msg)
  class FailedToDeleteProfileException(msg: String) : Exception(msg)
  class FailedToSetCurrentProfileException(msg: String): Exception(msg)

  init {
    profileDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e("DOMAIN", "Failed to prime cache ahead of LiveData conversion for ProfileManagementController.", it)
      }
    }
  }

  /** Returns the list of created profiles. */
  fun getProfiles(): LiveData<AsyncResult<List<Profile>>> {
    val transformedDataProvider = dataProviders.transform(TRANSFORMED_GET_PROFILES_PROVIDER_ID, profileDataStore) {
      it.profilesMap.values.toList()
    }
    return dataProviders.convertToLiveData(transformedDataProvider)
  }

  /** Returns a single profile, specified by profiledId. */
  fun getProfile(profileId: ProfileId): LiveData<AsyncResult<Profile>> {
    val transformedDataProvider = dataProviders.transform(TRANSFORMED_GET_PROFILE_PROVIDER_ID, profileDataStore) {
      it.profilesMap[profileId.internalId] ?: Profile.getDefaultInstance()
    }
    return dataProviders.convertToLiveData(transformedDataProvider)
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
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    profileDataStore.storeDataAsync(updateInMemoryCache = true) {
      if (!isUniqueName(name, it)) {
        throw ProfileNameNotUniqueException("New profile name is not unique to other profiles")
      }

      val nextProfileId = it.nextProfileId
      val profileDir = directoryManagementUtil.getOrCreateDir(nextProfileId.toString())

      val imageUri: String
      if (avatarImagePath != null) {
        imageUri = saveImageToInternalStorage(avatarImagePath, profileDir)
        if (imageUri.isEmpty()) {
          throw FailedToStoreImageException("Failed to store user submitted profile image")
        }
      } else {
        // gravatar url is a md5 hash of an email address
        imageUri = GRAVATAR_URL_PREFIX + md5("${name.toLowerCase()}$nextProfileId@gmail.com") + GRAVATAR_QUERY_STRING
      }

      val newProfile = Profile.newBuilder()
        .setName(name).setPin(pin).setAvatarImageUri(imageUri)
        .setAllowDownloadAccess(allowDownloadAccess).setId(ProfileId.newBuilder().setInternalId(nextProfileId))
        .build()

      val profileDatabaseBuilder = it.toBuilder().putProfiles(nextProfileId, newProfile).setNextProfileId(nextProfileId + 1)
      profileDatabaseBuilder.build()
    }.invokeOnCompletion {
      if (it == null) {
        pendingLiveData.postValue(AsyncResult.success(null))
      } else {
        logger.e("ProfileManagementController", "Failed when adding new profile", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      }
    }
    return pendingLiveData
  }

  /**
   * Updates the name of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param newName New name for the profile being updated.
   * @return a [LiveData] that indicates the success/failure of this update operation.
   */
  fun updateName(profileId: ProfileId, newName: String): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    profileDataStore.storeDataAsync(updateInMemoryCache = true) {
      if (!isUniqueName(newName, it)) {
        throw ProfileNameNotUniqueException("Updated profile name is not unique to other profiles")
      }
      val profile = it.profilesMap[profileId.internalId]
      checkNotNull(profile) { "ProfileId is not associated with a Profile" }
      val updatedProfile = profile.toBuilder().setName(newName).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(profileId.internalId, updatedProfile)
      profileDatabaseBuilder.build()
    }.invokeOnCompletion {
      if (it == null) {
        pendingLiveData.postValue(AsyncResult.success(null))
      } else {
        logger.e("ProfileManagementController", "Failed when storing the updated PIN for profile with ID: ${profileId.internalId}", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      }
    }
    return pendingLiveData
  }

  /**
   * Updates the PIN of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param newPin New pin for the profile being updated.
   * @return a [LiveData] that indicates the success/failure of this update operation.
   */
  fun updatePin(profileId: ProfileId, newPin: String): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    profileDataStore.storeDataAsync(updateInMemoryCache = true) {
      val profile = it.profilesMap[profileId.internalId]
      checkNotNull(profile) { "ProfileId is not associated with a Profile" }
      val updatedProfile = profile.toBuilder().setPin(newPin).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(profileId.internalId, updatedProfile)
      profileDatabaseBuilder.build()
    }.invokeOnCompletion {
      if (it == null) {
        pendingLiveData.postValue(AsyncResult.success(null))
      } else {
        logger.e("ProfileManagementController", "Failed when storing the updated PIN for profile with ID: ${profileId.internalId}", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      }
    }
    return pendingLiveData
  }

  /**
   * Updates the download access of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param allowDownloadAccess New download access status for the profile being updated.
   * @return a [LiveData] that indicates the success/failure of this update operation.
   */
  fun updateAllowDownloadAccess(
    profileId: ProfileId, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    profileDataStore.storeDataAsync(updateInMemoryCache = true) {
      val profile = it.profilesMap[profileId.internalId]
      checkNotNull(profile) { "ProfileId is not associated with a Profile" }
      val updatedProfile = profile.toBuilder().setAllowDownloadAccess(allowDownloadAccess).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(profileId.internalId, updatedProfile)
      profileDatabaseBuilder.build()
    }.invokeOnCompletion {
      if (it == null) {
        pendingLiveData.postValue(AsyncResult.success(null))
      } else {
        logger.e("ProfileManagementController", "Failed when storing the updated allowDownLoadAccess for profile with ID: ${profileId.internalId}", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      }
    }
    return pendingLiveData
  }

  /**
   * Deletes an existing profile.
   *
   * @param profileId the ID corresponding to the profile being deleted.
   * @return a [LiveData] that indicates the success/failure of this delete operation.
   */
  fun deleteProfile(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    profileDataStore.storeDataAsync(updateInMemoryCache = true) {
      if (!it.profilesMap.containsKey(profileId.internalId)) {
        throw FailedToDeleteProfileException("ProfileId does not match an existing Profile")
      }
      if (!directoryManagementUtil.deleteDir(profileId.internalId.toString())) {
        throw FailedToDeleteProfileException("Failed to delete directory")
      }
      val profileDatabaseBuilder = it.toBuilder().removeProfiles(profileId.internalId)
      profileDatabaseBuilder.build()
    }.invokeOnCompletion {
      if (it == null) {
        pendingLiveData.postValue(AsyncResult.success(null))
      } else {
        logger.e("ProfileManagementController", "Failed when deleting the profile with ID: ${profileId.internalId}", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      }
    }
    return pendingLiveData
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
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    val profileLiveData = dataProviders.convertToLiveData(profileDataStore)
    // TODO: Update DataProviders to allow for a one time access read operation
    profileLiveData.observeForever(object : Observer<AsyncResult<ProfileDatabase>> {
      override fun onChanged(result: AsyncResult<ProfileDatabase>?) {
        result?.let {
          if (it.isSuccess()) {
            if (result.getOrDefault(ProfileDatabase.getDefaultInstance()).profilesMap.containsKey(profileId.internalId)) {
              currentProfileId = profileId.internalId
              pendingLiveData.postValue(AsyncResult.success(null))
            } else {
              pendingLiveData.postValue(AsyncResult.failed(FailedToSetCurrentProfileException("ProfileId is not associated with an existing profile")))
            }
          } else {
            pendingLiveData.postValue(AsyncResult.failed(FailedToSetCurrentProfileException("Failed to read ProfileDatabase, could not validate profileId")))
          }
        }
        profileLiveData.removeObserver(this)
      }
    })
    return pendingLiveData
  }

  private fun isUniqueName(newName: String, profileDatabase: ProfileDatabase): Boolean {
    profileDatabase.profilesMap.values.forEach {
      if (it.name == newName) {
        return false
      }
    }
    return true
  }

  private fun saveImageToInternalStorage(avatarImagePath: Uri, profileDir: File): String {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, avatarImagePath)
    val imageFile = File(profileDir, "profile.png")
    var fos: FileOutputStream? = null
    try {
      fos = FileOutputStream(imageFile)
      bitmap.compress(Bitmap.CompressFormat.PNG, /** quality= */ 100, fos)
    } catch (e: Exception) {
      logger.e("ProfileManagementController", "Failed to store user submitted avatar image", e)
      return ""
    } finally {
      try {
        fos?.close()
      } catch (e: IOException) {
        logger.e("ProfileManagementController", "Failed to close FileOutputStream for avatar image", e)
      }
    }
    return imageFile.absolutePath
  }

  // https://stackoverflow.com/questions/3934331/how-to-hash-a-string-in-android
  private fun md5(s: String): String {
    var digest: MessageDigest? = null
    try {
      digest = MessageDigest.getInstance("MD5")
      digest.update(s.toByteArray(Charset.forName("US-ASCII")), 0, s.length)
      val magnitude = digest.digest()
      val bi = BigInteger(1, magnitude)
      val hash = String.format("%0" + (magnitude.size.shl(1)) + "x", bi)
      return hash
    } catch (e: NoSuchAlgorithmException) {
      logger.e("ProfileManagementController", "No such algorithm when creating md5 hash for gravatar", e)
    }
    return ""
  }
}
