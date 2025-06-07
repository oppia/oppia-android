package org.oppia.android.domain.platformparameter

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * [PlatformParameterModuleTest] verifies the working of [PlatformParameterModule] by testing
 * the [PlatformParameterValue] received in different cases
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterModuleTest.TestApplication::class)
class PlatformParameterModuleTest {
  @Inject lateinit var platformParameterController: PlatformParameterController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context

  @field:[Inject EnableAppAndOsDeprecation]
  lateinit var enableAppAndOsDeprecationProvider: Provider<PlatformParameterValue<Boolean>>

  @field:[Inject OptionalAppUpdateVersionCode]
  lateinit var optionalAppUpdateVersionCodeProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject ForcedAppUpdateVersionCode]
  lateinit var forcedAppUpdateVersionCodeProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject LowestSupportedApiLevel]
  lateinit var lowestSupportedApiLevelProvider: Provider<PlatformParameterValue<Int>>

  // TODO(#5835): Finish the tests for this suite & the test module version.

  @Test
  fun testModule_injectEnableAppAndOsDeprecation_hasCorrectDefaultValue() {
    setUpTestApplicationComponent()
    assertThat(enableAppAndOsDeprecationProvider.get().value)
      .isEqualTo(TEST_ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE)
  }

  @Test
  fun testModule_injectOptionalAppUpdateVersionCode_hasCorrectAppVersionCode() {
    setUpTestApplicationComponent()
    assertThat(optionalAppUpdateVersionCodeProvider.get().value).isEqualTo(0)
  }

  @Test
  fun testModule_injectForcedAppUpdateVersionCode_hasCorrectAppVersionCode() {
    setUpTestApplicationComponent()
    assertThat(forcedAppUpdateVersionCodeProvider.get().value).isEqualTo(0)
  }

  @Test
  fun testModule_injectLowestSupportedApiLevel_hasCorrectMinimumApiLevel() {
    setUpTestApplicationComponent()
    assertThat(lowestSupportedApiLevelProvider.get().value)
      .isEqualTo(TEST_LOWEST_SUPPORTED_API_LEVEL)
  }

  private fun registerTestApplication() {
    val packageManager = Shadows.shadowOf(context.packageManager)
    val applicationInfo =
      ApplicationInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .build()
    val packageInfo =
      PackageInfoBuilder.newBuilder()
        .setPackageName(context.packageName)
        .setApplicationInfo(applicationInfo)
        .build()
    packageInfo.versionName = TEST_APP_VERSION_NAME
    packageInfo.longVersionCode = TEST_APP_VERSION_CODE
    packageManager.installPackage(packageInfo)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    val loadDeferred = platformParameterController.loadParametersAsync()
    testCoroutineDispatchers.runCurrent()
    check(loadDeferred.isCompleted) { "Expected parameter loading to have finished." }
    registerTestApplication()
  }

  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context {
      return application
    }
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
      TestModule::class,
      PlatformParameterModule::class,
      PlatformParameterControllerProdModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(platformParameterModuleTest: PlatformParameterModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterModuleTest: PlatformParameterModuleTest) {
      component.inject(platformParameterModuleTest)
    }
  }

  private companion object {
    private const val TEST_APP_VERSION_NAME = "oppia-android-test-0123456789"
    private const val TEST_APP_VERSION_CODE = 125L
    private const val TEST_LOWEST_SUPPORTED_API_LEVEL = 21
    private const val TEST_ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE = false
  }
}
