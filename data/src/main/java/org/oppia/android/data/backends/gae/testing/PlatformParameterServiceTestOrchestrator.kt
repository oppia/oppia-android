package data.src.main.java.org.oppia.android.data.backends.gae.testing

import com.squareup.moshi.Moshi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.oppia.android.data.backends.gae.XssiPrefix
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_BOOLEAN_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_INTEGER_PARAM_SERVER_VALUE
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_NAME
import org.oppia.android.testing.platformparameter.TEST_STRING_PARAM_SERVER_VALUE
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
  private val mapAdapter by lazy { moshi.adapter(Map::class.java) }

  /**
   * Sets the next web response to be a success.
   *
   * @param parameterValues the name-to-value mapping that should be returned (representing the
   *     latest remote parameter values being sent from the server)
   */
  fun setNextResponseAsSuccess(
    parameterValues: Map<String, Any> = DEFAULT_REMOTE_PLATFORM_PARAMETERS
  ) {
    val paramValuesJson = mapAdapter.toJson(parameterValues)
    mockWebServer.enqueue(MockResponse().setBody("$xssiPrefix\n$paramValuesJson"))
  }

  /** Sets the next web response to be a 500 server error. */
  fun setNextResponseAsServerError() {
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
  }

  companion object {
    /** A default map of parameters that can be orchestrated using [setNextResponseAsSuccess]. */
    val DEFAULT_REMOTE_PLATFORM_PARAMETERS = mapOf(
      TEST_STRING_PARAM_NAME to TEST_STRING_PARAM_SERVER_VALUE,
      TEST_INTEGER_PARAM_NAME to TEST_INTEGER_PARAM_SERVER_VALUE,
      TEST_BOOLEAN_PARAM_NAME to TEST_BOOLEAN_PARAM_SERVER_VALUE
    )

    /** A version of [DEFAULT_REMOTE_PLATFORM_PARAMETERS] with unsupported parameter types. */
    val REMOTE_PLATFORM_PARAMETERS_WITH_UNSUPPORTED_TYPE = mapOf(
      TEST_STRING_PARAM_NAME to TEST_STRING_PARAM_SERVER_VALUE,
      TEST_INTEGER_PARAM_NAME to TEST_INTEGER_PARAM_SERVER_VALUE,
      TEST_BOOLEAN_PARAM_NAME to emptyList<String>()
    )
  }
}
