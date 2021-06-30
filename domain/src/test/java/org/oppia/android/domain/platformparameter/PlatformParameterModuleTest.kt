package org.oppia.android.domain.platformparameter

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterSingleton
import org.oppia.android.util.platformparameter.PlatformParameter
import org.oppia.android.util.platformparameter.TEST_BOOLEAN_PARAM_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.util.platformparameter.TEST_BOOLEAN_PARAM_VALUE
import org.oppia.android.util.platformparameter.TEST_INTEGER_PARAM_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.util.platformparameter.TEST_INTEGER_PARAM_VALUE
import org.oppia.android.util.platformparameter.TEST_STRING_PARAM_DEFAULT_VALUE
import org.oppia.android.util.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.util.platformparameter.TEST_STRING_PARAM_VALUE
import org.oppia.android.util.platformparameter.TestBooleanParam
import org.oppia.android.util.platformparameter.TestIntegerParam
import org.oppia.android.util.platformparameter.TestStringParam
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.PlatformParameter as ParameterValue

/**
 * [PlatformParameterModuleTest] verifies the working of [PlatformParameterModule] by testing
 * the [PlatformParameterValue] received in different cases
 * */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterModuleTest.TestApplication::class)
class PlatformParameterModuleTest {

  @Inject
  @TestStringParam
  lateinit var testStringPlatformParameter: PlatformParameter<String>

  @Inject
  @TestIntegerParam
  lateinit var testIntegerPlatformParameter: PlatformParameter<Int>

  @Inject
  @TestBooleanParam
  lateinit var testBooleanPlatformParameter: PlatformParameter<Boolean>

  private val mockPlatformParameterMap by lazy {
    val mockStringPlatformParameter = ParameterValue.newBuilder()
      .setString(TEST_STRING_PARAM_VALUE).build()
    val mockIntegerPlatformParameter = ParameterValue.newBuilder()
      .setInteger(TEST_INTEGER_PARAM_VALUE).build()
    val mockBooleanPlatformParameter = ParameterValue.newBuilder()
      .setBoolean(TEST_BOOLEAN_PARAM_VALUE).build()

    mapOf(
      TEST_STRING_PARAM_NAME to mockStringPlatformParameter,
      TEST_INTEGER_PARAM_NAME to mockIntegerPlatformParameter,
      TEST_BOOLEAN_PARAM_NAME to mockBooleanPlatformParameter
    )
  }

  @Test
  fun testModule_initPlatformParameterMap_retrieveTestStringParameter_returnsParamValue() {
    setUpTestApplicationComponent(mockPlatformParameterMap)
    assertThat(testStringPlatformParameter.value).isEqualTo(TEST_STRING_PARAM_VALUE)
  }

  @Test
  fun testModule_doNotInitPlatformParameterMap_retrieveTestStringParameter_returnsDefaultValue() {
    setUpTestApplicationComponent(mapOf())
    assertThat(testStringPlatformParameter.value).isEqualTo(TEST_STRING_PARAM_DEFAULT_VALUE)
  }

  @Test
  fun testModule_initPlatformParameterMap_retrieveTestIntegerParameter_returnsParamValue() {
    setUpTestApplicationComponent(mockPlatformParameterMap)
    assertThat(testIntegerPlatformParameter.value).isEqualTo(TEST_INTEGER_PARAM_VALUE)
  }

  @Test
  fun testModule_doNotInitPlatformParameterMap_retrieveTestIntegerParameter_returnsDefaultValue() {
    setUpTestApplicationComponent(mapOf())
    assertThat(testIntegerPlatformParameter.value).isEqualTo(TEST_INTEGER_PARAM_DEFAULT_VALUE)
  }

  @Test
  fun testModule_initPlatformParameterMap_retrieveTestBooleanParameter_returnsParamValue() {
    setUpTestApplicationComponent(mockPlatformParameterMap)
    assertThat(testBooleanPlatformParameter.value).isEqualTo(TEST_BOOLEAN_PARAM_VALUE)
  }

  @Test
  fun testModule_doNotInitPlatformParameterMap_retrieveTestBooleanParameter_returnsDefaultValue() {
    setUpTestApplicationComponent(mapOf())
    assertThat(testBooleanPlatformParameter.value).isEqualTo(TEST_BOOLEAN_PARAM_DEFAULT_VALUE)
  }

  private fun setUpTestApplicationComponent(platformParameterMap: Map<String, ParameterValue>) {
    TestPlatformParameterSingleton.mockPlatformParameterMap = platformParameterMap
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestPlatformParameterModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): PlatformParameterModuleTest.TestApplicationComponent
    }

    fun inject(platformParameterModuleTest: PlatformParameterModuleTest)
  }

  class TestApplication : Application() {
    private val component: PlatformParameterModuleTest.TestApplicationComponent by lazy {
      DaggerPlatformParameterModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterModuleTest: PlatformParameterModuleTest) {
      component.inject(platformParameterModuleTest)
    }
  }
}
