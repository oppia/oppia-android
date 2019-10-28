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
import org.oppia.app.model.ProfileId
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
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
  lateinit var mockProfileStatusObserver: Observer<AsyncResult<Any?>>
  @Captor
  lateinit var profileManagementStatusCaptor: ArgumentCaptor<AsyncResult<Any?>>

  private val profilesList = mutableListOf<Profile>()

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
    profilesList.add(Profile.newBuilder().setName("James").setPin("123").setAllowDownloadAccess(true).build())
    profilesList.add(Profile.newBuilder().setName("Sean").setPin("234").setAllowDownloadAccess(false).build())
    profilesList.add(Profile.newBuilder().setName("Ben").setPin("345").setAllowDownloadAccess(true).build())
    profilesList.add(Profile.newBuilder().setName("Rajat").setPin("456").setAllowDownloadAccess(false).build())
    profilesList.add(Profile.newBuilder().setName("Veena").setPin("567").setAllowDownloadAccess(true).build())
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
  fun testAddAndGetProfile_addProfile_checkProfileIsAdded() = runBlockingTest(coroutineContext) {
    profileManagementController.addProfile("James", "123", null, true)
    advanceUntilIdle()

    profileManagementController.getProfile(ProfileId.newBuilder().setInternalId(0).build())
      .observeForever(mockProfileObserver)

    verify(mockProfileObserver, atLeastOnce()).onChanged(profileResultCaptor.capture())
    val profile = profileResultCaptor.value.getOrThrow()
    assertThat(profile.name).isEqualTo("James")
    assertThat(profile.pin).isEqualTo("123")
    assertThat(profile.allowDownloadAccess).isEqualTo(true)
    assertThat(profile.id.internalId).isEqualTo(0)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddAndGetProfiles_addManyProfiles_checkAllProfilesAreAdded() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()

    profileManagementController.getProfiles().observeForever(mockProfilesObserver)

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    val profiles = profilesResultCaptor.value.getOrThrow().sortedBy { it.id.internalId }
    assertThat(profiles.size).isEqualTo(profilesList.size)
    checkOriginalProfilesArePresent(profiles)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfile_addProfileWithUri_checkImageIsSaved() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddProfile_addProfileWithNotUniqueName_checkResultIsFailure() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfile_addManyProfiles_checkGetCorrectProfile() = runBlockingTest(coroutineContext) {
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
  fun testGetProfiles_addManyProfiles_restartApplication_addProfile_checkAllProfilesAreAdded() = runBlockingTest(coroutineContext) {
    addTestProfiles()
    advanceUntilIdle()
    // TODO: Restarting application deletes cache?
    setUpTestApplicationComponent()
    profileManagementController.addProfile("Nikita", "678", null, false)
    advanceUntilIdle()

    profileManagementController.getProfiles().observeForever(mockProfilesObserver)

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    val profiles = profilesResultCaptor.value.getOrThrow().sortedBy { it.id.internalId }
    assertThat(profiles.size).isEqualTo(profilesList.size + 1)
    checkOriginalProfilesArePresent(profiles)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithUniqueName_checkIsSuccessful() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithNotUniqueName_checkIsFailure() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateName_addProfiles_updateWithBadProfileId_checkIsFailure() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdatePin_addProfiles_updatePin_checkIsSuccessful() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdatePin_addProfiles_updateWithBadProfileId_checkIsFailure() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAllowDownloadAccess_addProfiles_updateDownloadAccess_checkIsSuccessful() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAllowDownloadAccess_addProfiles_updateWithBadProfileId_checkIsFailure() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfile_deleteProfile_checkIsSuccessful() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfiles_deleteProfiles_addProfile_checkIdIsNotReused() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteProfile_addProfiles_deleteProfiles_restartApplication_checkIsSuccessful() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSetCurrentProfileId_addProfiles_setValidProfile_checkIsSuccessful() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testSetCurrentProfileId_addProfiles_setInvalidProfile_checkIsFailure() = runBlockingTest(coroutineContext) {

  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetProfileId_addProfiles_setProfile_checkGetProfileIdIsCorrect() = runBlockingTest(coroutineContext) {

  }



  @Test
  @ExperimentalCoroutinesApi
  fun testAllowDownloadAccess_addManyProfiles_updateDownloadAccess_checkDownloadAccessIsUpdated()
      = runBlockingTest(coroutineContext) {

  }

  private fun addTestProfiles() {
    profilesList.forEach {
      profileManagementController.addProfile(it.name, it.pin, null, it.allowDownloadAccess)
    }
  }

  private fun checkOriginalProfilesArePresent(resultList: List<Profile>) {
    profilesList.forEachIndexed { idx, profile ->
      assertThat(resultList[idx].name).isEqualTo(profile.name)
      assertThat(resultList[idx].pin).isEqualTo(profile.pin)
      assertThat(resultList[idx].allowDownloadAccess).isEqualTo(profile.allowDownloadAccess)
      assertThat(resultList[idx].id.internalId).isEqualTo(idx)
    }
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
