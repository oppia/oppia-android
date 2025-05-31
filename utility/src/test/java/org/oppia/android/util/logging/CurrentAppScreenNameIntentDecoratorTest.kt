package org.oppia.android.util.logging

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ScreenName
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjector
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjectorProvider
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestInitializer
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [CurrentAppScreenNameIntentDecorator]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = CurrentAppScreenNameIntentDecoratorTest.TestApplication::class)
class CurrentAppScreenNameIntentDecoratorTest {
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testDecorator_decorateWithScreenName_returnsIntentWithCorrectScreenName() {
    val intent = Intent().apply { decorateWithScreenName(ScreenName.BACKGROUND_SCREEN) }

    val currentScreen = intent.extractCurrentAppScreenName()
    assertThat(currentScreen).isEqualTo(ScreenName.BACKGROUND_SCREEN)
  }

  @Test
  fun testDecorator_withoutScreenName_returnsIntentWithUnspecifiedScreenName() {
    val currentScreen = Intent().extractCurrentAppScreenName()

    assertThat(currentScreen).isEqualTo(ScreenName.SCREEN_NAME_UNSPECIFIED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  interface TestModule {
    @Binds fun provideContext(application: Application): Context
  }

  @Singleton
  @Component(
    modules = [
      AssetModule::class,
      FakeOppiaClockModule::class,
      LocaleProdModule::class,
      LoggerModule::class,
      NetworkConfigTestModule::class,
      PlatformParameterTestModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent :
    DataProvidersInjector,
    PlatformParameterInitializationInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getPlatformParameterTestInitializer(): PlatformParameterTestInitializer

    fun inject(test: CurrentAppScreenNameIntentDecoratorTest)
  }

  class TestApplication :
    Application(),
    DataProvidersInjectorProvider,
    PlatformParameterInitializationInjectorProvider {
    val component: TestApplicationComponent by lazy {
      DaggerCurrentAppScreenNameIntentDecoratorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: CurrentAppScreenNameIntentDecoratorTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector() = component

    override fun getPlatformParameterInitializationInjector() = component
  }
}
