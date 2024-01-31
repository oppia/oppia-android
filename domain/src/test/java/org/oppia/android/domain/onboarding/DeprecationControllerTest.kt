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
import org.oppia.android.app.model.BuildFlavor
import org.oppia.android.app.model.DeprecationNoticeType
import org.oppia.android.app.model.DeprecationResponse
import org.oppia.android.app.model.DeprecationResponseDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
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
import org.oppia.android.util.system.OppiaClockModule
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import org.oppia.android.app.model.AppStartupState
import org.oppia.android.app.model.AppStartupState.StartupMode
import org.oppia.android.app.model.OnboardingState
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.model.PlatformParameter.SyncStatus
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.LOWEST_SUPPORTED_API_LEVEL
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue

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

  @Test
  fun testController_providesInitialState_indicatesNoUpdatesReceivedFromGatingConsole() {
    val defaultDeprecationResponseDatabase = DeprecationResponseDatabase.getDefaultInstance()

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
  fun testController_observedAfterSavingAppDeprecation_providesUpdatedDeprecationResponse() {
    executeInPreviousAppInstance { testComponent ->
      val appDeprecationResponse = DeprecationResponse.newBuilder().apply {
        deprecatedVersion = 5
        deprecationNoticeType = DeprecationNoticeType.APP_DEPRECATION
      }.build()

      testComponent.getDeprecationController().saveDeprecationResponse(appDeprecationResponse)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpDefaultTestApplicationComponent()

    val deprecationDataProvider = deprecationController
      .getDeprecationDatabase()

    val deprecationResponseDatabase = monitorFactory
      .waitForNextSuccessfulResult(deprecationDataProvider)

    assertThat(deprecationResponseDatabase.appDeprecationResponse)
      .isEqualTo(
        DeprecationResponse.newBuilder().apply {
          deprecatedVersion = 5
          deprecationNoticeType = DeprecationNoticeType.APP_DEPRECATION
        }.build()
      )
  }

  @Test
  fun testController_observedAfterSavingOsDeprecation_providesUpdatedDeprecationResponse() {
    executeInPreviousAppInstance { testComponent ->
      val osDeprecationResponse = DeprecationResponse.newBuilder().apply {
        deprecatedVersion = 5
        deprecationNoticeType = DeprecationNoticeType.OS_DEPRECATION
      }.build()

      testComponent.getDeprecationController().saveDeprecationResponse(osDeprecationResponse)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpDefaultTestApplicationComponent()

    val deprecationDataProvider = deprecationController
      .getDeprecationDatabase()

    val deprecationResponseDatabase = monitorFactory
      .waitForNextSuccessfulResult(deprecationDataProvider)

    assertThat(deprecationResponseDatabase.osDeprecationResponse)
      .isEqualTo(
        DeprecationResponse.newBuilder().apply {
          deprecatedVersion = 5
          deprecationNoticeType = DeprecationNoticeType.OS_DEPRECATION
        }.build()
      )
  }

  @Test
  fun testController_ifUserNotOnboarded_returnsUserNotOnboardedStartUpMode() {
    setUpDefaultTestApplicationComponent()

    val onboardingState = OnboardingState.newBuilder().build()
    val deprecationResponseDatabase = DeprecationResponseDatabase.getDefaultInstance()
    val startUpMode = deprecationController.processStartUpMode(
      onboardingState, deprecationResponseDatabase
    )

    assertThat(startUpMode).isEqualTo(StartupMode.USER_NOT_YET_ONBOARDED)
  }

  @Test
  fun testController_ifUserIsOnboarded_returnsUserIsOnboardedStartUpMode() {
    setUpDefaultTestApplicationComponent()

    val onboardingState = OnboardingState.newBuilder()
      .setAlreadyOnboardedApp(true)
      .build()
    val deprecationResponseDatabase = DeprecationResponseDatabase.getDefaultInstance()
    val startupMode = deprecationController.processStartUpMode(
      onboardingState, deprecationResponseDatabase
    )

    assertThat(startupMode).isEqualTo(StartupMode.USER_IS_ONBOARDED)
  }

  @Test
  fun testController_ifUserIsOnboarded_andOsIsDeprecated_returnsOsIsDeprecatedStartUpMode() {
    val lowestApiLevel = PlatformParameter.newBuilder()
      .setName(LOWEST_SUPPORTED_API_LEVEL)
      .setInteger(Int.MAX_VALUE)
      .setSyncStatus(SyncStatus.SYNCED_FROM_SERVER)
      .build()

//    executeInPreviousAppInstance { testComponent ->
//      testComponent.getPlatformParameterController().updatePlatformParameterDatabase(
//        listOf(lowestApiLevel)
//      )
//
//      testComponent.getTestCoroutineDispatchers().runCurrent()
//    }

    setUpDefaultTestApplicationComponent()

    platformParameterController.updatePlatformParameterDatabase(
      listOf(lowestApiLevel)
    )
    testCoroutineDispatchers.runCurrent()

    // Retrieve the previously cached Platform Parameters from Cache Store.
    monitorFactory.ensureDataProviderExecutes(platformParameterController.getParameterDatabase())

    testCoroutineDispatchers.runCurrent()

    val lowestSupportedApiLevel = platformParameterSingleton.getIntegerPlatformParameter(
      LOWEST_SUPPORTED_API_LEVEL
    )
    assertThat(lowestSupportedApiLevel?.value).isEqualTo(Int.MAX_VALUE)

    val platformParameterMap = platformParameterSingleton.getPlatformParameterMap()

    // Previous String Platform Parameter is still same in the Database.
    assertThat(platformParameterMap)
      .containsEntry(LOWEST_SUPPORTED_API_LEVEL, lowestApiLevel)

    val onboardingState = OnboardingState.newBuilder()
      .setAlreadyOnboardedApp(true)
      .build()
    val deprecationResponseDatabase = DeprecationResponseDatabase.newBuilder()
      .setOsDeprecationResponse(
        DeprecationResponse.newBuilder()
          .setDeprecationNoticeType(DeprecationNoticeType.OS_DEPRECATION)
          .setDeprecatedVersion(0)
          .build()
      ).build()
    val startupMode = deprecationController.processStartUpMode(
      onboardingState, deprecationResponseDatabase
    )

    assertThat(startupMode).isEqualTo(StartupMode.OS_IS_DEPRECATED)
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
}
