package org.oppia.android.domain.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AppLanguage
import org.oppia.android.app.model.AppLanguage.CHINESE_APP_LANGUAGE
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguage.FRENCH_AUDIO_LANGUAGE
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileDatabase
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize.MEDIUM_TEXT_SIZE
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.profile.ProfileNameValidatorModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileManagementControllerTest]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileManagementControllerTest.TestApplication::class)
class ProfileManagementControllerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private companion object {
    private val PROFILES_LIST = listOf<Profile>(
      Profile.newBuilder().setName("James").setPin("123").setAllowDownloadAccess(true).build(),
      Profile.newBuilder().setName("Sean").setPin("234").setAllowDownloadAccess(false).build(),
      Profile.newBuilder().setName("Ben").setPin("345").setAllowDownloadAccess(true).build(),
      Profile.newBuilder().setName("Rajat").setPin("456").setAllowDownloadAccess(false).build(),
      Profile.newBuilder().setName("Veena").setPin("567").setAllowDownloadAccess(true).build()
    )

  private val allowedNames = listOf<String>("नमन", "Ben-Henning", "Rajat.T", "جيشنو")

  private val disallowedNames = listOf<String>("नमन7", "Ben_Henning", "Rajat..T", "جيشنو^&&")

    private val ADMIN_PROFILE_ID_0 = ProfileId.newBuilder().setInternalId(0).build()
    private val PROFILE_ID_1 = ProfileId.newBuilder().setInternalId(1).build()
    private val PROFILE_ID_2 = ProfileId.newBuilder().setInternalId(2).build()
    private val PROFILE_ID_3 = ProfileId.newBuilder().setInternalId(3).build()
    private val PROFILE_ID_4 = ProfileId.newBuilder().setInternalId(4).build()
    private val PROFILE_ID_6 = ProfileId.newBuilder().setInternalId(6).build()

    private const val DEFAULT_PIN = "12345"
    private const val DEFAULT_ALLOW_DOWNLOAD_ACCESS = true
    private const val DEFAULT_AVATAR_COLOR_RGB = -10710042
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testAddProfile_addProfile_checkProfileIsAdded() {
    val dataProvider = addAdminProfile(name = "James", pin = "123")

    monitorFactory.waitForNextSuccessfulResult(dataProvider)

    val profileDatabase = readProfileDatabase()
    val profile = profileDatabase.profilesMap[0]!!
    assertThat(profile.name).isEqualTo("James")
    assertThat(profile.pin).isEqualTo("123")
    assertThat(profile.allowDownloadAccess).isEqualTo(true)
    assertThat(profile.id.internalId).isEqualTo(0)
    assertThat(profile.readingTextSize).isEqualTo(MEDIUM_TEXT_SIZE)
    assertThat(profile.appLanguage).isEqualTo(AppLanguage.ENGLISH_APP_LANGUAGE)
    assertThat(profile.audioLanguage).isEqualTo(AudioLanguage.ENGLISH_AUDIO_LANGUAGE)
    assertThat(File(getAbsoluteDirPath("0")).isDirectory).isTrue()
  }

  @Test
  fun testAddProfile_addProfileWithNotUniqueName_checkResultIsFailure() {
    addTestProfiles()

    val dataProvider = addAdminProfile(name = "JAMES", pin = "321")

    val failure = monitorFactory.waitForNextFailureResult(dataProvider)
    assertThat(failure).hasMessageThat().contains("JAMES is not unique to other profiles")
  }

  @Test
  fun testAddProfile_addProfilesWithDisallowedNames_checkResultIsFailure() {
    addTestProfiles()

    val dataProvider = addAdminProfile(name = "James034", pin = "321")
    disallowedNames.forEach {
      profileManagementController.addProfile(
        name = it,
        pin = "321",
        avatarImagePath = null,
        allowDownloadAccess = false,
        colorRgb = -10710042,
        isAdmin = true
      ).toLiveData().observeForever(mockUpdateResultObserver)
      testCoroutineDispatchers.runCurrent()

    val failure = monitorFactory.waitForNextFailureResult(dataProvider)
    assertThat(failure).hasMessageThat().contains("James034 does not contain only letters")
      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains("$it does not contain only letters")
    }
  }

  @Test
  fun testAddProfiles_addProfilesWithAllowedNames_checkAllProfilesAreAdded() {
    addAllowedNameProfiles()
    testCoroutineDispatchers.runCurrent()

    val profileDatabase = readProfileDatabase()
    verifyUpdateSucceeded()

    val profiles = profileDatabase.profilesMap

    profiles.forEach { index, profile ->
      assertThat(profile.name).isEqualTo(allowedNames[index])
      assertThat(File(getAbsoluteDirPath("$index")).isDirectory).isTrue()
    }

    assertThat(profiles.size).isEqualTo(allowedNames.size)
  }

  @Test
  fun testGetProfile_addManyProfiles_checkGetProfileIsCorrect() {
    addTestProfiles()

    val dataProvider = profileManagementController.getProfile(PROFILE_ID_3)

    val profile = monitorFactory.waitForNextSuccessfulResult(dataProvider)
    assertThat(profile.name).isEqualTo("Rajat")
    assertThat(profile.pin).isEqualTo("456")
    assertThat(profile.allowDownloadAccess).isEqualTo(false)
    assertThat(profile.id.internalId).isEqualTo(3)
    assertThat(profile.readingTextSize).isEqualTo(MEDIUM_TEXT_SIZE)
    assertThat(profile.appLanguage).isEqualTo(AppLanguage.ENGLISH_APP_LANGUAGE)
    assertThat(profile.audioLanguage).isEqualTo(AudioLanguage.ENGLISH_AUDIO_LANGUAGE)
  }

  @Test
  fun testGetProfiles_addManyProfiles_checkAllProfilesAreAdded() {
    addTestProfiles()

    val dataProvider = profileManagementController.getProfiles()

    val profiles = monitorFactory.waitForNextSuccessfulResult(dataProvider).sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  fun testGetProfiles_addManyProfiles_restartApplication_addProfile_checkAllProfilesAreAdded() {
    addTestProfiles()

    setUpTestApplicationComponent()
    addNonAdminProfileAndWait(name = "Nikita", pin = "678", allowDownloadAccess = false)
    val dataProvider = profileManagementController.getProfiles()

    val profiles = monitorFactory.waitForNextSuccessfulResult(dataProvider).sortedBy {
      it.id.internalId
    }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size + 1)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  fun testUpdateName_addProfiles_updateWithUniqueName_checkUpdateIsSuccessful() {
    addTestProfiles()

    val updateProvider = profileManagementController.updateName(PROFILE_ID_2, "John")
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.name).isEqualTo("John")
  }

  @Test
  fun testUpdateName_addProfiles_updateWithNotUniqueName_checkUpdatedFailed() {
    addTestProfiles()

    val updateProvider = profileManagementController.updateName(PROFILE_ID_2, "James")

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("James is not unique to other profiles")
  }

  @Test
  fun testUpdateName_addProfiles_updateWithBadProfileId_checkUpdatedFailed() {
    addTestProfiles()

    val updateProvider = profileManagementController.updateName(PROFILE_ID_6, "John")

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateName_addProfiles_updateProfileAvatar_checkUpdateIsSuccessful() {
    addTestProfiles()

    val updateProvider = profileManagementController
      .updateProfileAvatar(
        PROFILE_ID_2,
        /* avatarImagePath = */ null,
        colorRgb = DEFAULT_AVATAR_COLOR_RGB
      )
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.avatar.avatarColorRgb).isEqualTo(DEFAULT_AVATAR_COLOR_RGB)
  }

  @Test
  fun testUpdatePin_addProfiles_updatePin_checkUpdateIsSuccessful() {
    addTestProfiles()

    val updateProvider = profileManagementController.updatePin(PROFILE_ID_2, "321")
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.pin).isEqualTo("321")
  }

  @Test
  fun testUpdatePin_addProfiles_updateWithBadProfileId_checkUpdateFailed() {
    addTestProfiles()

    val updateProvider = profileManagementController.updatePin(PROFILE_ID_6, "321")
    testCoroutineDispatchers.runCurrent()

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateAllowDownloadAccess_addProfiles_updateDownloadAccess_checkUpdateIsSuccessful() {
    addTestProfiles()

    val updateProvider = profileManagementController.updateAllowDownloadAccess(PROFILE_ID_2, false)
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.allowDownloadAccess).isEqualTo(false)
  }

  @Test
  fun testUpdateAllowDownloadAccess_addProfiles_updateWithBadProfileId_checkUpdatedFailed() {
    addTestProfiles()

    val updateProvider = profileManagementController.updateAllowDownloadAccess(PROFILE_ID_6, false)
    testCoroutineDispatchers.runCurrent()

    val error = monitorFactory.waitForNextFailureResult(updateProvider)
    assertThat(error).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  fun testUpdateReadingTextSize_addProfiles_updateWithFontSize18_checkUpdateIsSuccessful() {
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateReadingTextSize(PROFILE_ID_2, MEDIUM_TEXT_SIZE)

    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.readingTextSize).isEqualTo(MEDIUM_TEXT_SIZE)
  }

  @Test
  fun testUpdateAppLanguage_addProfiles_updateWithChineseLanguage_checkUpdateIsSuccessful() {
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAppLanguage(PROFILE_ID_2, CHINESE_APP_LANGUAGE)

    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.appLanguage).isEqualTo(CHINESE_APP_LANGUAGE)
  }

  @Test
  fun testUpdateAudioLanguage_addProfiles_updateWithFrenchLanguage_checkUpdateIsSuccessful() {
    addTestProfiles()

    val updateProvider =
      profileManagementController.updateAudioLanguage(PROFILE_ID_2, FRENCH_AUDIO_LANGUAGE)
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(updateProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profile.audioLanguage).isEqualTo(FRENCH_AUDIO_LANGUAGE)
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfile_checkDeletionIsSuccessful() {
    addTestProfiles()

    val deleteProvider = profileManagementController.deleteProfile(PROFILE_ID_2)
    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)

    monitorFactory.waitForNextSuccessfulResult(deleteProvider)
    monitorFactory.waitForNextFailureResult(profileProvider)
    assertThat(File(getAbsoluteDirPath("2")).isDirectory).isFalse()
  }

  @Test
  fun testDeleteProfile_addProfiles_deleteProfiles_addProfile_checkIdIsNotReused() {
    addTestProfiles()

    profileManagementController.deleteProfile(PROFILE_ID_3)
    profileManagementController.deleteProfile(PROFILE_ID_4)
    addAdminProfileAndWait(name = "John", pin = "321")

    val profilesProvider = profileManagementController.getProfiles()
    val profiles = monitorFactory.waitForNextSuccessfulResult(profilesProvider).sortedBy {
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

    profileManagementController.deleteProfile(PROFILE_ID_1)
    profileManagementController.deleteProfile(PROFILE_ID_2)
    profileManagementController.deleteProfile(PROFILE_ID_3)
    testCoroutineDispatchers.runCurrent()
    setUpTestApplicationComponent()

    val profilesProvider = profileManagementController.getProfiles()
    val profiles = monitorFactory.waitForNextSuccessfulResult(profilesProvider)
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

    val loginProvider = profileManagementController.loginToProfile(PROFILE_ID_2)

    val profileProvider = profileManagementController.getProfile(PROFILE_ID_2)
    monitorFactory.waitForNextSuccessfulResult(loginProvider)
    val profile = monitorFactory.waitForNextSuccessfulResult(profileProvider)
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(2)
    assertThat(profile.lastLoggedInTimestampMs).isNotEqualTo(0)
  }

  @Test
  fun testLoginToProfile_addProfiles_loginToProfileWithBadProfileId_checkLoginFailed() {
    addTestProfiles()

    val loginProvider = profileManagementController.loginToProfile(PROFILE_ID_6)

    val error = monitorFactory.waitForNextFailureResult(loginProvider)
    assertThat(error)
      .hasMessageThat()
      .contains(
        "org.oppia.android.domain.profile.ProfileManagementController\$ProfileNotFoundException: " +
          "ProfileId 6 is not associated with an existing profile"
      )
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_checkIfProfileEverAdded() {
    val addProvider = addAdminProfile(name = "James", pin = "123")

    monitorFactory.waitForNextSuccessfulResult(addProvider)

    val profileDatabase = readProfileDatabase()
    assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(false)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_getWasProfileEverAdded() {
    addAdminProfileAndWait(name = "James")

    val wasProfileAddedProvider = profileManagementController.getWasProfileEverAdded()

    val wasProfileEverAdded = monitorFactory.waitForNextSuccessfulResult(wasProfileAddedProvider)
    assertThat(wasProfileEverAdded).isFalse()
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_checkIfProfileEverAdded() {
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val profileDatabase = readProfileDatabase()

    assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(true)
    assertThat(profileDatabase.profilesMap.size).isEqualTo(2)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_getWasProfileEverAdded() {
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val wasProfileAddedProvider = profileManagementController.getWasProfileEverAdded()
    testCoroutineDispatchers.runCurrent()

    val wasProfileEverAdded = monitorFactory.waitForNextSuccessfulResult(wasProfileAddedProvider)
    assertThat(wasProfileEverAdded).isTrue()
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_profileIsAdded() {
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    profileManagementController.deleteProfile(PROFILE_ID_1)
    testCoroutineDispatchers.runCurrent()

    val profileDatabase = readProfileDatabase()
    assertThat(profileDatabase.profilesMap.size).isEqualTo(1)
  }

  @Test
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_profileWasAdded() {
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")
    profileManagementController.deleteProfile(PROFILE_ID_1)
    testCoroutineDispatchers.runCurrent()

    val wasProfileAddedProvider = profileManagementController.getWasProfileEverAdded()
    testCoroutineDispatchers.runCurrent()

    val wasProfileEverAdded = monitorFactory.waitForNextSuccessfulResult(wasProfileAddedProvider)
    assertThat(wasProfileEverAdded).isTrue()
  }

  @Test
  fun testAddAdminProfile_addAnotherAdminProfile_checkSecondAdminProfileWasNotAdded() {
    addAdminProfileAndWait(name = "Rohit")

    val addProfile2 = addAdminProfile(name = "Ben")

    val error = monitorFactory.waitForNextFailureResult(addProfile2)
    assertThat(error).hasMessageThat().contains("Profile cannot be an admin")
  }

  @Test
  fun testDeviceSettings_addAdminProfile_getDefaultDeviceSettings_isSuccessful() {
    addAdminProfileAndWait(name = "James")

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()

    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
    assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_getDeviceSettings_isSuccessful() {
    addAdminProfileAndWait(name = "James")

    val updateProvider = profileManagementController.updateWifiPermissionDeviceSettings(
      ADMIN_PROFILE_ID_0,
      downloadAndUpdateOnWifiOnly = true
    )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()
    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
    assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateTopicsAutoDeviceSettings_isSuccessful() {
    addAdminProfileAndWait(name = "James")

    val updateProvider =
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        ADMIN_PROFILE_ID_0, automaticallyUpdateTopics = true
      )
    monitorFactory.ensureDataProviderExecutes(updateProvider)

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()
    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
    assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
  }

  @Test
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_andTopicDevSettings_succeeds() {
    addAdminProfileAndWait(name = "James")

    val updateProvider1 =
      profileManagementController.updateWifiPermissionDeviceSettings(
        ADMIN_PROFILE_ID_0, downloadAndUpdateOnWifiOnly = true
      )
    monitorFactory.ensureDataProviderExecutes(updateProvider1)
    val updateProvider2 =
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        ADMIN_PROFILE_ID_0, automaticallyUpdateTopics = true
      )
    monitorFactory.ensureDataProviderExecutes(updateProvider2)

    val deviceSettingsProvider = profileManagementController.getDeviceSettings()
    val deviceSettings = monitorFactory.waitForNextSuccessfulResult(deviceSettingsProvider)
    assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
    assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
  }

  @Test
  fun testDeviceSettings_updateDeviceWifiSettings_fromUserProfile_isFailure() {
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val updateProvider =
      profileManagementController.updateWifiPermissionDeviceSettings(
        PROFILE_ID_1, downloadAndUpdateOnWifiOnly = true
      )

    monitorFactory.waitForNextFailureResult(updateProvider)
  }

  @Test
  fun testDeviceSettings_updateTopicsAutomaticallyDeviceSettings_fromUserProfile_isFailure() {
    addAdminProfileAndWait(name = "James")
    addNonAdminProfileAndWait(name = "Rajat", pin = "01234")

    val updateProvider =
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        PROFILE_ID_1, automaticallyUpdateTopics = true
      )

    monitorFactory.waitForNextFailureResult(updateProvider)
  }

  private fun addTestProfiles() {
    val profileAdditionProviders = PROFILES_LIST.map {
      addNonAdminProfile(it.name, pin = it.pin, allowDownloadAccess = it.allowDownloadAccess)
    }
    profileAdditionProviders.forEach(monitorFactory::ensureDataProviderExecutes)
  }

  private fun addAllowedNameProfiles() {
    allowedNames.forEach {
      profileManagementController.addProfile(
        name = it,
        pin = "314",
        avatarImagePath = null,
        allowDownloadAccess = false,
        colorRgb = -10710042,
        isAdmin = false
      ).toLiveData().observeForever(mockUpdateResultObserver)
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
     * context.filesDir.toString() looks like /tmp/robolectric-Method_test_name/org.oppia.android.util.test-dataDir/files
     * dropLast(5) removes files from the path and then it appends the real path with "app_" as a prefix
     */
    return context.filesDir.toString().dropLast(5) + "app_" + path
  }

  private fun readProfileDatabase(): ProfileDatabase {
    return FileInputStream(
      File(context.filesDir, "profile_database.cache")
    ).use(ProfileDatabase::parseFrom)
  }

  private fun addAdminProfile(name: String, pin: String = DEFAULT_PIN): DataProvider<Any?> =
    addProfile(name, pin, isAdmin = true)

  private fun addAdminProfileAndWait(name: String, pin: String = DEFAULT_PIN) {
    monitorFactory.ensureDataProviderExecutes(addAdminProfile(name, pin))
  }

  private fun addNonAdminProfile(
    name: String,
    pin: String = DEFAULT_PIN,
    allowDownloadAccess: Boolean = DEFAULT_ALLOW_DOWNLOAD_ACCESS,
    colorRgb: Int = DEFAULT_AVATAR_COLOR_RGB
  ): DataProvider<Any?> {
    return addProfile(
      name, pin, avatarImagePath = null, allowDownloadAccess, colorRgb, isAdmin = false
    )
  }

  private fun addNonAdminProfileAndWait(
    name: String,
    pin: String = DEFAULT_PIN,
    allowDownloadAccess: Boolean = DEFAULT_ALLOW_DOWNLOAD_ACCESS,
    colorRgb: Int = DEFAULT_AVATAR_COLOR_RGB
  ) {
    monitorFactory.ensureDataProviderExecutes(
      addNonAdminProfile(name, pin, allowDownloadAccess, colorRgb)
    )
  }

  private fun addProfile(
    name: String,
    pin: String = DEFAULT_PIN,
    avatarImagePath: Uri? = null,
    allowDownloadAccess: Boolean = DEFAULT_ALLOW_DOWNLOAD_ACCESS,
    colorRgb: Int = DEFAULT_AVATAR_COLOR_RGB,
    isAdmin: Boolean
  ): DataProvider<Any?> {
    return profileManagementController.addProfile(
      name, pin, avatarImagePath, allowDownloadAccess, colorRgb, isAdmin
    )
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
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      ProfileNameValidatorModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(profileManagementControllerTest: ProfileManagementControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileManagementControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(profileManagementControllerTest: ProfileManagementControllerTest) {
      component.inject(profileManagementControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
