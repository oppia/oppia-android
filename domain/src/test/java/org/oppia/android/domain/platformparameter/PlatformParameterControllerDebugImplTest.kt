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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.RemoteFeatureFlag
import org.oppia.android.app.model.RemotePlatformParameterAndFeatureFlagDatabase
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.platformparameter.testing.TestPlatformParameterConfigRetriever
import org.oppia.android.testing.TestLogReportingModule
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

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLoadEphemeralPlatformParameters_returnsNonEmptyList() {
    val ephemeralParamsProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()

    val ephemeralParams =
      monitorFactory.waitForNextSuccessfulResult(ephemeralParamsProvider)

    assertThat(ephemeralParams).isNotEmpty()
  }

  @Test
  fun testLoadEphemeralFeatureFlags_returnsNonEmptyList() {
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()

    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)

    assertThat(ephemeralFeatureFlags).isNotEmpty()
  }

  @Test
  fun testLoadEphemeralFeatureFlags_returnsCorrectDefaultValue() {
    val ephemeralFeatureFlagsProvider =
      platformParameterControllerDebugImpl.loadEphemeralFeatureFlags()

    val ephemeralFeatureFlags =
      monitorFactory.waitForNextSuccessfulResult(ephemeralFeatureFlagsProvider)
    println("Ephemeral Feature Flags: $ephemeralFeatureFlags")
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
    val ephemeralPlatformParametersProvider =
      platformParameterControllerDebugImpl.loadEphemeralPlatformParameters()

    val ephemeralPlatformParameters =
      monitorFactory.waitForNextSuccessfulResult(ephemeralPlatformParametersProvider)

    val defaultValues = platformParameterConfigRetriever
      .loadSupportedPlatformParameters()
      .platformParameterDefinitionList

    val splashScreenWelcomeMessageParam = defaultValues
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    val splashScreenWelcomeMessageEphemeralParam = ephemeralPlatformParameters
      .find { it.id == PlatformParameterId.SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS }

    assertThat(splashScreenWelcomeMessageEphemeralParam?.currentValue)
      .isEqualTo(splashScreenWelcomeMessageParam?.defaultValue)
  }

  @Test
  fun testLoadParametersAsync_loadsCorrectDefaultFlagValue() {
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

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    private val processState by lazy { PlatformParameterProcessState() }

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

    @Provides
    @Singleton
    fun providePlatformParameterController(
      factory: PlatformParameterControllerProdImpl.Factory
    ): PlatformParameterController = factory.create(processState)

    @Provides
    fun providePlatformParameterConfigRetriever(
      impl: TestPlatformParameterConfigRetriever
    ): PlatformParameterConfigRetriever = impl

    @Provides
    @Singleton
    fun providePlatformParameterProcessState(
      platformParameterController: PlatformParameterController,
      testCoroutineDispatchers: TestCoroutineDispatchers,
      persistentCacheStoreFactory: PersistentCacheStore.Factory
    ): PlatformParameterProcessState {
      val database =
        persistentCacheStoreFactory.create(
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

      // TODO(#5835): Remove this blocking hack to ensure tests are properly initialized for params.
      val loadDeferred = platformParameterController.loadParametersAsync()
      testCoroutineDispatchers.runCurrent()
      check(loadDeferred.isCompleted) { "Expected parameter loading to have finished." }
      return processState
    }
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
