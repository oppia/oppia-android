package org.oppia.android.domain.auth

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.oppia.android.domain.auth.AuthenticationModuleTest.AuthenticationModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.logging.firebase.DebugLogReportingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AuthenticationModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AuthenticationModuleTest {

  @Inject
  lateinit var wrapper: AuthenticationWrapper

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_injectsInstanceOfAuthenticationWrapper() {
    assertThat(wrapper).isInstanceOf(AuthenticationController::class.java)
  }

  private fun setUpTestApplicationComponent() {
    DaggerAuthenticationModuleTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(): Context {
      return ApplicationProvider.getApplicationContext()
    }
  }

  @Module
  class AuthenticationModule {
    @Provides
    @Singleton
    fun provideAuthenticationController():
      AuthenticationWrapper = AuthenticationController(mock(FirebaseAuth::class.java))
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, AuthenticationModule::class,
      RobolectricModule::class, DebugLogReportingModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: AuthenticationModuleTest)
  }
}
