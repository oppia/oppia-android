package org.oppia.testing.profile

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
import org.oppia.app.model.Profile
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileTestHelperTest]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ProfileTestHelperTest {
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
  lateinit var mockUpdateResultObserver: Observer<AsyncResult<Any?>>

  @Captor
  lateinit var updateResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileTestHelperTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testInitializeProfiles_initializeProfiles_checkProfilesAreAddedAndCurrentIsSet() {
    profileTestHelper.initializeProfiles().observeForever(mockUpdateResultObserver)
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    val profiles = profilesResultCaptor.value.getOrThrow()
    assertThat(profiles[0].name).isEqualTo("Sean")
    assertThat(profiles[0].isAdmin).isTrue()
    assertThat(profiles[1].name).isEqualTo("Ben")
    assertThat(profiles[1].isAdmin).isFalse()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  fun testInitializeProfiles_addOnlyAdminProfile_checkProfileIsAddedAndCurrentIsSet() {
    profileTestHelper.addOnlyAdminProfile().observeForever(mockUpdateResultObserver)
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    val profiles = profilesResultCaptor.value.getOrThrow()
    assertThat(profiles.size).isEqualTo(1)
    assertThat(profiles[0].name).isEqualTo("Sean")
    assertThat(profiles[0].isAdmin).isTrue()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  fun testAddMoreProfiles_addMoreProfiles_checkProfilesAreAdded() {
    profileTestHelper.addMoreProfiles(10)
    testCoroutineDispatchers.runCurrent()
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    assertThat(profilesResultCaptor.value.getOrThrow().size).isEqualTo(10)
  }

  @Test
  fun testLoginToAdmin_initializeProfiles_loginToAdmin_checkIsSuccessful() {
    profileTestHelper.initializeProfiles()

    profileTestHelper.loginToAdmin().observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  fun testLoginToUser_initializeProfiles_loginToUser_checkIsSuccessful() {
    profileTestHelper.initializeProfiles()

    profileTestHelper.loginToUser().observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(1)
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

    fun inject(profileTestHelperTest: ProfileTestHelperTest)
  }
}
