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
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
import org.oppia.domain.topic.StoryProgressControllerTest.TestFirebaseModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config

/** Tests for [ProfileManagementControllerTest]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileManagementControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var context: Context

  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController

  @Mock lateinit var mockProfilesObserver: Observer<AsyncResult<List<Profile>>>
  @Captor lateinit var profilesResultCaptor: ArgumentCaptor<AsyncResult<List<Profile>>>

  @Mock lateinit var mockProfileObserver: Observer<AsyncResult<Profile>>
  @Captor lateinit var profileResultCaptor: ArgumentCaptor<AsyncResult<Profile>>

  @Mock lateinit var mockUpdateResultObserver: Observer<AsyncResult<Any?>>
  @Captor lateinit var updateResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock lateinit var mockWasProfileAddedResultObserver: Observer<AsyncResult<Boolean>>
  @Captor lateinit var wasProfileAddedResultCaptor: ArgumentCaptor<AsyncResult<Boolean>>

  @Mock lateinit var mockDeviceSettingsObserver: Observer<AsyncResult<DeviceSettings>>
  @Captor lateinit var deviceSettingsResultCaptor: ArgumentCaptor<AsyncResult<DeviceSettings>>

  private val PROFILES_LIST = listOf<Profile>(
    Profile.newBuilder().setName("James").setPin("123").setAllowDownloadAccess(true).build(),
    Profile.newBuilder().setName("Sean").setPin("234").setAllowDownloadAccess(false).build(),
    Profile.newBuilder().setName("Ben").setPin("345").setAllowDownloadAccess(true).build(),
    Profile.newBuilder().setName("Rajat").setPin("456").setAllowDownloadAccess(false).build(),
    Profile.newBuilder().setName("Veena").setPin("567").setAllowDownloadAccess(true).build()
  )

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileManagementControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfile_addProfile_checkProfileIsAdded() = runBlockingTest(coroutineContext) {
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true,
      storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
      appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
      audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
    ).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    val profileDatabase = readProfileDatabase()
    verifyUpdateSucceeded()

    val profile = profileDatabase.profilesMap[0]!!
    assertThat(profile.name).isEqualTo("James")
    assertThat(profile.pin).isEqualTo("123")
    assertThat(profile.allowDownloadAccess).isEqualTo(true)
    assertThat(profile.id.internalId).isEqualTo(0)
    assertThat(File(getAbsoluteDirPath("0")).isDirectory).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfile_addProfileWithNotUniqueName_checkResultIsFailure() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "JAMES",
        pin = "321",
        avatarImagePath = null,
        allowDownloadAccess = false,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)

      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains("JAMES is not unique to other profiles")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfile_addProfileWithNumberInName_checkResultIsFailure() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "James034",
        pin = "321",
        avatarImagePath = null,
        allowDownloadAccess = false,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)

      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains("James034 does not contain only letters")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfile_addManyProfiles_checkGetProfileIsCorrect() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      profileManagementController.getProfile(ProfileId.newBuilder().setInternalId(3).build())
        .observeForever(mockProfileObserver)

      verifyGetProfileSucceeded()
      val profile = profileResultCaptor.value.getOrThrow()
      assertThat(profile.name).isEqualTo("Rajat")
      assertThat(profile.pin).isEqualTo("456")
      assertThat(profile.allowDownloadAccess).isEqualTo(false)
      assertThat(profile.id.internalId).isEqualTo(3)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfiles_addManyProfiles_checkAllProfilesAreAdded() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      profileManagementController.getProfiles().observeForever(mockProfilesObserver)

      verifyGetMultipleProfilesSucceeded()
      val profiles = profilesResultCaptor.value.getOrThrow().sortedBy { it.id.internalId }
      assertThat(profiles.size).isEqualTo(PROFILES_LIST.size)
      checkTestProfilesArePresent(profiles)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfiles_addManyProfiles_restartApplication_addProfile_checkAllProfilesAreAdded() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      setUpTestApplicationComponent()
      profileManagementController.addProfile(
        name = "Nikita",
        pin = "678",
        avatarImagePath = null,
        allowDownloadAccess = false,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      )
      advanceUntilIdle()
      profileManagementController.getProfiles().observeForever(mockProfilesObserver)

      verifyGetMultipleProfilesSucceeded()
      val profiles = profilesResultCaptor.value.getOrThrow().sortedBy { it.id.internalId }
      assertThat(profiles.size).isEqualTo(PROFILES_LIST.size + 1)
      checkTestProfilesArePresent(profiles)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithUniqueName_checkUpdateIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updateName(profileId, "John")
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileResultCaptor.value.getOrThrow().name).isEqualTo("John")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithNotUniqueName_checkUpdatedFailed() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updateName(profileId, "James")
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains("James is not unique to other profiles")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithBadProfileId_checkUpdatedFailed() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(6).build()
      profileManagementController.updateName(profileId, "John")
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains("ProfileId 6 does not match an existing Profile")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateProfileAvatar_checkUpdateIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updateProfileAvatar(profileId, /* avatarImagePath = */ null, colorRgb = -10710042)
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileResultCaptor.value.getOrThrow().avatar.avatarColorRgb).isEqualTo(-10710042)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdatePin_addProfiles_updatePin_checkUpdateIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updatePin(profileId, "321")
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileResultCaptor.value.getOrThrow().pin).isEqualTo("321")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdatePin_addProfiles_updateWithBadProfileId_checkUpdateFailed() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(6).build()
      profileManagementController.updatePin(profileId, "321")
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains("ProfileId 6 does not match an existing Profile")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateAllowDownloadAccess_addProfiles_updateDownloadAccess_checkUpdateIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updateAllowDownloadAccess(profileId, false)
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileResultCaptor.value.getOrThrow().allowDownloadAccess).isEqualTo(false)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateAllowDownloadAccess_addProfiles_updateWithBadProfileId_checkUpdatedFailed() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(6).build()
      profileManagementController.updateAllowDownloadAccess(profileId, false)
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains("ProfileId 6 does not match an existing Profile")
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateStoryTextSize_addProfiles_updateWithFontSize18_checkUpdateIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updateStoryTextSize(profileId, StoryTextSize.MEDIUM_TEXT_SIZE)
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileResultCaptor.value.getOrThrow().storyTextSize).isEqualTo(StoryTextSize.MEDIUM_TEXT_SIZE)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateAppLanguage_addProfiles_updateWithChineseLanguage_checkUpdateIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updateAppLanguage(profileId, AppLanguage.CHINESE_APP_LANGUAGE)
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileResultCaptor.value.getOrThrow().appLanguage).isEqualTo(AppLanguage.CHINESE_APP_LANGUAGE)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateAudioLanguage_addProfiles_updateWithFrenchLanguage_checkUpdateIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.updateAudioLanguage(profileId, AudioLanguage.FRENCH_AUDIO_LANGUAGE)
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileResultCaptor.value.getOrThrow().audioLanguage).isEqualTo(AudioLanguage.FRENCH_AUDIO_LANGUAGE)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfiles_deleteProfile_checkDeletionIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.deleteProfile(profileId).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
      assertThat(profileResultCaptor.value.isFailure()).isTrue()
      assertThat(File(getAbsoluteDirPath("2")).isDirectory).isFalse()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfiles_deleteProfiles_addProfile_checkIdIsNotReused() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

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
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      )
      advanceUntilIdle()
      profileManagementController.getProfiles().observeForever(mockProfilesObserver)

      verifyGetMultipleProfilesSucceeded()
      val profiles = profilesResultCaptor.value.getOrThrow().sortedBy { it.id.internalId }
      assertThat(profiles.size).isEqualTo(4)
      assertThat(profiles[profiles.size - 2].name).isEqualTo("Ben")
      assertThat(profiles.last().name).isEqualTo("John")
      assertThat(profiles.last().id.internalId).isEqualTo(5)
      assertThat(File(getAbsoluteDirPath("3")).isDirectory).isFalse()
      assertThat(File(getAbsoluteDirPath("4")).isDirectory).isFalse()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfiles_deleteProfiles_restartApplication_checkDeletionIsSuccessful() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId1 = ProfileId.newBuilder().setInternalId(1).build()
      val profileId2 = ProfileId.newBuilder().setInternalId(2).build()
      val profileId3 = ProfileId.newBuilder().setInternalId(3).build()
      profileManagementController.deleteProfile(profileId1)
      profileManagementController.deleteProfile(profileId2)
      profileManagementController.deleteProfile(profileId3)
      advanceUntilIdle()
      setUpTestApplicationComponent()
      profileManagementController.getProfiles().observeForever(mockProfilesObserver)

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
  @ExperimentalCoroutinesApi
  fun testLoginToProfile_addProfiles_loginToProfile_checkGetProfileIdAndLoginTimestampIsCorrect() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(2).build()
      profileManagementController.loginToProfile(profileId).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

      verifyUpdateSucceeded()
      verifyGetProfileSucceeded()
      assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(2)
      assertThat(profileResultCaptor.value.getOrThrow().lastLoggedInTimestampMs).isNotEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testLoginToProfile_addProfiles_loginToProfileWithBadProfileId_checkLoginFailed() =
    runBlockingTest(coroutineContext) {
      addTestProfiles()
      advanceUntilIdle()

      val profileId = ProfileId.newBuilder().setInternalId(6).build()
      profileManagementController.loginToProfile(profileId).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      verifyUpdateFailed()
      assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
        .contains(
          "org.oppia.domain.profile.ProfileManagementController\$ProfileNotFoundException: " +
              "ProfileId 6 is not associated with an existing profile"
        )
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testWasProfileEverAdded_addAdminProfile_checkIfProfileEverAdded() = runBlockingTest(coroutineContext) {
    profileManagementController.addProfile(
      name = "James",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true,
      storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
      appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
      audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
    ).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    val profileDatabase = readProfileDatabase()

    verifyUpdateSucceeded()
    assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(false)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testWasProfileEverAdded_addAdminProfile_getWasProfileEverAdded() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.getWasProfileEverAdded()
        .observeForever(mockWasProfileAddedResultObserver)

      verifyWasProfileEverAddedSucceeded()
      val wasProfileEverAdded = wasProfileAddedResultCaptor.value.getOrThrow()
      assertThat(wasProfileEverAdded).isFalse()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_checkIfProfileEverAdded() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "Rajat",
        pin = "01234",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val profileDatabase = readProfileDatabase()

      verifyUpdateSucceeded()
      assertThat(profileDatabase.wasProfileEverAdded).isEqualTo(true)
      assertThat(profileDatabase.profilesMap.size).isEqualTo(2)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_getWasProfileEverAdded() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "Rajat",
        pin = "01234",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.getWasProfileEverAdded()
        .observeForever(mockWasProfileAddedResultObserver)

      verifyWasProfileEverAddedSucceeded()

      val wasProfileEverAdded = wasProfileAddedResultCaptor.value.getOrThrow()
      assertThat(wasProfileEverAdded).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_checkIfProfileEverAdded() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "Rajat",
        pin = "01234",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val profileId1 = ProfileId.newBuilder().setInternalId(1).build()
      profileManagementController.deleteProfile(profileId1)
      advanceUntilIdle()

      val profileDatabase = readProfileDatabase()

      verifyUpdateSucceeded()
      assertThat(profileDatabase.profilesMap.size).isEqualTo(1)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testWasProfileEverAdded_addAdminProfile_addUserProfile_deleteUserProfile_getWasProfileEverAdded() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "Rajat",
        pin = "01234",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val profileId1 = ProfileId.newBuilder().setInternalId(1).build()
      profileManagementController.deleteProfile(profileId1)
      advanceUntilIdle()

      profileManagementController.getWasProfileEverAdded()
        .observeForever(mockWasProfileAddedResultObserver)

      verifyWasProfileEverAddedSucceeded()
      val wasProfileEverAdded = wasProfileAddedResultCaptor.value.getOrThrow()
      assertThat(wasProfileEverAdded).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeviceSettings_addAdminProfile_getDefaultDeviceSettings_isSuccessful() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
      advanceUntilIdle()
      verifyGetDeviceSettingsSucceeded()

      val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
      assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
      assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_getDeviceSettings_isSuccessful() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()
      profileManagementController.updateWifiPermissionDeviceSettings(
        adminProfileId, /* downloadAndUpdateOnWifiOnly = */
        true
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      verifyUpdateSucceeded()

      profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
      advanceUntilIdle()

      verifyGetDeviceSettingsSucceeded()
      val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
      assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
      assertThat(deviceSettings.automaticallyUpdateTopics).isFalse()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeviceSettings_addAdminProfile_updateTopicsAutomaticallyDeviceSettings_getDeviceSettings_isSuccessful() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        adminProfileId, /* automaticallyUpdateTopics = */
        true
      )
        .observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      verifyUpdateSucceeded()

      profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
      advanceUntilIdle()

      verify(mockDeviceSettingsObserver, atLeastOnce()).onChanged(deviceSettingsResultCaptor.capture())
      assertThat(deviceSettingsResultCaptor.value.isSuccess()).isTrue()

      val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
      assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isFalse()
      assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeviceSettings_addAdminProfile_updateDeviceWifiSettings_updateTopicsAutomaticallyDeviceSettings_getDeviceSettings_isSuccessful() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val adminProfileId = ProfileId.newBuilder().setInternalId(0).build()
      profileManagementController.updateWifiPermissionDeviceSettings(
        adminProfileId, /* downloadAndUpdateOnWifiOnly = */
        true
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      verifyUpdateSucceeded()

      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        adminProfileId, /* automaticallyUpdateTopics = */
        true
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      verifyUpdateSucceeded()

      profileManagementController.getDeviceSettings().observeForever(mockDeviceSettingsObserver)
      advanceUntilIdle()
      verifyGetDeviceSettingsSucceeded()

      val deviceSettings = deviceSettingsResultCaptor.value.getOrThrow()
      assertThat(deviceSettings.allowDownloadAndUpdateOnlyOnWifi).isTrue()
      assertThat(deviceSettings.automaticallyUpdateTopics).isTrue()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeviceSettings_updateDeviceWifiSettings_fromUserProfile_isFailure() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "Rajat",
        pin = "01234",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val userProfileId = ProfileId.newBuilder().setInternalId(1).build()
      profileManagementController.updateWifiPermissionDeviceSettings(
        userProfileId, /* downloadAndUpdateOnWifiOnly = */
        true
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      verifyUpdateFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeviceSettings_updateTopicsAutomaticallyDeviceSettings_fromUserProfile_isFailure() =
    runBlockingTest(coroutineContext) {
      profileManagementController.addProfile(
        name = "James",
        pin = "12345",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = true,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      profileManagementController.addProfile(
        name = "Rajat",
        pin = "01234",
        avatarImagePath = null,
        allowDownloadAccess = true,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()

      val userProfileId = ProfileId.newBuilder().setInternalId(1).build()
      profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
        userProfileId, /* automaticallyUpdateTopics = */
        true
      ).observeForever(mockUpdateResultObserver)
      advanceUntilIdle()
      verifyUpdateFailed()
    }

  private fun verifyGetDeviceSettingsSucceeded() {
    verify(mockDeviceSettingsObserver, atLeastOnce()).onChanged(deviceSettingsResultCaptor.capture())
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
    verify(mockWasProfileAddedResultObserver, atLeastOnce()).onChanged(wasProfileAddedResultCaptor.capture())
    assertThat(wasProfileAddedResultCaptor.value.isSuccess()).isTrue()
  }

  @ExperimentalCoroutinesApi
  private fun addTestProfiles() {
    PROFILES_LIST.forEach {
      profileManagementController.addProfile(
        name = it.name,
        pin = it.pin,
        avatarImagePath = null,
        allowDownloadAccess = it.allowDownloadAccess,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = it.storyTextSize,
        appLanguage = it.appLanguage,
        audioLanguage = it.audioLanguage
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

  @Qualifier
  annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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
  @Component(modules = [TestModule::class, TestFirebaseModule::class])
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
