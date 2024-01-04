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
import org.oppia.android.testing.FakeFirebaseWrapperImpl
import org.oppia.android.testing.TestAuthenticationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.threading.BackgroundDispatcher
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
  lateinit var authenticationController: AuthenticationController

  @Inject
  lateinit var firebaseAuthWrapper: FakeFirebaseWrapperImpl

  @field:[Inject BackgroundDispatcher]
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAuthentication_getCurrentFirebaseUser_noSignedInUser_returnsNull() {
    val user = authenticationController.currentFirebaseUser

    assertThat(user).isEqualTo(null)
  }

  @Test
  fun testAuthentication_getCurrentUser_userSignedIn_returnsInstanceOfFirebaseUserWrapper() {
    firebaseAuthWrapper.simulateSignInSuccess()

    firebaseAuthWrapper.signInAnonymously(
      onSuccess = {},
      onFailure = {}
    )

    val user = authenticationController.currentFirebaseUser

    assertThat(user).isInstanceOf(FirebaseUserWrapper::class.java)
  }

  @Test
  fun testAuthentication_signInAnonymously_success_returnsInstanceOfFirebaseUserWrapper() {
    firebaseAuthWrapper.simulateSignInSuccess()

    firebaseAuthWrapper.signInAnonymously(
      onSuccess = {},
      onFailure = {}
    )

    val user = authenticationController.currentFirebaseUser

    assertThat(user).isInstanceOf(FirebaseUserWrapper::class.java)
  }

  @Test
  fun testAuthentication_signInAnonymously_failure_returnsException() {
    firebaseAuthWrapper.simulateSignInFailure()

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
      ApplicationLifecycleModule::class, TestDispatcherModule::class,
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
