package org.oppia.android.domain.platformparameter

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.platformparameter.FakePlatformParameterModule
import org.oppia.android.testing.platformparameter.FakePlatformParameterSingleton
import org.oppia.android.testing.platformparameter.TEST_PARAM_DEFAULT_VALUE
import org.oppia.android.testing.platformparameter.TEST_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_PARAM_VALUE
import org.oppia.android.testing.platformparameter.TestParam
import org.oppia.android.util.platformparameter.PlatformParameter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.PlatformParameter as ParameterValue

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterModuleTest.TestApplication::class)
class PlatformParameterModuleTest {

  @Inject
  @TestParam
  lateinit var testPlatformParameter: PlatformParameter<String>

  @Inject
  lateinit var platformParameterSingleton: PlatformParameterSingleton

  private val mockPlatformParameterMap by lazy {
    val testPlatformParameter = ParameterValue.getDefaultInstance().toBuilder()
      .setString(TEST_PARAM_VALUE).build()

    mapOf(
      TEST_PARAM_NAME to testPlatformParameter,
    )
  }

  @Test
  fun testFakeModule_initPlatformParameterMap_retrieveTestParameter_verifyItsValue() {
    setUpTestApplicationComponent(mockPlatformParameterMap)
    assertThat(testPlatformParameter.value).isEqualTo(TEST_PARAM_VALUE)
  }

  @Test
  fun testFakeModule_doNotInitPlatformParameterMap_retrieveTestParameter_verifyItsValue() {
    setUpTestApplicationComponent(mapOf())
    assertThat(testPlatformParameter.value).isEqualTo(TEST_PARAM_DEFAULT_VALUE)
  }

  fun setUpTestApplicationComponent(platformParameterMap: Map<String, ParameterValue>) {
    FakePlatformParameterSingleton.platformParameterMap = platformParameterMap
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [FakePlatformParameterModule::class])
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
