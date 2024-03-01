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
import org.oppia.android.domain.auth.FirebaseUserWrapper
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.FakeFirebaseAuthWrapperImpl.FakeAuthState
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [FakeFirebaseAuthWrapperImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FakeFirebaseAuthWrapperImplTest.TestApplication::class)
class FakeFirebaseAuthWrapperImplTest {
  @Inject
  lateinit var fakeFirebaseAuthWrapperImpl: FakeFirebaseAuthWrapperImpl

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeAuthWrapper_getCurrentSignedInUser_userIsSignedIn_returnsFirebaseUserWrapper() {
    fakeFirebaseAuthWrapperImpl.simulateSignInSuccess()
    val user = fakeFirebaseAuthWrapperImpl.currentUser

    assertThat(user).isInstanceOf(FirebaseUserWrapper::class.java)
  }

  @Test
  fun testFakeAuthWrapper_getCurrentSignedInUser_userIsNotSignedIn_returnsNull() {
    fakeFirebaseAuthWrapperImpl.simulateSignInFailure()
    val user = fakeFirebaseAuthWrapperImpl.currentUser

    assertThat(user).isNull()
  }

  @Test
  fun testFakeAuthWrapper_simulateSignInSuccess_returnsFakeAuthStateSuccess() {
    fakeFirebaseAuthWrapperImpl.simulateSignInSuccess()
    val authState = fakeFirebaseAuthWrapperImpl.getAuthState()

    assertThat(authState).isEqualTo(FakeAuthState.SUCCESS)
  }

  @Test
  fun testFakeAuthWrapper_simulateSignInFailure_returnsFakeAuthStateFailure() {
    fakeFirebaseAuthWrapperImpl.simulateSignInFailure()
    val authState = fakeFirebaseAuthWrapperImpl.getAuthState()

    assertThat(authState).isEqualTo(FakeAuthState.FAILURE)
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

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ApplicationLifecycleModule::class, TestDispatcherModule::class,
      TestAuthenticationModule::class, TestLogReportingModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: FakeFirebaseAuthWrapperImplTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFakeFirebaseAuthWrapperImplTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: FakeFirebaseAuthWrapperImplTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
