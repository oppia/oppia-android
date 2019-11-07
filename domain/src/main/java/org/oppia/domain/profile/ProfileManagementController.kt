package org.oppia.domain.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.util.*
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

  class ProfileNameNotUniqueException(msg: String): Exception(msg)
  class ProfileNameOnlyLettersException(msg: String): Exception(msg)
  class FailedToStoreImageException(msg: String): Exception(msg)
  class FailedToDeleteDirException(msg: String): Exception(msg)
  class ProfileNotFoundException(msg: String): Exception(msg)
  class FailedToReadProfilesException(msg: String): Exception(msg)

  enum class ProfileActionStatus {
    Success,
    ProfileNameNotUnique,
    FailedToStoreImage,
    FailedToDeleteDir,
    ProfileNotFound
  }

  // TODO(#272): Remove init block when storeDataAsync is fixed
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
  @ExperimentalCoroutinesApi
  fun addProfile(
    name: String, pin: String, avatarImagePath: Uri?, allowDownloadAccess: Boolean, isAdmin: Boolean = false
  ): LiveData<AsyncResult<Any?>> {
    if (!onlyLetters(name)) {
      return MutableLiveData(AsyncResult.failed(ProfileNameOnlyLettersException("$name does not contain only letters")))
    }
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      if (!isUniqueName(name, it)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.ProfileNameNotUnique)
      }
      val nextProfileId = it.nextProfileId
      val profileDir = directoryManagementUtil.getOrCreateDir(nextProfileId.toString())

      val imageUri: String
      if (avatarImagePath != null) {
        imageUri = saveImageToInternalStorage(avatarImagePath, profileDir)
        if (imageUri.isEmpty()) {
          return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.FailedToStoreImage)
        }
      } else {
        // gravatar url is a md5 hash of an email address
        imageUri = GRAVATAR_URL_PREFIX + md5("${name.toLowerCase()}$nextProfileId@gmail.com") + GRAVATAR_QUERY_STRING
      }

      val newProfile = Profile.newBuilder()
        .setName(name).setPin(pin).setAvatarImageUri(imageUri)
        .setAllowDownloadAccess(allowDownloadAccess).setId(ProfileId.newBuilder().setInternalId(nextProfileId))
        .setDateCreatedTimestampMs(Date().time).setIsAdmin(isAdmin)
        .build()

      val profileDatabaseBuilder = it.toBuilder().putProfiles(nextProfileId, newProfile).setNextProfileId(nextProfileId + 1)
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.Success)
    }
    deferred.invokeOnCompletion {
      if (it != null) {
        logger.e("ProfileManagementController", "Failed to add profile", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      } else {
        when (deferred.getCompleted()) {
          ProfileActionStatus.Success -> pendingLiveData.postValue(AsyncResult.success(null))
          ProfileActionStatus.ProfileNameNotUnique -> pendingLiveData.postValue(AsyncResult.failed(ProfileNameNotUniqueException("$name is not unique to other profiles")))
          else -> pendingLiveData.postValue(AsyncResult.failed(FailedToStoreImageException("Failed to store user's selected avatar image")))
        }
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
  @ExperimentalCoroutinesApi
  fun updateName(profileId: ProfileId, newName: String): LiveData<AsyncResult<Any?>> {
    if (!onlyLetters(newName)) {
      return MutableLiveData(AsyncResult.failed(ProfileNameOnlyLettersException("$newName does not contain only letters")))
    }
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      if (!isUniqueName(newName, it)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.ProfileNameNotUnique)
      }
      val profile = it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.ProfileNotFound)
      val updatedProfile = profile.toBuilder().setName(newName).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(profileId.internalId, updatedProfile)
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.Success)
    }
    deferred.invokeOnCompletion {
      if (it != null) {
        logger.e("ProfileManagementController", "Failed to update name", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      } else {
        when (deferred.getCompleted()) {
          ProfileActionStatus.Success -> pendingLiveData.postValue(AsyncResult.success(null))
          ProfileActionStatus.ProfileNameNotUnique -> pendingLiveData.postValue(AsyncResult.failed(ProfileNameNotUniqueException("$newName is not unique to other profiles")))
          else -> pendingLiveData.postValue(AsyncResult.failed(ProfileNotFoundException("ProfileId ${profileId.internalId} does not match an existing Profile")))
        }
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
  @ExperimentalCoroutinesApi
  fun updatePin(profileId: ProfileId, newPin: String): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      val profile = it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.ProfileNotFound)
      val updatedProfile = profile.toBuilder().setPin(newPin).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(profileId.internalId, updatedProfile)
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.Success)
    }
    deferred.invokeOnCompletion {
      if (it != null) {
        logger.e("ProfileManagementController", "Failed to update PIN", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      } else {
        when (deferred.getCompleted()) {
          ProfileActionStatus.Success -> pendingLiveData.postValue(AsyncResult.success(null))
          else -> pendingLiveData.postValue(AsyncResult.failed(ProfileNotFoundException("ProfileId ${profileId.internalId} does not match an existing Profile")))
        }
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
  @ExperimentalCoroutinesApi
  fun updateAllowDownloadAccess(
    profileId: ProfileId, allowDownloadAccess: Boolean
  ): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      val profile = it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.ProfileNotFound)
      val updatedProfile = profile.toBuilder().setAllowDownloadAccess(allowDownloadAccess).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(profileId.internalId, updatedProfile)
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.Success)
    }
    deferred.invokeOnCompletion {
      if (it != null) {
        logger.e("ProfileManagementController", "Failed to update allowDownloadAccess", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      } else {
        when (deferred.getCompleted()) {
          ProfileActionStatus.Success -> pendingLiveData.postValue(AsyncResult.success(null))
          else -> pendingLiveData.postValue(AsyncResult.failed(ProfileNotFoundException("ProfileId ${profileId.internalId} does not match an existing Profile")))
        }
      }
    }
    return pendingLiveData
  }

  /**
   * Login to the user's Profile by setting the current profile Id and updating profile's last logged in time.
   *
   * @param profileId the ID corresponding to the profile being logged into.
   * @return a [LiveData] that indicates the success/failure of this login operation.
   */
  @ExperimentalCoroutinesApi
  fun loginToProfile (profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    setCurrentProfileId(profileId).observeForever { setIdResult ->
      if (setIdResult.isSuccess()) {
        updateLastLoggedIn(profileId).observeForever { updateLoggedInResult ->
          if (updateLoggedInResult.isSuccess()) {
            pendingLiveData.postValue(AsyncResult.success(null))
          } else if (updateLoggedInResult.isFailure()) {
            pendingLiveData.postValue(AsyncResult.failed(updateLoggedInResult.getErrorOrNull()!!))
          }
        }
      } else if (setIdResult.isFailure()) {
        pendingLiveData.postValue(AsyncResult.failed(setIdResult.getErrorOrNull()!!))
      }
    }
    return pendingLiveData
  }

  /**
   * Sets the currentProfileId to the selected profile in ProfileChooserFragment.
   * Checks to ensure that profileId corresponds to an existing profile.
   *
   * @param profileId the ID corresponding to the profile being set.
   * @return a [LiveData] that indicates the success/failure of this set operation.
   */
  internal fun setCurrentProfileId(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
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
              pendingLiveData.postValue(AsyncResult.failed(ProfileNotFoundException("ProfileId ${profileId.internalId} is not associated with an existing profile")))
            }
          } else {
            pendingLiveData.postValue(AsyncResult.failed(FailedToReadProfilesException("Failed to read ProfileDatabase, could not validate profileId ${profileId.internalId}")))
          }
        }
        profileLiveData.removeObserver(this)
      }
    })
    return pendingLiveData
  }

  /**
   * Updates the last logged in timestamp of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @return a [LiveData] that indicates the success/failure of this update operation.
   */
  @ExperimentalCoroutinesApi
  internal fun updateLastLoggedIn(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      val profile = it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.ProfileNotFound)
      val updatedProfile = profile.toBuilder().setLastLoggedInTimestampMs(Date().time).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(profileId.internalId, updatedProfile)
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.Success)
    }
    deferred.invokeOnCompletion {
      if (it != null) {
        logger.e("ProfileManagementController", "Failed to update last logged in", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      } else {
        when (deferred.getCompleted()) {
          ProfileActionStatus.Success -> pendingLiveData.postValue(AsyncResult.success(null))
          else -> pendingLiveData.postValue(AsyncResult.failed(ProfileNotFoundException("ProfileId ${profileId.internalId} does not match an existing Profile")))
        }
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
  @ExperimentalCoroutinesApi
  fun deleteProfile(profileId: ProfileId): LiveData<AsyncResult<Any?>> {
    val pendingLiveData = MutableLiveData(AsyncResult.pending<Any?>())
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      if (!it.profilesMap.containsKey(profileId.internalId)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.ProfileNotFound)
      }
      if (!directoryManagementUtil.deleteDir(profileId.internalId.toString())) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.FailedToDeleteDir)
      }
      val profileDatabaseBuilder = it.toBuilder().removeProfiles(profileId.internalId)
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.Success)
    }
    deferred.invokeOnCompletion {
      if (it != null) {
        logger.e("ProfileManagementController", "Failed to update name", it)
        pendingLiveData.postValue(AsyncResult.failed(it))
      } else {
        when (deferred.getCompleted()) {
          ProfileActionStatus.Success -> pendingLiveData.postValue(AsyncResult.success(null))
          ProfileActionStatus.ProfileNotFound -> pendingLiveData.postValue(AsyncResult.failed(ProfileNameNotUniqueException("ProfileId ${profileId.internalId} does not match an existing Profile")))
          else -> pendingLiveData.postValue(AsyncResult.failed(FailedToDeleteDirException("Failed to delete directory with ${profileId.internalId}")))
        }
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

  private fun isUniqueName(newName: String, profileDatabase: ProfileDatabase): Boolean {
    val lowerCaseNewName = newName.toLowerCase()
    profileDatabase.profilesMap.values.forEach {
      if (it.name.toLowerCase(Locale.getDefault()) == lowerCaseNewName) {
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
      rotateAndCompressBitmap(avatarImagePath, bitmap, 300)
        .compress(Bitmap.CompressFormat.PNG, /** quality= */ 100, fos)
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

  private fun onlyLetters(name: String): Boolean {
    return name.matches(Regex("^[ A-Za-z]+\$"))
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

  private fun rotateAndCompressBitmap(uri: Uri, bitmap: Bitmap, cropSize: Int): Bitmap {
    val croppedBitmap = ThumbnailUtils.extractThumbnail(bitmap, cropSize, cropSize)
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val orientation = ExifInterface(inputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
    var rotate = 0
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
      ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
      ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
    }
    val matrix = Matrix()
    matrix.postRotate(rotate.toFloat())
    return Bitmap.createBitmap(croppedBitmap, 0, 0, cropSize,  cropSize, matrix, true)
  }
}
