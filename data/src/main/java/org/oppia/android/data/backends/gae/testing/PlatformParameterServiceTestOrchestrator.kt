package org.oppia.android.data.backends.gae.testing

import com.squareup.moshi.Moshi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.oppia.android.data.backends.gae.XssiPrefix
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
  fun setNextResponseAsSuccess(parameterValues: Map<String, Any> = emptyMap()) {
    val paramValuesJson = mapAdapter.toJson(parameterValues)
    mockWebServer.enqueue(MockResponse().setBody("$xssiPrefix\n$paramValuesJson"))
  }

  /** Sets the next web response to be a 500 server error. */
  fun setNextResponseAsServerError() {
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
  }
}
