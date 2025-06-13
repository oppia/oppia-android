package org.oppia.android.domain.platformparameter

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.RemoteFeatureFlag
import org.oppia.android.app.model.RemotePlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterAndFeatureFlagDatabase
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterController]. */

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterControllerDebugImplTest.TestApplication::class)
class PlatformParameterControllerDebugImplTest {
  @Inject
  lateinit var platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var platformParameterConfigRetriever: PlatformParameterConfigRetriever

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var platformParameterProcessState: PlatformParameterProcessState
  @Inject
  lateinit var cacheStoreFactory: PersistentCacheStore.Factory

  @Test
  fun testLoadEphemeralPlatformParameters_returnsNonEmptyList() {
    setUpTestApplicationComponent()
    val ephemeralParamsProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()

    val ephemeralParams =
      monitorFactory.waitForNextSuccessfulResult(ephemeralParamsProvider)

    assertThat(ephemeralParams).isNotEmpty()
  }

  @Test
  fun testLoadEphemeralFeatureFlags_returnsNonEmptyList() {
    setUpTestApplicationComponent()
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()

    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)

    assertThat(ephemeralFeatureFlags).isNotEmpty()
  }

  @Test
  fun testLoadEphemeralFeatureFlags_returnsCorrectDefaultValue() {
    setUpTestApplicationComponent()
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()

    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val defaultValues = platformParameterConfigRetriever
      .loadSupportedFeatureFlags()
      .featureFlagDefinitionList

    val multipleClassroomDefaultFlag = defaultValues
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    val multipleClassroomEphemeralFeatureFlag = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }
    assertThat(multipleClassroomEphemeralFeatureFlag?.currentValue)
      .isEqualTo(multipleClassroomDefaultFlag?.defaultIsEnabled)
  }

  @Test
  fun testLoadEphemeralPlatformParameters_returnsCorrectDefaultValue() {
    setUpTestApplicationComponent()
    val ephemeralPlatformParametersProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()

    val ephemeralPlatformParameters =
      monitorFactory.waitForNextSuccessfulResult(ephemeralPlatformParametersProvider)

    val defaultValues = platformParameterConfigRetriever
      .loadSupportedPlatformParameters()
      .platformParameterDefinitionList

    val cacheLatexRenderingParam = defaultValues
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    val cacheLatexRenderingEphemeralParam = ephemeralPlatformParameters
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    assertThat(cacheLatexRenderingEphemeralParam?.currentValue)
      .isEqualTo(cacheLatexRenderingParam?.defaultValue)
  }

  @Test
  fun testLoadParametersAsync_loadsCorrectDefaultFlagValue() {
    setUpTestApplicationComponent()
    platformParameterControllerDebugImpl.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()

    val defaultFlagValues = platformParameterConfigRetriever
      .loadSupportedFeatureFlags()
      .featureFlagDefinitionList

    val multipleClassroomDefaultFlag = defaultFlagValues
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    val multipleClassroomFlagFromProcessState = platformParameterProcessState
      .retrieveFeatureFlagState(FeatureFlagId.MULTIPLE_CLASSROOMS)

    assertThat(multipleClassroomDefaultFlag?.defaultIsEnabled)
      .isEqualTo(multipleClassroomFlagFromProcessState)
  }

  @Test
  fun testLoadParametersAsync_loadsCorrectDefaultParameterValue() {
    setUpTestApplicationComponent()
    platformParameterControllerDebugImpl.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()

    val defaultParameterValues = platformParameterConfigRetriever
      .loadSupportedPlatformParameters()
      .platformParameterDefinitionList

    val splashScreenWelcomeMessageDefaultParam = defaultParameterValues
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    val splashScreenWelcomeMessageParamFromProcessState = platformParameterProcessState
      .retrievePlatformParameterIntegerState(
        PlatformParameterId
          .SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
      )

    assertThat(splashScreenWelcomeMessageDefaultParam?.defaultValue?.integer)
      .isEqualTo(splashScreenWelcomeMessageParamFromProcessState)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_addRemoteValues_verifesCorrectResolvedValue() {
    executeInPreviousAppInstance { component ->
      addTestRemoteFeatureFlagToDatabase(component)

      val platformParameterControllerDebugImpl = component.platformParameterControllerDebugImpl()
      val testCoroutineDispatchers = component.testCoroutineDispatchers()

      platformParameterControllerDebugImpl.loadParametersAsync()
      testCoroutineDispatchers.runCurrent()
    }

    setUpTestApplicationComponent()

    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()

    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)

    val multipleClassroomEphemeralFeatureFlag = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    assertThat(multipleClassroomEphemeralFeatureFlag?.syncStatus)
      .isEqualTo(SyncStatus.SYNCED_FROM_SERVER)
    assertThat(multipleClassroomEphemeralFeatureFlag?.currentValue).isEqualTo(true)
  }

  @Test
  fun testLoadEphemeralPlatformParameters_addRemoteValues_verifiesCorrectResolvedValue() {
    executeInPreviousAppInstance { component ->
      addTestRemotePlatformParameterToDatabase(component)

      val platformParameterControllerDebugImpl = component.platformParameterControllerDebugImpl()
      val testCoroutineDispatchers = component.testCoroutineDispatchers()

      platformParameterControllerDebugImpl.loadParametersAsync()
      testCoroutineDispatchers.runCurrent()
    }

    setUpTestApplicationComponent()

    val ephemeralPlatformParametersProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()

    val ephemeralPlatformParameters =
      monitorFactory.waitForNextSuccessfulResult(ephemeralPlatformParametersProvider)

    val syncUpWorkerTimePeriodParam = ephemeralPlatformParameters
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    assertThat(syncUpWorkerTimePeriodParam?.syncStatus).isEqualTo(SyncStatus.SYNCED_FROM_SERVER)
    assertThat(syncUpWorkerTimePeriodParam?.currentValue?.integer).isEqualTo(24)
  }
  @Test
  fun testLoadParametersAsync_withRemotePlatformParameterValue_updatesProcessStateCorrectly() {
    executeInPreviousAppInstance { component ->
      addTestRemotePlatformParameterToDatabase(component)

      val platformParameterControllerDebugImpl = component.platformParameterControllerDebugImpl()
      val testCoroutineDispatchers = component.testCoroutineDispatchers()

      platformParameterControllerDebugImpl.loadParametersAsync()
      testCoroutineDispatchers.runCurrent()
    }

    setUpTestApplicationComponent()

    val platformParameterValueFromProcessState = platformParameterProcessState
      .retrievePlatformParameterIntegerState(
        PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
      )

    assertThat(platformParameterValueFromProcessState).isEqualTo(24L)
  }

  @Test
  fun testLoadParametersAsync_withRemoteFeatureFlagValue_updatesProcessStateCorrectly() {
    executeInPreviousAppInstance { component ->
      addTestRemoteFeatureFlagToDatabase(component)

      val platformParameterControllerDebugImpl = component.platformParameterControllerDebugImpl()
      val testCoroutineDispatchers = component.testCoroutineDispatchers()

      platformParameterControllerDebugImpl.loadParametersAsync()
      testCoroutineDispatchers.runCurrent()
    }

    setUpTestApplicationComponent()

    val featureFlagValueFromProcessState = platformParameterProcessState
      .retrieveFeatureFlagState(FeatureFlagId.MULTIPLE_CLASSROOMS)

    assertThat(featureFlagValueFromProcessState).isEqualTo(true)
  }

  // Adds test remote feature flag to DB for MULTIPLE_CLASSROOMS.
  private fun addTestRemoteFeatureFlagToDatabase(component: TestApplicationComponent) {
    val database = component.cacheStoreFactory().create(
      DATABASE_NAME,
      RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      RemotePlatformParameterAndFeatureFlagDatabase.newBuilder().apply {
        addRemoteFeatureFlag(
          RemoteFeatureFlag.newBuilder().apply {
            id = FeatureFlagId.MULTIPLE_CLASSROOMS
            remoteIsEnabled = true
            syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        )
      }.build()
    }
  }

  // Adds test remote platform parameter to DB for SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS.
  private fun addTestRemotePlatformParameterToDatabase(component: TestApplicationComponent) {
    val database = component.cacheStoreFactory().create(
      DATABASE_NAME,
      RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      RemotePlatformParameterAndFeatureFlagDatabase.newBuilder().apply {
        addRemotePlatformParameter(
          RemotePlatformParameter.newBuilder().apply {
            id = PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
            remoteValue = PlatformParameterValue.newBuilder().apply {
              integer = 24
            }.build()
            syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        )
      }.build()
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      DaggerPlatformParameterControllerDebugImplTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build()
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

    @Provides
    @Singleton
    fun providePlatformParameterControllerProdImpl(
      platformParameterProcessState: PlatformParameterProcessState,
      factory: PlatformParameterControllerProdImpl.Factory
    ) = factory.create(platformParameterProcessState)
  }
  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleTestModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      NetworkConnectionUtilDebugModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class,
      TestPlatformParameterModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }
    fun cacheStoreFactory(): PersistentCacheStore.Factory
    fun platformParameterControllerDebugImpl(): PlatformParameterControllerDebugImpl
    fun testCoroutineDispatchers(): TestCoroutineDispatchers
    fun inject(platformParameterControllerTest: PlatformParameterControllerDebugImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterControllerDebugImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterControllerTest: PlatformParameterControllerDebugImplTest) {
      component.inject(platformParameterControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}

private const val DATABASE_NAME = "platform_parameter_and_feature_flag_database"
