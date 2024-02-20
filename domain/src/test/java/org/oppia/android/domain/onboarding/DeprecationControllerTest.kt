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
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.app.model.DeprecationResponseDatabase
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.PlatformParameter.SyncStatus
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.FORCED_APP_UPDATE_VERSION_CODE
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LOWEST_SUPPORTED_API_LEVEL
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.OPTIONAL_APP_UPDATE_VERSION_CODE
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClockModule
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Tests for [DeprecationController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@Config(application = DeprecationControllerTest.TestApplication::class)
class DeprecationControllerTest {
  @Inject lateinit var context: Context
  @Inject lateinit var deprecationController: DeprecationController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var platformParameterSingleton: PlatformParameterSingleton
  @Inject lateinit var platformParameterController: PlatformParameterController

  @field:[Inject LowestSupportedApiLevel]
  lateinit var lowestSupportedApiLevelProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject OptionalAppUpdateVersionCode]
  lateinit var optionalAppUpdateVersionProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject ForcedAppUpdateVersionCode]
  lateinit var forcedAppUpdateVersionProvider: Provider<PlatformParameterValue<Int>>

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
  fun testController_osIsDeprecated_returnsOsIsDeprecatedStartUpMode() {
    setUpTestApplicationComponentWithParameters(
      platformParameters = listOf(lowestApiLevel)
    )

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, defaultDeprecationResponseDatabase
    )

    assertThat(startupMode).isEqualTo(StartupMode.OS_IS_DEPRECATED)
  }

  @Test
  fun testController_osIsDeprecated_previousResponseExists_returnsUserIsOnboardedStartUpMode() {
    setUpTestApplicationComponentWithParameters(
      platformParameters = listOf(lowestApiLevel)
    )

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, deprecationResponseDatabaseWithPreviousResponses
    )
    assertThat(startupMode).isEqualTo(StartupMode.USER_IS_ONBOARDED)
  }

  @Test
  fun testController_hasOptionalUpdate_returnsOptionalUpdateAvailableStartupMode() {
    setUpTestApplicationComponentWithParameters(
      platformParameters = listOf(optionalAppUpdateVersion)
    )

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, defaultDeprecationResponseDatabase
    )
    assertThat(startupMode).isEqualTo(StartupMode.OPTIONAL_UPDATE_AVAILABLE)
  }

  @Test
  fun testController_hasOptionalUpdate_previousResponseExists_returnsUserIsOnboardedStartupMode() {
    setUpTestApplicationComponentWithParameters(
      platformParameters = listOf(optionalAppUpdateVersion)
    )

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, deprecationResponseDatabaseWithPreviousResponses
    )
    assertThat(startupMode).isEqualTo(StartupMode.USER_IS_ONBOARDED)
  }

  @Test
  fun testController_hasForcedUpdate_returnsAppIsDeprecatedStartupMode() {
    setUpTestApplicationComponentWithParameters(
      platformParameters = listOf(forcedAppUpdateVersion)
    )

    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())
    testCoroutineDispatchers.runCurrent()

    val startupMode = deprecationController.processStartUpMode(
      alreadyOnboardedOnboardingState, defaultDeprecationResponseDatabase
    )
    assertThat(startupMode).isEqualTo(StartupMode.APP_IS_DEPRECATED)
  }

  @Test
  fun testController_hasForcedUpdate_previousResponseExists_returnsUserIsOnboardedStartupMode() {
    setUpTestApplicationComponentWithParameters(
      platformParameters = listOf(forcedAppUpdateVersion)
    )

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

  private fun setUpTestApplicationComponentWithParameters(
    platformParameters: List<PlatformParameter>
  ) {
    executeInPreviousAppInstance { testComponent ->
      testComponent.getPlatformParameterController().updatePlatformParameterDatabase(
        platformParameters
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpDefaultTestApplicationComponent()

    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())
    testCoroutineDispatchers.runCurrent()
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
    block(
      DaggerDeprecationControllerTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build()
    )
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
      LogStorageModule::class, RobolectricModule::class,
      TestModule::class, TestDispatcherModule::class, TestLogReportingModule::class,
      NetworkConnectionUtilDebugModule::class,
      OppiaClockModule::class, LocaleProdModule::class,
      ExpirationMetaDataRetrieverModule::class, // Use real implementation to test closer to prod.
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getDeprecationController(): DeprecationController

    fun getDataProviderTestMonitor(): DataProviderTestMonitor.Factory

    fun getPlatformParameterSingleton(): PlatformParameterSingleton

    fun getPlatformParameterController(): PlatformParameterController

    fun getCacheFactory(): PersistentCacheStore.Factory

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun getContext(): Context

    fun inject(deprecationControllerTest: DeprecationControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
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
  }

  companion object {
    val alreadyOnboardedOnboardingState: OnboardingState = OnboardingState.newBuilder()
      .setAlreadyOnboardedApp(true)
      .build()

    val lowestApiLevel: PlatformParameter = PlatformParameter.newBuilder()
      .setName(LOWEST_SUPPORTED_API_LEVEL)
      .setInteger(Int.MAX_VALUE)
      .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
      .build()

    val optionalAppUpdateVersion: PlatformParameter = PlatformParameter.newBuilder()
      .setName(OPTIONAL_APP_UPDATE_VERSION_CODE)
      .setInteger(Int.MAX_VALUE)
      .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
      .build()

    val forcedAppUpdateVersion: PlatformParameter = PlatformParameter.newBuilder()
      .setName(FORCED_APP_UPDATE_VERSION_CODE)
      .setInteger(Int.MAX_VALUE)
      .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
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
