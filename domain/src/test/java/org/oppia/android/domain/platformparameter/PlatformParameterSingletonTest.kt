package org.oppia.android.domain.platformparameter

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.PlatformParameter as ParameterValue

private const val STRING_PLATFORM_PARAMETER_NAME = "string_platform_parameter_name"
private const val STRING_PLATFORM_PARAMETER_VALUE = "string_platform_parameter_value"

private const val INTEGER_PLATFORM_PARAMETER_NAME = "integer_platform_parameter_name"
private const val INTEGER_PLATFORM_PARAMETER_VALUE = 1

private const val BOOLEAN_PLATFORM_PARAMETER_NAME = "boolean_platform_parameter_name"
private const val BOOLEAN_PLATFORM_PARAMETER_VALUE = true

private const val INCORRECT_PLATFORM_PARAMETER_NAME = "incorrect_platform_parameter_name"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterSingletonTest.TestApplication::class)
class PlatformParameterSingletonTest {

  @Inject
  lateinit var platformParameterSingleton: PlatformParameterSingleton

  private val mockPlatformParameterMap by lazy {
    val stringPlatformParameter = ParameterValue.getDefaultInstance().toBuilder()
      .setString(STRING_PLATFORM_PARAMETER_VALUE).build()
    val integerPlatformParameter = ParameterValue.getDefaultInstance().toBuilder()
      .setInteger(INTEGER_PLATFORM_PARAMETER_VALUE).build()
    val booleanPlatformParameter = ParameterValue.getDefaultInstance().toBuilder()
      .setBoolean(BOOLEAN_PLATFORM_PARAMETER_VALUE).build()

    mapOf(
      STRING_PLATFORM_PARAMETER_NAME to stringPlatformParameter,
      INTEGER_PLATFORM_PARAMETER_NAME to integerPlatformParameter,
      BOOLEAN_PLATFORM_PARAMETER_NAME to booleanPlatformParameter,
    )
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSingleton_initPlatformParameterMap_isEmpty() {
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isEmpty()
  }

  @Test
  fun testSingleton_initPlatformParameterMap_isNotEmpty() {
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isEmpty()
    platformParameterSingleton.setPlatformParameterMap(mockPlatformParameterMap)
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isNotEmpty()
  }

  @Test
  fun testSingleton_initPlatformParameterMapTwice_checkIsNotUpdatedTwice() {
    platformParameterSingleton.setPlatformParameterMap(mockPlatformParameterMap)
    assertThat(platformParameterSingleton.getPlatformParameterMap()).isNotEmpty()
    val emptyPlatformParameterMap = mapOf<String, ParameterValue>()
    platformParameterSingleton.setPlatformParameterMap(emptyPlatformParameterMap)
    assertThat(
      platformParameterSingleton.getPlatformParameterMap()
    ).isNotEqualTo(emptyPlatformParameterMap)
    assertThat(
      platformParameterSingleton.getPlatformParameterMap()
    ).isEqualTo(mockPlatformParameterMap)
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveStringParameter_verifyItsValue() {
    platformParameterSingleton.setPlatformParameterMap(mockPlatformParameterMap)
    val stringPlatformParameter = platformParameterSingleton.getStringPlatformParameter(
      STRING_PLATFORM_PARAMETER_NAME
    )
    assertThat(stringPlatformParameter?.value).isEqualTo(STRING_PLATFORM_PARAMETER_VALUE)
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveIntegerParameter_verifyItsValue() {
    platformParameterSingleton.setPlatformParameterMap(mockPlatformParameterMap)
    val integerPlatformParameter = platformParameterSingleton.getIntegerPlatformParameter(
      INTEGER_PLATFORM_PARAMETER_NAME
    )
    assertThat(integerPlatformParameter?.value).isEqualTo(INTEGER_PLATFORM_PARAMETER_VALUE)
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveBooleanParameter_verifyItsValue() {
    platformParameterSingleton.setPlatformParameterMap(mockPlatformParameterMap)
    val booleanPlatformParameter = platformParameterSingleton.getBooleanPlatformParameter(
      BOOLEAN_PLATFORM_PARAMETER_NAME
    )
    assertThat(booleanPlatformParameter?.value).isEqualTo(BOOLEAN_PLATFORM_PARAMETER_VALUE)
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveIncorrectNamedParameter_verifyIsNull() {
    platformParameterSingleton.setPlatformParameterMap(mockPlatformParameterMap)
    val incorrectPlatformParameter = platformParameterSingleton.getStringPlatformParameter(
      INCORRECT_PLATFORM_PARAMETER_NAME
    )
    assertThat(incorrectPlatformParameter).isNull()
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveIncorrectTypeParameter_verifyIsNull() {
    platformParameterSingleton.setPlatformParameterMap(mockPlatformParameterMap)
    val incorrectPlatformParameter = platformParameterSingleton.getStringPlatformParameter(
      BOOLEAN_PLATFORM_PARAMETER_NAME
    )
    assertThat(incorrectPlatformParameter).isNull()
  }

  fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): PlatformParameterSingletonTest.TestApplicationComponent
    }

    fun inject(platformParameterSingletonTest: PlatformParameterSingletonTest)
  }

  class TestApplication : Application() {
    private val component: PlatformParameterSingletonTest.TestApplicationComponent by lazy {
      DaggerPlatformParameterSingletonTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(platformParameterSingletonTest: PlatformParameterSingletonTest) {
      component.inject(platformParameterSingletonTest)
    }
  }
}
