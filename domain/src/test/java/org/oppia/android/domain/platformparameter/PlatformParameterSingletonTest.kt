package org.oppia.android.domain.platformparameter

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.PlatformParameter as ParameterValue
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

const val stringPlatformParameterName = "string_platform_parameter_name"
const val stringPlatformParameterValue = "string_platform_parameter_value"

const val integerPlatformParameterName = "integer_platform_parameter_name"
const val integerPlatformParameterValue = 1

const val booleanPlatformParameterName = "boolean_platform_parameter_name"
const val booleanPlatformParameterValue = true

const val incorrectPlatformParameterName = "incorrect_platform_parameter_name"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterSingletonTest.TestApplication::class)
class PlatformParameterSingletonTest {

  @Inject
  lateinit var platformParameterSingleton: PlatformParameterSingleton

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSingleton_initialPlatformParameterMap_isEmpty(){
    assertThat(platformParameterSingleton.platformParameterMap.isEmpty()).isTrue()
  }

  @Test
  fun testSingleton_initPlatformParameterMap_isNotEmpty(){
    assertThat(platformParameterSingleton.platformParameterMap.isEmpty()).isTrue()
    platformParameterSingleton.initPlatformParameterMap(makePlatformParameterMap())
    assertThat(platformParameterSingleton.platformParameterMap.isEmpty()).isFalse()
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveStringParameter_verifyItsValue() {
    platformParameterSingleton.initPlatformParameterMap(makePlatformParameterMap())
    val stringPlatformParameter = platformParameterSingleton.getPlatformParameter<String>(
      stringPlatformParameterName
    )
    assertThat(stringPlatformParameter?.value).isEqualTo(stringPlatformParameterValue)
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveIntegerParameter_verifyItsValue() {
    platformParameterSingleton.initPlatformParameterMap(makePlatformParameterMap())
    val integerPlatformParameter = platformParameterSingleton.getPlatformParameter<Int>(
      integerPlatformParameterName
    )
    assertThat(integerPlatformParameter?.value).isEqualTo(integerPlatformParameterValue)
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveBooleanParameter_verifyItsValue() {
    platformParameterSingleton.initPlatformParameterMap(makePlatformParameterMap())
    val booleanPlatformParameter = platformParameterSingleton.getPlatformParameter<Boolean>(
      booleanPlatformParameterName
    )
    assertThat(booleanPlatformParameter?.value).isEqualTo(booleanPlatformParameterValue)
  }

  @Test
  fun testSingleton_initPlatformParameterMap_retrieveIncorrectParameter_verifyItsValue() {
    platformParameterSingleton.initPlatformParameterMap(makePlatformParameterMap())
    val incorrectPlatformParameter = platformParameterSingleton.getPlatformParameter<String>(
      incorrectPlatformParameterName
    )
    assertThat(incorrectPlatformParameter).isNull()
  }

  private fun makePlatformParameterMap(): Map<String, ParameterValue> {
    val stringPlatformParameter = ParameterValue.getDefaultInstance().toBuilder()
      .setString(stringPlatformParameterValue).build()
    val integerPlatformParameter = ParameterValue.getDefaultInstance().toBuilder()
      .setInteger(integerPlatformParameterValue).build()
    val booleanPlatformParameter = ParameterValue.getDefaultInstance().toBuilder()
      .setBoolean(booleanPlatformParameterValue).build()

    return mapOf(
      stringPlatformParameterName to stringPlatformParameter,
      integerPlatformParameterName to integerPlatformParameter,
      booleanPlatformParameterName to booleanPlatformParameter,
    )
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