package org.oppia.android.domain.onboarding

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.app.model.DeprecationResponseDatabase
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.app.model.PlatformParameterId.FORCED_APP_UPDATE_VERSION_CODE
import org.oppia.android.app.model.PlatformParameterId.LOWEST_SUPPORTED_API_LEVEL
import org.oppia.android.app.model.PlatformParameterId.OPTIONAL_APP_UPDATE_VERSION_CODE
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjector
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjectorProvider
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.OverrideIntParameter
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.system.OppiaClockModule
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DeprecationController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@Config(application = DeprecationControllerTest.TestApplication::class)
class DeprecationControllerTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var deprecationController: DeprecationController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Test
  fun testController_initialAppLaunch_returnsDefaultDeprecationResponseDatabase() {
    setUpDefaultTestApplicationComponent()

    val deprecationDataProvider = deprecationController.getDeprecationDatabase()
    val deprecationResponseDatabase = monitorFactory
      .waitForNextSuccessfulResult(deprecationDataProvider)

    assertThat(deprecationResponseDatabase.osDeprecationResponse)
      .isEqualTo(defaultDeprecationResponseDatabase.osDeprecationResponse)
    assertThat(deprecationResponseDatabase.appDeprecationResponse)
      .isEqualTo(defaultDeprecationResponseDatabase.appDeprecationResponse)
  }

  @Test
  fun testController_previousResponseSaved_providesDeprecationDatabaseWithAppResponse() {
    executeInPreviousAppInstance { testComponent ->
      testComponent.getDeprecationController().saveDeprecationResponse(appDeprecationResponse)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpDefaultTestApplicationComponent()

    val deprecationDataProvider = deprecationController.getDeprecationDatabase()
    val deprecationResponseDatabase = monitorFactory
      .waitForNextSuccessfulResult(deprecationDataProvider)

    assertThat(deprecationResponseDatabase.appDeprecationResponse)
      .isEqualTo(appDeprecationResponse)
  }

  @Test
  fun testController_previousResponseSaved_providesDeprecationDatabaseWithOsResponse() {
    executeInPreviousAppInstance { testComponent ->
      testComponent.getDeprecationController().saveDeprecationResponse(osDeprecationResponse)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpDefaultTestApplicationComponent()

    val deprecationDataProvider = deprecationController.getDeprecationDatabase()
    val deprecationResponseDatabase = monitorFactory
      .waitForNextSuccessfulResult(deprecationDataProvider)

    assertThat(deprecationResponseDatabase.osDeprecationResponse).isEqualTo(osDeprecationResponse)
  }

  @Test
  fun testController_userNotOnboarded_returnsUserNotOnboardedStartUpMode() {
    setUpDefaultTestApplicationComponent()

    val onboardingState = OnboardingState.newBuilder().build()
    val startUpMode = deprecationController.processStartUpMode(
      onboardingState, defaultDeprecationResponseDatabase
    )

    assertThat(startUpMode).isEqualTo(StartupMode.USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testController_userIsOnboarded_returnsUserIsOnboardedStartUpMode() {
    setUpDefaultTestApplicationComponent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, defaultDeprecationResponseDatabase
    )

    assertThat(startupMode).isEqualTo(StartupMode.USER_IS_ONBOARDED)
  }

  @Test
  @OverrideIntParameter(LOWEST_SUPPORTED_API_LEVEL, Int.MAX_VALUE)
  fun testController_osIsDeprecated_returnsOsIsDeprecatedStartUpMode() {
    setUpDefaultTestApplicationComponent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, defaultDeprecationResponseDatabase
    )

    assertThat(startupMode).isEqualTo(StartupMode.OS_IS_DEPRECATED)
  }

  @Test
  @OverrideIntParameter(LOWEST_SUPPORTED_API_LEVEL, Int.MAX_VALUE)
  fun testController_osIsDeprecated_previousResponseExists_returnsUserIsOnboardedStartUpMode() {
    setUpDefaultTestApplicationComponent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, deprecationResponseDatabaseWithPreviousResponses
    )
    assertThat(startupMode).isEqualTo(StartupMode.USER_IS_ONBOARDED)
  }

  @Test
  @OverrideIntParameter(OPTIONAL_APP_UPDATE_VERSION_CODE, Int.MAX_VALUE)
  fun testController_hasOptionalUpdate_returnsOptionalUpdateAvailableStartupMode() {
    setUpDefaultTestApplicationComponent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, defaultDeprecationResponseDatabase
    )
    assertThat(startupMode).isEqualTo(StartupMode.OPTIONAL_UPDATE_AVAILABLE)
  }

  @Test
  @OverrideIntParameter(OPTIONAL_APP_UPDATE_VERSION_CODE, Int.MAX_VALUE)
  fun testController_hasOptionalUpdate_previousResponseExists_returnsUserIsOnboardedStartupMode() {
    setUpDefaultTestApplicationComponent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, deprecationResponseDatabaseWithPreviousResponses
    )
    assertThat(startupMode).isEqualTo(StartupMode.USER_IS_ONBOARDED)
  }

  @Test
  @OverrideIntParameter(FORCED_APP_UPDATE_VERSION_CODE, Int.MAX_VALUE)
  fun testController_hasForcedUpdate_returnsAppIsDeprecatedStartupMode() {
    setUpDefaultTestApplicationComponent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, defaultDeprecationResponseDatabase
    )
    assertThat(startupMode).isEqualTo(StartupMode.APP_IS_DEPRECATED)
  }

  @Test
  @OverrideIntParameter(FORCED_APP_UPDATE_VERSION_CODE, Int.MAX_VALUE)
  fun testController_hasForcedUpdate_previousResponseExists_returnsUserIsOnboardedStartupMode() {
    setUpDefaultTestApplicationComponent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, deprecationResponseDatabaseWithPreviousResponses
    )
    assertThat(startupMode).isEqualTo(StartupMode.USER_IS_ONBOARDED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun setUpOppiaApplication(expirationEnabled: Boolean, expDate: String) {
    setUpOppiaApplicationForContext(context, expirationEnabled, expDate)
  }

  /**
   * Creates a separate test application component and executes the specified block. This should be
   * called before [setUpTestApplicationComponent] to avoid undefined behavior in production code.
   * This can be used to simulate arranging state in a "prior" run of the app.
   *
   * Note that only dependencies fetched from the specified [TestApplicationComponent] should be
   * used, not any class-level injected dependencies.
   */
  private fun executeInPreviousAppInstance(block: (TestApplicationComponent) -> Unit) {
    val testApplication = TestApplication()
    // The true application is hooked as a base context. This is to make sure the new application
    // can behave like a real Android application class (per Robolectric) without having a shared
    // Dagger dependency graph with the application under test.
    testApplication.attachBaseContext(ApplicationProvider.getApplicationContext())
    block(testApplication.component)
  }

  private fun setUpOppiaApplicationForContext(
    context: Context,
    expirationEnabled: Boolean,
    expDate: String
  ) {
    val packageManager = Shadows.shadowOf(context.packageManager)
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

  private fun setUpDefaultTestApplicationComponent() {
    setUpTestApplicationComponent()

    // By default, set up the application to never expire.
    setUpOppiaApplication(expirationEnabled = false, expDate = "9999-12-31")
  }

  @Module
  class TestModule {
    companion object {
      var buildFlavor = BuildFlavor.BUILD_FLAVOR_UNSPECIFIED
    }

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

    @Provides
    fun provideTestingBuildFlavor(): BuildFlavor = buildFlavor
  }

  @Singleton
  @Component(
    modules = [
      ApplicationLifecycleModule::class,
      AssetModule::class,
      ExpirationMetaDataRetrieverModule::class,
      LocaleProdModule::class,
      LogStorageModule::class,
      LoggingIdentifierModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionUtilDebugModule::class,
      OppiaClockModule::class,
      PlatformParameterTestModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SyncStatusModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent :
    DataProvidersInjector,
    PlatformParameterInitializationInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getDeprecationController(): DeprecationController

    fun getContext(): Context

    fun inject(deprecationControllerTest: DeprecationControllerTest)
  }

  class TestApplication :
    Application(),
    DataProvidersInjectorProvider,
    PlatformParameterInitializationInjectorProvider {
    val component: TestApplicationComponent by lazy {
      DaggerDeprecationControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(deprecationControllerTest: DeprecationControllerTest) {
      component.inject(deprecationControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component

    override fun getPlatformParameterInitializationInjector() = component
  }

  companion object {
    val alreadyOnboardedOnboardingState: OnboardingState = OnboardingState.newBuilder()
      .setAlreadyOnboardedApp(true)
      .build()

    val osDeprecationResponse: DeprecationResponse = DeprecationResponse.newBuilder()
      .setDeprecationNoticeType(DeprecationNoticeType.OS_DEPRECATION)
      .setDeprecatedVersion(Int.MAX_VALUE)
      .build()

    val appDeprecationResponse: DeprecationResponse = DeprecationResponse.newBuilder()
      .setDeprecationNoticeType(DeprecationNoticeType.APP_DEPRECATION)
      .setDeprecatedVersion(Int.MAX_VALUE)
      .build()

    val defaultDeprecationResponseDatabase: DeprecationResponseDatabase =
      DeprecationResponseDatabase.getDefaultInstance()

    val deprecationResponseDatabaseWithPreviousResponses: DeprecationResponseDatabase =
      DeprecationResponseDatabase.newBuilder()
        .setOsDeprecationResponse(osDeprecationResponse)
        .setAppDeprecationResponse(appDeprecationResponse)
        .build()
  }
}
