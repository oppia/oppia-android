package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/**
 * Tests for [GoogleVertexAiClient].
 *
 * Uses [MockWebServer] to intercept HTTP calls and verify request structure and response handling
 * without making real network calls to the Vertex AI API.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GoogleVertexAiClientTest {
  private lateinit var server: MockWebServer
  private lateinit var client: GoogleVertexAiClient

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()
    client = GoogleVertexAiClient(
      gcpProject = "test-project",
      location = "us-central1",
      modelId = "gemini-flash",
      gcpAccessToken = "test-token",
      overrideApiBaseUrl = server.url("/").toString().trimEnd('/')
    )
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun testGenerateText_sendsPostRequest() {
    server.enqueue(successResponse("Generated text."))

    client.generateText("my prompt")

    val request = server.takeRequest()
    assertThat(request.method).isEqualTo("POST")
  }

  @Test
  fun testGenerateText_requestPathContainsProjectAndModel() {
    server.enqueue(successResponse("Generated text."))

    client.generateText("my prompt")

    val request = server.takeRequest()
    assertThat(request.path).contains("test-project")
    assertThat(request.path).contains("gemini-flash")
    assertThat(request.path).contains("generateContent")
  }

  @Test
  fun testGenerateText_requestPathContainsLocation() {
    server.enqueue(successResponse("Generated text."))

    client.generateText("my prompt")

    val request = server.takeRequest()
    assertThat(request.path).contains("us-central1")
  }

  @Test
  fun testGenerateText_sendsAuthorizationHeader() {
    server.enqueue(successResponse("Generated text."))

    client.generateText("my prompt")

    val request = server.takeRequest()
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token")
  }

  @Test
  fun testGenerateText_sendsJsonContentTypeHeader() {
    server.enqueue(successResponse("Generated text."))

    client.generateText("my prompt")

    val request = server.takeRequest()
    assertThat(request.getHeader("Content-Type")).contains("application/json")
  }

  @Test
  fun testGenerateText_requestBodyContainsPrompt() {
    server.enqueue(successResponse("Generated text."))

    client.generateText("my specific prompt text")

    val request = server.takeRequest()
    assertThat(request.body.readUtf8()).contains("my specific prompt text")
  }

  // ---------------------------------------------------------------------------
  // Successful response handling
  // ---------------------------------------------------------------------------

  @Test
  fun testGenerateText_successResponse_returnsGeneratedText() {
    server.enqueue(successResponse("This is the generated changelog summary."))

    val result = client.generateText("some prompt")

    assertThat(result).isEqualTo("This is the generated changelog summary.")
  }

  @Test
  fun testGenerateText_responseTextWithLeadingAndTrailingWhitespace_isTrimmed() {
    server.enqueue(successResponse("  Summary with whitespace.  "))

    val result = client.generateText("some prompt")

    assertThat(result).isEqualTo("Summary with whitespace.")
  }

  @Test
  fun testGenerateText_multipleCandidates_returnsFirstCandidateText() {
    server.enqueue(
      MockResponse().setResponseCode(200).setBody(
        """
        {
          "candidates": [
            {"content": {"parts": [{"text": "First candidate."}]}},
            {"content": {"parts": [{"text": "Second candidate."}]}}
          ]
        }
        """.trimIndent()
      )
    )

    val result = client.generateText("some prompt")

    assertThat(result).isEqualTo("First candidate.")
  }

  // ---------------------------------------------------------------------------
  // Error response handling
  // ---------------------------------------------------------------------------

  @Test
  fun testGenerateText_nonSuccessResponse_throwsWithStatusCode() {
    server.enqueue(MockResponse().setResponseCode(403).setBody("Forbidden"))

    val exception = assertThrows<IllegalStateException> {
      client.generateText("some prompt")
    }

    assertThat(exception).hasMessageThat().contains("403")
  }

  @Test
  fun testGenerateText_serverError_throwsWithStatusCode() {
    server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

    val exception = assertThrows<IllegalStateException> {
      client.generateText("some prompt")
    }

    assertThat(exception).hasMessageThat().contains("500")
  }

  // ---------------------------------------------------------------------------
  // Malformed / empty response handling
  // ---------------------------------------------------------------------------

  @Test
  fun testGenerateText_nullCandidatesField_throwsWithMessage() {
    server.enqueue(
      MockResponse().setResponseCode(200).setBody("""{"candidates": null}""")
    )

    val exception = assertThrows<IllegalStateException> {
      client.generateText("some prompt")
    }

    assertThat(exception).hasMessageThat().contains("no text candidates")
  }

  @Test
  fun testGenerateText_emptyCandidatesList_throwsWithMessage() {
    server.enqueue(
      MockResponse().setResponseCode(200).setBody("""{"candidates": []}""")
    )

    val exception = assertThrows<IllegalStateException> {
      client.generateText("some prompt")
    }

    assertThat(exception).hasMessageThat().contains("no text candidates")
  }

  @Test
  fun testGenerateText_nullContentInCandidate_throwsWithMessage() {
    server.enqueue(
      MockResponse().setResponseCode(200).setBody(
        """{"candidates": [{"content": null}]}"""
      )
    )

    val exception = assertThrows<IllegalStateException> {
      client.generateText("some prompt")
    }

    assertThat(exception).hasMessageThat().contains("no text candidates")
  }

  @Test
  fun testGenerateText_emptyPartsInContent_throwsWithMessage() {
    server.enqueue(
      MockResponse().setResponseCode(200).setBody(
        """{"candidates": [{"content": {"parts": []}}]}"""
      )
    )

    val exception = assertThrows<IllegalStateException> {
      client.generateText("some prompt")
    }

    assertThat(exception).hasMessageThat().contains("no text candidates")
  }

  @Test
  fun testApiBaseUrl_derivedFromLocation_pathContainsLocation() {
    // When a different location is supplied the endpoint path encodes that location,
    // verifying that the base URL is derived from the 'location' parameter rather than
    // being hardcoded to a fixed region.
    val euClient = GoogleVertexAiClient(
      gcpProject = "p",
      location = "europe-west4",
      modelId = "m",
      gcpAccessToken = "t",
      overrideApiBaseUrl = server.url("/").toString().trimEnd('/')
    )
    server.enqueue(successResponse("ok"))
    euClient.generateText("prompt")
    assertThat(server.takeRequest().path).contains("europe-west4")
  }

  /**
   * Returns a [MockResponse] with a 200 status and a valid Vertex AI generateContent response
   * body containing [text] as the first candidate's text part.
   */
  private fun successResponse(text: String): MockResponse {
    val escapedText = text.replace("\"", "\\\"")
    return MockResponse().setResponseCode(200).setBody(
      """{"candidates": [{"content": {"parts": [{"text": "$escapedText"}]}}]}"""
    )
  }
}
