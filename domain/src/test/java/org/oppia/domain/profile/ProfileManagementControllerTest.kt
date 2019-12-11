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
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileDatabase
import org.oppia.app.model.ProfileId
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

/** Tests for [ProfileManagementControllerTest]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileManagementControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileManagementController: ProfileManagementController

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
    profileManagementController.addProfile("James", "123", null, true).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    val profileDatabase = readProfileDatabase()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    val profile = profileDatabase.profilesMap[0]!!
    assertThat(profile.name).isEqualTo("James")
    assertThat(profile.pin).isEqualTo("123")
    assertThat(profile.allowDownloadAccess).isEqualTo(true)
    assertThat(profile.id.internalId).isEqualTo(0)
    assertThat(File(getAbsoluteDirPath("0")).isDirectory).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfile_addProfileWithNotUniqueName_checkResultIsFailure() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    profileManagementController.addProfile("JAMES", "321", null, true).observeForever(mockUpdateResultObserver)

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("JAMES is not unique to other profiles")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfile_addProfileWithNumberInName_checkResultIsFailure() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    profileManagementController.addProfile("James034", "321", null, true).observeForever(mockUpdateResultObserver)

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("James034 does not contain only letters")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfile_addManyProfiles_checkGetProfileIsCorrect() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    profileManagementController.getProfile(ProfileId.newBuilder().setInternalId(3).build())
      .observeForever(mockProfileObserver)

    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(profileResultCaptor.value.isSuccess()).isTrue()
    val profile = profileResultCaptor.value.getOrThrow()
    assertThat(profile.name).isEqualTo("Rajat")
    assertThat(profile.pin).isEqualTo("456")
    assertThat(profile.allowDownloadAccess).isEqualTo(false)
    assertThat(profile.id.internalId).isEqualTo(3)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfiles_addManyProfiles_checkAllProfilesAreAdded() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    profileManagementController.getProfiles().observeForever(mockProfilesObserver)

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    val profiles = profilesResultCaptor.value.getOrThrow().sortedBy { it.id.internalId }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfiles_addManyProfiles_restartApplication_addProfile_checkAllProfilesAreAdded() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    setUpTestApplicationComponent()
    profileManagementController.addProfile("Nikita", "678", null, false)
    advanceUntilIdle()
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    val profiles = profilesResultCaptor.value.getOrThrow().sortedBy { it.id.internalId }
    assertThat(profiles.size).isEqualTo(PROFILES_LIST.size + 1)
    checkTestProfilesArePresent(profiles)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithUniqueName_checkUpdateIsSuccessful() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateName(profileId, "John").observeForever(mockUpdateResultObserver)
    advanceUntilIdle()
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.getOrThrow().name).isEqualTo("John")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithNotUniqueName_checkUpdatedFailed() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateName(profileId, "James").observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("James is not unique to other profiles")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithBadProfileId_checkUpdatedFailed() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.updateName(profileId, "John").observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdatePin_addProfiles_updatePin_checkUpdateIsSuccessful() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updatePin(profileId, "321").observeForever(mockUpdateResultObserver)
    advanceUntilIdle()
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.getOrThrow().pin).isEqualTo("321")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdatePin_addProfiles_updateWithBadProfileId_checkUpdateFailed() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.updatePin(profileId, "321").observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateAllowDownloadAccess_addProfiles_updateDownloadAccess_checkUpdateIsSuccessful() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.updateAllowDownloadAccess(profileId, false).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.getOrThrow().allowDownloadAccess).isEqualTo(false)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateAllowDownloadAccess_addProfiles_updateWithBadProfileId_checkUpdatedFailed() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.updateAllowDownloadAccess(profileId, false).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("ProfileId 6 does not match an existing Profile")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfiles_deleteProfile_checkDeletionIsSuccessful() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.deleteProfile(profileId).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.isFailure()).isTrue()
    assertThat(File(getAbsoluteDirPath("2")).isDirectory).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfiles_deleteProfiles_addProfile_checkIdIsNotReused() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId3 = ProfileId.newBuilder().setInternalId(3).build()
    val profileId4 = ProfileId.newBuilder().setInternalId(4).build()
    profileManagementController.deleteProfile(profileId3)
    profileManagementController.deleteProfile(profileId4)
    profileManagementController.addProfile("John", "321", null, true)
    advanceUntilIdle()
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
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
  fun testDeleteProfile_addProfiles_deleteProfiles_restartApplication_checkDeletionIsSuccessful() = runBlockingTest(coroutineContext) {
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

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
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
  fun testLoginToProfile_addProfiles_loginToProfile_checkGetProfileIdAndLoginTimestampIsCorrect() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(2).build()
    profileManagementController.loginToProfile(profileId).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()
    profileManagementController.getProfile(profileId).observeForever(mockProfileObserver)

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(2)
    assertThat(profileResultCaptor.value.getOrThrow().lastLoggedInTimestampMs).isNotEqualTo(0)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testLoginToProfile_addProfiles_loginToProfileWithBadProfileId_checkLoginFailed() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    val profileId = ProfileId.newBuilder().setInternalId(6).build()
    profileManagementController.loginToProfile(profileId).observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isFailure()).isTrue()
    assertThat(updateResultCaptor.value.getErrorOrNull()).hasMessageThat()
      .contains("org.oppia.domain.profile.ProfileManagementController\$ProfileNotFoundException: " +
          "ProfileId 6 is not associated with an existing profile")
  }

  @ExperimentalCoroutinesApi
  private fun addTestProfiles() {
    PROFILES_LIST.forEach {
      profileManagementController.addProfile(it.name, it.pin, null, it.allowDownloadAccess)
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
    return FileInputStream(File(context.filesDir, "profile_database.cache")).use(ProfileDatabase::parseFrom)
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
  @Component(modules = [TestModule::class])
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
