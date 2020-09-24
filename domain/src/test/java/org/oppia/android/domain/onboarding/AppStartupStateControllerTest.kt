package org.oppia.android.domain.onboarding

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
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
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.AppStartupState.StartupMode.APP_IS_DEPRECATED
import org.oppia.android.app.model.AppStartupState.StartupMode.USER_IS_ONBOARDED
import org.oppia.android.app.model.AppStartupState.StartupMode.USER_NOT_YET_ONBOARDED
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AppStartupStateController]. */
@RunWith(AndroidJUnit4::class)
@Config(application = AppStartupStateControllerTest.TestApplication::class)
class AppStartupStateControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var appStartupStateController: AppStartupStateController

  @Inject
  lateinit var cacheFactory: PersistentCacheStore.Factory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockOnboardingObserver: Observer<AsyncResult<AppStartupState>>

  @Captor
  lateinit var appStartupStateCaptor: ArgumentCaptor<AsyncResult<AppStartupState>>

  private val expirationDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

  @Test
  fun testController_providesInitialLiveData_indicatesUserHasNotOnboardedTheApp() {
    setUpDefaultTestApplicationComponent()

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testControllerObserver_observedAfterSettingAppOnboarded_providesLiveData_userDidNotOnboardApp() { // ktlint-disable max-line-length
    setUpDefaultTestApplicationComponent()
    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()

    appStartupState.observeForever(mockOnboardingObserver)
    appStartupStateController.markOnboardingFlowCompleted()
    testCoroutineDispatchers.runCurrent()

    // The result should not indicate that the user onboarded the app because markUserOnboardedApp
    // does not notify observers of the change.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testController_settingAppOnboarded_observedNewController_userOnboardedApp() {
    // Simulate the previous app already having completed onboarding.
    executeInPreviousApp { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpDefaultTestApplicationComponent()
    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    // The app should be considered onboarded since a new LiveData instance was observed after
    // marking the app as onboarded.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_IS_ONBOARDED)
  }

  @Test
  @Suppress("DeferredResultUnused")
  fun testController_onboardedApp_cleared_observeNewController_userDidNotOnboardApp() {
    // Simulate the previous app already having completed onboarding, then cleared.
    executeInPreviousApp { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()

      val onboardingFlowStore = testComponent.getCacheFactory().create(
        "on_boarding_flow",
        OnboardingState.getDefaultInstance()
      )
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
      // Clear, then recreate the controller.
      onboardingFlowStore.clearCacheAsync()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpDefaultTestApplicationComponent()
    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    // The app should be considered not yet onboarded since the previous history was cleared.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationEnabled_beforeDeprecationDate_appNotDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringAfterToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationEnabled_onDeprecationDate_appIsDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringForToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(APP_IS_DEPRECATED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationEnabled_afterDeprecationDate_appIsDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(APP_IS_DEPRECATED)
  }

  @Test
  fun testInitialAppOpen_appDeprecationDisabled_afterDeprecationDate_appIsNotDeprecated() {
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = false, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowNotDone_deprecationEnabled_beforeDepDate_appNotDeprecated() {
    executeInPreviousApp { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringAfterToday()
      )
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringAfterToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowNotDone_deprecationEnabled_afterDepDate_appIsDeprecated() {
    executeInPreviousApp { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringBeforeToday()
      )
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(APP_IS_DEPRECATED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowCompleted_depEnabled_beforeDepDate_appNotDeprecated() {
    executeInPreviousApp { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringAfterToday()
      )

      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringAfterToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    // The user should be considered onboarded, but the app is not yet deprecated.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(USER_IS_ONBOARDED)
  }

  @Test
  fun testSecondAppOpen_onboardingFlowCompleted_deprecationEnabled_afterDepDate_appIsDeprecated() {
    executeInPreviousApp { testComponent ->
      setUpOppiaApplicationForContext(
        context = testComponent.getContext(),
        expirationEnabled = true,
        expDate = dateStringBeforeToday()
      )

      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    setUpOppiaApplication(expirationEnabled = true, expDate = dateStringBeforeToday())

    val appStartupState = appStartupStateController.getAppStartupState().toLiveData()
    appStartupState.observeForever(mockOnboardingObserver)
    testCoroutineDispatchers.runCurrent()

    // Despite the user completing the onboarding flow, the app is still deprecated.
    verify(mockOnboardingObserver, atLeastOnce()).onChanged(appStartupStateCaptor.capture())
    assertThat(appStartupStateCaptor.value.isSuccess()).isTrue()
    assertThat(appStartupStateCaptor.getStartupMode()).isEqualTo(APP_IS_DEPRECATED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpDefaultTestApplicationComponent() {
    setUpTestApplicationComponent()

    // By default, set up the application to never expire.
    setUpOppiaApplication(expirationEnabled = false, expDate = "9999-12-31")
  }

  /**
   * Creates a separate test application component and executes the specified block. This should be
   * called before [setUpTestApplicationComponent] to avoid undefined behavior in production code.
   * This can be used to simulate arranging state in a "prior" run of the app.
   *
   * Note that only dependencies fetched from the specified [TestApplicationComponent] should be
   * used, not any class-level injected dependencies.
   */
  private fun executeInPreviousApp(block: (TestApplicationComponent) -> Unit) {
    val testApplication = TestApplication()
    // The true application is hooked as a base context. This is to make sure the new application
    // can behave like a real Android application class (per Robolectric) without having a shared
    // Dagger dependency graph with the application under test.
    testApplication.attachBaseContext(ApplicationProvider.getApplicationContext())
    block(
      DaggerAppStartupStateControllerTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build()
    )
  }

  /** Returns a date string occurring before today. */
  private fun dateStringBeforeToday(): String {
    return computeDateString(Instant.now() - Duration.ofDays(1))
  }

  private fun dateStringForToday(): String {
    return computeDateString(Instant.now())
  }

  /** Returns a date string occurring after today. */
  private fun dateStringAfterToday(): String {
    return computeDateString(Instant.now() + Duration.ofDays(1))
  }

  private fun computeDateString(instant: Instant): String {
    return computeDateString(Date.from(instant))
  }

  private fun computeDateString(date: Date): String {
    return expirationDateFormat.format(date)
  }

  private fun setUpOppiaApplication(expirationEnabled: Boolean, expDate: String) {
    setUpOppiaApplicationForContext(context, expirationEnabled, expDate)
  }

  private fun setUpOppiaApplicationForContext(
    context: Context,
    expirationEnabled: Boolean,
    expDate: String
  ) {
    val packageManager = shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setName("Oppia")
        .build()
    applicationInfo.metaData = Bundle()
    applicationInfo.metaData.putBoolean("automatic_app_expiration_enabled", expirationEnabled)
    applicationInfo.metaData.putString("expiration_date", expDate)
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageManager.installPackage(packageInfo)
  }

  private fun ArgumentCaptor<AsyncResult<AppStartupState>>.getStartupMode(): StartupMode {
    return value.getOrThrow().startupMode
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
      TestModule::class, TestDispatcherModule::class, TestLogReportingModule::class,
      ExpirationMetaDataRetrieverModule::class // Use real implementation to test closer to prod.
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getAppStartupStateController(): AppStartupStateController

    fun getCacheFactory(): PersistentCacheStore.Factory

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun getContext(): Context

    fun inject(appStartupStateControllerTest: AppStartupStateControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppStartupStateControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(appStartupStateControllerTest: AppStartupStateControllerTest) {
      component.inject(appStartupStateControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
