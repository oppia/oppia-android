package org.oppia.android.data.backends.gae.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import data.src.main.java.org.oppia.android.data.backends.gae.testing.PlatformParameterServiceTestOrchestrator
import data.src.main.java.org.oppia.android.data.backends.gae.testing.PlatformParameterServiceTestOrchestrator.Companion.REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.OppiaRetrofit
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [PlatformParameterServiceTestOrchestrator]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PlatformParameterServiceTestOrchestratorTest.TestApplication::class)
class PlatformParameterServiceTestOrchestratorTest {
  @Inject lateinit var serviceOrchestrator: PlatformParameterServiceTestOrchestrator
  @Inject lateinit var mockWebServer: MockWebServer
  @Inject lateinit var moshi: Moshi
  @field:[Inject OppiaRetrofit] lateinit var retrofit: Retrofit

  private lateinit var mapAdapter: JsonAdapter<Map<*, *>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    mapAdapter = moshi.adapter(Map::class.java)
  }

  @Test
  fun testOrchestrator_setNextResponseAsEmptySuccess_sendRequest_returnsSuccess() {
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = emptyMap())

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    assertThat(result.isSuccessful).isTrue()
  }

  @Test
  fun testOrchestrator_setNextResponseAsEmptySuccess_sendTwoRequests_secondTimesOut() {
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = emptyMap())
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    assertThrows<InterruptedIOException> {
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    }
  }

  @Test
  fun testOrchestrator_setNextResponseAsEmptySuccess_twice_sendTwoRequests_secondIsSuccess() {
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = emptyMap())
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = emptyMap())
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    assertThat(result2.isSuccessful).isTrue()
  }

  @Test
  fun testOrchestrator_setNextResponseAsEmptySuccess_responseReturnsEmptyParameterList() {
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = emptyMap())

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    val parameterValues = result.body?.source()?.let { mapAdapter.fromJson(it) }
    assertThat(parameterValues).isEmpty()
  }

  @Test
  fun testOrchestrator_setNextResponseAsSuccess_withCustomStr_responseReturnsCustomStr() {
    serviceOrchestrator.setNextResponseAsSuccess(parameterValues = mapOf("test" to "str"))

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    val parameterValues = result.body?.source()?.let { mapAdapter.fromJson(it) }
    assertThat(parameterValues).hasSize(1)
    assertThat(parameterValues?.get("test")).isEqualTo("str")
  }

  @Test
  fun testOrchestrator_setNextResponseAsSuccess_withDefaults_responseIncludesAllTypes() {
    serviceOrchestrator.setNextResponseAsSuccess()

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    val paramVals = result.body?.source()?.let { mapAdapter.fromJson(it) }
    assertThat(paramVals).hasSize(3)
    assertThat(paramVals?.get(TEST_STRING_PARAM_NAME)).isEqualTo(TEST_STRING_PARAM_SERVER_VALUE)
    assertThat(paramVals?.get(TEST_INTEGER_PARAM_NAME)).isEqualTo(TEST_INTEGER_PARAM_SERVER_VALUE)
    assertThat(paramVals?.get(TEST_BOOLEAN_PARAM_NAME)).isEqualTo(TEST_BOOLEAN_PARAM_SERVER_VALUE)
  }

  @Test
  fun testOrchestrator_setNextResponseAsSuccess_withUnsupportedType_responseIncludesList() {
    serviceOrchestrator.setNextResponseAsSuccess(REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE)

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    val paramVals = result.body?.source()?.let { mapAdapter.fromJson(it) }
    assertThat(paramVals).hasSize(3)
    assertThat(paramVals?.get(TEST_BOOLEAN_PARAM_NAME)).isEqualTo(emptyList<String>())
  }

  @Test
  fun testOrchestrator_setNextResponseAsServerError_sendRequest_returnsFailure() {
    serviceOrchestrator.setNextResponseAsServerError()

    val result = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    assertThat(result.isSuccessful).isFalse()
    assertThat(result.code).isEqualTo(500)
  }

  @Test
  fun testOrchestrator_setNextResponseAsServerError_sendTwoRequests_secondIsNull() {
    serviceOrchestrator.setNextResponseAsServerError()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    assertThrows<InterruptedIOException> {
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    }
  }

  @Test
  fun testOrchestrator_setNextResponseAsServerError_twice_sendTwoRequests_secondIsFailure() {
    serviceOrchestrator.setNextResponseAsServerError()
    serviceOrchestrator.setNextResponseAsServerError()
    retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())

    assertThat(result2.isSuccessful).isFalse()
    assertThat(result2.code).isEqualTo(500)
  }

  @Test
  fun testOrchestrator_setSuccessThenFailure_sendThreeRequest_returnsSuccessFailTimesOut() {
    serviceOrchestrator.setNextResponseAsSuccess()
    serviceOrchestrator.setNextResponseAsServerError()

    val result1 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    assertThrows<InterruptedIOException> { // Request 3 times out.
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    }

    assertThat(result1.isSuccessful).isTrue()
    assertThat(result2.isSuccessful).isFalse()
    assertThat(result2.code).isEqualTo(500)
  }

  @Test
  fun testOrchestrator_setFailureThenSuccess_sendThreeRequest_returnsFailSuccessTimesOut() {
    serviceOrchestrator.setNextResponseAsServerError()
    serviceOrchestrator.setNextResponseAsSuccess()

    val result1 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    val result2 = retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    assertThrows<InterruptedIOException> { // Request 3 times out.
      retrofit.sendRawOkHttpRequestWithShortTimeout(createRequest())
    }

    assertThat(result1.isSuccessful).isFalse()
    assertThat(result1.code).isEqualTo(500)
    assertThat(result2.isSuccessful).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun createRequest(): Request {
    val requestBody = "{}".toRequestBody("text/json".toMediaType())
    return Request.Builder().url(mockWebServer.url("/testpath")).post(requestBody).build()
  }

  private fun Retrofit.sendRawOkHttpRequestWithShortTimeout(request: Request): Response {
    val oldOkHttpClient = callFactory() as OkHttpClient
    val adjustedOkHttpClient = oldOkHttpClient.newBuilder().callTimeout(1, TimeUnit.SECONDS).build()
    return adjustedOkHttpClient.newCall(request).execute()
  }

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, RetrofitModule::class, NetworkConfigTestModule::class,
      TestDispatcherModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: PlatformParameterServiceTestOrchestratorTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerPlatformParameterServiceTestOrchestratorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: PlatformParameterServiceTestOrchestratorTest) = component.inject(test)
  }
}
