package org.oppia.android.data.backends.gae.testing

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.oppia.android.data.backends.gae.XssiPrefix
import org.oppia.android.data.backends.gae.model.GaePlatformParameterValue
import javax.inject.Inject

/**
 * Orchestrator for configuring [MockWebServer] to properly respond to requests via
 * `PlatformParameterService`.
 */
class PlatformParameterServiceTestOrchestrator @Inject constructor(
  private val mockWebServer: MockWebServer,
  private val moshi: Moshi,
  @XssiPrefix private val xssiPrefix: String
) {
  private val parameterMapAdapter: JsonAdapter<Map<String, GaePlatformParameterValue>> by lazy {
    moshi.adapter(
      Types.newParameterizedType(
        Map::class.java, String::class.java, GaePlatformParameterValue::class.java
      )
    )
  }
  private val genericMapAdapter by lazy { moshi.adapter(Map::class.java) }

  /**
   * Sets the next web response to be a success.
   *
   * @param parameterValues the name-to-value mapping that should be returned (representing the
   *     latest remote parameter values being sent from the server)
   */
  @JvmName("setNextResponseAsSuccessForGaePlatformParameterValues")
  fun setNextResponseAsSuccess(
    parameterValues: Map<String, GaePlatformParameterValue> = DEFAULT_REMOTE_PLATFORM_PARAMETERS
  ) {
    val paramValuesJson = parameterMapAdapter.toJson(parameterValues)
    mockWebServer.enqueue(MockResponse().setBody("$xssiPrefix\n$paramValuesJson"))
  }

  /**
   * Sets the next web response to be a success, but for a set of parameters that can include
   * non-[GaePlatformParameterValue]s.
   *
   * Note that this method will fail if the provided map includes any [GaePlatformParameterValue]s.
   *
   * @param parameterValues the name-to-value mapping that should be returned (representing the
   *     latest remote parameter values being sent from the server)
   */
  @JvmName("setNextResponseAsSuccessForNonGaePlatformParameterValues")
  fun setNextResponseAsSuccess(parameterValues: Map<String, Any>) {
    val paramValuesJson = genericMapAdapter.toJson(parameterValues)
    mockWebServer.enqueue(MockResponse().setBody("$xssiPrefix\n$paramValuesJson"))
  }

  /** Sets the next web response to be a 500 server error. */
  fun setNextResponseAsServerError() {
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
  }

  companion object {
    /**
     * The parameter name for [TEST_STRING_PARAM_SERVER_VALUE] included in
     * [DEFAULT_REMOTE_PLATFORM_PARAMETERS].
     */
    const val TEST_STRING_PARAM_NAME = "test_string_param_name"

    /**
     * The parameter name for [TEST_INTEGER_PARAM_SERVER_VALUE] included in
     * [DEFAULT_REMOTE_PLATFORM_PARAMETERS].
     */
    const val TEST_INTEGER_PARAM_NAME = "test_integer_param_name"

    /**
     * The parameter name for [TEST_BOOLEAN_PARAM_SERVER_VALUE] included in
     * [DEFAULT_REMOTE_PLATFORM_PARAMETERS].
     */
    const val TEST_BOOLEAN_PARAM_NAME = "test_boolean_param_name"

    /**
     * The parameter name for the unknown parameter included in
     * [REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE].
     */
    const val TEST_UNKNOWN_PARAM_NAME = "test_unknown_param_name"

    private val TEST_STRING_PARAM_SERVER_VALUE =
      GaePlatformParameterValue.StringValue("test_string_param_value")
    private val TEST_INTEGER_PARAM_SERVER_VALUE = GaePlatformParameterValue.IntValue(1)
    private val TEST_BOOLEAN_PARAM_SERVER_VALUE = GaePlatformParameterValue.BooleanValue(true)

    /** A default map of parameters that can be orchestrated using [setNextResponseAsSuccess]. */
    val DEFAULT_REMOTE_PLATFORM_PARAMETERS = mapOf(
      TEST_STRING_PARAM_NAME to TEST_STRING_PARAM_SERVER_VALUE,
      TEST_INTEGER_PARAM_NAME to TEST_INTEGER_PARAM_SERVER_VALUE,
      TEST_BOOLEAN_PARAM_NAME to TEST_BOOLEAN_PARAM_SERVER_VALUE
    )

    /** A map of parameters that contains an unsupported parameter type. */
    val REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE = mapOf(
      TEST_UNKNOWN_PARAM_NAME to emptyList<String>()
    )
  }
}
