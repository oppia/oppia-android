package org.oppia.android.testing.firebase

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
import org.oppia.android.domain.auth.FirebaseAuthWrapper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.DispatcherTestModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.logging.firebase.LogReportingDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AuthenticationTestModule]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AuthenticationTestModuleTest {

  @Inject
  lateinit var firebaseAuthWrapper: FirebaseAuthWrapper

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_injectsFakeInstanceOfFirebaseAuthWrapper() {
    assertThat(firebaseAuthWrapper).isInstanceOf(FakeFirebaseAuthWrapperImpl::class.java)
  }

  private fun setUpTestApplicationComponent() {
    DaggerAuthenticationTestModuleTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

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
      AuthenticationTestModule::class,
      DispatcherTestModule::class,
      LogReportingDebugModule::class,
      RobolectricModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: AuthenticationTestModuleTest)
  }
}
