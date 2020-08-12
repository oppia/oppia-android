package org.oppia.domain.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.DeviceSettings
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileDatabase
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileManagementControllerTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ProfileManagementControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockProfilesObserver: Observer<AsyncResult<List<Profile>>>

  @Captor
  lateinit var profilesResultCaptor: ArgumentCaptor<AsyncResult<List<Profile>>>

  @Mock
  lateinit var mockProfileObserver: Observer<AsyncResult<Profile>>

  @Captor
  lateinit var profileResultCaptor: ArgumentCaptor<AsyncResult<Profile>>

  @Mock
  lateinit var mockUpdateResultObserver: Observer<AsyncResult<Any?>>

  @Captor
  lateinit var updateResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock
  lateinit var mockWasProfileAddedResultObserver: Observer<AsyncResult<Boolean>>

  @Captor
  lateinit var wasProfileAddedResultCaptor: ArgumentCaptor<AsyncResult<Boolean>>

  @Mock
  lateinit var mockDeviceSettingsObserver: Observer<AsyncResult<DeviceSettings>>

  @Captor
  lateinit var deviceSettingsResultCaptor: ArgumentCaptor<AsyncResult<DeviceSettings>>

  private val PROFILES_LIST = listOf<Profile>(
    Profile.newBuilder().setName("James").setPin("123").setAllowDownloadAccess(true).build(),
    Profile.newBuilder().setName("Sean").setPin("234").setAllowDownloadAccess(false).build(),
    Profile.newBuilder().setName("Ben").setPin("345").setAllowDownloadAccess(true).build(),
    Profile.newBuilder().setName("Rajat").setPin("456").setAllowDownloadAccess(false).build(),
    Profile.newBuilder().setName("Veena").setPin("567").setAllowDownloadAccess(true).build()
  )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileManagementControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testAddProfile_addProfile_checkProfileIsAdded() {
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val profileDatabase = readProfileDatabase()
    verifyUpdateSucceeded()

    val profile = profileDatabase.profilesMap[0]!!
    assertThat(profile.name).isEqualTo("James")
    assertThat(profile.pin).isEqualTo("123")
    assertThat(profile.allowDownloadAccess).isEqualTo(true)
    assertThat(profile.id.internalId).isEqualTo(0)
    assertThat(profile.storyTextSize).isEqualTo(StoryTextSize.MEDIUM_TEXT_SIZE)
    assertThat(profile.appLanguage).isEqualTo(AppLanguage.ENGLISH_APP_LANGUAGE)
    assertThat(profile.audioLanguage).isEqualTo(AudioLanguage.ENGLISH_AUDIO_LANGUAGE)
    assertThat(File(getAbsoluteDirPath("0")).isDirectory).isTrue()
  }

  @Test
  fun testAddProfile_addProfileWithNotUniqueName_checkResultIsFailure() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "JAMES",
      pin = "321",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateFailed()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("JAMES is not unique to other profiles")
  }

  @Test
  fun testAddProfile_addProfileWithNumberInName_checkResultIsFailure() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "James034",
      pin = "321",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateFailed()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("James034 does not contain only letters")
  }

  @Test
  fun testGetProfile_addManyProfiles_checkGetProfileIsCorrect() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    profileManagementController.getProfile(ProfileId.newBuilder().setInternalId(3).build())
      .observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetProfileSucceeded()
    val profile = profileResultCaptor.value.getOrThrow()
    assertThat(profile.name).isEqualTo("Rajat")
    assertThat(profile.pin).isEqualTo("456")
    assertThat(profile.allowDownloadAccess).isEqualTo(false)
    assertThat(profile.id.internalId).isEqualTo(3)
    assertThat(profile.storyTextSize).isEqualTo(StoryTextSize.MEDIUM_TEXT_SIZE)
    assertThat(profile.appLanguage).isEqualTo(AppLanguage.ENGLISH_APP_LANGUAGE)
    assertThat(profile.audioLanguage).isEqualTo(AudioLanguage.ENGLISH_AUDIO_LANGUAGE)
  }

  @Test
  fun testGetProfiles_addManyProfiles_checkAllProfilesAreAdded() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetMultipleProfilesSucceeded()
    val profiles = profilesResultCaptor.value.getOrThrow().sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  fun testGetProfiles_addManyProfiles_restartApplication_addProfile_checkAllProfilesAreAdded() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    setUpTestApplicationComponent()
    profileManagementController.addProfile(
      name = "Nikita",
      pin = "678",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false
    )
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetMultipleProfilesSucceeded()
    val profiles = profilesResultCaptor.value.getOrThrow().sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size + 1)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  fun testUpdateName_addProfiles_updateWithUniqueName_checkUpdateIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateName(profileId, "John")
      .observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileResultCaptor.value.getOrThrow().name).isEqualTo("John")
  }

  @Test
  fun testUpdateName_addProfiles_updateWithNotUniqueName_checkUpdatedFailed() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateName(profileId, "James")
      .observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateFailed()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("James is not unique to other profiles")
  }

  @Test
  fun testUpdateName_addProfiles_updateWithBadProfileId_checkUpdatedFailed() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.updateName(profileId, "John")
      .observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateFailed()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateName_addProfiles_updateProfileAvatar_checkUpdateIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController
      .updateProfileAvatar(
        profileId,
        /* avatarImagePath = */ null,
        colorRgb = -10710042
      )
      .observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileResultCaptor.value.getOrThrow().avatar.avatarColorRgb)
      .isEqualTo(-10710042)
  }

  @Test
  fun testUpdatePin_addProfiles_updatePin_checkUpdateIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updatePin(profileId, "321")
      .observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileResultCaptor.value.getOrThrow().pin).isEqualTo("321")
  }

  @Test
  fun testUpdatePin_addProfiles_updateWithBadProfileId_checkUpdateFailed() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.updatePin(profileId, "321")
      .observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateFailed()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateAllowDownloadAccess_addProfiles_updateDownloadAccess_checkUpdateIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateAllowDownloadAccess(profileId, false)
      .observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileResultCaptor.value.getOrThrow().allowDownloadAccess)
      .isEqualTo(false)
  }

  @Test
  fun testUpdateAllowDownloadAccess_addProfiles_updateWithBadProfileId_checkUpdatedFailed() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.updateAllowDownloadAccess(profileId, false)
      .observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateFailed()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateStoryTextSize_addProfiles_updateWithFontSize18_checkUpdateIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateStoryTextSize(profileId, StoryTextSize.MEDIUM_TEXT_SIZE)
      .observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileResultCaptor.value.getOrThrow().storyTextSize)
      .isEqualTo(StoryTextSize.MEDIUM_TEXT_SIZE)
  }

  @Test
  fun testUpdateAppLanguage_addProfiles_updateWithChineseLanguage_checkUpdateIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateAppLanguage(profileId, AppLanguage.CHINESE_APP_LANGUAGE)
      .observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileResultCaptor.value.getOrThrow().appLanguage)
      .isEqualTo(AppLanguage.CHINESE_APP_LANGUAGE)
  }

  @Test
  fun testUpdateAudioLanguage_addProfiles_updateWithFrenchLanguage_checkUpdateIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController
      .updateAudioLanguage(profileId, AudioLanguage.FRENCH_AUDIO_LANGUAGE)
      .observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileResultCaptor.value.getOrThrow().audioLanguage)
      .isEqualTo(AudioLanguage.FRENCH_AUDIO_LANGUAGE)
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfile_checkDeletionIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.deleteProfile(profileId).observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(profileResultCaptor.value.isFailure()).isTrue()
    assertThat(File(getAbsoluteDirPath("2")).isDirectory).isFalse()
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfiles_addProfile_checkIdIsNotReused() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId3 = ProfileId.newBuilder().setInternalId(3).build()
    val profileId4 = ProfileId.newBuilder().setInternalId(4).build()
    profileManagementController.deleteProfile(profileId3)
    profileManagementController.deleteProfile(profileId4)
    profileManagementController.addProfile(
      name = "John",
      pin = "321",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = true
    )
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetMultipleProfilesSucceeded()
    val profiles = profilesResultCaptor.value.getOrThrow().sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(4)
    assertThat(profiles[profiles.size - 2].name).isEqualTo("Ben")
    assertThat(profiles.last().name).isEqualTo("John")
    assertThat(profiles.last().id.internalId).isEqualTo(5)
    assertThat(File(getAbsoluteDirPath("3")).isDirectory).isFalse()
    assertThat(File(getAbsoluteDirPath("4")).isDirectory).isFalse()
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfiles_restartApplication_checkDeletionIsSuccessful() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId1 = ProfileId.newBuilder().setInternalId(1).build()
    val profileId2 = ProfileId.newBuilder().setInternalId(2).build()
    val profileId3 = ProfileId.newBuilder().setInternalId(3).build()
    profileManagementController.deleteProfile(profileId1)
    profileManagementController.deleteProfile(profileId2)
    profileManagementController.deleteProfile(profileId3)
    testCoroutineDispatchers.runCurrent()
    setUpTestApplicationComponent()
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetMultipleProfilesSucceeded()
    val profiles = profilesResultCaptor.value.getOrThrow()
    assertThat(profiles.size).isEqualTo(2)
    assertThat(profiles.first().name).isEqualTo("James")
    assertThat(profiles.last().name).isEqualTo("Veena")
    assertThat(File(getAbsoluteDirPath("1")).isDirectory).isFalse()
    assertThat(File(getAbsoluteDirPath("2")).isDirectory).isFalse()
    assertThat(File(getAbsoluteDirPath("3")).isDirectory).isFalse()
  }

  @Test
  fun testLoginToProfile_addProfiles_loginToProfile_checkGetProfileIdAndLoginTimestampIsCorrect() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.loginToProfile(profileId).observeForever(mockUpdateResultObserver)
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()
    verifyGetProfileSucceeded()
    assertThat(profileManagementController.getCurrentProfileId().internalId)
      .isEqualTo(2)
    assertThat(profileResultCaptor.value.getOrThrow().lastLoggedInTimestampMs)
      .isNotEqualTo(0)
  }

  @Test
  fun testLoginToProfile_addProfiles_loginToProfileWithBadProfileId_checkLoginFailed() {
    addTestProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.loginToProfile(profileId).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateFailed()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains(
        "org.oppia.domain.profile.ProfileManagementController\$ProfileNotFoundException: " +
          "ProfileId 6 is not associated with an existing profile"
      )
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_checkIfProfileEverAdded() {
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val profileDatabase = readProfileDatabase()

    verifyUpdateSucceeded()
    assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(false)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_getWasProfileEverAdded() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.getWasProfileEverAdded()
      .observeForever(mockWasProfileAddedResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyWasProfileEverAddedSucceeded()
    val wasProfileEverAdded = wasProfileAddedResultCaptor.value.getOrThrow()
    assertThat(wasProfileEverAdded).isFalse()
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_checkIfProfileEverAdded() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "Rajat",
      pin = "01234",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val profileDatabase = readProfileDatabase()

    verifyUpdateSucceeded()
    assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(true)
    assertThat(profileDatabase.profilesMap.size).isEqualTo(2)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_getWasProfileEverAdded() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "Rajat",
      pin = "01234",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.getWasProfileEverAdded()
      .observeForever(mockWasProfileAddedResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyWasProfileEverAddedSucceeded()

    val wasProfileEverAdded = wasProfileAddedResultCaptor.value.getOrThrow()
    assertThat(wasProfileEverAdded).isTrue()
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_profileIsAdded() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "Rajat",
      pin = "01234",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val profileId1 = ProfileId.newBuilder().setInternalId(1).build()
    profileManagementController.deleteProfile(profileId1)
    testCoroutineDispatchers.runCurrent()

    val profileDatabase = readProfileDatabase()

    verifyUpdateSucceeded()
    assertThat(profileDatabase.profilesMap.size).isEqualTo(1)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_profileWasAdded() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "Rajat",
      pin = "01234",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val profileId1 = ProfileId.newBuilder().setInternalId(1).build()
    profileManagementController.deleteProfile(profileId1)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.getWasProfileEverAdded()
      .observeForever(mockWasProfileAddedResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyWasProfileEverAddedSucceeded()
    val wasProfileEverAdded = wasProfileAddedResultCaptor.value.getOrThrow()
    assertThat(wasProfileEverAdded).isTrue()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_getDefaultDeviceSettings_isSuccessful() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
    testCoroutineDispatchers.runCurrent()
    verifyGetDeviceSettingsSucceeded()

    val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
    assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_getDeviceSettings_isSuccessful() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()
    profileManagementController.updateWifiPermissionDeviceSettings(
      adminProfileId,
      /* downloadAndUpdateOnWifiOnly = */ true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()

    profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetDeviceSettingsSucceeded()
    val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
    assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateTopicsAutoDeviceSettings_isSuccessful() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()
    profileManagementController
      .updateTopicAutomaticallyPermissionDeviceSettings(
        adminProfileId,
        /* automaticallyUpdateTopics = */ true
      )
      .observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verifyUpdateSucceeded()

    profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
    testCoroutineDispatchers.runCurrent()

    verify(
      mockDeviceSettingsObserver,
      atLeastOnce()
    ).onChanged(deviceSettingsResultCaptor.capture())
    assertThat(deviceSettingsResultCaptor.value.isSuccess()).isTrue()

    val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
    assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_andTopicDevSettings_succeeds() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()
    profileManagementController.updateWifiPermissionDeviceSettings(
      adminProfileId,
      /* downloadAndUpdateOnWifiOnly = */ true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()
    verifyUpdateSucceeded()

    profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
      adminProfileId,
      /* automaticallyUpdateTopics = */ true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()
    verifyUpdateSucceeded()

    profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
    testCoroutineDispatchers.runCurrent()
    verifyGetDeviceSettingsSucceeded()

    val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
    assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
  }

  @Test
  fun testDeviceSettings_updateDeviceWifiSettings_fromUserProfile_isFailure() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "Rajat",
      pin = "01234",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val userProfileId = ProfileId.newBuilder().setInternalId(1).build()
    profileManagementController.updateWifiPermissionDeviceSettings(
      userProfileId,
      /* downloadAndUpdateOnWifiOnly = */ true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()
    verifyUpdateFailed()
  }

  @Test
  fun testDeviceSettings_updateTopicsAutomaticallyDeviceSettings_fromUserProfile_isFailure() {
    profileManagementController.addProfile(
      name = "James",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    profileManagementController.addProfile(
      name = "Rajat",
      pin = "01234",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = false
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    val userProfileId = ProfileId.newBuilder().setInternalId(1).build()
    profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
      userProfileId,
      /* automaticallyUpdateTopics = */ true
    ).observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()
    verifyUpdateFailed()
  }

  private fun verifyGetDeviceSettingsSucceeded() {
    verify(
      mockDeviceSettingsObserver,
      atLeastOnce()
    ).onChanged(deviceSettingsResultCaptor.capture())
    assertThat(deviceSettingsResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetProfileSucceeded() {
    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(profileResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetMultipleProfilesSucceeded() {
    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyUpdateSucceeded() {
    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyUpdateFailed() {
    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
  }

  private fun verifyWasProfileEverAddedSucceeded() {
    verify(
      mockWasProfileAddedResultObserver,
      atLeastOnce()
    ).onChanged(wasProfileAddedResultCaptor.capture())
    assertThat(wasProfileAddedResultCaptor.value.isSuccess()).isTrue()
  }

  private fun addTestProfiles() {
    PROFILES_LIST.forEach {
      profileManagementController.addProfile(
        name = it.name,
        pin = it.pin,
        avatarImagePath = null,
        allowDownloadAccess = it.allowDownloadAccess,
        colorRgb = -10710042,
        isAdmin = false
      )
    }
  }

  private fun checkTestProfilesArePresent(resultList: List<Profile>) {
    PROFILES_LIST.forEachIndexed { idx, profile ->
      assertThat(resultList[idx].name).isEqualTo(profile.name)
      assertThat(resultList[idx].pin).isEqualTo(profile.pin)
      assertThat(resultList[idx].allowDownloadAccess).isEqualTo(profile.allowDownloadAccess)
      assertThat(resultList[idx].id.internalId).isEqualTo(idx)
      assertThat(File(getAbsoluteDirPath(idx.toString())).isDirectory).isTrue()
    }
  }

  private fun getAbsoluteDirPath(path: String): String {
    /**
     * context.filesDir.toString() looks like /tmp/robolectric-Method_test_name/org.oppia.util.test-dataDir/files
     * dropLast(5) removes files from the path and then it appends the real path with "app_" as a prefix
     */
    return context.filesDir.toString().dropLast(5) + "app_" + path
  }

  private fun readProfileDatabase(): ProfileDatabase {
    return FileInputStream(
      File(
        context.filesDir,
        "profile_database.cache"
      )
    ).use(ProfileDatabase::parseFrom)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(profileManagementControllerTest: ProfileManagementControllerTest)
  }
}
