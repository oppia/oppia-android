package org.oppia.android.util.locale.testing

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
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.MachineLocaleImpl
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaLocale
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [LocaleTestModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class LocaleTestModuleTest {
  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  lateinit var formatterFactory: OppiaBidiFormatter.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_injectsProductionImplementationOfMachineLocale() {
    assertThat(machineLocale).isInstanceOf(MachineLocaleImpl::class.java)
  }

  @Test
  fun testModule_injectsTestImplementationOfBidiFormatterFactory() {
    assertThat(formatterFactory).isInstanceOf(TestOppiaBidiFormatter.FactoryImpl::class.java)
  }

  private fun setUpTestApplicationComponent() {
    DaggerLocaleTestModuleTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LocaleTestModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(localeTestModuleTest: LocaleTestModuleTest)
  }
}
