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
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.firebase.FakeFirebaseAuthWrapperImpl
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.util.threading.DispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AuthenticationController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AuthenticationControllerTest.TestApplication::class)
class AuthenticationControllerTest {
  @Inject
  lateinit var firebaseAuthWrapper: FirebaseAuthWrapper

  @Inject
  lateinit var fakeFirebaseAuthWrapperImpl: FakeFirebaseAuthWrapperImpl

  @Inject
  lateinit var authenticationController: AuthenticationController

  @field:[Inject BackgroundDispatcher]
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAuthentication_getCurrentUser_userSignedIn_returnsInstanceOfFirebaseUserWrapper() {
    fakeFirebaseAuthWrapperImpl.simulateSignInSuccess()

    firebaseAuthWrapper.signInAnonymously(
      onSuccess = {},
      onFailure = {}
    )

    val user = authenticationController.currentFirebaseUser

    assertThat(user).isInstanceOf(FirebaseUserWrapper::class.java)
  }

  @Test
  fun testAuthentication_signInAnonymously_succeeds() {
    fakeFirebaseAuthWrapperImpl.simulateSignInSuccess()

    firebaseAuthWrapper.signInAnonymously(
      onSuccess = {},
      onFailure = {}
    )

    val user = authenticationController.currentFirebaseUser

    assertThat(user).isInstanceOf(FirebaseUserWrapper::class.java)
  }

  @Test
  fun testAuthentication_signInAnonymously_failure_returnsException() {
    fakeFirebaseAuthWrapperImpl.simulateSignInFailure()

    assertThrows(Throwable::class) {
      firebaseAuthWrapper.signInAnonymously(
        onSuccess = {},
        onFailure = {}
      )
    }
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
      ApplicationLifecycleModule::class, DispatcherModule::class,
      TestLogReportingModule::class, TestAuthenticationModule::class,
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: AuthenticationControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAuthenticationControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: AuthenticationControllerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
