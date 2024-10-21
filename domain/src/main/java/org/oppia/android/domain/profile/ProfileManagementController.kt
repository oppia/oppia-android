package org.oppia.android.domain.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.model.ProfileDatabase
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.data.persistence.PersistentCacheStore.PublishMode
import org.oppia.android.data.persistence.PersistentCacheStore.UpdateMode
import org.oppia.android.domain.oppialogger.LoggingIdentifierController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.LearnerAnalyticsLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.EnableLearnerStudyAnalytics
import org.oppia.android.util.platformparameter.EnableLoggingLearnerStudyIds
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.DirectoryManagementUtil
import org.oppia.android.util.profile.ProfileNameValidator
import org.oppia.android.util.system.OppiaClock
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_LOGGED_OUT_INTERNAL_PROFILE_ID = -1
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
private const val UPDATE_SESSION_ID_PROVIDER_ID = "update_session_id_after_login_provider_id"
private const val DELETE_PROFILE_PROVIDER_ID = "delete_profile_provider_id"
private const val SET_CURRENT_PROFILE_ID_PROVIDER_ID = "set_current_profile_id_provider_id"
private const val UPDATE_READING_TEXT_SIZE_PROVIDER_ID =
  "update_reading_text_size_provider_id"
private const val UPDATE_APP_LANGUAGE_PROVIDER_ID = "update_app_language_provider_id"
private const val GET_AUDIO_LANGUAGE_PROVIDER_ID = "get_audio_language_provider_id"
private const val UPDATE_AUDIO_LANGUAGE_PROVIDER_ID = "update_audio_language_provider_id"
private const val UPDATE_LEARNER_ID_PROVIDER_ID = "update_learner_id_provider_id"
private const val SET_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID =
  "record_survey_last_shown_timestamp_provider_id"
private const val RETRIEVE_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID =
  "retrieve_survey_last_shown_timestamp_provider_id"
private const val SET_LAST_SELECTED_CLASSROOM_ID_PROVIDER_ID =
  "set_last_selected_classroom_id_provider_id"
private const val RETRIEVE_LAST_SELECTED_CLASSROOM_ID_PROVIDER_ID =
  "retrieve_last_selected_classroom_id_provider_id"
private const val UPDATE_PROFILE_DETAILS_PROVIDER_ID = "update_profile_details_data_provider_id"
private const val UPDATE_PROFILE_TYPE_PROVIDER_ID = "update_profile_type_data_provider_id"

/** Controller for retrieving, adding, updating, and deleting profiles. */
@Singleton
class ProfileManagementController @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val context: Context,
  private val directoryManagementUtil: DirectoryManagementUtil,
  private val exceptionsController: ExceptionsController,
  private val oppiaClock: OppiaClock,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val loggingIdentifierController: LoggingIdentifierController,
  private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
  @EnableLearnerStudyAnalytics
  private val enableLearnerStudyAnalytics: PlatformParameterValue<Boolean>,
  @EnableLoggingLearnerStudyIds
  private val enableLoggingLearnerStudyIds: PlatformParameterValue<Boolean>,
  private val profileNameValidator: ProfileNameValidator,
  private val translationController: TranslationController
) {
  private var currentProfileId: Int = DEFAULT_LOGGED_OUT_INTERNAL_PROFILE_ID
  private val profileDataStore =
    cacheStoreFactory.create("profile_database", ProfileDatabase.getDefaultInstance())

  /** Indicates that the given name was is not unique. */
  class ProfileNameNotUniqueException(msg: String) : Exception(msg)

  /** Indicates that the given name does not contain only letters. */
  class ProfileNameOnlyLettersException(msg: String) : Exception(msg)

  /** Indicates that the selected image was not stored properly. */
  class FailedToStoreImageException(msg: String) : Exception(msg)

  /** Indicates that the profile's directory was not deleted properly. */
  class FailedToDeleteDirException(msg: String) : Exception(msg)

  /** Indicates that the given profileId is not associated with an existing profile. */
  class ProfileNotFoundException(msg: String) : Exception(msg)

  /** Indicates that the given profileId is not associated with an admin. */
  class ProfileNotAdminException(msg: String) : Exception(msg)

  /** Indicates that the Profile already has admin. */
  class ProfileAlreadyHasAdminException(msg: String) : Exception(msg)

  /** Indicates that the a ProfileType was not passed. */
  class UnknownProfileTypeException(msg: String) : Exception(msg)

  /** Indicates that the there is not device settings currently. */
  class DeviceSettingsNotFoundException(msg: String) : Exception(msg)

  /**
   * These statuses correspond to the exceptions above such that if the deferred contains
   * PROFILE_NOT_FOUND, the [ProfileNotFoundException] will be passed to a failed AsyncResult.
   */
  private enum class ProfileActionStatus {
    /** Indicates that the profile operation succeeded. */
    SUCCESS,

    /** Indicates that the operation failed due to an invalid profile name being provided. */
    INVALID_PROFILE_NAME,

    /**
     * Indicates that the operation failed due to a provided profile name not being unique among all
     * other existing profiles.
     */
    PROFILE_NAME_NOT_UNIQUE,

    /**
     * Indicates that the operation failed due to an internal failure when trying to store the
     * profile's avatar image.
     */
    FAILED_TO_STORE_IMAGE,

    /**
     * Indicates that the operation failed due to an internal failure when trying to delete a
     * profile's data directory.
     */
    FAILED_TO_DELETE_DIR,

    /** Indicates that the operation failed due to no profile existing for the provided ID. */
    PROFILE_NOT_FOUND,

    /**
     * Indicates that the operation failed due to the current user not being an app administrator
     * despite the operation requiring administrator privileges.
     */
    PROFILE_NOT_ADMIN,

    /**
     * Indicates that the operation failed due to an attempt to re-elevate an administrator to
     * administrator status (this should never happen in regular app operations).
     */
    PROFILE_ALREADY_HAS_ADMIN,

    /** Indicates that the operation failed due to the profileType property not supplied. */
    PROFILE_TYPE_UNKNOWN,
  }

  // TODO(#272): Remove init block when storeDataAsync is fixed
  init {
    profileDataStore.primeInMemoryAndDiskCacheAsync(
      updateMode = UpdateMode.UPDATE_IF_NEW_CACHE,
      publishMode = PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion {
      it?.let {
        oppiaLogger.e(
          "ProfileManagementController",
          "Failed to prime cache ahead of data retrieval for ProfileManagementController.",
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
        AsyncResult.Success(profile)
      } else {
        AsyncResult.Failure(
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
      AsyncResult.Success(wasProfileEverAdded)
    }
  }

  /** Returns device settings for the app. */
  fun getDeviceSettings(): DataProvider<DeviceSettings> {
    return profileDataStore.transformAsync(GET_DEVICE_SETTINGS_PROVIDER_ID) {
      val deviceSettings = it.deviceSettings
      if (deviceSettings != null) {
        AsyncResult.Success(deviceSettings)
      } else {
        AsyncResult.Failure(DeviceSettingsNotFoundException("Device Settings not found."))
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
    isAdmin: Boolean,
    allowInLessonQuickLanguageSwitching: Boolean = false
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      if (!enableLearnerStudyAnalytics.value && !profileNameValidator.isNameValid(name)) {
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

      val newProfile = Profile.newBuilder().apply {
        this.name = name
        this.pin = pin
        this.allowDownloadAccess = allowDownloadAccess
        this.allowInLessonQuickLanguageSwitching = allowInLessonQuickLanguageSwitching
        this.id = ProfileId.newBuilder().setInternalId(nextProfileId).build()
        dateCreatedTimestampMs = oppiaClock.getCurrentTimeMs()
        this.isAdmin = isAdmin
        readingTextSize = ReadingTextSize.MEDIUM_TEXT_SIZE
        numberOfLogins = 0

        if (enableLoggingLearnerStudyIds.value) {
          // Only set a learner ID if there's an ongoing user study.
          learnerId = loggingIdentifierController.createLearnerId()
        }

        avatar = ProfileAvatar.newBuilder().apply {
          if (avatarImagePath != null) {
            val imageUri =
              saveImageToInternalStorage(avatarImagePath, profileDir)
                ?: return@storeDataWithCustomChannelAsync Pair(
                  it,
                  ProfileActionStatus.FAILED_TO_STORE_IMAGE
                )
            avatarImageUri = imageUri
          } else avatarColorRgb = colorRgb
        }.build()
      }.build()

      val wasProfileEverAdded = it.profilesCount > 0

      val profileDatabaseBuilder =
        it.toBuilder()
          .putProfiles(nextProfileId, newProfile)
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
   * @param newName new name for the profile being updated.
   * @return a [DataProvider] that indicates the success/failure of this update operation.
   */
  fun updateName(profileId: ProfileId, newName: String): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      if (!enableLearnerStudyAnalytics.value && !profileNameValidator.isNameValid(newName)) {
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
   * Updates the profile type field of an existing profile.
   *
   * @param profileId the ID of the profile to update
   * @return a [DataProvider] that represents the result of the update operation
   */
  fun updateProfileType(
    profileId: ProfileId,
    profileType: ProfileType
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )

      val updatedProfile = profile.toBuilder()

      if (profileType == ProfileType.PROFILE_TYPE_UNSPECIFIED) {
        return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_TYPE_UNKNOWN
        )
      } else {
        updatedProfile.profileType = profileType
      }

      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile.build()
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(UPDATE_PROFILE_TYPE_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
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
   * Updates whether the user of the profile is allowed to use a user study-only in-lesson quick
   * content language switcher.
   *
   * @param profileId the ID corresponding to the profile being updated
   * @param allowInLessonQuickLanguageSwitching the new allowance status for the updating profile
   * @return a [DataProvider] that indicates the success/failure of this update operation
   */
  fun updateEnableInLessonQuickLanguageSwitching(
    profileId: ProfileId,
    allowInLessonQuickLanguageSwitching: Boolean
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfileDatabase = it.toBuilder().putProfiles(
        profileId.internalId,
        profile.toBuilder().apply {
          this.allowInLessonQuickLanguageSwitching = allowInLessonQuickLanguageSwitching
        }.build()
      ).build()
      Pair(updatedProfileDatabase, ProfileActionStatus.SUCCESS)
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
   * Initializes the learner ID of the specified profile (if not set), otherwise clears it if there
   * is no ongoing study.
   *
   * @param profileId the ID corresponding to the profile being updated
   */
  fun initializeLearnerId(profileId: ProfileId): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val updatedProfile = profile.toBuilder().apply {
        learnerId = when {
          // There should be no learner ID if no ongoing study.
          !enableLoggingLearnerStudyIds.value -> ""
          learnerId.isEmpty() -> loggingIdentifierController.createLearnerId() // Generate new ID.
          else -> learnerId // Keep it unchanged.
        }
      }.build()
      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_LEARNER_ID_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Returns the current audio language configured for the specified profile ID, as possibly set by
   * [updateAudioLanguage].
   *
   * The return [DataProvider] will automatically update for subsequent calls to
   * [updateAudioLanguage] for this [profileId].
   */
  fun getAudioLanguage(profileId: ProfileId): DataProvider<AudioLanguage> {
    return translationController.getAudioTranslationContentLanguage(
      profileId
    ).transform(GET_AUDIO_LANGUAGE_PROVIDER_ID) { oppiaLanguage ->
      when (oppiaLanguage) {
        OppiaLanguage.UNRECOGNIZED, OppiaLanguage.LANGUAGE_UNSPECIFIED, OppiaLanguage.HINGLISH,
        OppiaLanguage.PORTUGUESE, OppiaLanguage.SWAHILI -> AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED
        OppiaLanguage.ARABIC -> AudioLanguage.ARABIC_LANGUAGE
        OppiaLanguage.ENGLISH -> AudioLanguage.ENGLISH_AUDIO_LANGUAGE
        OppiaLanguage.HINDI -> AudioLanguage.HINDI_AUDIO_LANGUAGE
        OppiaLanguage.BRAZILIAN_PORTUGUESE -> AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE
        OppiaLanguage.NIGERIAN_PIDGIN -> AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE
      }
    }
  }

  /**
   * Updates the audio language of the profile.
   *
   * @param profileId the ID corresponding to the profile being updated
   * @param audioLanguage New audio language for the profile being updated
   * @return a [DataProvider] that indicates the success/failure of this update operation
   */
  fun updateAudioLanguage(profileId: ProfileId, audioLanguage: AudioLanguage): DataProvider<Any?> {
    val audioSelection = AudioTranslationLanguageSelection.newBuilder().apply {
      this.selectedLanguage = when (audioLanguage) {
        AudioLanguage.UNRECOGNIZED, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED,
        AudioLanguage.NO_AUDIO -> OppiaLanguage.LANGUAGE_UNSPECIFIED
        AudioLanguage.ENGLISH_AUDIO_LANGUAGE -> OppiaLanguage.ENGLISH
        AudioLanguage.HINDI_AUDIO_LANGUAGE -> OppiaLanguage.HINDI
        AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE -> OppiaLanguage.BRAZILIAN_PORTUGUESE
        AudioLanguage.ARABIC_LANGUAGE -> OppiaLanguage.ARABIC
        AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE -> OppiaLanguage.NIGERIAN_PIDGIN
      }
    }.build()
    // The transformation is needed to reinterpreted the result of the update to 'Any?'.
    return translationController.updateAudioTranslationContentLanguage(
      profileId, audioSelection
    ).transform(UPDATE_AUDIO_LANGUAGE_PROVIDER_ID) { value -> value }
  }

  /**
   * Updates the provided details of an newly created profile to migrate onboarding flow v2 support.
   *
   * @param profileId the ID of the profile to update
   * @param avatarImagePath the path to the profile's avatar image, or null if unset
   * @param colorRgb the randomly selected unique color to be used in place of a picture
   * @param newName the nickname to identify the profile
   * @param isAdmin whether the profile has administrator privileges
   * @return [DataProvider] that represents the result of the update operation
   */
  fun updateNewProfileDetails(
    profileId: ProfileId,
    profileType: ProfileType,
    avatarImagePath: Uri?,
    colorRgb: Int,
    newName: String,
    isAdmin: Boolean
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      if (!enableLearnerStudyAnalytics.value && !profileNameValidator.isNameValid(newName)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.INVALID_PROFILE_NAME)
      }
      val profile =
        it.profilesMap[profileId.internalId] ?: return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_NOT_FOUND
        )
      val profileDir = directoryManagementUtil.getOrCreateDir(profileId.toString())

      val updatedProfile = profile.toBuilder()

      if (avatarImagePath != null) {
        val imageUri =
          saveImageToInternalStorage(avatarImagePath, profileDir)
            ?: return@storeDataWithCustomChannelAsync Pair(
              it,
              ProfileActionStatus.FAILED_TO_STORE_IMAGE
            )
        updatedProfile.avatar =
          ProfileAvatar.newBuilder().setAvatarImageUri(imageUri).build()
      } else {
        updatedProfile.avatar =
          ProfileAvatar.newBuilder().setAvatarColorRgb(colorRgb).build()
      }

      if (profileType == ProfileType.PROFILE_TYPE_UNSPECIFIED) {
        return@storeDataWithCustomChannelAsync Pair(
          it,
          ProfileActionStatus.PROFILE_TYPE_UNKNOWN
        )
      } else {
        updatedProfile.profileType = profileType
      }

      updatedProfile.name = newName

      updatedProfile.isAdmin = isAdmin

      val profileDatabaseBuilder = it.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile.build()
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(UPDATE_PROFILE_DETAILS_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, newName, deferred)
    }
  }

  /**
   * Log in to the user's Profile by setting the current profile Id, updating profile's last logged
   * in time and updating the total number of logins for the current profile Id.
   *
   * @param profileId the ID corresponding to the profile being logged into.
   * @return a [DataProvider] that indicates the success/failure of this login operation.
   */
  fun loginToProfile(profileId: ProfileId): DataProvider<Any?> {
    return setCurrentProfileId(profileId).transformAsync(LOGIN_TO_PROFILE_PROVIDER_ID) {
      return@transformAsync getDeferredResult(
        profileId,
        null,
        updateLastLoggedInAsyncAndNumberOfLogins(profileId)
      )
    }.transform(UPDATE_SESSION_ID_PROVIDER_ID) {
      // Since a new user has logged in (or the same user logged in again), a new session ID should
      // be generated.
      loggingIdentifierController.updateSessionId()
      it
    }
  }

  private fun setCurrentProfileId(profileId: ProfileId): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(SET_CURRENT_PROFILE_ID_PROVIDER_ID) {
      val profileDatabase = profileDataStore.readDataAsync().await()
      if (profileDatabase.profilesMap.containsKey(profileId.internalId)) {
        currentProfileId = profileId.internalId
        return@createInMemoryDataProviderAsync AsyncResult.Success(0)
      }
      AsyncResult.Failure(
        ProfileNotFoundException(
          "ProfileId ${profileId.internalId} is" +
            " not associated with an existing profile"
        )
      )
    }
  }

  private fun updateLastLoggedInAsyncAndNumberOfLogins(profileId: ProfileId):
    Deferred<ProfileActionStatus> {
      return profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val profile = it.profilesMap[profileId.internalId]
          ?: return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.PROFILE_NOT_FOUND)
        val updatedProfile = profile.toBuilder()
          .setLastLoggedInTimestampMs(oppiaClock.getCurrentTimeMs())
          .setNumberOfLogins(profile.numberOfLogins + 1)
          .build()
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
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      if (!it.profilesMap.containsKey(profileId.internalId)) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.PROFILE_NOT_FOUND)
      }
      if (!directoryManagementUtil.deleteDir(profileId.internalId.toString())) {
        return@storeDataWithCustomChannelAsync Pair(it, ProfileActionStatus.FAILED_TO_DELETE_DIR)
      }
      val installationId = loggingIdentifierController.fetchInstallationId()
      val learnerId = it.profilesMap.getValue(profileId.internalId).learnerId
      learnerAnalyticsLogger.logDeleteProfile(installationId, profileId = null, learnerId)
      Pair(it.toBuilder().removeProfiles(profileId.internalId).build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(DELETE_PROFILE_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Deletes all profiles installed on the device (and logs out the current user).
   *
   * Note that this will not update the in-memory cache as the app is expected to be forcibly closed
   * after deletion (since there's no mechanism to notify existing cache stores that they need to
   * reload/reset from their on-disk copies).
   *
   * Finally, this method attempts to never fail by forcibly deleting all profiles even if some are
   * in a bad state (and would normally failed if attempted to be deleted via [deleteProfile]).
   */
  fun deleteAllProfiles(): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync {
      val installationId = loggingIdentifierController.fetchInstallationId()
      it.profilesMap.forEach { (internalProfileId, profile) ->
        directoryManagementUtil.deleteDir(internalProfileId.toString())
        learnerAnalyticsLogger.logDeleteProfile(installationId, profileId = null, profile.learnerId)
      }
      Pair(ProfileDatabase.getDefaultInstance(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(DELETE_PROFILE_PROVIDER_ID) {
      getDeferredResult(profileId = null, name = null, deferred)
    }
  }

  /** Returns the [ProfileId] of the current profile, or null if one hasn't yet been logged into. */
  fun getCurrentProfileId(): ProfileId? {
    return currentProfileId.takeIf { it != DEFAULT_LOGGED_OUT_INTERNAL_PROFILE_ID }?.let {
      ProfileId.newBuilder().setInternalId(it).build()
    }
  }

  /**
   * Returns the learner ID corresponding to the current logged-in profile (as given by
   * [getCurrentProfileId]), or null if there's no currently logged-in user.
   *
   * See [fetchLearnerId] for specifics.
   */
  suspend fun fetchCurrentLearnerId(): String? = getCurrentProfileId()?.let { fetchLearnerId(it) }

  /**
   * Returns the learner ID corresponding to the specified [profileId], or null if the specified
   * profile doesn't exist.
   *
   * There are three important considerations when using this method:
   * 1. The returned ID may be empty or undefined if analytics IDs are not currently enabled for
   *    logging.
   * 2. The learner ID can change for a profile, so this method only guarantees returning the
   *    *current* learner ID corresponding to the profile. A [DataProvider] on the profile itself
   *    should be used if the caller requires the learner ID be kept up-to-date.
   * 3. This method is meant to only be called by background coroutines and should never be used
   *    from UI code.
   */
  suspend fun fetchLearnerId(profileId: ProfileId): String? {
    val profileDatabase = profileDataStore.readDataAsync().await()
    return profileDatabase.profilesMap[profileId.internalId]?.learnerId
  }

  /**
   * Returns whether the exploration continue button animation has shown (or been disabled) for the
   * specified [profileId], or null if the profile doesn't exist.
   */
  suspend fun fetchContinueAnimationSeenStatus(profileId: ProfileId): Boolean? {
    val profileDatabase = profileDataStore.readDataAsync().await()
    return profileDatabase.profilesMap[profileId.internalId]?.isContinueButtonAnimationSeen
  }

  /** Marks that the continue button animation has been seen for the specified profile. */
  suspend fun markContinueButtonAnimationSeen(profileId: ProfileId) {
    val updateDatabaseDeferred = profileDataStore.storeDataAsync(true) {
      val profile = it.profilesMap[profileId.internalId]
      if (profile != null) {
        val updatedProfile = profile.toBuilder().setIsContinueButtonAnimationSeen(true).build()
        val profileDatabaseBuilder = it.toBuilder().putProfiles(
          profileId.internalId,
          updatedProfile
        )
        return@storeDataAsync profileDatabaseBuilder.build()
      } else it
    }
    updateDatabaseDeferred.await()
  }

  /**
   * Sets the timestamp when a nps survey was last shown for the specified profile.
   * Returns a [DataProvider] indicating whether the save was a success.
   */
  fun updateSurveyLastShownTimestamp(profileId: ProfileId): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { profileDatabase ->
      val profile = profileDatabase.profilesMap[profileId.internalId]
      val updatedProfile = profile?.toBuilder()?.setSurveyLastShownTimestampMs(
        oppiaClock.getCurrentTimeMs()
      )?.build()
      val profileDatabaseBuilder = profileDatabase.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      SET_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /** Returns the timestamp at which the nps survey was last shown. */
  fun retrieveSurveyLastShownTimestamp(
    profileId: ProfileId
  ): DataProvider<Long> {
    return profileDataStore.transformAsync(RETRIEVE_SURVEY_LAST_SHOWN_TIMESTAMP_PROVIDER_ID) {
      val surveyLastShownTimestampMs =
        it.profilesMap[profileId.internalId]?.surveyLastShownTimestampMs ?: 0L
      AsyncResult.Success(surveyLastShownTimestampMs)
    }
  }

  /**
   * Sets the last selected [classroomId] for the specified [profileId]. Returns a [DataProvider]
   * indicating whether the save was a success.
   */
  fun updateLastSelectedClassroomId(
    profileId: ProfileId,
    classroomId: String
  ): DataProvider<Any?> {
    val deferred = profileDataStore.storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { profileDatabase ->
      val profile = profileDatabase.profilesMap[profileId.internalId]
      val updatedProfile = profile?.toBuilder()?.setLastSelectedClassroomId(
        classroomId
      )?.build()
      val profileDatabaseBuilder = profileDatabase.toBuilder().putProfiles(
        profileId.internalId,
        updatedProfile
      )
      Pair(profileDatabaseBuilder.build(), ProfileActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      SET_LAST_SELECTED_CLASSROOM_ID_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(profileId, null, deferred)
    }
  }

  /**
   * Returns a [DataProvider] containing a nullable last selected classroom ID for the specified
   * [profileId].
   */
  fun retrieveLastSelectedClassroomId(
    profileId: ProfileId
  ): DataProvider<String?> {
    return profileDataStore.transformAsync(RETRIEVE_LAST_SELECTED_CLASSROOM_ID_PROVIDER_ID) {
      val lastSelectedClassroomId = it.profilesMap[profileId.internalId]?.lastSelectedClassroomId
      AsyncResult.Success(lastSelectedClassroomId)
    }
  }

  private suspend fun getDeferredResult(
    profileId: ProfileId?,
    name: String?,
    deferred: Deferred<ProfileActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      ProfileActionStatus.SUCCESS -> AsyncResult.Success(null)
      ProfileActionStatus.INVALID_PROFILE_NAME ->
        AsyncResult.Failure(
          ProfileNameOnlyLettersException("$name does not contain only letters")
        )
      ProfileActionStatus.PROFILE_NAME_NOT_UNIQUE ->
        AsyncResult.Failure(
          ProfileNameNotUniqueException("$name is not unique to other profiles")
        )
      ProfileActionStatus.FAILED_TO_STORE_IMAGE ->
        AsyncResult.Failure(
          FailedToStoreImageException(
            "Failed to store user's selected avatar image"
          )
        )
      ProfileActionStatus.FAILED_TO_DELETE_DIR ->
        AsyncResult.Failure(
          FailedToDeleteDirException(
            "Failed to delete directory with ${profileId?.internalId}"
          )
        )
      ProfileActionStatus.PROFILE_NOT_FOUND ->
        AsyncResult.Failure(
          ProfileNotFoundException(
            "ProfileId ${profileId?.internalId} does not match an existing Profile"
          )
        )
      ProfileActionStatus.PROFILE_NOT_ADMIN ->
        AsyncResult.Failure(
          ProfileNotAdminException(
            "ProfileId ${profileId?.internalId} does not match an existing admin"
          )
        )
      ProfileActionStatus.PROFILE_ALREADY_HAS_ADMIN ->
        AsyncResult.Failure(
          ProfileAlreadyHasAdminException(
            "Profile cannot be an admin"
          )
        )
      ProfileActionStatus.PROFILE_TYPE_UNKNOWN ->
        AsyncResult.Failure(UnknownProfileTypeException("ProfileType must be set."))
    }
  }

  private fun isNameUnique(newName: String, profileDatabase: ProfileDatabase): Boolean {
    val lowerCaseNewName = machineLocale.run { newName.toMachineLowerCase() }
    profileDatabase.profilesMap.values.forEach {
      if (machineLocale.run { it.name.toMachineLowerCase() } == lowerCaseNewName) {
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
    // TODO(#3616): Migrate to the proper SDK 29+ APIs.
    @Suppress("DEPRECATION") // The code is correct for targeted versions of Android.
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, avatarImagePath)
    val fileName = avatarImagePath.pathSegments.last()
    val imageFile = File(profileDir, fileName)
    try {
      FileOutputStream(imageFile).use { fos ->
        rotateAndCompressBitmap(avatarImagePath, bitmap, /* cropSize= */ 300)
          .compress(Bitmap.CompressFormat.PNG, /* quality = */ 100, fos)
      }
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      oppiaLogger.e(
        "ProfileManagementController",
        "Failed to store user submitted avatar image",
        e
      )
      return null
    }
    return imageFile.absolutePath
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
      /* x = */ 0,
      /* y = */ 0,
      cropSize,
      cropSize,
      matrix,
      /* filter = */ true
    )
  }
}
