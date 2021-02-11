package org.oppia.android.domain.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.AppLanguage
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.model.ProfileDatabase
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.profile.DirectoryManagementUtil
import org.oppia.android.util.system.OppiaClock
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_PROFILES_PROVIDER_ID = "get_profiles_provider_id"
private const val GET_PROFILE_PROVIDER_ID = "get_profile_provider_id"
private const val GET_WAS_PROFILE_EVER_ADDED_PROVIDER_ID =
  "get_was_profile_ever_added_provider_id"
private const val GET_DEVICE_SETTINGS_PROVIDER_ID =
  "get_device_settings_provider_id"
private const val ADD_PROFILE_PROVIDER_ID = "add_profile_provided_id"
private const val UPDATE_NAME_PROVIDER_ID = "update_name_provider_id"
private const val UPDATE_PIN_PROVIDER_ID = "update_pin_provider_id"
private const val UPDATE_PROFILE_AVATAR_PROVIDER_ID =
  "update_profile_avatar_provider_id"
private const val UPDATE_WIFI_PERMISSION_DEVICE_SETTINGS_PROVIDER_ID =
  "update_wifi_permission_device_settings_provider_id"
private const val UPDATE_TOPIC_AUTOMATICALLY_PERMISSION_DEVICE_SETTINGS_PROVIDER_ID =
  "update_topic_automatically_permission_device_settings_provider_id"
private const val UPDATE_ALL_DOWNLOAD_ACCESS_PROVIDER_ID =
  "update_all_download_provider_id"
private const val LOGIN_TO_PROFILE_PROVIDER_ID = "login_to_profile_provider_id"
private const val DELETE_PROFILE_PROVIDER_ID = "delete_profile_provider_id"
private const val SET_CURRENT_PROFILE_ID_PROVIDER_ID = "set_current_profile_id_provider_id"
private const val UPDATE_READING_TEXT_SIZE_PROVIDER_ID =
  "update_reading_text_size_provider_id"
private const val UPDATE_APP_LANGUAGE_PROVIDER_ID = "update_app_language_provider_id"
private const val UPDATE_AUDIO_LANGUAGE_PROVIDER_ID =
  "update_audio_language_provider_id"

/** Controller for retrieving, adding, updating, and deleting profiles. */
@Singleton
class ProfileManagementController @Inject constructor(
  private val logger: ConsoleLogger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val context: Context,
  private val directoryManagementUtil: DirectoryManagementUtil,
  private val exceptionsController: ExceptionsController,
  private val oppiaClock: OppiaClock
) {
  private var currentProfileId: Int = -1
  private val profileDataStore =
    cacheStoreFactory.create("profile_database", ProfileDatabase.getDefaultInstance())

  /** Indicates that the given name was is not unique. */
  class ProfileNameNotUniqueException(msg: String) : Exception(msg)

  /** Indicates that the given name does not contain only letters. */
  class ProfileNameOnlyLettersException(msg: String) : Exception(msg)

  /** Indicates that the selected image was not stored properly. */
  class FailedToStoreImageException(msg: String) : Exception(msg)

  /** Indicates that the gravatar url was not formed properly. */
  class FailedToGenerateGravatarException(msg: String) : Exception(msg)

  /** Indicates that the profile's directory was not delete properly. */
  class FailedToDeleteDirException(msg: String) : Exception(msg)

  /** Indicates that the given profileId is not associated with an existing profile. */
  class ProfileNotFoundException(msg: String) : Exception(msg)

  /** Indicates that the given profileId is not associated with an admin. */
  class ProfileNotAdminException(msg: String) : Exception(msg)

  /** Indicates that the Profile already has admin. */
  class ProfileAlreadyHasAdminException(msg: String) : Exception(msg)

  /** Indicates that the there is not device settings currently. */
  class DeviceSettingsNotFoundException(msg: String) : Exception(msg)

  /**
   * These Statuses correspond to the exceptions above such that if the deferred contains
   * PROFILE_NOT_FOUND, the [ProfileNotFoundException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class ProfileActionStatus {
    SUCCESS,
    INVALID_PROFILE_NAME,
    PROFILE_NAME_NOT_UNIQUE,
    FAILED_TO_STORE_IMAGE,
    FAILED_TO_GENERATE_GRAVATAR,
    FAILED_TO_DELETE_DIR,
    PROFILE_NOT_FOUND,
    PROFILE_NOT_ADMIN,
    PROFILE_ALREADY_HAS_ADMIN
  }

  // TODO(#272): Remove init block when storeDataAsync is fixed
  init {
    profileDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache ahead of LiveData conversion for ProfileManagementController.",
          it
        )
      }
    }
  }

  /** Returns the list of created profiles. */
  fun getProfiles(): DataProvider<List<Profile>> {
    return profileDataStore.transform(GET_PROFILES_PROVIDER_ID) {
      it.profilesMap.values.toList()
    }
  }

  /** Returns a single profile, specified by profiledId. */
  fun getProfile(profileId: ProfileId): DataProvider<Profile> {
    return profileDataStore.transformAsync(GET_PROFILE_PROVIDER_ID) {
      val profile = it.profilesMap[profileId.internalId]
      if (profile != null) {
        AsyncResult.success(profile)
      } else {
        AsyncResult.failed(
          ProfileNotFoundException(
            "ProfileId ${profileId.internalId} does" +
              " not match an existing Profile"
          )
        )
      }
    }
  }

  /** Returns a boolean determining whether the profile was ever added or not. */
  fun getWasProfileEverAdded(): DataProvider<Boolean> {
    return profileDataStore.transformAsync(GET_WAS_PROFILE_EVER_ADDED_PROVIDER_ID) {
      val wasProfileEverAdded = it.wasProfileEverAdded
      AsyncResult.success(wasProfileEverAdded)
    }
  }

  /** Returns device settings for the app. */
  fun getDeviceSettings(): DataProvider<DeviceSettings> {
    return profileDataStore.transformAsync(GET_DEVICE_SETTINGS_PROVIDER_ID) {
      val deviceSettings = it.deviceSettings
      if (deviceSettings != null) {
        AsyncResult.success(deviceSettings)
      } else {
        AsyncResult.failed(DeviceSettingsNotFoundException("Device Settings not found."))
      }
    }
  }

  /**
   * Adds a new profile with the specified parameters.
   *
   * @param name Name of the new profile.
   * @param pin Pin of the new profile.
   * @param avatarImagePath Uri path to user selected image. If null, the user did not select an image.
   * @param allowDownloadAccess Indicates whether the new profile can download content.
   * @param colorRgb Indicates the color RGB integer used for the avatar background.
   * @return a [DataProvider] that indicates the success/failure of this add operation.
   */
  fun addProfile(
    name: String,
    pin: String,
    avatarImagePath: Uri?,
    allowDownloadAccess: Boolean,
    colorRgb: Int,
    isAdmin: Boolean
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      if (!onlyLetters(name)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.INVALID_PROFILE_NAME)
      }
      if (!isNameUnique(name, it)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.PROFILE_NAME_NOT_UNIQUE)
      }
      if (isAdmin && alreadyHasAdmin(it)) {
        return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_ALREADY_HAS_ADMIN
        )
      }

      val nextProfileId = it.nextProfileId
      val profileDir = directoryManagementUtil.getOrCreateDir(nextProfileId.toString())

      val newProfileBuilder = Profile.newBuilder()
        .setName(name)
        .setPin(pin)
        .setAllowDownloadAccess(allowDownloadAccess)
        .setId(ProfileId.newBuilder().setInternalId(nextProfileId))
        .setDateCreatedTimestampMs(oppiaClock.getCurrentTimeMs())
        .setIsAdmin(isAdmin)
        .setReadingTextSize(ReadingTextSize.MEDIUM_TEXT_SIZE)
        .setAppLanguage(AppLanguage.ENGLISH_APP_LANGUAGE)
        .setAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE)

      if (avatarImagePath != null) {
        val imageUri =
          saveImageToInternalStorage(avatarImagePath, profileDir)
            ?: return@storeDataWithCustomChannelAsync Pair(
              it,
              ProfileActionStatus.FAILED_TO_STORE_IMAGE
            )
        newProfileBuilder.avatar = ProfileAvatar.newBuilder().setAvatarImageUri(imageUri).build()
      } else {
        newProfileBuilder.avatar = ProfileAvatar.newBuilder().setAvatarColorRgb(colorRgb).build()
      }

      val wasProfileEverAdded = it.profilesCount > 0

      val profileDatabaseBuilder =
        it.toBuilder()
          .putProfiles(nextProfileId, newProfileBuilder.build())
          .setWasProfileEverAdded(wasProfileEverAdded)
          .setNextProfileId(nextProfileId + 1)
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(ADD_PROFILE_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(null, name, deferred)
    }
  }

  /**
   * Updates the profile avatar of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param avatarImagePath New profile avatar for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateProfileAvatar(
    profileId: ProfileId,
    avatarImagePath: Uri?,
    colorRgb: Int
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val profileDir = directoryManagementUtil.getOrCreateDir(profileId.toString())

      val updatedProfileBuilder = profile.toBuilder()
      if (avatarImagePath != null) {
        val imageUri =
          saveImageToInternalStorage(avatarImagePath, profileDir)
            ?: return@storeDataWithCustomChannelAsync Pair(
              it,
              ProfileActionStatus.FAILED_TO_STORE_IMAGE
            )
        updatedProfileBuilder.avatar =
          ProfileAvatar.newBuilder().setAvatarImageUri(imageUri).build()
      } else {
        updatedProfileBuilder.avatar =
          ProfileAvatar.newBuilder().setAvatarColorRgb(colorRgb).build()
      }

      val profileDatabaseBuilder =
        it.toBuilder().putProfiles(profileId.internalId, updatedProfileBuilder.build())
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_PROFILE_AVATAR_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Updates the name of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param newName New name for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateName(profileId: ProfileId, newName: String): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      if (!onlyLetters(newName)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.INVALID_PROFILE_NAME)
      }
      if (!isNameUnique(newName, it)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.PROFILE_NAME_NOT_UNIQUE)
      }
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfile = profile.toBuilder().setName(newName).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(UPDATE_NAME_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, newName, deferred)
    }
  }

  /**
   * Updates the PIN of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param newPin New pin for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updatePin(profileId: ProfileId, newPin: String): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfile = profile.toBuilder().setPin(newPin).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(UPDATE_PIN_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Updates the download/update on wifi only permission.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param downloadAndUpdateOnWifiOnly download and update permission on wifi only.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateWifiPermissionDeviceSettings(
    profileId: ProfileId,
    downloadAndUpdateOnWifiOnly: Boolean
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val profileDatabaseBuilder = it.toBuilder()
      if (profile.isAdmin) {
        val deviceSettingsBuilder = it.deviceSettings.toBuilder()
        deviceSettingsBuilder.allowDownloadAndUpdateOnlyOnWifi = downloadAndUpdateOnWifiOnly
        profileDatabaseBuilder.deviceSettings = deviceSettingsBuilder.build()
        Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
      } else {
        Pair(profileDatabaseBuilder.build(), ProfileActionStatus.PROFILE_NOT_ADMIN)
      }
    }
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_WIFI_PERMISSION_DEVICE_SETTINGS_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Updates the automatically update topics permission.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param automaticallyUpdateTopics automatically update topic permission.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateTopicAutomaticallyPermissionDeviceSettings(
    profileId: ProfileId,
    automaticallyUpdateTopics: Boolean
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val profileDatabaseBuilder = it.toBuilder()
      if (profile.isAdmin) {
        val deviceSettingsBuilder = it.deviceSettings.toBuilder()
        deviceSettingsBuilder.automaticallyUpdateTopics = automaticallyUpdateTopics
        profileDatabaseBuilder.deviceSettings = deviceSettingsBuilder.build()
        Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
      } else {
        Pair(profileDatabaseBuilder.build(), ProfileActionStatus.PROFILE_NOT_ADMIN)
      }
    }
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_TOPIC_AUTOMATICALLY_PERMISSION_DEVICE_SETTINGS_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Updates the download access of an existing profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param allowDownloadAccess New download access status for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateAllowDownloadAccess(
    profileId: ProfileId,
    allowDownloadAccess: Boolean
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfile = profile.toBuilder().setAllowDownloadAccess(allowDownloadAccess)
        .build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_ALL_DOWNLOAD_ACCESS_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Updates the story text size of the profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param readingTextSize New text size for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateReadingTextSize(
    profileId: ProfileId,
    readingTextSize: ReadingTextSize
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfile = profile.toBuilder().setReadingTextSize(readingTextSize).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(UPDATE_READING_TEXT_SIZE_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Updates the app language of the profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param appLanguage New app language for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateAppLanguage(profileId: ProfileId, appLanguage: AppLanguage): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfile = profile.toBuilder().setAppLanguage(appLanguage).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_APP_LANGUAGE_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Updates the audio language of the profile.
   *
   * @param profileId the ID corresponding to the profile being updated.
   * @param audioLanguage New audio language for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateAudioLanguage(profileId: ProfileId, audioLanguage: AudioLanguage): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfile = profile.toBuilder().setAudioLanguage(audioLanguage).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_AUDIO_LANGUAGE_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Log in to the user's Profile by setting the current profile Id and updating profile's last logged in time.
   *
   * @param profileId the ID corresponding to the profile being logged into.
   * @return a [DataProvider] that indicates the success/failure of this login operation.
   */
  fun loginToProfile(profileId: ProfileId): DataProvider<Any?> {
    return setCurrentProfileId(profileId).transformAsync(LOGIN_TO_PROFILE_PROVIDER_ID) {
      return@transformAsync getDeferredResult(
        profileId,
        null,
        updateLastLoggedInAsync(profileId)
      )
    }
  }

  private fun setCurrentProfileId(profileId: ProfileId): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(SET_CURRENT_PROFILE_ID_PROVIDER_ID) {
      val profileDatabase = profileDataStore.readDataAsync().await()
      if (profileDatabase.profilesMap.containsKey(profileId.internalId)) {
        currentProfileId = profileId.internalId
        return@createInMemoryDataProviderAsync AsyncResult.success<Any?>(0)
      }
      AsyncResult.failed<Any?>(
        ProfileNotFoundException(
          "ProfileId ${profileId.internalId} is" +
            " not associated with an existing profile"
        )
      )
    }
  }

  private fun updateLastLoggedInAsync(profileId: ProfileId): Deferred<ProfileActionStatus> {
    return profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      val profile = it.profilesMap[profileId.internalId]
        ?: return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.PROFILE_NOT_FOUND)
      val updatedProfile =
        profile.toBuilder().setLastLoggedInTimestampMs(oppiaClock.getCurrentTimeMs()).build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
  }

  /**
   * Deletes an existing profile.
   *
   * @param profileId the ID corresponding to the profile being deleted.
   * @return a [DataProvider] that indicates the success/failure of this delete operation.
   */
  fun deleteProfile(profileId: ProfileId): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      if (!it.profilesMap.containsKey(profileId.internalId)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.PROFILE_NOT_FOUND)
      }
      if (!directoryManagementUtil.deleteDir(profileId.internalId.toString())) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.FAILED_TO_DELETE_DIR)
      }
      val profileDatabaseBuilder = it.toBuilder().removeProfiles(
        profileId.internalId
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(DELETE_PROFILE_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Returns the ProfileId of the current profile. The default value is -1 if currentProfileId
   * hasn't been set.
   */
  fun getCurrentProfileId(): ProfileId {
    return ProfileId.newBuilder().setInternalId(currentProfileId).build()
  }

  private suspend fun getDeferredResult(
    profileId: ProfileId?,
    name: String?,
    deferred: Deferred<ProfileActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      ProfileActionStatus.SUCCESS -> AsyncResult.success(null)
      ProfileActionStatus.INVALID_PROFILE_NAME -> AsyncResult.failed(
        ProfileNameOnlyLettersException("$name does not contain only letters")
      )
      ProfileActionStatus.PROFILE_NAME_NOT_UNIQUE -> AsyncResult.failed(
        ProfileNameNotUniqueException("$name is not unique to other profiles")
      )
      ProfileActionStatus.FAILED_TO_STORE_IMAGE -> AsyncResult.failed(
        FailedToStoreImageException(
          "Failed to store user's selected avatar image"
        )
      )
      ProfileActionStatus.FAILED_TO_GENERATE_GRAVATAR -> AsyncResult.failed(
        FailedToGenerateGravatarException("Failed to generate a gravatar url")
      )
      ProfileActionStatus.FAILED_TO_DELETE_DIR -> AsyncResult.failed(
        FailedToDeleteDirException(
          "Failed to delete directory with ${profileId?.internalId}"
        )
      )
      ProfileActionStatus.PROFILE_NOT_FOUND -> AsyncResult.failed(
        ProfileNotFoundException(
          "ProfileId ${profileId?.internalId} does not match an existing Profile"
        )
      )
      ProfileActionStatus.PROFILE_NOT_ADMIN -> AsyncResult.failed(
        ProfileNotAdminException(
          "ProfileId ${profileId?.internalId} does not match an existing admin"
        )
      )
      ProfileActionStatus.PROFILE_ALREADY_HAS_ADMIN -> AsyncResult.failed(
        ProfileAlreadyHasAdminException(
          "Profile cannot be an admin"
        )
      )
    }
  }

  private fun isNameUnique(newName: String, profileDatabase: ProfileDatabase): Boolean {
    val lowerCaseNewName = newName.toLowerCase(Locale.getDefault())
    profileDatabase.profilesMap.values.forEach {
      if (it.name.toLowerCase(Locale.getDefault()) == lowerCaseNewName) {
        return false
      }
    }
    return true
  }

  private fun alreadyHasAdmin(profileDatabase: ProfileDatabase): Boolean {
    profileDatabase.profilesMap.values.forEach {
      if (it.isAdmin) {
        return true
      }
    }
    return false
  }

  private fun saveImageToInternalStorage(avatarImagePath: Uri, profileDir: File): String? {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, avatarImagePath)
    val fileName = avatarImagePath.pathSegments.last()
    val imageFile = File(profileDir, fileName)
    try {
      FileOutputStream(imageFile).use { fos ->
        rotateAndCompressBitmap(avatarImagePath, bitmap, /* cropSize= */ 300)
          .compress(Bitmap.CompressFormat.PNG, /* quality= */ 100, fos)
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      logger.e(
        "ProfileManagementController",
        "Failed to store user submitted avatar image",
        e
      )
      return null
    }
    return imageFile.absolutePath
  }

  private fun onlyLetters(name: String): Boolean {
    return name.matches(Regex("^[ A-Za-z]+\$"))
  }

  private fun rotateAndCompressBitmap(uri: Uri, bitmap: Bitmap, cropSize: Int): Bitmap {
    val croppedBitmap = ThumbnailUtils.extractThumbnail(bitmap, cropSize, cropSize)
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val orientation = ExifInterface(inputStream).getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      1
    )
    var rotate = 0
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
      ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
      ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
    }
    val matrix = Matrix()
    matrix.postRotate(rotate.toFloat())
    return Bitmap.createBitmap(
      croppedBitmap,
      /* x= */ 0,
      /* y= */ 0,
      cropSize,
      cropSize,
      matrix,
      /* filter= */ true
    )
  }
}
