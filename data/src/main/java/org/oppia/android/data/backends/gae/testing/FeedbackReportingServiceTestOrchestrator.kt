package data.src.main.java.org.oppia.android.data.backends.gae.testing

import android.content.Context
import com.squareup.moshi.Moshi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Orchestrator for configuring [MockWebServer] to properly respond to requests via
 * `FeedbackReportingService`.
 */
class FeedbackReportingServiceTestOrchestrator @Inject constructor(
  private val context: Context,
  private val mockWebServer: MockWebServer,
  private val moshi: Moshi
) {
  private val gaeFeedbackReportAdapter by lazy { moshi.adapter(GaeFeedbackReport::class.java) }

  /**
   * Returns the new request as a [GaeFeedbackReport] (assuming that the most recent request was to
   * `FeedbackReportingService`.
   */
  fun takeGaeFeedbackReportRequest(): GaeFeedbackReport {
    val request = mockWebServer.takeRequest(1, TimeUnit.SECONDS)
      ?: error("Failed to retrieve request within timeout.")
    // Note that '!!' is used here because it should be impossible for a null value to be returned
    // based on how Moshi is configured (it will throw a JsonDataException instead).
    return request.body.use(gaeFeedbackReportAdapter::fromJson)!!
  }

  /** Sets the next web response to be a success. */
  fun setNextResponseAsSuccess() {
    mockWebServer.enqueue(MockResponse().setBody("{}"))
  }

  /** Sets the next web response to be a 500 server error. */
  fun setNextResponseAsServerError() {
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
  }

  /**
   * Loads and returns the specified file by [filename] from the local test assets as a
   * [GaeFeedbackReport].
   */
  fun loadGaeFeedbackReportFromTestData(filename: String): GaeFeedbackReport {
    return gaeFeedbackReportAdapter.fromJson(openAssetStream(filename))
      ?: error("Failed to read GaeFeedbackReport from file: $filename.")
  }

  private fun openAssetStream(filename: String): String =
    context.assets.open("api_mocks/$filename").bufferedReader().use { it.readText() }
}
