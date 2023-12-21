package org.oppia.android.domain.auth

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.testing.TestAuthenticationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncResult
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

  @field:[Inject BackgroundDispatcher]
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
  }

  @Test
  fun testAuthentication_getCurrentSignedInUser_noSignedInUser_returnsNull() {
    authenticationController.signInAnonymouslyWithFirebase()
    val user = authenticationController.currentFirebaseUser

    assertThat(user).isEqualTo(null)
  }

  @Test
  fun testAuthentication_signInAnonymously_success_returnsSuccessAsyncResult() {
    val authResult = authenticationController.signInAnonymouslyWithFirebase()
    testCoroutineDispatchers.runCurrent()
    assertThat(authResult).isInstanceOf(AsyncResult.Success::class.java)
    // A successful result is returned
    runSynchronously { authenticationController.signInAnonymouslyWithFirebase().await() }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  private fun runSynchronously(operation: suspend () -> Unit) =
    CoroutineScope(backgroundDispatcher).async { operation() }.waitForSuccessfulResult()

  private fun <T> Deferred<T>.waitForSuccessfulResult() {
    return when (val result = waitForResult()) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> {} // Nothing to do; the result succeeded.
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult() = toStateFlow().waitForLatestValue()

  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      CoroutineScope(backgroundDispatcher).async {
        try {
          val result = deferred.await()
          flow.emit(AsyncResult.Success(result))
        } catch (e: Throwable) {
          flow.emit(AsyncResult.Failure(e))
        }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(): T =
    also { testCoroutineDispatchers.runCurrent() }.value

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
    fun provideAuthenticationController():
      AuthenticationWrapper = AuthenticationController(mock(FirebaseAuth::class.java))
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
