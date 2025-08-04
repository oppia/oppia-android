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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.LocalOverridePlatformParameterDatabase
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.RemoteFeatureFlag
import org.oppia.android.app.model.RemotePlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterAndFeatureFlagDatabase
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.platformparameter.testing.TestPlatformParameterConfigRetriever
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.platformparameter.PlatformParameterTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterControllerDebugImpl]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterControllerDebugImplTest.TestApplication::class)
class PlatformParameterControllerDebugImplTest {
  private companion object {
    private const val TEST_REMOTE_MULTIPLE_CLASSROOMS = true
    private const val TEST_LOCAL_OVERRIDE_MULTIPLE_CLASSROOMS = true
    private const val TEST_REMOTE_SYNC_UP_WORKER_PERIOD_HOURS = 24
    private const val REMOTE_DATABASE_NAME = "platform_parameter_and_feature_flag_database"
    private const val LOCAL_OVERRIDE_DATABASE_NAME =
      "local_overridden_platform_parameter_and_feature_flag_database"
  }

  @Inject lateinit var platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var platformParameterConfigRetriever: PlatformParameterConfigRetriever
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var platformParameterProcessState: PlatformParameterProcessState
  @Inject lateinit var cacheStoreFactory: PersistentCacheStore.Factory

  @After
  fun tearDown() {
    PlatformParameterTestModule.reset()
  }

  @Test
  fun testLoadEphemeralPlatformParameters_returnsAllDefinedParameters() {
    setUpTestApplicationComponent()
    val ephemeralParamsProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()
    val ephemeralParams = monitorFactory.waitForNextSuccessfulResult(ephemeralParamsProvider)

    val expectedParamIds = platformParameterConfigRetriever
      .loadSupportedPlatformParameters()
      .platformParameterDefinitionList
      .map { it.id }
    val actualParamIds = ephemeralParams.map { it.id }

    assertThat(actualParamIds).containsExactlyElementsIn(expectedParamIds)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_returnsAllDefinedFlags() {
    setUpTestApplicationComponent()
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)

    val expectedFlagIds = platformParameterConfigRetriever
      .loadSupportedFeatureFlags()
      .featureFlagDefinitionList
      .map { it.id }
    val actualFlagIds = ephemeralFeatureFlags.map { it.id }

    assertThat(actualFlagIds).containsExactlyElementsIn(expectedFlagIds)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withNoRemoteOrLocalOverrides_returnsCorrectDefaultValue() {
    setUpTestApplicationComponent()
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()

    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue =
      ephemeralFeatureFlags.find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }
        ?.currentValue

    val expectedMultipleClassroomDefaultValue = platformParameterConfigRetriever
      .loadSupportedFeatureFlags()
      .featureFlagDefinitionList
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }
      ?.defaultIsEnabled

    assertThat(ephemeralMultipleClassroomValue)
      .isEqualTo(expectedMultipleClassroomDefaultValue)
  }

  @Test
  fun testLoadEphemeralPlatformParameter_withNoRemoteOrLocalOverrides_returnsCorrectDefaultValue() {
    setUpTestApplicationComponent()
    val ephemeralPlatformParametersProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()

    val ephemeralPlatformParameters =
      monitorFactory.waitForNextSuccessfulResult(ephemeralPlatformParametersProvider)

    val ephemeralSyncUpWorkerTimePeriodInHoursValue =
      ephemeralPlatformParameters.find {
        it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
      }?.currentValue?.integer
    val expectedSyncUpWorkerTimePeriodInHoursValue = platformParameterConfigRetriever
      .loadSupportedPlatformParameters()
      .platformParameterDefinitionList
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }
      ?.defaultValue?.integer

    assertThat(ephemeralSyncUpWorkerTimePeriodInHoursValue)
      .isEqualTo(expectedSyncUpWorkerTimePeriodInHoursValue)
  }

  @Test
  @Suppress("DeferredResultUnused")
  fun testLoadParametersAsync_withNoRemoteOrLocalOverrides_loadsCorrectDefaultFlagValue() {
    setUpTestApplicationComponent()
    platformParameterControllerDebugImpl.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()

    val actualMultipleClassroomFlagValue = platformParameterProcessState
      .retrieveFeatureFlagState(FeatureFlagId.MULTIPLE_CLASSROOMS)

    val expectedMultipleClassroomDefaultValue = platformParameterConfigRetriever
      .loadSupportedFeatureFlags()
      .featureFlagDefinitionList
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }
      ?.defaultIsEnabled

    assertThat(actualMultipleClassroomFlagValue)
      .isEqualTo(expectedMultipleClassroomDefaultValue)
  }

  @Test
  @Suppress("DeferredResultUnused")
  fun testLoadParametersAsync_withNoRemoteOrLocalOverrides_loadsCorrectDefaultParameterValue() {
    setUpTestApplicationComponent()
    platformParameterControllerDebugImpl.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()
    val actualSyncUpWorkerTimePeriodValue = platformParameterProcessState
      .retrievePlatformParameterIntegerState(
        PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
      )

    val expectedSyncUpWorkerTimePeriodDefaultValue = platformParameterConfigRetriever
      .loadSupportedPlatformParameters()
      .platformParameterDefinitionList
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }
      ?.defaultValue
      ?.integer

    assertThat(actualSyncUpWorkerTimePeriodValue)
      .isEqualTo(expectedSyncUpWorkerTimePeriodDefaultValue)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withNoRemoteOrLocalOverride_hasNotSyncedFromServerStatus() {
    setUpTestApplicationComponent()
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    assertThat(ephemeralMultipleClassroomValue?.syncStatus)
      .isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
  }

  @Test
  fun testLoadEphemeralPlatformParameter_withNoRemoteLocalOverride_hasNotSyncedFromServerStatus() {
    setUpTestApplicationComponent()
    val ephemeralPlatformParametersProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()
    val ephemeralPlatformParameters =
      monitorFactory.waitForNextSuccessfulResult(ephemeralPlatformParametersProvider)
    val ephemeralSyncUpWorkerTimePeriodValue = ephemeralPlatformParameters
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    assertThat(ephemeralSyncUpWorkerTimePeriodValue?.syncStatus)
      .isEqualTo(SyncStatus.NOT_SYNCED_FROM_SERVER)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withRemoteFlagAndNoLocalOverride_returnsRemoteValue() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }?.currentValue

    assertThat(ephemeralMultipleClassroomValue)
      .isEqualTo(TEST_REMOTE_MULTIPLE_CLASSROOMS)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withLocalOverrideFlagAndNoRemote_returnsOverriddenValue() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    executeInPreviousAppInstance { testComponent ->
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()

    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    assertThat(ephemeralMultipleClassroomValue?.currentValue)
      .isEqualTo(TEST_REMOTE_MULTIPLE_CLASSROOMS)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withLocalOverrideFlagAndNoRemote_hasLocalOverrideStatus() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    executeInPreviousAppInstance { testComponent ->
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()

    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    assertThat(ephemeralMultipleClassroomValue?.syncStatus)
      .isEqualTo(SyncStatus.LOCAL_OVERRIDE)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withLocalOverrideAndRemoteFlag_hasLocalOverrideStatus() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, false)
      testComponent.getTestCoroutineDispatchers().runCurrent()
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()

    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    assertThat(ephemeralMultipleClassroomValue?.syncStatus)
      .isEqualTo(SyncStatus.LOCAL_OVERRIDE)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withLocalOverrideAndRemoteFlag_hasLocalOverrideValue() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, false)
      testComponent.getTestCoroutineDispatchers().runCurrent()
      addTestOverriddenFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()

    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    assertThat(ephemeralMultipleClassroomValue?.currentValue)
      .isEqualTo(TEST_LOCAL_OVERRIDE_MULTIPLE_CLASSROOMS)
  }

  @Test
  fun testLoadEphemeralFeatureFlags_withRemoteFlagAndNoLocalOverride_hasSyncedFromServerStatus() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()
    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    val ephemeralMultipleClassroomValue = ephemeralFeatureFlags
      .find { it.id == FeatureFlagId.MULTIPLE_CLASSROOMS }

    assertThat(ephemeralMultipleClassroomValue?.syncStatus)
      .isEqualTo(SyncStatus.SYNCED_FROM_SERVER)
  }

  @Test
  fun testLoadEphemeralPlatformParameter_withRemoteAndNoLocalOverride_returnsRemoteValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestRemotePlatformParameterToDatabase(testComponent)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    val ephemeralPlatformParametersProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()
    val ephemeralPlatformParameters =
      monitorFactory.waitForNextSuccessfulResult(ephemeralPlatformParametersProvider)
    val ephemeralSyncUpWorkerTimePeriodValue = ephemeralPlatformParameters
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }
      ?.currentValue?.integer

    assertThat(ephemeralSyncUpWorkerTimePeriodValue)
      .isEqualTo(TEST_REMOTE_SYNC_UP_WORKER_PERIOD_HOURS)
  }

  @Test
  fun testLoadEphemeralPlatformParameters_withRemoteParameter_hasSyncedFromServerStatus() {
    executeInPreviousAppInstance { testComponent ->
      addTestRemotePlatformParameterToDatabase(testComponent)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    val ephemeralPlatformParametersProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()
    val ephemeralPlatformParameters =
      monitorFactory.waitForNextSuccessfulResult(ephemeralPlatformParametersProvider)
    val ephemeralSyncUpWorkerTimePeriodValue = ephemeralPlatformParameters
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    assertThat(ephemeralSyncUpWorkerTimePeriodValue?.syncStatus)
      .isEqualTo(SyncStatus.SYNCED_FROM_SERVER)
  }

  @Test
  @Suppress("DeferredResultUnused")
  fun testLoadParametersAsync_withRemoteAndNoLocalOverride_setsProcessStateToRemoteValue() {
    executeInPreviousAppInstance { testComponent ->
      addTestRemotePlatformParameterToDatabase(testComponent)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    platformParameterControllerDebugImpl.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()
    val platformParameterValueFromProcessState = platformParameterProcessState
      .retrievePlatformParameterIntegerState(
        PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS
      )

    assertThat(platformParameterValueFromProcessState)
      .isEqualTo(TEST_REMOTE_SYNC_UP_WORKER_PERIOD_HOURS)
  }

  @Test
  @Suppress("DeferredResultUnused")
  fun testLoadParametersAsync_withRemoteFlagAndNoLocalOverride_setsProcessStateToRemoteValue() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    executeInPreviousAppInstance { testComponent ->
      addTestRemoteFeatureFlagToDatabase(testComponent, true)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }
    setUpTestApplicationComponent()
    platformParameterControllerDebugImpl.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()
    val featureFlagValueFromProcessState = platformParameterProcessState
      .retrieveFeatureFlagState(FeatureFlagId.MULTIPLE_CLASSROOMS)

    assertThat(featureFlagValueFromProcessState).isEqualTo(TEST_REMOTE_MULTIPLE_CLASSROOMS)
  }

  @Test
  @Suppress("DeferredResultUnused")
  fun testGetParameterInitializationStatus_onLoadingParameters_returnsTrue() {
    setUpTestApplicationComponent()
    platformParameterControllerDebugImpl.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()
    val initStatusProvider = platformParameterControllerDebugImpl.getParameterInitializationStatus()
    val isInitialised = monitorFactory.waitForNextSuccessfulResult(initStatusProvider)
    assertThat(isInitialised).isTrue()
  }

  @Test
  fun testGetParameterInitializationStatus_withoutLoadingParameters_returnsFalse() {
    setUpTestApplicationComponent()
    val initStatusProvider = platformParameterControllerDebugImpl.getParameterInitializationStatus()
    val isInitialised = monitorFactory.waitForNextSuccessfulResult(initStatusProvider)
    assertThat(isInitialised).isFalse()
  }

  @Test
  fun testDownloadRemoteParameters_returnsAsyncResultSuccess() {
    setUpTestApplicationComponent()
    // TODO(#5345): Finish implementing forcing remote parameter downloads test.
    val downloadProvider = platformParameterControllerDebugImpl.downloadRemoteParameters()
    val downloadMonitor = monitorFactory.createMonitor(downloadProvider)
    val downloadResult = downloadMonitor.waitForNextResult()
    assertThat(downloadResult).isInstanceOf(AsyncResult.Success::class.java)
  }

  // Populates the remote DB with test feature flag for MULTIPLE_CLASSROOM.
  private fun addTestRemoteFeatureFlagToDatabase(
    component: TestApplicationComponent,
    value: Boolean
  ) {
    val database = component.getCacheStoreFactory().create(
      REMOTE_DATABASE_NAME,
      RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )

    database.storeDataAsync {
      RemotePlatformParameterAndFeatureFlagDatabase.newBuilder().apply {
        addRemoteFeatureFlag(
          RemoteFeatureFlag.newBuilder().apply {
            id = FeatureFlagId.MULTIPLE_CLASSROOMS
            remoteIsEnabled = value
            syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        )
      }.build()
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  // Populates the Local Override DB with test Overridden feature flag for MULTIPLE_CLASSROOMS.
  private fun addTestOverriddenFeatureFlagToDatabase(
    component: TestApplicationComponent,
    value: Boolean
  ) {
    val database = component.getCacheStoreFactory().create(
      LOCAL_OVERRIDE_DATABASE_NAME,
      LocalOverridePlatformParameterDatabase.getDefaultInstance()
    )
    database.storeDataAsync {
      LocalOverridePlatformParameterDatabase.newBuilder().apply {
        addOverriddenFeatureFlag(
          OverriddenFeatureFlag.newBuilder()
            .setId(FeatureFlagId.MULTIPLE_CLASSROOMS)
            .setOverriddenValue(value)
            .build()
        )
      }.build()
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  // Populates the remote DB with test platform parameter for SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS.
  private fun addTestRemotePlatformParameterToDatabase(component: TestApplicationComponent) {
    val database = component.getCacheStoreFactory().create(
      REMOTE_DATABASE_NAME,
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
    }.waitForSuccessfulResult(
      component.getTestCoroutineDispatchers(), component.getBackgroundDispatcher()
    )
  }

  private fun <T> Deferred<T>.waitForSuccessfulResult(
    testCoroutineDispatchers: TestCoroutineDispatchers,
    backgroundDispatcher: CoroutineDispatcher
  ) {
    return when (
      val result = waitForResult(
        testCoroutineDispatchers, backgroundDispatcher
      )
    ) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> {} // Nothing to do; the result succeeded.
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult(
    testCoroutineDispatchers: TestCoroutineDispatchers,
    backgroundDispatcher: CoroutineDispatcher
  ) = toStateFlow(backgroundDispatcher).waitForLatestValue(testCoroutineDispatchers)

  private fun <T> Deferred<T>.toStateFlow(
    backgroundDispatcher: CoroutineDispatcher
  ): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(
    testCoroutineDispatchers: TestCoroutineDispatchers
  ): T =
    also { testCoroutineDispatchers.runCurrent() }.value

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
      factory: PlatformParameterControllerProdImpl.Factory,
      processState: PlatformParameterProcessState
    ) = factory.create(processState)

    @Provides
    @Singleton
    fun providesPlatformParameterController(
      impl: PlatformParameterControllerDebugImpl
    ): PlatformParameterController = impl

    @Provides
    fun providePlatformParameterConfigRetriever(
      impl: TestPlatformParameterConfigRetriever
    ): PlatformParameterConfigRetriever = impl

    @Provides
    @Singleton
    fun providePlatformParameterProcessState(): PlatformParameterProcessState =
      PlatformParameterProcessState()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleTestModule::class,
      LoggerModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun getCacheStoreFactory(): PersistentCacheStore.Factory
    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers
    @BackgroundDispatcher
    fun getBackgroundDispatcher(): CoroutineDispatcher
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
