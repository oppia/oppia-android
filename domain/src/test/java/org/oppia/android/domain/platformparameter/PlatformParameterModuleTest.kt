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
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_DEFAULT_VALUE
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_DEFAULT_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_DEFAULT_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TestBooleanParam
import org.oppia.android.testing.platformparameter.TestIntegerParam
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.platformparameter.TestStringParam
import org.oppia.android.util.extensions.getVersionCode
import org.oppia.android.util.platformparameter.EnableAppAndOsDeprecation
import org.oppia.android.util.platformparameter.ForcedAppUpdateVersionCode
import org.oppia.android.util.platformparameter.LowestSupportedApiLevel
import org.oppia.android.util.platformparameter.OptionalAppUpdateVersionCode
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
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

  @Inject
  lateinit var platformParameterSingleton: PlatformParameterSingleton

  @Inject
  lateinit var context: Context

  @field:[Inject TestStringParam]
  lateinit var stringPlatformParameterProvider: Provider<PlatformParameterValue<String>>

  @field:[Inject TestIntegerParam]
  lateinit var integerPlatformParameterProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject TestBooleanParam]
  lateinit var booleanPlatformParameterProvider: Provider<PlatformParameterValue<Boolean>>

  @field:[Inject EnableAppAndOsDeprecation]
  lateinit var enableAppAndOsDeprecationProvider: Provider<PlatformParameterValue<Boolean>>

  @field:[Inject OptionalAppUpdateVersionCode]
  lateinit var optionalAppUpdateVersionCodeProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject ForcedAppUpdateVersionCode]
  lateinit var forcedAppUpdateVersionCodeProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject LowestSupportedApiLevel]
  lateinit var lowestSupportedApiLevelProvider: Provider<PlatformParameterValue<Int>>

  private val platformParameterMapWithValues by lazy {
    val mockStringPlatformParameter = PlatformParameter.newBuilder()
      .setString(TEST_STRING_PARAM_SERVER_VALUE).build()
    val mockIntegerPlatformParameter = PlatformParameter.newBuilder()
      .setInteger(TEST_INTEGER_PARAM_SERVER_VALUE).build()
    val mockBooleanPlatformParameter = PlatformParameter.newBuilder()
      .setBoolean(TEST_BOOLEAN_PARAM_SERVER_VALUE).build()

    mapOf<String, PlatformParameter>(
      TEST_STRING_PARAM_NAME to mockStringPlatformParameter,
      TEST_INTEGER_PARAM_NAME to mockIntegerPlatformParameter,
      TEST_BOOLEAN_PARAM_NAME to mockBooleanPlatformParameter
    )
  }

  private val partialPlatformParameterMapWithValues by lazy {
    val mockIntegerPlatformParameter = PlatformParameter.newBuilder()
      .setInteger(TEST_INTEGER_PARAM_SERVER_VALUE).build()
    val mockBooleanPlatformParameter = PlatformParameter.newBuilder()
      .setBoolean(TEST_BOOLEAN_PARAM_SERVER_VALUE).build()

    mapOf<String, PlatformParameter>(
      TEST_INTEGER_PARAM_NAME to mockIntegerPlatformParameter,
      TEST_BOOLEAN_PARAM_NAME to mockBooleanPlatformParameter
    )
  }

  @Test
  fun testModule_initPlatformParameterMap_retrieveTestStringParameter_returnsParamValue() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
    assertThat(stringPlatformParameterProvider.get().value)
      .isEqualTo(TEST_STRING_PARAM_SERVER_VALUE)
  }

  @Test
  fun testModule_doNotInitPlatformParameterMap_retrieveTestStringParameter_returnsDefaultValue() {
    setUpTestApplicationComponent(mapOf())
    assertThat(stringPlatformParameterProvider.get().value)
      .isEqualTo(TEST_STRING_PARAM_DEFAULT_VALUE)
  }

  @Test
  fun testModule_initPlatformParameterMap_retrieveTestIntegerParameter_returnsParamValue() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
    assertThat(integerPlatformParameterProvider.get().value)
      .isEqualTo(TEST_INTEGER_PARAM_SERVER_VALUE)
  }

  @Test
  fun testModule_doNotInitPlatformParameterMap_retrieveTestIntegerParameter_returnsDefaultValue() {
    setUpTestApplicationComponent(mapOf())
    assertThat(integerPlatformParameterProvider.get().value)
      .isEqualTo(TEST_INTEGER_PARAM_DEFAULT_VALUE)
  }

  @Test
  fun testModule_initPlatformParameterMap_retrieveTestBooleanParameter_returnsParamValue() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
    assertThat(booleanPlatformParameterProvider.get().value)
      .isEqualTo(TEST_BOOLEAN_PARAM_SERVER_VALUE)
  }

  @Test
  fun testModule_doNotInitPlatformParameterMap_retrieveTestBooleanParameter_returnsDefaultValue() {
    setUpTestApplicationComponent(mapOf())
    assertThat(booleanPlatformParameterProvider.get().value)
      .isEqualTo(TEST_BOOLEAN_PARAM_DEFAULT_VALUE)
  }

  @Test
  fun testModule_initPlatformParameterMapPartially_returnsExpectedDefaultsAndValues() {
    setUpTestApplicationComponent(partialPlatformParameterMapWithValues)

    // As the partial map didn't had String Parameter therefore default parameter value was injected
    assertThat(stringPlatformParameterProvider.get().value)
      .isEqualTo(TEST_STRING_PARAM_DEFAULT_VALUE)

    // As the partial map had Integer and Boolean Parameter therefore true parameter value was injected
    assertThat(integerPlatformParameterProvider.get().value)
      .isEqualTo(TEST_INTEGER_PARAM_SERVER_VALUE)
    assertThat(booleanPlatformParameterProvider.get().value)
      .isEqualTo(TEST_BOOLEAN_PARAM_SERVER_VALUE)
  }

  @Test
  fun testModule_injectEnableAppAndOsDeprecation_hasCorrectDefaultValue() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
    assertThat(enableAppAndOsDeprecationProvider.get().value)
      .isEqualTo(TEST_ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE)
  }

  @Test
  fun testModule_injectOptionalAppUpdateVersionCode_hasCorrectAppVersionCode() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
    assertThat(optionalAppUpdateVersionCodeProvider.get().value)
      .isEqualTo(context.getVersionCode())
    assertThat(optionalAppUpdateVersionCodeProvider.get().value)
      .isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testModule_injectForcedAppUpdateVersionCode_hasCorrectAppVersionCode() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
    assertThat(forcedAppUpdateVersionCodeProvider.get().value)
      .isEqualTo(context.getVersionCode())
    assertThat(forcedAppUpdateVersionCodeProvider.get().value)
      .isEqualTo(TEST_APP_VERSION_CODE)
  }

  @Test
  fun testModule_injectLowestSupportedApiLevel_hasCorrectMinimumApiLevel() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
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

  private fun setUpTestApplicationComponent(platformParameterMap: Map<String, PlatformParameter>) {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    platformParameterSingleton.setPlatformParameterMap(platformParameterMap)
    registerTestApplication()
  }

  @Module
  class TestModule {
    @Provides
    fun providePlatformParameterSingleton(
      platformParameterSingletonImpl: PlatformParameterSingletonImpl
    ): PlatformParameterSingleton = platformParameterSingletonImpl

    @Provides
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestPlatformParameterModule::class
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
    private const val TEST_LOWEST_SUPPORTED_API_LEVEL = 19
    private const val TEST_ENABLE_APP_AND_OS_DEPRECATION_DEFAULT_VALUE = false
  }
}
