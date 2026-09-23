package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.BackgroundTestDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatcher
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integration tests that verify the expected interaction between [RetryInterceptor] and
 * [NetworkLoggingInterceptor] when placed in the order defined by [RetrofitModule]
 * (retry → logging → network).
 *
 * Since [RetryInterceptor] calls [okhttp3.Interceptor.Chain.proceed] in a loop, OkHttp
 * re-invokes all downstream interceptors (including [NetworkLoggingInterceptor]) for each
 * attempt. These tests confirm that logging captures every individual attempt rather than
 * only the final result.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = RetryInterceptorIntegrationTest.TestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RetryInterceptorIntegrationTest {

  @Inject lateinit var networkLoggingInterceptor: NetworkLoggingInterceptor
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @field:[Inject BackgroundTestDispatcher]
  lateinit var backgroundTestDispatcher: TestCoroutineDispatcher

  private lateinit var mockWebServer: MockWebServer
  private lateinit var clientWithRetryAndLogging: OkHttpClient

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    mockWebServer = MockWebServer()
    mockWebServer.start()
    // Mirrors the interceptor order in RetrofitModule: retry → logging → network.
    val retryInterceptor = RetryInterceptor(FakeNetworkDelayHandler())
    clientWithRetryAndLogging = OkHttpClient.Builder()
      .addInterceptor(retryInterceptor)
      .addInterceptor(networkLoggingInterceptor)
      .build()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun testIntegration_requestSucceedsOnFirstAttempt_logNetworkCallFlowEmitsOnce() {
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

    // Collect the single expected emission before making the network call.
    val loggedCallsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logNetworkCallFlow.take(1).toList()
    }
    testCoroutineDispatchers.advanceUntilIdle() // Ensure the flow is subscribed before emit().

    clientWithRetryAndLogging
      .newCall(Request.Builder().url(mockWebServer.url("/")).build())
      .execute()
    testCoroutineDispatchers.advanceUntilIdle()

    val loggedCalls = loggedCallsDeferred.getCompleted()
    assertThat(loggedCalls).hasSize(1)
    assertThat(loggedCalls.single().responseStatusCode).isEqualTo(200)
  }

  @Test
  fun testIntegration_requestFailsThenSucceeds_logNetworkCallFlowEmitsBothAttempts() {
    mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody("{}"))
    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

    // The logging interceptor sits after the retry interceptor in the chain. OkHttp re-invokes
    // downstream interceptors on each call to chain.proceed(), so the 503 attempt and the
    // successful 200 retry must both be logged individually.
    val loggedCallsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logNetworkCallFlow.take(2).toList()
    }
    val loggedFailedCallsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logFailedNetworkCallFlow.take(1).toList()
    }
    testCoroutineDispatchers.advanceUntilIdle() // Ensure the flows are subscribed before emit().

    val response = clientWithRetryAndLogging
      .newCall(Request.Builder().url(mockWebServer.url("/")).build())
      .execute()
    testCoroutineDispatchers.advanceUntilIdle()

    assertThat(response.code).isEqualTo(200)
    // Both the failed attempt (503) and the successful retry (200) should be logged.
    val loggedCodes = loggedCallsDeferred.getCompleted().map { it.responseStatusCode }
    // Note: .inOrder() is intentionally omitted — background coroutines are scheduled in a
    // non-deterministic order in the test environment. What matters is that both the failed
    // 503 attempt and the successful 200 retry are each individually logged by the interceptor.
    assertThat(loggedCodes).containsExactly(503, 200)
    // Only the failed 503 attempt should appear in the failed-calls flow.
    assertThat(loggedFailedCallsDeferred.getCompleted().single().responseStatusCode)
      .isEqualTo(503)
  }

  @Test
  fun testIntegration_requestFailsAllAttempts_logNetworkCallFlowEmitsAllAttempts() {
    // Enqueue 4 consecutive failures: 1 original attempt + 3 retries.
    repeat(4) { mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody("{}")) }

    val loggedCallsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logNetworkCallFlow.take(4).toList()
    }
    val loggedFailedCallsDeferred = CoroutineScope(backgroundTestDispatcher).async {
      networkLoggingInterceptor.logFailedNetworkCallFlow.take(4).toList()
    }
    testCoroutineDispatchers.advanceUntilIdle() // Ensure the flows are subscribed before emit().

    val response = clientWithRetryAndLogging
      .newCall(Request.Builder().url(mockWebServer.url("/")).build())
      .execute()
    testCoroutineDispatchers.advanceUntilIdle()

    // All 4 attempts (original + 3 retries) should be logged, even though all failed.
    assertThat(response.code).isEqualTo(503)
    val loggedCodes = loggedCallsDeferred.getCompleted().map { it.responseStatusCode }
    assertThat(loggedCodes).containsExactly(503, 503, 503, 503).inOrder()
    val failedCodes = loggedFailedCallsDeferred.getCompleted().map { it.responseStatusCode }
    assertThat(failedCodes).containsExactly(503, 503, 503, 503).inOrder()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  /** A [NetworkDelayHandler] that skips all actual sleeping so tests run instantly. */
  private class FakeNetworkDelayHandler : NetworkDelayHandler {
    override fun delay(millis: Long) {
      // No-op: avoids real Thread.sleep() calls during tests so retries complete instantly.
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
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

    fun inject(retryInterceptorIntegrationTest: RetryInterceptorIntegrationTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerRetryInterceptorIntegrationTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(retryInterceptorIntegrationTest: RetryInterceptorIntegrationTest) {
      component.inject(retryInterceptorIntegrationTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
