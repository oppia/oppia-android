package org.oppia.android.domain.platformparameter

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
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
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val STRING_PLATFORM_PARAMETER_NAME = "string_platform_parameter_name"
private const val STRING_PLATFORM_PARAMETER_VALUE = "string_platform_parameter_value"

private const val INTEGER_PLATFORM_PARAMETER_NAME = "integer_platform_parameter_name"
private const val INTEGER_PLATFORM_PARAMETER_VALUE = 1

private const val BOOLEAN_PLATFORM_PARAMETER_NAME = "boolean_platform_parameter_name"
private const val BOOLEAN_PLATFORM_PARAMETER_VALUE = true

/** Tests for [PlatformParameterController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterControllerTest.TestApplication::class)
class PlatformParameterControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var platformParameterController: PlatformParameterController

  @Inject
  lateinit var platformParameterSingleton: PlatformParameterSingleton

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockUnitObserver: Observer<AsyncResult<Unit>>

  @Captor
  lateinit var unitCaptor: ArgumentCaptor<AsyncResult<Unit>>

  @Mock
  lateinit var mockObserverForAny: Observer <AsyncResult<Any?>>

  @Captor
  lateinit var captorForAny: ArgumentCaptor<AsyncResult<Any?>>

  private val mockPlatformParameterList by lazy {
    listOf<PlatformParameter>(
      PlatformParameter.newBuilder().setName(STRING_PLATFORM_PARAMETER_NAME)
        .setString(STRING_PLATFORM_PARAMETER_VALUE).build(),
      PlatformParameter.newBuilder().setName(INTEGER_PLATFORM_PARAMETER_NAME)
        .setInteger(INTEGER_PLATFORM_PARAMETER_VALUE).build(),
      PlatformParameter.newBuilder().setName(BOOLEAN_PLATFORM_PARAMETER_NAME)
        .setBoolean(BOOLEAN_PLATFORM_PARAMETER_VALUE).build()
    )
  }

  @Test
  fun testController_noPreviousDatabase_readPlatformParameters_platformParameterMapIsEmpty() {
    setUpTestApplicationComponent()
    platformParameterController.getParameterDatabase().toLiveData().observeForever(mockUnitObserver)
    testCoroutineDispatchers.runCurrent()

    // The platformParameterMap must be empty as there was no previously cached data.
    verify(mockUnitObserver, atLeastOnce()).onChanged(unitCaptor.capture())
    assertThat(unitCaptor.value.isSuccess()).isTrue()
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isEmpty()
  }

  @Test
  fun testController_existingDatabase_readPlatformParameters_platformParameterMapHasValues() {
    // Simulate that previous app already has cached platform parameter values in cache store.
    executeInPrevious { testComponent ->
      testComponent.getPlatformParameterController().updatePlatformParameterDatabase(
        mockPlatformParameterList
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpTestApplicationComponent()
    platformParameterController.getParameterDatabase().toLiveData().observeForever(mockUnitObserver)
    testCoroutineDispatchers.runCurrent()

    // The platformParameterMap must have values as application had cached platform parameter data.
    verify(mockUnitObserver, atLeastOnce()).onChanged(unitCaptor.capture())
    assertThat(unitCaptor.value.isSuccess()).isTrue()
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isNotEmpty()
    verifyEntriesInsidePlatformParameterMap(platformParameterSingleton.getPlatformParameterMap())
  }

  @Test
  fun testController_updateEmptyDatabase_readPlatformParameters_platformParameterMapHasValues() {
    setUpTestApplicationComponent()
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)
    testCoroutineDispatchers.runCurrent()
    platformParameterController.getParameterDatabase().toLiveData().observeForever(mockUnitObserver)
    testCoroutineDispatchers.runCurrent()

    // The platformParameterMap must have values as we updated the database with dummy list.
    verify(mockUnitObserver, atLeastOnce()).onChanged(unitCaptor.capture())
    assertThat(unitCaptor.value.isSuccess()).isTrue()
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isNotEmpty()
    verifyEntriesInsidePlatformParameterMap(platformParameterSingleton.getPlatformParameterMap())
  }

  @Test
  fun testController_updateExistingDatabase_readPlatformParameters_platformParameterMapHasNewValues() { // ktlint-disable max-line-length
    // Simulate that previous app already has cached platform parameter values in cache store.
    executeInPrevious { testComponent ->
      testComponent.getPlatformParameterController().updatePlatformParameterDatabase(
        mockPlatformParameterList
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpTestApplicationComponent()
    platformParameterController.updatePlatformParameterDatabase(listOf())
    testCoroutineDispatchers.runCurrent()
    platformParameterController.getParameterDatabase().toLiveData().observeForever(mockUnitObserver)
    testCoroutineDispatchers.runCurrent()

    // The new set of values must be empty as we updated the database with an empty list.
    verify(mockUnitObserver, atLeastOnce()).onChanged(unitCaptor.capture())
    assertThat(unitCaptor.value.isSuccess()).isTrue()
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isEmpty()
  }

  @Test
  fun testController_noPreviousDatabase_performUpdateOperation_returnsSuccess() {
    setUpTestApplicationComponent()
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)
      .toLiveData().observeForever(mockObserverForAny)
    testCoroutineDispatchers.runCurrent()

    // After a successful update operation we should receive a async result for success
    verify(mockObserverForAny, atLeastOnce()).onChanged(captorForAny.capture())
    assertThat(captorForAny.value.isSuccess()).isTrue()
  }

  @Test
  fun testController_existingDatabase_performUpdateOperation_returnsSuccess() {
    // Simulate that previous app already has cached platform parameter values in cache store.
    executeInPrevious { testComponent ->
      testComponent.getPlatformParameterController().updatePlatformParameterDatabase(
        mockPlatformParameterList
      )
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpTestApplicationComponent()
    platformParameterController.updatePlatformParameterDatabase(mockPlatformParameterList)
      .toLiveData().observeForever(mockObserverForAny)
    testCoroutineDispatchers.runCurrent()

    // After a successful update operation we should receive a async result for success
    verify(mockObserverForAny, atLeastOnce()).onChanged(captorForAny.capture())
    assertThat(captorForAny.value.isSuccess()).isTrue()
  }

  /**
   * This function checks does all the entries inside the [mockPlatformParameterList] exist inside
   * [platformParameterMap] that was retrieved from cache store.
   * @param platformParameterMap Map<String, PlatformParameter> map of cached values
   */
  private fun verifyEntriesInsidePlatformParameterMap(
    platformParameterMap: Map<String, PlatformParameter>
  ) {
    assertThat(platformParameterMap.size).isEqualTo(mockPlatformParameterList.size)
    mockPlatformParameterList.forEach {
      assertThat(platformParameterMap[it.name]).isEqualTo(it)
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun executeInPrevious(block: (TestApplicationComponent) -> Unit) {
    val testApplication = TestApplication()
    testApplication.attachBaseContext(ApplicationProvider.getApplicationContext())

    block(
      DaggerPlatformParameterControllerTest_TestApplicationComponent.builder()
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
    fun providePlatformParameterSingleton(
      platformParameterSingletonImpl: PlatformParameterSingletonImpl
    ): PlatformParameterSingleton = platformParameterSingletonImpl

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
      LogStorageModule::class, RobolectricModule::class, TestDispatcherModule::class,
      TestModule::class, TestLogReportingModule::class, NetworkConnectionUtilDebugModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): PlatformParameterControllerTest.TestApplicationComponent
    }

    fun inject(platformParameterControllerTest: PlatformParameterControllerTest)

    fun getPlatformParameterController(): PlatformParameterController

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: PlatformParameterControllerTest.TestApplicationComponent by lazy {
      DaggerPlatformParameterControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterControllerTest: PlatformParameterControllerTest) {
      component.inject(platformParameterControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
