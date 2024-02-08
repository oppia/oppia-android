package org.oppia.android.domain.auth

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
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.FakeFirebaseAuthInstanceWrapperImpl
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [firebaseAuthWrapperImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FirebaseAuthWrapperImplTest.TestApplication::class)
class FirebaseAuthWrapperImplTest {

  @Inject
  lateinit var firebaseAuthWrapperImpl: FirebaseAuthWrapperImpl

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAuthWrapperImpl_getCurrentSignedInUser_userIsNotSignedIn_returnsNull() {
    val user = firebaseAuthWrapperImpl.currentUser

    assertThat(user).isNull()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
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

  @Module
  class AuthenticationModule {
    @Provides
    @Singleton
    fun provideFirebaseAuthWrapper(firebaseAuthInstanceWrapper: FirebaseAuthInstanceWrapper):
      FirebaseAuthWrapper = FirebaseAuthWrapperImpl(firebaseAuthInstanceWrapper)

    @Provides
    @Singleton
    fun provideFirebaseAuthInstanceWrapper(): FirebaseAuthInstanceWrapper =
      FakeFirebaseAuthInstanceWrapperImpl()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ApplicationLifecycleModule::class, TestDispatcherModule::class,
      AuthenticationModule::class, TestLogReportingModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: FirebaseAuthWrapperImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFirebaseAuthWrapperImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FirebaseAuthWrapperImplTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
