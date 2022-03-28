package org.oppia.android.testing.profile

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
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [ProfileTestHelper]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileTestHelperTest.TestApplication::class)
class ProfileTestHelperTest {
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Mock lateinit var mockUpdateResultObserver: Observer<AsyncResult<Any?>>

  @Captor lateinit var updateResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testInitializeProfiles_initializeProfiles_checkProfilesAreAddedAndCurrentIsSet() {
    profileTestHelper.initializeProfiles().observeForever(mockUpdateResultObserver)
    val profilesProvider = profileManagementController.getProfiles()
    testCoroutineDispatchers.runCurrent()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value).isSuccess()
    val profiles = monitorFactory.waitForNextSuccessfulResult(profilesProvider)
    assertThat(profiles[0].name).isEqualTo("Admin")
    assertThat(profiles[0].isAdmin).isTrue()
    assertThat(profiles[1].name).isEqualTo("Ben")
    assertThat(profiles[1].isAdmin).isFalse()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  fun testInitializeProfiles_addOnlyAdminProfile_checkProfileIsAddedAndCurrentIsSet() {
    profileTestHelper.addOnlyAdminProfile().observeForever(mockUpdateResultObserver)
    val profilesProvider = profileManagementController.getProfiles()
    testCoroutineDispatchers.runCurrent()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value).isSuccess()
    val profiles = monitorFactory.waitForNextSuccessfulResult(profilesProvider)
    assertThat(profiles.size).isEqualTo(1)
    assertThat(profiles[0].name).isEqualTo("Admin")
    assertThat(profiles[0].isAdmin).isTrue()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  fun testAddMoreProfiles_addMoreProfiles_checkProfilesAreAdded() {
    profileTestHelper.addMoreProfiles(10)
    testCoroutineDispatchers.runCurrent()
    val profilesProvider = profileManagementController.getProfiles()
    testCoroutineDispatchers.runCurrent()

    val profiles = monitorFactory.waitForNextSuccessfulResult(profilesProvider)
    assertThat(profiles).hasSize(10)
  }

  @Test
  fun testLogIntoAdmin_initializeProfiles_logIntoAdmin_checkIsSuccessful() {
    profileTestHelper.initializeProfiles()

    profileTestHelper.logIntoAdmin().observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value).isSuccess()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  fun testLogIntoUser_initializeProfiles_logIntoUser_checkIsSuccessful() {
    profileTestHelper.initializeProfiles()

    profileTestHelper.logIntoUser().observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value).isSuccess()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(1)
  }

  @Test
  fun testLogIntoNewUser_initializeProfiles_logIntoUser_checkIsSuccessful() {
    profileTestHelper.initializeProfiles()

    profileTestHelper.logIntoNewUser().observeForever(mockUpdateResultObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value).isSuccess()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(2)
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
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggingIdentifierModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(profileTestHelperTest: ProfileTestHelperTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileTestHelperTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(profileTestHelperTest: ProfileTestHelperTest) {
      component.inject(profileTestHelperTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
