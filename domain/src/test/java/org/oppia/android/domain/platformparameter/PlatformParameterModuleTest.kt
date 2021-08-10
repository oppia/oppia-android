package org.oppia.android.domain.platformparameter

import android.app.Application
import androidx.test.core.app.ApplicationProvider
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
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_DEFAULT_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_DEFAULT_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_VALUE
import org.oppia.android.testing.platformparameter.TestBooleanParam
import org.oppia.android.testing.platformparameter.TestIntegerParam
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.platformparameter.TestStringParam
import org.oppia.android.util.platformparameter.PlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameterValue
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

  @field:[Inject TestStringParam]
  lateinit var stringPlatformParameterProvider: Provider<PlatformParameterValue<String>>

  @field:[Inject TestIntegerParam]
  lateinit var integerPlatformParameterProvider: Provider<PlatformParameterValue<Int>>

  @field:[Inject TestBooleanParam]
  lateinit var booleanPlatformParameterProvider: Provider<PlatformParameterValue<Boolean>>

  private val platformParameterMapWithValues by lazy {
    val mockStringPlatformParameter = PlatformParameter.newBuilder()
      .setString(TEST_STRING_PARAM_VALUE).build()
    val mockIntegerPlatformParameter = PlatformParameter.newBuilder()
      .setInteger(TEST_INTEGER_PARAM_VALUE).build()
    val mockBooleanPlatformParameter = PlatformParameter.newBuilder()
      .setBoolean(TEST_BOOLEAN_PARAM_VALUE).build()

    mapOf<String, PlatformParameter>(
      TEST_STRING_PARAM_NAME to mockStringPlatformParameter,
      TEST_INTEGER_PARAM_NAME to mockIntegerPlatformParameter,
      TEST_BOOLEAN_PARAM_NAME to mockBooleanPlatformParameter
    )
  }

  private val partialPlatformParameterMapWithValues by lazy {
    val mockIntegerPlatformParameter = PlatformParameter.newBuilder()
      .setInteger(TEST_INTEGER_PARAM_VALUE).build()
    val mockBooleanPlatformParameter = PlatformParameter.newBuilder()
      .setBoolean(TEST_BOOLEAN_PARAM_VALUE).build()

    mapOf<String, PlatformParameter>(
      TEST_INTEGER_PARAM_NAME to mockIntegerPlatformParameter,
      TEST_BOOLEAN_PARAM_NAME to mockBooleanPlatformParameter
    )
  }

  @Test
  fun testModule_initPlatformParameterMap_retrieveTestStringParameter_returnsParamValue() {
    setUpTestApplicationComponent(platformParameterMapWithValues)
    assertThat(stringPlatformParameterProvider.get().value)
      .isEqualTo(TEST_STRING_PARAM_VALUE)
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
      .isEqualTo(TEST_INTEGER_PARAM_VALUE)
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
      .isEqualTo(TEST_BOOLEAN_PARAM_VALUE)
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
    assertThat(integerPlatformParameterProvider.get().value).isEqualTo(TEST_INTEGER_PARAM_VALUE)
    assertThat(booleanPlatformParameterProvider.get().value).isEqualTo(TEST_BOOLEAN_PARAM_VALUE)
  }

  private fun setUpTestApplicationComponent(platformParameterMap: Map<String, PlatformParameter>) {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    platformParameterSingleton.setPlatformParameterMap(platformParameterMap)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun providePlatformParameterSingleton(
      platformParameterSingletonImpl: PlatformParameterSingletonImpl
    ): PlatformParameterSingleton = platformParameterSingletonImpl
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
}
